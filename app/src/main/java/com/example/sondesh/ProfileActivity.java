package com.example.sondesh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId,senderUserId,Current_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button SendMessageRequestButton,DeclineMessaheButton;
    private DatabaseReference UserRef,ChatRequestRef,ContactsRef,notificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();
        UserRef=FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserId=getIntent().getExtras().get("visit_user_id").toString();
        senderUserId=mAuth.getCurrentUser().getUid();



        userProfileImage=(CircleImageView)findViewById(R.id.visit_profile_image);
        userProfileName=(TextView) findViewById(R.id.visit_user_name);
        userProfileStatus=(TextView) findViewById(R.id.visit_profile_status);
        SendMessageRequestButton=(Button)findViewById(R.id.send_message_button);
        DeclineMessaheButton=(Button)findViewById(R.id.decline_message_button);
        Current_State="new";


        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists())&& (dataSnapshot.hasChild("image")))
                {
//                    String userImage= dataSnapshot.child("image").getValue().toString();
                    String userName= dataSnapshot.child("names").getValue().toString();
                    String userStatus= dataSnapshot.child("status").getValue().toString();

//                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
                else
                {
                    String userName= dataSnapshot.child("names").getValue().toString();
                    String userStatus= dataSnapshot.child("status").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatRequest();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {

        ChatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiverUserId))
                        {
                            String request_type=dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if(request_type.equals("sent"))
                            {
                                Current_State="request_state";
                                SendMessageRequestButton.setText("Cancel");
                            }
                            else  if(request_type.equals("receieve"))
                            {
                                Current_State="request_receive";
                                SendMessageRequestButton.setText("Accept Chat Request");
                                DeclineMessaheButton.setVisibility(View.VISIBLE);
                                DeclineMessaheButton.setEnabled(true);
                                DeclineMessaheButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatRequest();
                                    }
                                });
                            }


                        }
                        else
                        {
                            ContactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(receiverUserId))
                                            {
                                                Current_State="friends";
                                                SendMessageRequestButton.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        if(!senderUserId.equals(receiverUserId))
        {
        SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageRequestButton.setEnabled(false);
                if(Current_State=="new")
                {
                    SendChatRequest();
                }
                if(Current_State=="request_state")
                {
                    CancelChatRequest();
                }
                if(Current_State=="request_receive")
                {
                    AcceptChatRequest();
                }
                if(Current_State=="friends")
                {
                    RemoveSpecificContact();
                }
            }
        });
        }
        else
        {
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }

    }

    private void RemoveSpecificContact() {
        ContactsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            ContactsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State="new";
                                                SendMessageRequestButton.setText("Send Message");
                                                DeclineMessaheButton.setVisibility(View.INVISIBLE);
                                                DeclineMessaheButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {


        ContactsRef.child(senderUserId).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ContactsRef.child(receiverUserId).child(senderUserId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                ChatRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful())
                                                                {
                                                                    ChatRequestRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        SendMessageRequestButton.setEnabled(true);
                                                                                        Current_State="friends";

                                                                                        SendMessageRequestButton.setText("Remove this Contact");
                                                                                        DeclineMessaheButton.setVisibility(View.INVISIBLE);
                                                                                        DeclineMessaheButton.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelChatRequest() {
        ChatRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            ChatRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State="new";
                                                SendMessageRequestButton.setText("Send Message");
                                                DeclineMessaheButton.setVisibility(View.INVISIBLE);
                                                DeclineMessaheButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest() {
        ChatRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                               receivertosender();
                            }
                    }
                });
    }

    private void receivertosender() {

        ChatRequestRef.child(receiverUserId).child(senderUserId)
                .child("request_type").setValue("receieve")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            HashMap<String,String> chatNotificationMap= new HashMap<>();
                            chatNotificationMap.put("from",senderUserId);
                            chatNotificationMap.put("type","request");


                            notificationRef.child(receiverUserId).push().setValue(chatNotificationMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State="request_state";
                                                SendMessageRequestButton.setText("Cancel");
                                            }
                                        }
                                    });



                        }

                    }
                });

    }


}
