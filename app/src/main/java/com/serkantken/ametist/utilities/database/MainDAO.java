package com.serkantken.ametist.utilities.database;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.serkantken.ametist.models.MessageModel;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface MainDAO {
    @Insert(onConflict = REPLACE)
    void insert(MessageModel messageModel);
    @Query("SELECT * FROM messages WHERE senderId = :senderId AND receiverId = :receiverId")
    List<MessageModel> getMessages(String senderId, String receiverId);
    @Query("SELECT * FROM messages WHERE messageId = :messageId")
    boolean isMessageExist(String messageId);
    @Query("UPDATE messages SET isSeen = :isSeen, seenTimestamp = :seenDate WHERE messageId = :messageId")
    void  update(String messageId, boolean isSeen, long seenDate);
    @Delete
    void delete(MessageModel messageModel);
}