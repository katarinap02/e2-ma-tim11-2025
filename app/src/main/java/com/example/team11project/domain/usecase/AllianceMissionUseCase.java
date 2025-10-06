package com.example.team11project.domain.usecase;

import android.util.Log;

import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.Alliance;
import com.example.team11project.domain.model.AllianceBoss;
import com.example.team11project.domain.model.AllianceMission;
import com.example.team11project.domain.model.AllianceMissionReward;
import com.example.team11project.domain.model.Boss;
import com.example.team11project.domain.model.Clothing;
import com.example.team11project.domain.model.Equipment;
import com.example.team11project.domain.model.MemberProgress;
import com.example.team11project.domain.model.MissionFinalizationResult;
import com.example.team11project.domain.model.Potion;
import com.example.team11project.domain.model.SpecialTaskType;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.BossRepository;
import com.example.team11project.domain.repository.EquipmentRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.TaskInstanceRepository;
import com.example.team11project.domain.repository.TaskRepository;
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

    private final TaskRepository taskRepository;

    private final TaskInstanceRepository taskInstanceRepository;

    private final EquipmentRepository equipmentRepository;

    private final BossRepository bossRepository;

    public AllianceMissionUseCase(AllianceMissionRepository allianceMissionRepository, AllianceRepository allianceRepository, UserRepository userRepository, TaskRepository taskRepository, TaskInstanceRepository taskInstanceRepository, EquipmentRepository equipmentRepository, BossRepository bossRepository)
    {
        this.allianceMissionRepository = allianceMissionRepository;
        this.allianceRepository = allianceRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.taskInstanceRepository = taskInstanceRepository;
        this.equipmentRepository = equipmentRepository;
        this.bossRepository = bossRepository;
    }



    public void startSpecialMission(Alliance alliance, String userId, RepositoryCallback<AllianceMission> callback) {
        if (alliance == null) {
            if (callback != null) callback.onFailure(new IllegalArgumentException("Alliance is null"));
            return;
        }

        allianceMissionRepository.getActiveMissionByAllianceId(alliance.getId(), new RepositoryCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission existingMission) {
                if (existingMission != null && existingMission.isActive()) {
                    // Ima aktivnu misiju, vrati je
                    if (callback != null) callback.onSuccess(existingMission);
                    return;
                }

                // Nema aktivne misije, kreiraj novu ako je lider
                if (userId != null && userId.equals(alliance.getLeader())) {
                    createNewMission(alliance, userId, callback);
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

    public void checkAndFinalizeMission(Alliance alliance, RepositoryCallback<MissionFinalizationResult> callback) {
        if (alliance == null) {
            if (callback != null) callback.onFailure(new IllegalArgumentException("Alliance is null"));
            return;
        }

        allianceMissionRepository.getActiveMissionByAllianceId(alliance.getId(), new RepositoryCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission existingMission) {
                if (existingMission != null && existingMission.isActive()) {
                    Date now = new Date();
                    if (now.after(existingMission.getEndDate())) {
                        // Misija je istekla - obradi je
                        handleMissionExpiration(existingMission, alliance, new RepositoryCallback<AllianceMission>() {
                            @Override
                            public void onSuccess(AllianceMission result) {
                                // Misija je finalizovana
                                MissionFinalizationResult finResult = new MissionFinalizationResult(true, existingMission.getBoss().getCurrentHp() == 0);
                                if (callback != null) callback.onSuccess(finResult);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                if (callback != null) callback.onFailure(e);
                            }
                        });
                    } else {
                        // Misija još traje
                        MissionFinalizationResult finResult = new MissionFinalizationResult(false, false);
                        if (callback != null) callback.onSuccess(finResult);
                    }
                } else {
                    // Nema aktivne misije
                    MissionFinalizationResult finResult = new MissionFinalizationResult(false, false);
                    if (callback != null) callback.onSuccess(finResult);
                }
            }

            @Override
            public void onFailure(Exception e) {
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
        mission.setActive(true);


        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, 2);
        mission.setEndDate(calendar.getTime());

        // 2. Kreiraj MemberProgress za svakog člana
        List<MemberProgress> memberProgress = new ArrayList<>();
        for (String memberId : alliance.getMembers()) {
            MemberProgress progress = new MemberProgress();
            progress.setId(UUID.randomUUID().toString());
            progress.setUserId(memberId);
            progress.setMissionId(mission.getId());
            progress.setStorePurchases(0);
            progress.setRegularBossHits(0);
            progress.setEasyNormalTasks(0);
            progress.setOtherTasks(0);
            progress.setNoUnresolvedTasks(false);
            progress.setTotalDamageDealt(0);
            progress.setMessageDays(new ArrayList<>());

            memberProgress.add(progress);
        }

        mission.setMemberProgress(memberProgress);

        allianceMissionRepository.createAllianceMission(mission, new RepositoryCallback<String>() {
            @Override
            public void onSuccess(String missionId) {
                updateAllianceMissionStatus(alliance, true, new RepositoryCallback<Void>() {
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

    public void updateAllianceMissionStatus(Alliance alliance, boolean isActive, RepositoryCallback<Void> callback) {
        alliance.setMissionActive(isActive);

        // Ažuriraj savez
        allianceRepository.updateAlliance(alliance, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Ažuriraj status misije za sve članove
                updateAllMembersStatus(alliance.getMembers(), isActive, 0, callback);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AllianceMission", "Greška pri ažuriranju saveza: " + e.getMessage());
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    private void updateAllMembersStatus(List<String> memberIds, boolean isActive, int index, RepositoryCallback<Void> callback) {
        // Bazni slučaj: svi članovi su ažurirani
        if (index >= memberIds.size()) {
            Log.d("AllianceMission", "Svi članovi uspešno ažurirani");
            if (callback != null) callback.onSuccess(null);
            return;
        }

        String memberId = memberIds.get(index);

        userRepository.getUserById(memberId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getCurrentAlliance() != null) {
                    // Postavi status misije u trenutnom savezu korisnika
                    user.getCurrentAlliance().setMissionActive(isActive);

                    // Ažuriraj korisnika
                    userRepository.updateUser(user, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void userResult) {
                            Log.d("AllianceMission", "Ažuriran status misije za člana: " + memberId);
                            // Nastavi sa sledećim članom
                            updateAllMembersStatus(memberIds, isActive, index + 1, callback);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("AllianceMission", "Greška pri ažuriranju člana " + memberId + ": " + e.getMessage());
                            // Nastavi sa sledećim uprkos grešci
                            updateAllMembersStatus(memberIds, isActive, index + 1, callback);
                        }
                    });
                } else {
                    // Korisnik nema trenutni savez, preskoči ga
                    Log.w("AllianceMission", "Član " + memberId + " nema trenutni savez");
                    updateAllMembersStatus(memberIds, isActive, index + 1, callback);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AllianceMission", "Greška pri dobavljanju člana " + memberId + ": " + e.getMessage());
                // Nastavi sa sledećim uprkos grešci
                updateAllMembersStatus(memberIds, isActive, index + 1, callback);
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

                MemberProgress userProgress = null;
                for (MemberProgress progress : mission.getMemberProgress()) {
                    if (progress.getUserId().equals(userId)) {
                        userProgress = progress;
                        break;
                    }
                }

                if (userProgress == null) {
                    if (callback != null) callback.onFailure(new Exception("Progress nije pronađen"));
                    return;
                }
                // Proveri da li je misija još aktivna (nije istekla)
                Date now = new Date();
                if (now.after(mission.getEndDate()) && taskType != SpecialTaskType.NO_UNRESOLVED_TASKS) {
                    if (callback != null) callback.onSuccess(false);
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

    private void handleMissionExpiration(AllianceMission mission, Alliance alliance, RepositoryCallback<AllianceMission> callback) {
        // 1. Prvo obradi bonuse za SVE članove
        processMemberBonuses(mission, mission.getMemberProgress(), 0, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // 2. Nakon bonusa, proveri da li je boss pobedjen
                if (mission.getBoss().getCurrentHp() == 0) {
                    // Boss pobedjen - dodeli nagrade
                    distributeRewards(mission, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result2) {
                            closeMission(mission, alliance, callback);
                        }

                        @Override
                        public void onFailure(Exception e) {
                           Log.e("AllianceMission", "Greška pri dodeli nagrada: " + e.getMessage());
                            closeMission(mission, alliance, callback);
                        }
                    });
                } else {
                    // Boss nije pobedjen - samo zatvori misiju bez nagrada
                    closeMission(mission, alliance, callback);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AllianceMission", "Greška pri obradi bonusa: " + e.getMessage());
                closeMission(mission, alliance, callback);
            }
        });
    }

    private void processMemberBonuses(AllianceMission mission, List<MemberProgress> progressList, int index, RepositoryCallback<Void> callback) {
        // Bazni slučaj: svi članovi obrađeni
        if (index >= progressList.size()) {
            callback.onSuccess(null);
            return;
        }

        MemberProgress userProgress = progressList.get(index);

        // Proveri da li ovaj član zaslužuje bonus
        checkAndAwardNoUnresolvedTasksBonus(userProgress.getUserId(), mission, userProgress, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Nastavi sa sledećim članom
                processMemberBonuses(mission, progressList, index + 1, callback);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AllianceMission", "Greška pri obradi bonusa za člana: " + e.getMessage());
                // Nastavi sa sledećim uprkos grešci
                processMemberBonuses(mission, progressList, index + 1, callback);
            }
        });
    }

    private void checkAndAwardNoUnresolvedTasksBonus(String userId, AllianceMission mission, MemberProgress userProgress, RepositoryCallback<Void> callback) {
        Date startDate = mission.getStartDate();
        Date endDate = mission.getEndDate();

        // Proveri da li postoje nezavršeni task instance-i
        taskInstanceRepository.hasUncompletedTaskInstanceInPeriod(userId, startDate, endDate, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean hasUncompleted) {
                if (hasUncompleted) {
                    Log.d("Taskovi", "Ima nezavrsenih intsanci taskova");
                    callback.onSuccess(null);
                    return;
                }

                // Proveri da li postoje task-ovi koji nisu completed
                taskRepository.IsTaskNotCompleted(userId, startDate, endDate, new RepositoryCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean hasNotCompleted) {
                        if (hasNotCompleted) {
                            Log.d("Taskovi", "Ima nezavrsenih taskova");
                            callback.onSuccess(null);
                            return;
                        }

                        // Oba su false - korisnik zaslužuje bonus!
                        if (!userProgress.isNoUnresolvedTasks()) {
                            Log.d("Taskovi", "Promeni na true");
                            // Još nije dobio bonus, dodaj 10 HP damage
                            int bonusDamage = SpecialTaskType.NO_UNRESOLVED_TASKS.getHpDamage();
                            userProgress.setNoUnresolvedTasks(true); // Markira da je dobio bonus
                            userProgress.setTotalDamageDealt(userProgress.getTotalDamageDealt() + bonusDamage);

                            // Smanji HP bossa
                            AllianceBoss boss = mission.getBoss();
                            int newHp = Math.max(0, boss.getCurrentHp() - bonusDamage);
                            boss.setCurrentHp(newHp);

                            // Ažuriraj u bazi
                            allianceMissionRepository.updateMemberProgress(userProgress, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    allianceMissionRepository.updateBossHp(mission.getId(), newHp, new RepositoryCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void result2) {
                                            Log.d("AllianceMission", "Bonus za završene zadatke dodeljen korisniku: " + userId);
                                            callback.onSuccess(null);
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
                        } else {
                            Log.d("Taskovi", "Vec je dobio bonus");
                            callback.onSuccess(null);
                        }
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

    private void closeMission(AllianceMission mission, Alliance alliance, RepositoryCallback<AllianceMission> callback) {
        // 1. Postavi misiju na neaktivnu
        mission.setActive(false);

        allianceMissionRepository.updateAllianceMission(mission, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // 2. Postavi alliance.missionActive na false
                updateAllianceMissionStatus(alliance, false, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result2) {
                        Log.d("AllianceMission", "Misija uspešno zatvorena");
                        if (callback != null) callback.onSuccess(null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("AllianceMission", "Greška pri zatvaranju misije: " + e.getMessage());
                        if (callback != null) callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AllianceMission", "Greška pri ažuriranju misije: " + e.getMessage());
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    private void distributeRewards(AllianceMission mission, RepositoryCallback<Void> callback) {
        List<MemberProgress> progressList = mission.getMemberProgress();

        // Obradi nagrade za sve članove
        distributeRewardsForMembers(mission, progressList, 0, callback);
    }

    private void distributeRewardsForMembers(AllianceMission mission, List<MemberProgress> progressList, int index, RepositoryCallback<Void> callback) {
        // Bazni slučaj: sve nagrade dodeljene
        if (index >= progressList.size()) {
            callback.onSuccess(null);
            return;
        }

        MemberProgress progress = progressList.get(index);

        // 1. Generiši random clothing
        generateRandomClothing(new EquipmentCallback<Clothing>() {
            @Override
            public void onSuccess(Clothing clothing) {
                // 2. Generiši random potion
                generateRandomPotion(new EquipmentCallback<Potion>() {
                    @Override
                    public void onSuccess(Potion potion) {
                        // 3. Izračunaj 50% nagrade od sledećeg bossa
                        calculateNextBossReward(progress.getUserId(), new RepositoryCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer nextBossReward) {
                                int coinsReward = nextBossReward / 2;

                                // 4. Kreiraj reward objekat
                                AllianceMissionReward reward = new AllianceMissionReward();
                                reward.setUserId(progress.getUserId());
                                reward.setPotion(potion);
                                reward.setClothing(clothing);
                                reward.setCoins(coinsReward);
                                reward.setBadgeCount(1);

                                // 5. Sačuvaj reward u bazu
                                allianceMissionRepository.createAllianceMissionReward(reward, new RepositoryCallback<String>() {
                                    @Override
                                    public void onSuccess(String rewardId) {
                                        // 6. Dodaj nagradu korisniku
                                        addRewardToUser(progress.getUserId(), reward, new RepositoryCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void result) {
                                                Log.d("AllianceMission", "Nagrada dodeljena korisniku: " + progress.getUserId());
                                                // Nastavi sa sledećim članom
                                                distributeRewardsForMembers(mission, progressList, index + 1, callback);
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                Log.e("AllianceMission", "Greška pri dodavanju nagrade korisniku: " + e.getMessage());
                                                // Nastavi sa sledećim uprkos grešci
                                                distributeRewardsForMembers(mission, progressList, index + 1, callback);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e("AllianceMission", "Greška pri kreiranju reward zapisa: " + e.getMessage());
                                        distributeRewardsForMembers(mission, progressList, index + 1, callback);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("AllianceMission", "Greška pri računanju nagrade: " + e.getMessage());
                                distributeRewardsForMembers(mission, progressList, index + 1, callback);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("AllianceMission", "Greška pri generisanju potiona: " + e.getMessage());
                        distributeRewardsForMembers(mission, progressList, index + 1, callback);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AllianceMission", "Greška pri generisanju odeće: " + e.getMessage());
                distributeRewardsForMembers(mission, progressList, index + 1, callback);
            }
        });
    }

    private void generateRandomClothing(EquipmentCallback<Clothing> callback) {
        equipmentRepository.getAllEquipment(new RemoteDataSource.DataSourceCallback<List<Equipment>>() {
            @Override
            public void onSuccess(List<Equipment> allEquipment) {
                List<Clothing> clothingList = new ArrayList<>();

                for (Equipment equipment : allEquipment) {
                    if (equipment instanceof Clothing) {
                        clothingList.add((Clothing) equipment);
                    }
                }

                if (clothingList.isEmpty()) {
                    callback.onFailure(new Exception("Nema dostupne odeće"));
                    return;
                }

                Clothing randomClothing = clothingList.get((int) (Math.random() * clothingList.size()));
                callback.onSuccess(randomClothing);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    private void generateRandomPotion(EquipmentCallback<Potion> callback) {
        equipmentRepository.getAllEquipment(new RemoteDataSource.DataSourceCallback<List<Equipment>>() {
            @Override
            public void onSuccess(List<Equipment> allEquipment) {
                List<Potion> potionList = new ArrayList<>();

                for (Equipment equipment : allEquipment) {
                    if (equipment instanceof Potion) {
                        potionList.add((Potion) equipment);
                    }
                }

                if (potionList.isEmpty()) {
                    callback.onFailure(new Exception("Nema dostupnih napitaka"));
                    return;
                }

                Potion randomPotion = potionList.get((int) (Math.random() * potionList.size()));
                callback.onSuccess(randomPotion);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }



    private void calculateNextBossReward(String userId, RepositoryCallback<Integer> callback) {
        // Prvo dohvati trenutni nivo korisnika
        userRepository.getUserById(userId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                int currentLevel = user.getLevelInfo().getLevel();
                        bossRepository.getBossByUserIdAndLevel(userId, currentLevel, new RepositoryCallback<Boss>() {
                            @Override
                            public void onSuccess(Boss currentBoss) {
                                int estimatedNextBossReward = (int) (currentBoss.getCoinsReward() * 1.2);
                                callback.onSuccess(estimatedNextBossReward);
                            }

                            @Override
                            public void onFailure(Exception e2) {
                                // Fallback vrednost
                                callback.onSuccess(200);
                            }
                        });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    private void addRewardToUser(String userId, AllianceMissionReward reward, RepositoryCallback<Void> callback) {
        userRepository.getUserById(userId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                // Dodaj potion sa pravilnom logikom
                if (user.getPotions() == null) {
                    user.setPotions(new ArrayList<>());
                }

                Potion existingPotion = null;
                for (Potion p : user.getPotions()) {
                    if (p.getName().equals(reward.getPotion().getName())) {
                        existingPotion = p;
                        break;
                    }
                }

                if (existingPotion != null) {
                    existingPotion.setQuantity(existingPotion.getQuantity() + 1);
                } else {
                    Potion potionCopy = new Potion(
                            UUID.randomUUID().toString(),
                            reward.getPotion().getName(),
                            reward.getPotion().getPrice(),
                            reward.getPotion().getPowerBoostPercent(),
                            reward.getPotion().isPermanent(),
                            reward.getPotion().isActive(),
                            1,
                            reward.getPotion().getImage()
                    );
                    user.getPotions().add(potionCopy);
                }

                // Dodaj clothing sa pravilnom logikom
                if (user.getClothing() == null) {
                    user.setClothing(new ArrayList<>());
                }

                Clothing existingClothing = null;
                for (Clothing c : user.getClothing()) {
                    if (c.getName().equals(reward.getClothing().getName())) {
                        existingClothing = c;
                        break;
                    }
                }

                if (existingClothing != null) {
                    existingClothing.setQuantity(existingClothing.getQuantity() + 1);
                } else {
                    Clothing clothingCopy = new Clothing(
                            UUID.randomUUID().toString(),
                            reward.getClothing().getName(),
                            reward.getClothing().getPrice(),
                            reward.getClothing().getEffectPercent(),
                            reward.getClothing().isActive(),
                            1,
                            reward.getClothing().getEffectType(),
                            reward.getClothing().getImage()
                    );
                    user.getClothing().add(clothingCopy);
                }

                // Dodaj coins
                int newCoins = user.getCoins() + reward.getCoins();
                user.setCoins(newCoins);
                Log.d("AllianceMission", "Setting new coins: " + user.getCoins());

                // Ažuriraj korisnika
                userRepository.updateUser(user, callback);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    interface EquipmentCallback<T> {
        void onSuccess(T item);
        void onFailure(Exception e);
    }


    }
