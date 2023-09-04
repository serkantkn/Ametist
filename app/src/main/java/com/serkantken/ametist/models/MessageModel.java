package com.serkantken.ametist.models;

import java.io.Serializable;

public class MessageModel implements Serializable
{
    String messageId = "";
    String senderId = "";
    String receiverId = "";
    String message = "";
    String photo = "";
    String voice = "";
    String conversationId = "";
    String repliedMessage = "";
    String repliedPhoto = "";
    String repliedUserId = "";
    boolean hasReply = false;
    boolean isReplyHasPhoto = false;
    boolean isSeen = false;
    boolean isPlayed = false;
    Long timestamp = 0L;
    Long seenTimestamp = 0L;

    public MessageModel() {
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getSeenTimestamp() {
        return seenTimestamp;
    }

    public void setSeenTimestamp(Long seenTimestamp) {
        this.seenTimestamp = seenTimestamp;
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

    public String getRepliedUserId() {
        return repliedUserId;
    }

    public void setRepliedUserId(String repliedUserId) {
        this.repliedUserId = repliedUserId;
    }

    public boolean hasReply() {
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
}
