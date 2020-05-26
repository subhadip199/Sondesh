package com.example.sondesh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String messageReceiverId,messageReceiverName,messageReceiverImage,messageSenderId;
    private TextView userName,userLastseen;
    private CircleImageView userImage;
    private Toolbar chatToolBar;
    private ImageButton sendMessagebutton,sendFilesButton;
    private EditText messageInputText;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private String saveCurrentTime,saveCurrentDate;
    private String checker="",myUrl="";
    private StorageTask uploadTask;
    private ProgressDialog loadingBar;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        messageSenderId=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();

        messageReceiverId=getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName=getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage=getIntent().getExtras().get("user_image").toString();

        InitializeControllers();
        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);
        sendMessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendMessage();
            }
        });

        DisplayLastSeen();

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[]= new CharSequence[]
                        {
                                "Images",
//                                "PDF Files",
//                                "Ms Word Files"
                        };
                AlertDialog.Builder builder= new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select The File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0)
                        {

                            checker="image";
                            Intent intent= new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"),438);
                        }
                        if(which==1)
                        {

                            checker="pdf";
                            Intent intent= new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select PDF File"),438);
                        }
                        if(which==2)
                        {

                            checker="docx";
                            Intent intent= new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent,"Select Ms Word File"),438);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void InitializeControllers() {

        chatToolBar=(Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolBar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView= layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        userImage=(CircleImageView)findViewById(R.id.custom_profile_image);
        userName=(TextView) findViewById(R.id.custom_profile_name);
        userLastseen=(TextView) findViewById(R.id.custom_user_last_seen);
        sendMessagebutton=(ImageButton) findViewById(R.id.send_message_btn);
        sendFilesButton=(ImageButton) findViewById(R.id.send_files_btn);
        messageInputText=(EditText) findViewById(R.id.input_message);


        loadingBar=new ProgressDialog(this);

        messageAdapter= new MessageAdapter(messagesList);
        userMessagesList=(RecyclerView)findViewById(R.id.private_message_list_of_users);
        linearLayoutManager= new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


        Calendar calendar= Calendar.getInstance();

        SimpleDateFormat currentDate= new SimpleDateFormat("MMM dd, YYYY");
        saveCurrentDate= currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime= new SimpleDateFormat("hh:mm a");
        saveCurrentTime= currentTime.format(calendar.getTime());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==438 && resultCode==RESULT_OK&& data!=null && data.getData()!=null)
        {

            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please Wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            fileUri=data.getData();
            if(!checker.equals("image"))
            {

            }
            else if(checker.equals("image"))
            {
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef="Messages/"+messageSenderId+"/"+messageReceiverId;
                final String messageReceiverRef="Messages/"+messageReceiverId+"/"+messageSenderId;
                DatabaseReference userMessageKeyRef= RootRef.child("Messages")
                        .child(messageSenderId).child(messageReceiverId).push();
                final String messagePushId=userMessageKeyRef.getKey();

                final StorageReference filePath= storageReference.child(messagePushId+"."+"jpg");
                uploadTask=filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri> () {
                    @Override
                    public void onComplete(@NonNull Task<Uri>  task) {

                        if(task.isSuccessful())
                        {
                            Uri downloadUri=task.getResult();
                            myUrl=downloadUri.toString();

                            Map messageTextBody= new HashMap();
                            messageTextBody.put("message",myUrl);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type",checker);
                            messageTextBody.put("from",messageSenderId);
                            messageTextBody.put("to",messageReceiverId);
                            messageTextBody.put("messageID",messagePushId);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("date",saveCurrentDate);

                            Map messageBodyDetails= new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
                            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageTextBody);
                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "message sent successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    messageInputText.setText("");
                                }
                            });
                        }
                    }
                });

            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing selected or Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageReceiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state=dataSnapshot.child("userState").child("state").getValue().toString();
                            String date=dataSnapshot.child("userState").child("date").getValue().toString();
                            String time =dataSnapshot.child("userState").child("time").getValue().toString();

                            if(state.equals("Online"))
                            {
                                userLastseen.setText("Online");
                            }
                            else if(state.equals("Offline"))
                            {
                                userLastseen.setText("Last Seen: "+date+" "+time);
                            }
                        }
                        else
                        {
                            userLastseen.setText("Offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Messages messages=dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();


                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendMessage()
    {

        String messagetext=messageInputText.getText().toString();
        if(messagetext.isEmpty())
        {
            messageInputText.setError("Enter an email address");
            messageInputText.requestFocus();
            return;
        }
        else
        {
            String messageSenderRef="Messages/"+messageSenderId+"/"+messageReceiverId;
            String messageReceiverRef="Messages/"+messageReceiverId+"/"+messageSenderId;
            DatabaseReference userMessageKeyRef= RootRef.child("Messages")
                    .child(messageSenderId).child(messageReceiverId).push();
            String messagePushId=userMessageKeyRef.getKey();
            Map messageTextBody= new HashMap();
            messageTextBody.put("message",messagetext);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);
            messageTextBody.put("to",messageReceiverId);
            messageTextBody.put("messageID",messagePushId);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);

            Map messageBodyDetails= new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageTextBody);
            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "message sent successfully", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });

        }
    }
}
