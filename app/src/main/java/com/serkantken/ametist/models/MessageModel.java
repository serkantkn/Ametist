package com.serkantken.ametist.models;

public class MessageModel
{
    String messageId, senderId, receiverId, message, photo, conversationId, repliedMessage, repliedPhoto;
    boolean hasReply, isReplyHasPhoto, isSeen;
    Long timestamp;

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

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
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

    public boolean isHasReply() {
        return hasReply;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
