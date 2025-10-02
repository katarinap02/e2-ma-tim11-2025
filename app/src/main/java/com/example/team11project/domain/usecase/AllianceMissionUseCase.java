package com.example.team11project.domain.usecase;

import android.util.Log;

import com.example.team11project.domain.model.Alliance;
import com.example.team11project.domain.model.AllianceBoss;
import com.example.team11project.domain.model.AllianceMission;
import com.example.team11project.domain.model.MemberProgress;
import com.example.team11project.domain.model.SpecialTaskType;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AllianceMissionUseCase {
    private final AllianceMissionRepository allianceMissionRepository;

    private final AllianceRepository allianceRepository;

    private final UserRepository userRepository;

    public AllianceMissionUseCase(AllianceMissionRepository allianceMissionRepository, AllianceRepository allianceRepository, UserRepository userRepository)
    {
        this.allianceMissionRepository = allianceMissionRepository;
        this.allianceRepository = allianceRepository;
        this.userRepository = userRepository;
    }

    public void startSpecialMission(Alliance alliance, String userId, RepositoryCallback<AllianceMission> callback) {

        if (alliance == null) {
            if (callback != null) callback.onFailure(new IllegalArgumentException("Alliance is null"));
            return;
        }

        // Da li postoji aktivna misija
        allianceMissionRepository.getActiveMissionByAllianceId(alliance.getId(), new RepositoryCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission existingMission) {
                if (existingMission != null) {
                    if (callback != null) callback.onSuccess(existingMission);
                    return;
                }

                if (userId != null && userId.equals(alliance.getLeader())) {
                    createNewMission(alliance,userId, callback);
                } else {
                    if (callback != null)
                        callback.onFailure(new IllegalAccessException("Samo lider može da započne misiju"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AllianceMission", "Greška pri proveri misije: " + e.getMessage());
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    public void createNewMission(Alliance alliance, String userId, RepositoryCallback<AllianceMission> callback) {

        int numberOfMembers = alliance.getMembers().size();
        AllianceBoss boss = new AllianceBoss();
        boss.setId(UUID.randomUUID().toString());
        boss.setNumberOfMembers(numberOfMembers);
        boss.setMaxHp(100 * numberOfMembers);
        boss.setCurrentHp(100 * numberOfMembers);

        AllianceMission mission = new AllianceMission();
        mission.setId(UUID.randomUUID().toString());
        mission.setAllianceId(alliance.getId());
        mission.setBoss(boss);
        mission.setStartDate(new Date());


        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, 2);
        mission.setEndDate(calendar.getTime());

        // 2. Kreiraj MemberProgress za svakog člana
        List<MemberProgress> memberProgressList = new ArrayList<>();
        for (String memberId : alliance.getMembers()) {
            MemberProgress progress = new MemberProgress();
            progress.setId(UUID.randomUUID().toString());
            progress.setUserId(memberId);
            progress.setMissionId(mission.getId());
            progress.setStorePurchases(0);
            progress.setRegularBossHits(0);
            progress.setEasyNormalTasks(0);
            progress.setOtherTasks(0);
            progress.setNoUnresolvedTasks(true);
            progress.setTotalDamageDealt(0);
            progress.setMessageDays(new ArrayList<>());

            memberProgressList.add(progress);
        }

        mission.setMemberProgressList(memberProgressList);

        allianceMissionRepository.createAllianceMission(mission, new RepositoryCallback<String>() {
            @Override
            public void onSuccess(String missionId) {
                updateAllianceMissionStatus(alliance, true, userId, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (callback != null) callback.onSuccess(mission);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (callback != null) callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AllianceMission", "Greška pri kreiranju misije: " + e.getMessage());
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    public void updateAllianceMissionStatus(Alliance alliance, boolean isActive, String userId, RepositoryCallback<Void> callback) {
        alliance.setMissionActive(isActive);

        // Ažuriraj savez
        allianceRepository.updateAlliance(alliance, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Dobavi korisnika
                userRepository.getUserById(userId, new RepositoryCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user != null && user.getCurrentAlliance() != null) {
                            // Postavi status misije u trenutnom savezu korisnika
                            user.getCurrentAlliance().setMissionActive(isActive);

                            // Ažuriraj korisnika
                            userRepository.updateUser(user, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void userResult) {
                                    Log.d("AllianceMission", "Uspešno ažuriran status misije za savez i korisnika");
                                    if (callback != null) callback.onSuccess(null);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("AllianceMission", "Greška pri ažuriranju korisnika: " + e.getMessage());
                                    if (callback != null) callback.onFailure(e);
                                }
                            });
                        } else {
                            // Korisnik nema trenutni savez, ali savez je uspešno ažuriran
                            Log.w("AllianceMission", "Korisnik nema trenutni savez");
                            if (callback != null) callback.onSuccess(null);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("AllianceMission", "Greška pri dobavljanju korisnika: " + e.getMessage());
                        if (callback != null) callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AllianceMission", "Greška pri ažuriranju saveza: " + e.getMessage());
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    public void processSpecialTask(String userId, SpecialTaskType taskType, RepositoryCallback<Boolean> callback) {
        // Dohvati aktivnu misiju za korisnika
        fetchActiveMissionForUser(userId, new RepositoryCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission mission) {
                if (mission == null) {
                    // Nema aktivne misije, ništa ne radi
                    if (callback != null) callback.onSuccess(false);
                    return;
                }

                // Proveri da li je misija još aktivna (nije istekla)
                Date now = new Date();
                if (now.after(mission.getEndDate())) {
                    if (callback != null) callback.onSuccess(false);
                    return;
                }

                // Pronađi progress za korisnika
                MemberProgress userProgress = null;
                for (MemberProgress progress : mission.getMemberProgressList()) {
                    if (progress.getUserId().equals(userId)) {
                        userProgress = progress;
                        break;
                    }
                }

                if (userProgress == null) {
                    if (callback != null) callback.onFailure(new Exception("Progress nije pronađen"));
                    return;
                }

                // Obradi specifični task i proveri da li je postignut damage
                int damageDealt = processTask(userProgress, taskType);

                if (damageDealt > 0) {
                    // Smanji HP bossa
                    AllianceBoss boss = mission.getBoss();
                    int newHp = Math.max(0, boss.getCurrentHp() - damageDealt);
                    boss.setCurrentHp(newHp);

                    // Ažuriraj progress korisnika
                    allianceMissionRepository.updateMemberProgress(userProgress, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            // Ažuriraj HP bossa
                            allianceMissionRepository.updateBossHp(mission.getId(), newHp, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result2) {
                                     callback.onSuccess(true); //zavrsena obrada, za sad ne radimo nista
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    if (callback != null) callback.onFailure(e);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            if (callback != null) callback.onFailure(e);
                        }
                    });
                } else {
                    // Nije postignut damage (već dostignut max)
                    if (callback != null) callback.onSuccess(false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    public void fetchActiveMissionForUser(String userId, RepositoryCallback<AllianceMission> callback) {
        // 1. Dohvati korisnika
        userRepository.getUserById(userId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    callback.onFailure(new Exception("User nije pronađen"));
                    return;
                }

                // 2. Uzmi trenutnu alijansu korisnika
                Alliance currentAlliance = user.getCurrentAlliance();
                if (currentAlliance == null || !currentAlliance.isMissionActive()) {
                    // Nema aktivne alijanse ili misija nije aktivna
                    callback.onSuccess(null);
                    return;
                }

                String allianceId = currentAlliance.getId();

                // 3. Dohvati aktivnu misiju za tu alijansu
                allianceMissionRepository.getActiveMissionByAllianceId(allianceId, new RepositoryCallback<AllianceMission>() {
                    @Override
                    public void onSuccess(AllianceMission mission) {
                        callback.onSuccess(mission);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }


    private int processTask(MemberProgress progress, SpecialTaskType taskType) {
        int damage = 0;

        switch (taskType) {
            case STORE_PURCHASE:
                if (progress.getStorePurchases() < taskType.getMaxCount()) {
                    progress.setStorePurchases(progress.getStorePurchases() + 1);
                    damage = taskType.getHpDamage();
                    progress.setTotalDamageDealt(progress.getTotalDamageDealt() + damage);
                }
                break;

            case REGULAR_BOSS_HIT:
                if (progress.getRegularBossHits() < taskType.getMaxCount()) {
                    progress.setRegularBossHits(progress.getRegularBossHits() + 1);
                    damage = taskType.getHpDamage();
                    progress.setTotalDamageDealt(progress.getTotalDamageDealt() + damage);
                }
                break;

            case EASY_NORMAL_TASK:
                if (progress.getEasyNormalTasks() < taskType.getMaxCount()) {
                    progress.setEasyNormalTasks(progress.getEasyNormalTasks() + 1);
                    damage = taskType.getHpDamage();
                    progress.setTotalDamageDealt(progress.getTotalDamageDealt() + damage);
                }
                break;

            case OTHER_TASK:
                if (progress.getOtherTasks() < taskType.getMaxCount()) {
                    progress.setOtherTasks(progress.getOtherTasks() + 1);
                    damage = taskType.getHpDamage();
                    progress.setTotalDamageDealt(progress.getTotalDamageDealt() + damage);
                }
                break;

            case NO_UNRESOLVED_TASKS:
                if (progress.isNoUnresolvedTasks()) {
                    // Ovaj bonus se dodeljuje samo jednom
                    progress.setNoUnresolvedTasks(false); // Markira da je iskorišćen
                    damage = taskType.getHpDamage();
                    progress.setTotalDamageDealt(progress.getTotalDamageDealt() + damage);
                }
                break;

            case DAILY_MESSAGE:
                // Proveri da li je danas već dodato
                Date today = getTodayDate(); // Helper metoda za današnji datum bez vremena
                if (!progress.getMessageDays().contains(today)) {
                    progress.getMessageDays().add(today);
                    damage = taskType.getHpDamage();
                    progress.setTotalDamageDealt(progress.getTotalDamageDealt() + damage);
                }
                break;
        }

        return damage;
    }

    private Date getTodayDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }


    }
