package com.example.team11project.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.domain.model.AllianceMessage;
import com.example.team11project.domain.repository.AllianceMessageRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AllianceChatViewModel extends ViewModel {

    private final AllianceMessageRepository repository;
    private final Executor executor;
    private final MutableLiveData<List<AllianceMessage>> messages = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> notification = new MutableLiveData<>();

    public AllianceChatViewModel(AllianceMessageRepository repository, Executor executor) {
        this.repository = repository;
        this.executor = executor;
    }

    public LiveData<List<AllianceMessage>> getMessages() { return messages; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getNotification() { return notification; }

    public void loadMessages(String leaderId, String allianceId) {
        executor.execute(() -> repository.getAllMessages(leaderId, allianceId, new RepositoryCallback<List<AllianceMessage>>() {
            @Override
            public void onSuccess(List<AllianceMessage> data) {
                messages.postValue(data);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue(e.getMessage());
            }
        }));
    }

    public void sendMessage(String leaderId, AllianceMessage message) {
        executor.execute(() -> repository.sendMessage(leaderId, message, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                List<AllianceMessage> current = messages.getValue();
                if (current != null) current.add(message);
                messages.postValue(current);

                notification.postValue(message.getSenderUsername() + " je poslao novu poruku");
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue(e.getMessage());
            }
        }));
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final AllianceMessageRepository repository;
        private final Executor executor;

        public Factory(AllianceMessageRepository repository, Executor executor) {
            this.repository = repository;
            this.executor = executor;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AllianceChatViewModel.class)) {
                return (T) new AllianceChatViewModel(repository, executor);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
