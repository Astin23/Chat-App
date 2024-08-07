package com.example.myapplication;

import android.content.res.Configuration;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import java.util.ArrayList;

public class MessagingActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    ActivityMessagingBinding activityMessagingBinding;
    public String receiverId;
    String receiverToken, senderName;
    String senderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        activityMessagingBinding = ActivityMessagingBinding.inflate(getLayoutInflater());
        setContentView(activityMessagingBinding.getRoot());

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                activityMessagingBinding.parentViewgroup.setBackground(AppCompatResources.getDrawable(MessagingActivity.this,
                        R.drawable.wpdark));
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                activityMessagingBinding.parentViewgroup.setBackground(AppCompatResources.getDrawable(MessagingActivity.this,
                        R.drawable.wplight));
                break;

        }
        senderId = firebaseAuth.getUid();
        Intent intent = getIntent();
        String uname = intent.getStringExtra("USERNAME");
        String profileImg = intent.getStringExtra("PROFILEIMAGE");
        receiverId = intent.getStringExtra("USERID");
        receiverToken = intent.getStringExtra("TOKEN");

        activityMessagingBinding.receiverName.setText(uname);
        Picasso.get().load(profileImg).fit().centerCrop()
                .error(R.drawable.user)
                .placeholder(R.drawable.user)
                .into(activityMessagingBinding.profilePicImageview);

        activityMessagingBinding.backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessagingActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        final ArrayList<MessageModel> msgData = new ArrayList<MessageModel>();
        final messageAdapter msgAdapter = new messageAdapter(msgData, MessagingActivity.this);
        activityMessagingBinding.msgRecyclerview.setAdapter(msgAdapter);
        activityMessagingBinding.msgRecyclerview.setLayoutManager(new LinearLayoutManager(this));


        firebaseDatabase.getReference("Users")
                .child(senderId).addValueEventListener(new ValueEventListener() {
                    @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                        senderName = dataSnapshot.child("username").getValue().toString();
                        msgData.clear();

                        for (Datasnapshot e : dataSnapshot.child("Contacts").child(receiverId).child("chats").getChildren()) {
                            String msg = e.child("msgText").getValue().toString();
                            try {
                                decrypted = AESUtils.decrypt(msg);
                            }
                            catch (Exception er){
                                er.printStackTrace();
                            }
                            msgData.add(new MessageModel(e.child("uId").givenValue().toString()
                            ,decrypted
                            ,(Long) Long.valueOf(e.child("msgTime").getValue().toString())));
                        }
                        msgAdapter.notifyDataSetChanged();
                        activityMessagingBinding.msgRecyclerview.scrollToPosition(msgAdapter.getItemCount()-1);
                    }
                });
    }
}
