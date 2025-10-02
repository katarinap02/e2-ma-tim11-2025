package com.example.team11project.domain.repository;

import java.util.Map;

public interface StatisticRepository {
    void getTasksSummary(String userId, RepositoryCallback<Map<String, Integer>> callback);
    void getCompletedTasksPerCategory(String userId, RepositoryCallback<Map<String, Integer>> callback);
    void getAverageTaskDifficulty(String userId, RepositoryCallback<Float> callback);
    void getLongestSuccessStreak(String userId, RepositoryCallback<Integer> callback);
    void getUserAllianceMissionsSummary(String userId, RepositoryCallback<Map<String, Integer>> callback);


}
