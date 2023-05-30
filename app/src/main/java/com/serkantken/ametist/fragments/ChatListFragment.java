package com.serkantken.ametist.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.serkantken.ametist.activities.ChatActivity;
import com.serkantken.ametist.activities.MainActivity;
import com.serkantken.ametist.adapters.ChatListAdapter;
import com.serkantken.ametist.databinding.FragmentChatListBinding;
import com.serkantken.ametist.models.MessageModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.UserListener;
import com.serkantken.ametist.utilities.Utilities;

import java.util.ArrayList;
import java.util.Objects;

public class ChatListFragment extends Fragment implements UserListener
{
    private FragmentChatListBinding binding;
    private Utilities utilities;
    private FirebaseFirestore database;
    private FirebaseAuth auth;
    private ChatListAdapter adapter;
    private ArrayList<MessageModel> messageList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentChatListBinding.inflate(inflater);

        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        utilities = new Utilities(requireContext(), requireActivity());

        adapter = new ChatListAdapter(messageList, requireContext(), requireActivity(), this);
        binding.rvMessageList.setAdapter(adapter);
        binding.rvMessageList.setPadding(0, utilities.getStatusBarHeight()+utilities.convertDpToPixel(66), 0, utilities.getNavigationBarHeight(Configuration.ORIENTATION_PORTRAIT)+utilities.convertDpToPixel(66));
        binding.rvMessageList.setClipToPadding(false);

        listenConversations();

        binding.conversationRefresher.setOnRefreshListener(this::listenConversations);

        return binding.getRoot();
    }

    private void listenConversations()
    {
        messageList.clear();
        database.collection("conversations")
                .whereEqualTo("senderId", auth.getUid())
                .addSnapshotListener(eventListener);
        database.collection("conversations")
                .whereEqualTo("receiverId", auth.getUid())
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null)
        {
            return;
        }
        if (value != null)
        {
            for (DocumentChange documentChange : value.getDocumentChanges())
            {
                if (documentChange.getType() == DocumentChange.Type.ADDED)
                {
                    MessageModel model = new MessageModel();
                    model.setSenderId(documentChange.getDocument().getString("senderId"));
                    model.setReceiverId(documentChange.getDocument().getString("receiverId"));

                    if (Objects.equals(auth.getUid(), documentChange.getDocument().getString("senderId")))
                    {
                        model.setConversationId(documentChange.getDocument().getString("receiverId"));
                    }
                    else
                    {
                        model.setConversationId(documentChange.getDocument().getString("senderId"));
                    }

                    model.setMessage(documentChange.getDocument().getString("message"));
                    model.setTimestamp(documentChange.getDocument().getLong("timestamp"));
                    messageList.add(model);
                }
                else if (documentChange.getType() == DocumentChange.Type.MODIFIED)
                {
                    for (int i = 0; i < messageList.size(); i++)
                    {
                        String senderId = documentChange.getDocument().getString("senderId");
                        String receiverId = documentChange.getDocument().getString("receiverId");
                        if (messageList.get(i).getSenderId().equals(senderId) && messageList.get(i).getReceiverId().equals(receiverId))
                        {
                            messageList.get(i).setMessage(documentChange.getDocument().getString("message"));
                            messageList.get(i).setTimestamp(documentChange.getDocument().getLong("timestamp"));
                            break;
                        }
                    }
                }
            }
            messageList.sort((obj1, obj2) -> obj2.getTimestamp().compareTo(obj1.getTimestamp()));

            adapter.notifyDataSetChanged();
            binding.rvMessageList.smoothScrollToPosition(0);
            binding.conversationRefresher.setRefreshing(false);
        }
    });

    @Override
    public void onUserClicked(UserModel userModel) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra("receiverUser", userModel);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}