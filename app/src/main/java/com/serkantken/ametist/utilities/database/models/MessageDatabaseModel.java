package com.serkantken.ametist.utilities.database.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "messages")
public class MessageDatabaseModel implements Serializable
{
    @PrimaryKey(autoGenerate = true)
    int id = 0;
    @ColumnInfo(name = "messageId")
    String messageId = "";
    @ColumnInfo(name = "senderId")
    String senderId = "";
    @ColumnInfo(name = "receiverId")
    String receiverId = "";
    @ColumnInfo(name = "message")
    String message = "";
    @ColumnInfo(name = "photo")
    String photo = "";
    @ColumnInfo(name = "voice")
    String voice = "";
    @ColumnInfo(name = "conversationId")
    String conversationId = "";
    @ColumnInfo(name = "repliedMessage")
    String repliedMessage = "";
    @ColumnInfo(name = "repliedPhoto")
    String repliedPhoto = "";
    @ColumnInfo(name = "hasReply")
    boolean hasReply = false;
    @ColumnInfo(name = "isReplyHasPhoto")
    boolean isReplyHasPhoto = false;
    @ColumnInfo(name = "isSeen")
    boolean isSeen = false;
    @ColumnInfo(name = "isPlayed")
    boolean isPlayed = false;
    @ColumnInfo(name = "timestamp")
    Long timestamp = 0L;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getRepliedMessage() {
        return repliedMessage;
    }

    public void setRepliedMessage(String repliedMessage) {
        this.repliedMessage = repliedMessage;
    }

    public String getRepliedPhoto() {
        return repliedPhoto;
    }

    public void setRepliedPhoto(String repliedPhoto) {
        this.repliedPhoto = repliedPhoto;
    }

    public boolean isHasReply() {
        return hasReply;
    }

    public void setHasReply(boolean hasReply) {
        this.hasReply = hasReply;
    }

    public boolean isReplyHasPhoto() {
        return isReplyHasPhoto;
    }

    public void setReplyHasPhoto(boolean replyHasPhoto) {
        isReplyHasPhoto = replyHasPhoto;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public boolean isPlayed() {
        return isPlayed;
    }

    public void setPlayed(boolean played) {
        isPlayed = played;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
