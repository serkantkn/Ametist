package com.serkantken.ametist.firebase;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.orhanobut.hawk.Hawk;
import com.serkantken.ametist.R;
import com.serkantken.ametist.activities.ChatActivity;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.Utilities;

import java.util.Random;

public class MessagingService extends FirebaseMessagingService {
    UserModel user;
    int notificationId;
    String channelId;
    NotificationCompat.Builder notificationBuilder;
    String messageType = "0";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        user = new UserModel();
        user.setUserId(message.getData().get(Constants.USER_ID));
        user.setName(message.getData().get(Constants.USERNAME));
        user.setToken(message.getData().get(Constants.TOKEN));
        messageType = message.getData().get(Constants.MESSAGE_TYPE);

        Hawk.init(this).build();

        notificationId = new Random().nextInt();
        channelId = "chat_message";

        String username = message.getData().get(Constants.USERNAME);
        String messageString = message.getData().get(Constants.MESSAGE);
        buildNotification(username, messageString);

        createNotificationChannel();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
            notificationManagerCompat.notify(notificationId, notificationBuilder.build());
    }

    private void createNotificationChannel()
    {
        CharSequence channelName = "Chat Message";
        String channelDescription = getResources().getString(R.string.notif_channel_description);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription(channelDescription);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void buildNotification(String username, String message)
    {
        notificationBuilder = new NotificationCompat.Builder(this, channelId);
        notificationBuilder.setSmallIcon(R.mipmap.ametist_logo);
        switch (messageType) {
            case Constants.MESSAGE_TYPE_TEXT:
                notificationBuilder.setContentTitle(username);
                notificationBuilder.setContentText(message);
                break;
            case Constants.MESSAGE_TYPE_FOLLOW:
                notificationBuilder.setContentText(username + getApplicationContext().getResources().getString(R.string.is_now_following_you));
                break;
            case Constants.MESSAGE_TYPE_PHOTO:
                notificationBuilder.setContentText(username + getApplicationContext().getResources().getString(R.string.sent_you_a_photo));
                break;
        }
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        notificationBuilder.setContentIntent(createPendingIntent());
        notificationBuilder.setGroup("Ametist");
        notificationBuilder.setGroupSummary(true);
        notificationBuilder.setAutoCancel(true);
    }

    private PendingIntent createPendingIntent()
    {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("messageNotification", true);
        intent.putExtra("receiverUser", user);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);
        }
        else
        {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        }
        return pendingIntent;
    }
}
