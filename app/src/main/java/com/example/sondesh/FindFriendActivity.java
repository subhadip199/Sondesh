package com.example.sondesh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private RecyclerView FindFriendrecyclerlist;
    private DatabaseReference UserRef;
    private FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);


        database= FirebaseDatabase.getInstance();
        UserRef=database.getReference().child("Users");

        FindFriendrecyclerlist=(RecyclerView)findViewById(R.id.find_friends_recyclerview);
        mtoolbar=(Toolbar) findViewById(R.id.find_friends_toolbar);
        FindFriendrecyclerlist.setLayoutManager(new LinearLayoutManager(this));
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

    }

    @Override
    protected void onStart() {
        super.onStart();

        Query query = UserRef;
        FirebaseRecyclerOptions<Contacts> options= new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(query,
                        new SnapshotParser<Contacts>() {
                    @NonNull
                    @Override
                    public Contacts parseSnapshot(@NonNull DataSnapshot snapshot) {
                        String Name = snapshot.child("names").getValue().toString();
                        String State = snapshot.child("status").getValue().toString();
                        String Image = snapshot.child("image").getValue().toString();
                        Contacts user = new Contacts(Name,State,Image);
                        return user;
                    }
                }
                )
                .build();
        FirebaseRecyclerAdapter<Contacts,FindFriendViewHolder> adapter= new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder findFriendViewHolder, final int i, @NonNull Contacts contacts) {


                findFriendViewHolder.username.setText(contacts.getName());
                findFriendViewHolder.userstatus.setText(contacts.getStatus());
                Picasso.get().load(contacts.getImage()).placeholder(R.drawable.profile_image).into(findFriendViewHolder.profileImage);

                findFriendViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(i).getKey();
                        Intent profileintent= new Intent(FindFriendActivity.this,ProfileActivity.class);
                        profileintent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileintent);
                    }
                });


            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
               View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
               FindFriendViewHolder viewHolder= new FindFriendViewHolder(view);
               return viewHolder;
            }
        };

        FindFriendrecyclerlist.setAdapter(adapter);
        adapter.startListening();
    }

    public static  class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        TextView username,userstatus;
        CircleImageView profileImage;
        View parent;
        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.user_profile_name);
            userstatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.user_profile_image);
            parent = itemView;

        }
    }
}
