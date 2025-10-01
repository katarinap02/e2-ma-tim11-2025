package com.example.team11project.domain.usecase;

import android.util.Log;

import com.example.team11project.domain.model.Alliance;
import com.example.team11project.domain.model.AllianceBoss;
import com.example.team11project.domain.model.AllianceMission;
import com.example.team11project.domain.model.MemberProgress;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.AllianceMissionRewardRepository;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.MemberProgressRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class AllianceMissionUseCase {
    private final AllianceMissionRepository allianceMissionRepository;

    private final AllianceRepository allianceRepository;

    public AllianceMissionUseCase(AllianceMissionRepository allianceMissionRepository, AllianceRepository allianceRepository)
    {
        this.allianceMissionRepository = allianceMissionRepository;
        this.allianceRepository = allianceRepository;
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
                    createNewMission(alliance, callback);
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

    public void createNewMission(Alliance alliance, RepositoryCallback<AllianceMission> callback) {

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
                alliance.setMissionActive(true);

                // ažuriraj savez
                allianceRepository.updateAlliance(alliance, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (callback != null) callback.onSuccess(mission);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("AllianceMission", "Greška pri ažuriranju saveza: " + e.getMessage());
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


}
