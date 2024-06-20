package com.example.hw2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class MessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Intent intent = getIntent();
        Message message = (Message) intent.getSerializableExtra("message");

        if (message != null) {
            TextView Name = findViewById(R.id.name);
            TextView Text = findViewById(R.id.text);
            ImageView Avatar = findViewById(R.id.avatar);

            Glide.with(this).load(message.Avatar).into(Avatar);
            Name.setText(message.Name);
            Text.setText(message.Text);
        } else {
            // Handle the case where message is null
            // For example, show an error message or finish the activity
            finish();
        }
    }
}
