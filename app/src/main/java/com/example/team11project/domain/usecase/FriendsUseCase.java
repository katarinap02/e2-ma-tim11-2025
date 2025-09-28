package com.example.team11project.domain.usecase;

import android.util.Log;

import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FriendsUseCase {

    private UserRepository userRepository;

    public FriendsUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void getFriends(String currentUserId, RepositoryCallback<List<User>> callback) {
        userRepository.getUserById(currentUserId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User currentUser) {
                List<String> friendIds = currentUser.getFriends();
                if (friendIds == null || friendIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                List<User> friends = Collections.synchronizedList(new ArrayList<>());
                AtomicInteger counter = new AtomicInteger(friendIds.size());

                for (String friendId : friendIds) {
                    userRepository.getUserById(friendId, new RepositoryCallback<User>() {
                        @Override
                        public void onSuccess(User friend) {
                            friends.add(friend);
                            if (counter.decrementAndGet() == 0) {
                                callback.onSuccess(friends);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            if (counter.decrementAndGet() == 0) {
                                callback.onSuccess(friends);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void getNonFriends(String currentUserId, RepositoryCallback<List<User>> callback) {
        userRepository.getUserById(currentUserId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User currentUser) {
                userRepository.getAllUsers(new RepositoryCallback<List<User>>() {
                    @Override
                    public void onSuccess(List<User> allUsers) {
                        List<User> nonFriends = new ArrayList<>();
                        List<String> friendIds = currentUser.getFriends();
                        for (User user : allUsers) {
                            if (!user.getId().equals(currentUserId) &&
                                    (friendIds == null || !friendIds.contains(user.getId()))) {
                                nonFriends.add(user);
                            }
                        }
                        callback.onSuccess(nonFriends);
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

    public void addFriend(String currentUserId, String newFriendId, RepositoryCallback<Boolean> callback) {
        userRepository.getUserById(currentUserId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User currentUser) {
                userRepository.getUserById(newFriendId, new RepositoryCallback<User>() {
                    @Override
                    public void onSuccess(User newFriend) {
                        boolean updated = false;

                        if (!currentUser.getFriends().contains(newFriendId)) {
                            currentUser.getFriends().add(newFriendId);
                            updated = true;
                        }

                        if (!newFriend.getFriends().contains(currentUserId)) {
                            newFriend.getFriends().add(currentUserId);
                            updated = true;
                        }

                        if (updated) {
                            userRepository.updateUser(currentUser, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    userRepository.updateUser(newFriend, new RepositoryCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void data) {
                                            callback.onSuccess(true);
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
                            callback.onSuccess(true);
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

}
