package com.serkantken.ametist.models;

import java.io.Serializable;

public class UserModel implements Serializable
{
    String userId, name, email, password, profilePic, profilePicSquare, picSecond, picThird, picFourth, about, gender, age, token;
    int followerCount, followingCount;
    Boolean isOnline;
    Long lastSeen, signupDate;

    public UserModel()
    {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getPicSecond() {
        return picSecond;
    }

    public String getProfilePicSquare() {
        return profilePicSquare;
    }

    public void setProfilePicSquare(String profilePicSquare) {
        this.profilePicSquare = profilePicSquare;
    }

    public void setPicSecond(String picSecond) {
        this.picSecond = picSecond;
    }

    public String getPicThird() {
        return picThird;
    }

    public void setPicThird(String picThird) {
        this.picThird = picThird;
    }

    public String getPicFourth() {
        return picFourth;
    }

    public void setPicFourth(String picFourth) {
        this.picFourth = picFourth;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }

    public Long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public Long getSignupDate() {
        return signupDate;
    }

    public void setSignupDate(Long signupDate) {
        this.signupDate = signupDate;
    }
}
