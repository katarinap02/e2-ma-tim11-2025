package com.example.team11project.domain.model;

import java.util.Date;
import java.util.List;

public class AllianceMission {
    private String id;
    private String allianceId;
    private AllianceBoss boss;
    private Date startDate;
    private Date endDate;
    private List<MemberProgress> memberProgress;

    public AllianceMission() {}

    public AllianceMission(String id, String allianceId, AllianceBoss boss, Date startDate, Date endDate, List<MemberProgress> memberProgressMap) {
        this.id = id;
        this.allianceId = allianceId;
        this.boss = boss;
        this.startDate = startDate;
        this.endDate = endDate;
        this.memberProgress = memberProgressMap;
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

    public List<MemberProgress> getMemberProgressMap() {
        return memberProgress;
    }

    public void setMemberProgressMap(List<MemberProgress> memberProgressMap) {
        this.memberProgress = memberProgressMap;
    }
}
