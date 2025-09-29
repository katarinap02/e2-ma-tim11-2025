package com.example.team11project.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.domain.model.AllianceInvite;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.List;
import java.util.concurrent.Executor;

public class AllianceInvitationViewModel extends ViewModel {

    private final AllianceRepository allianceRepository;
    private final Executor executor;

    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<List<AllianceInvite>> invites = new MutableLiveData<>();

    public AllianceInvitationViewModel(AllianceRepository repository, Executor executor) {
        this.allianceRepository = repository;
        this.executor = executor;
    }

    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<String> getError() { return error; }
    public LiveData<List<AllianceInvite>> getInvites() { return invites; }

    public void loadInvites(String userId) {
        executor.execute(() -> {
            allianceRepository.getAllAllianceInvites(userId, new RepositoryCallback<List<AllianceInvite>>() {
                @Override
                public void onSuccess(List<AllianceInvite> data) {
                    invites.postValue(data);
                }

                @Override
                public void onFailure(Exception e) {
                    error.postValue(e.getMessage());
                }
            });
        });
    }

    public void acceptInvite(String inviteId, String userId) {
        executor.execute(() -> {
            allianceRepository.acceptInvite(userId, inviteId, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    successMessage.postValue("Invite accepted successfully!");
                    loadInvites(userId);
                }

                @Override
                public void onFailure(Exception e) {
                    error.postValue(e.getMessage());
                }
            });
        });
    }

    public void rejectInvite(String inviteId, String userId) {
        successMessage.setValue(null);
        error.setValue(null);

        executor.execute(() -> {
            allianceRepository.rejectInvite(userId, inviteId, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    successMessage.postValue("Invite rejected");
                    loadInvites(userId);
                }

                @Override
                public void onFailure(Exception e) {
                    error.postValue(e.getMessage());
                }
            });
        });
    }


    public static class Factory implements ViewModelProvider.Factory {
        private final AllianceRepository repository;
        private final Executor executor;

        public Factory(AllianceRepository repository, Executor executor) {
            this.repository = repository;
            this.executor = executor;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AllianceInvitationViewModel.class)) {
                return (T) new AllianceInvitationViewModel(repository, executor);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
