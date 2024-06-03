package com.example.hw2;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<Message> Messages;

    public MessageAdapter() {
        super();
        Messages = new ArrayList<>();
        db.collection("Messages")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Messages.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Message c = new Message(document.get("Avatar").toString(),document.get("Name").toString(),document.get("Text").toString(),document.get("ID").toString());
                                Messages.add(c);
                            }
                            notifyDataSetChanged();
                        }
                    }
                });
        db.collection("Messages").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                Messages.clear();
                for (QueryDocumentSnapshot document : value) {
                    Message c = new Message(document.get("Avatar").toString(),document.get("Name").toString(),document.get("Text").toString(),document.get("ID").toString());
                    Messages.add(c);
                }
                notifyDataSetChanged();
            }
        });
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message,parent,false);
        MessageViewHolder viewHolder = new MessageViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = Messages.get(position);
        Glide.with(holder.Avatar).load(message.Avatar).into(holder.Avatar);
        holder.Name.setText(message.Name);
        holder.Text.setText(message.Text);
        holder.Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),MessageActivity.class);
                //intent.putExtra("message",message);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        (Activity) v.getContext(),
                        holder.Card,
                        "cardTransition"
                );
                v.getContext().startActivity(intent,options.toBundle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return Messages.size();
    }

    public void DeleteMessage(int pos) {
        Message m = Messages.get(-1);
        db.collection("Messages").document(m.ID).delete();
    }
}
