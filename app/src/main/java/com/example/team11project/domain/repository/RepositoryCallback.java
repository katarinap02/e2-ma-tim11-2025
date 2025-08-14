package com.example.team11project.domain.repository;

public interface RepositoryCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception e);
}
