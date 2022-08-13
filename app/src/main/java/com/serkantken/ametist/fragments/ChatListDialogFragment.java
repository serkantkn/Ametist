package com.serkantken.ametist.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.serkantken.ametist.R;
import com.serkantken.ametist.activities.ChatActivity;
import com.serkantken.ametist.adapters.ChatListAdapter;
import com.serkantken.ametist.databinding.LayoutMessageListBinding;
import com.serkantken.ametist.models.MessageModel;
import com.serkantken.ametist.models.UserModel;
import com.serkantken.ametist.utilities.UserListener;
import com.serkantken.ametist.utilities.Utilities;

import java.util.ArrayList;
import java.util.Collections;

public class ChatListDialogFragment extends BottomSheetDialogFragment implements UserListener
{
    LayoutMessageListBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore database;
    ArrayList<MessageModel> messageList = new ArrayList<>();
    ChatListAdapter adapter;

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme_Chat;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        binding = LayoutMessageListBinding.inflate(getLayoutInflater(), container, false);
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        Utilities utilities = new Utilities(requireContext(), requireActivity());
        utilities.blur(binding.blur, 10f, false);
        adapter = new ChatListAdapter(messageList, getContext(), getActivity(), this);
        binding.rvMessageList.setAdapter(adapter);
        binding.rvMessageList.setLayoutManager(new LinearLayoutManager(getActivity()));
        //messageList(binding.progressbar);
        listenConversations();

        binding.imgRefresh.setOnClickListener(view -> listenConversations());
        return binding.getRoot();
    }

    private void messageList(View progressBar)
    {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("chats")
                .whereEqualTo("senderId", FirebaseAuth.getInstance().getUid())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        messageList.clear();
                        MessageModel model1 = new MessageModel();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                        {
                            model1.setMessage(documentSnapshot.getString("message"));
                            model1.setTimestamp(documentSnapshot.getLong("timestamp"));
                            model1.setSenderId(documentSnapshot.getString("senderId"));
                            model1.setReceiverId(documentSnapshot.getString("receiverId"));
                        }
                        messageList.add(model1);
                    }
                });
        FirebaseFirestore.getInstance().collection("chats")
                .whereEqualTo("receiverId", FirebaseAuth.getInstance().getUid())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        MessageModel model2 = new MessageModel();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                        {
                            model2.setMessage(documentSnapshot.getString("message"));
                            model2.setTimestamp(documentSnapshot.getLong("timestamp"));
                            model2.setSenderId(documentSnapshot.getString("senderId"));
                            model2.setReceiverId(documentSnapshot.getString("receiverId"));
                        }
                        messageList.add(model2);
                    }
                });

        Collections.sort(messageList, (obj1, obj2) -> obj2.getTimestamp().compareTo(obj1.getTimestamp()));
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    private void listenConversations()
    {
        binding.progressbar.setVisibility(View.VISIBLE);
        messageList.clear();
        database.collection("conversations")
                .whereEqualTo("senderId", auth.getUid())
                .addSnapshotListener(eventListener);
        database.collection("conversations")
                .whereEqualTo("receiverId", auth.getUid())
                .addSnapshotListener(eventListener);
    }

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

                    if (auth.getUid().equals(documentChange.getDocument().getString("senderId")))
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
            Collections.sort(messageList, (obj1, obj2) -> obj2.getTimestamp().compareTo(obj1.getTimestamp()));
            adapter.notifyDataSetChanged();
            binding.rvMessageList.smoothScrollToPosition(0);
            binding.progressbar.setVisibility(View.GONE);
        }
    });

    @Override
    public void onUserClicked(UserModel userModel) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("receiverUser", userModel);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(intent);
        dismiss();
    }
}
