package com.example.team11project.domain.model;

public class User {
    private String id;
    private String username;
    private String email;
    private String password;
    private String avatar;
    private boolean isVerified;

    public User(){}

    public User(String id, String username, String email, String password, String avatar, boolean isVerified){
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.isVerified = isVerified;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean getVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }
}
