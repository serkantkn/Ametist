package com.serkantken.ametist.utilities.database;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.serkantken.ametist.utilities.database.models.MessageDatabaseModel;

import java.util.List;

@Dao
public interface MainDAO {
    @Insert(onConflict = REPLACE)
    void insert(MessageDatabaseModel messageModel);
    @Query("SELECT * FROM messages WHERE senderId = :senderId AND receiverId = :receiverId")
    List<MessageDatabaseModel> getMessages(String senderId, String receiverId);
    @Delete
    void delete(MessageDatabaseModel messageModel);
}