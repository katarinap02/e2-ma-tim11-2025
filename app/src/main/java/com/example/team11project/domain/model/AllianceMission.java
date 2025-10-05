package com.example.team11project.domain.model;

import java.util.Date;
import java.util.List;

public class AllianceMission {
    private String id;
    private String allianceId;
    private AllianceBoss boss;
    private Date startDate;
    private Date endDate;

    private boolean isActive;
    private List<MemberProgress> memberProgress;

    public AllianceMission() {}

    public AllianceMission(String id, String allianceId, AllianceBoss boss, Date startDate, Date endDate, List<MemberProgress> memberProgress, boolean isActive) {
        this.id = id;
        this.allianceId = allianceId;
        this.boss = boss;
        this.startDate = startDate;
        this.endDate = endDate;
        this.memberProgress = memberProgress;
        this.isActive = isActive;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public AllianceBoss getBoss() {
        return boss;
    }

    public void setBoss(AllianceBoss boss) {
        this.boss = boss;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<MemberProgress> getMemberProgress() {
        return memberProgress;
    }

    public void setMemberProgress(List<MemberProgress> memberProgressMap) {
        this.memberProgress = memberProgressMap;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

}
