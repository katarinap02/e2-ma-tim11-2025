package com.example.team11project.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.AllianceMessage;
import com.example.team11project.domain.repository.AllianceMessageRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.List;
import java.util.concurrent.Executor;

public class AllianceMessageRepositoryImpl implements AllianceMessageRepository {

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;
    private final Executor executor;

    public AllianceMessageRepositoryImpl(Context context, Executor executor) {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
        this.executor = executor;
    }


    @Override
    public void sendMessage(String leaderId, AllianceMessage message, RepositoryCallback<Void> callback) {
        if (message.getAllianceId() == null || leaderId == null) {
            callback.onFailure(new Exception("Message must have allianceId and allianceLeaderId"));
            return;
        }

        executor.execute(() -> {
            remoteDataSource.addMessage(
                    leaderId,
                    message.getAllianceId(),
                    message,
                    new RepositoryCallback<String>() {
                        @Override
                        public void onSuccess(String docId) {
                            message.setId(docId); // setuj ID iz remote
                            executor.execute(() -> {
                                localDataSource.addMessage(message);
                                callback.onSuccess(null);
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
        });
    }
    @Override
    public void getAllMessages(String allianceLeaderId, String allianceId, RepositoryCallback<List<AllianceMessage>> callback) {
        remoteDataSource.getAllMessages(
                allianceLeaderId,
                allianceId,
                new RepositoryCallback<List<AllianceMessage>>() {
                    @Override
                    public void onSuccess(List<AllianceMessage> remoteMessages) {
                        executor.execute(() -> {
                            localDataSource.deleteAllMessagesForAlliance(allianceId);
                            for (AllianceMessage m : remoteMessages) localDataSource.addMessage(m);
                            callback.onSuccess(localDataSource.getAllMessages(allianceId));
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d("AllianceMessageRepo", "Message sync failed: " + e.getMessage());
                        executor.execute(() -> callback.onSuccess(localDataSource.getAllMessages(allianceId)));
                    }
                });
    }

}
