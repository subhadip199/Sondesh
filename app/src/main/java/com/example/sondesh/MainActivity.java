package com.example.sondesh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private ViewPager myviewpager;
    private TabLayout mytablayout;
    private  TabAccessorAdapter mytabAccessorAdapter;
//    private FirebaseUser CurrentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
//        CurrentUser=mAuth.getCurrentUser();
        RootRef= FirebaseDatabase.getInstance().getReference();


        mtoolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("সন্দেশ");


        myviewpager=(ViewPager)findViewById(R.id.main_tab_pager);
        mytabAccessorAdapter= new TabAccessorAdapter(getSupportFragmentManager());
        myviewpager.setAdapter(mytabAccessorAdapter);

        mytablayout=(TabLayout)findViewById(R.id.main_tab);
        mytablayout.setupWithViewPager(myviewpager);

    }

    @Override
    protected void onStart() {
        super.onStart();

         FirebaseUser CurrentUser=mAuth.getCurrentUser();
        if(CurrentUser==null)
        {
            SendUserToLogInActivity();
        }
        else
        {


            VerifyUserExistance();
            UpdateUserStatus("Online");

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser CurrentUser=mAuth.getCurrentUser();
        if(CurrentUser!=null)
        {
            UpdateUserStatus("Offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser CurrentUser=mAuth.getCurrentUser();
        if(CurrentUser!=null)
        {
            UpdateUserStatus("Offline");
        }
    }

    private void VerifyUserExistance() {

        final String currentUserId=mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.child("names").exists()))
                {

                    Toast.makeText(getApplicationContext(),"Welcome",Toast.LENGTH_LONG).show();
                }
                else
                {

                                SendUserToProfileImageActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });

    }

    private void SendUserToProfileImageActivity() {
        Intent imageintent = new Intent(MainActivity.this , ProfileImageActivity.class);
        imageintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(imageintent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.option_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_find_friends_option)
        {
            SendUserToFindFriendActivity();
        }
        if(item.getItemId()==R.id.main_settings_option)
        {
            SendUserToSettingsActivity();
        }
        if(item.getItemId()==R.id.main_logout_option)
        {
            UpdateUserStatus("Offline");
            mAuth.signOut();

            SendUserToLogInActivity();
        }
        if(item.getItemId()==R.id.main_create_group_option)
        {
            RequestNewGroup();
        }
        return true;
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name : ");
        final EditText groupNameField= new EditText(MainActivity.this);
        groupNameField.setHint("e.g Coding Cafe");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupname=groupNameField.getText().toString();
                if(groupname.isEmpty())
                {
                    groupNameField.setError("Enter an email address");
                    groupNameField.requestFocus();
                    return;
                }
                else
                {
                    CreateNewGroup(groupname);
                }
            }
        });
        builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void CreateNewGroup(final String groupname) {
        RootRef.child("Groups").child(groupname).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, groupname+"is created Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToLogInActivity() {

        Intent intent = new Intent(MainActivity.this , LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void SendUserToSettingsActivity() {

        Intent settingsintent = new Intent(MainActivity.this , SettingsActivity.class);
        settingsintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsintent);
        finish();
    }
    private void SendUserToFindFriendActivity() {

        Intent findfriendintent = new Intent(MainActivity.this , FindFriendActivity.class);
        startActivity(findfriendintent);

    }
    private  void UpdateUserStatus(String state)
    {
        String saveCurrentTime,saveCurrentDate;
        Calendar calendar= Calendar.getInstance();

        SimpleDateFormat currentDate= new SimpleDateFormat("MMM dd, YYYY");
        saveCurrentDate= currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime= new SimpleDateFormat("hh:mm a");
        saveCurrentTime= currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);


        currentUserId=mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).child("userState")
                .updateChildren(onlineStateMap);
    }



}
