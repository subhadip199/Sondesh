package com.example.sondesh;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{

    private List<Messages> usermessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    public MessageAdapter(List<Messages> usermessageList)
    {
        this.usermessageList=usermessageList;
    }

    public  class MessageViewHolder extends RecyclerView.ViewHolder
    {

        public TextView sendermessageText,receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture,messageReceiverPicture;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sendermessageText=(TextView)itemView.findViewById(R.id.sender_message_text);
            receiverMessageText=(TextView)itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage=(CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageSenderPicture=(ImageView)itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverPicture=(ImageView)itemView.findViewById(R.id.message_receiver_image_view);
        }
    }
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);
        mAuth=FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        String messageSenderId=mAuth.getCurrentUser().getUid();
        Messages messages=usermessageList.get(position);
        String fromUserId=messages.getFrom();
        String fromMessageType=messages.getType();

        usersRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("image"))
                {
                    String receiverImage=dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.sendermessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);

        if(fromMessageType.equals("text"))
        {

            if(fromUserId.equals(messageSenderId))
            {
                holder.sendermessageText.setVisibility(View.VISIBLE);
                holder.sendermessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.sendermessageText.setText(messages.getMessage()+"\n \n"+messages.getTime()+" - "+messages.getDate());
            }
            else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setText(messages.getMessage()+"\n \n"+messages.getTime()+" - "+messages.getDate());
            }
        }
        else if(fromMessageType.equals("image"))
        {

            if(fromUserId.equals(messageSenderId))
            {

                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);
            }
            else
            {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);
            }
        }


        if(fromUserId.equals(messageSenderId))
        {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(usermessageList.get(position).getType().equals("image"))
                    {

                        CharSequence options[]= new CharSequence[]
                                {
                                        "Delete For me",
                                        "View this Image",
                                        "Cancel",
                                        "Delete For Everyone"
                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                if(which==0)
                                {

                                    DeleteSentMessage(position,holder);
                                    Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                              else  if(which==1)
                                {
                                    Intent intent= new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",usermessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if(which==3)
                                {
                                    DeleteMessageForEveryOne(position,holder);
                                    Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }


                            }
                        });
                        builder.show();
                    }
                    else if(usermessageList.get(position).getType().equals("text"))
                    {
                        CharSequence options[]= new CharSequence[]
                                {
                                        "Delete For me",
                                        "Cancel",
                                        "Delete For Everyone"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which==0)
                                {

                                    DeleteSentMessage(position,holder);
                                    Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                else if(which==2)
                                {

                                    DeleteMessageForEveryOne(position,holder);
                                    Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }
                }
            });
        }
        else
        {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(usermessageList.get(position).getType().equals("image"))
                    {
                        CharSequence options[]= new CharSequence[]
                                {
                                        "Delete For me",
                                        "View this Image",
                                        "Cancel",
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                if(which==0)
                                {

                                    DeleteReceiveMessage(position,holder);
                                    Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                else if(which==1)
                                {
                                    Intent intent= new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",usermessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                }



                            }
                        });
                        builder.show();
                    }
                    else if(usermessageList.get(position).getType().equals("text"))
                    {
                        CharSequence options[]= new CharSequence[]
                                {
                                        "Delete For me",
                                        "Cancel",

                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(which==0)
                                {

                                    DeleteReceiveMessage(position,holder);
                                    Intent intent= new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }


                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return usermessageList.size();
    }


    private void DeleteSentMessage(final int position, final MessageViewHolder holder)
    {

        DatabaseReference RootRef=FirebaseDatabase.getInstance().getReference();
        RootRef.child("Messages").child( usermessageList.get(position).getFrom())
                .child( usermessageList.get(position).getTo())
                .child(usermessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void DeleteReceiveMessage(final int position, final MessageViewHolder holder)
    {

        DatabaseReference RootRef=FirebaseDatabase.getInstance().getReference();
        RootRef.child("Messages").child( usermessageList.get(position).getTo())
                .child( usermessageList.get(position).getFrom())
                .child(usermessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void DeleteMessageForEveryOne(final int position, final MessageViewHolder holder)
    {

        final DatabaseReference RootRef=FirebaseDatabase.getInstance().getReference();
        RootRef.child("Messages").child( usermessageList.get(position).getTo())
                .child( usermessageList.get(position).getFrom())
                .child(usermessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {

                    RootRef.child("Messages").child( usermessageList.get(position).getFrom())
                            .child( usermessageList.get(position).getTo())
                            .child(usermessageList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occured", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
