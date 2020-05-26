package com.example.sondesh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserNameActivity extends AppCompatActivity {
    private Button UpdateAccountSettings;
    private EditText username,userstatus;
    private String currentUserId ;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);

        mAuth = FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();

        Initializefields();
    }
    private void Initializefields() {

        UpdateAccountSettings=(Button)findViewById(R.id.update_button);
        username=(EditText)findViewById(R.id.user_name);
        userstatus=(EditText)findViewById(R.id.profile_status);
        loadingBar=new ProgressDialog(this);

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                updatesettings();



            }
        });
    }


    private void updatesettings() {
        String setUserName=username.getText().toString();
        String setStatus = userstatus.getText().toString();
        String newstatus="Online";

        if(setUserName.isEmpty())
        {
            username.setError("Enter UserName");
            username.requestFocus();
            return;
        }
        else
        {
            if(!setStatus.isEmpty())
            {
                newstatus=setStatus;
            }
            HashMap<String,Object> profilemap= new HashMap<>();
            profilemap.put("uid",currentUserId);
            profilemap.put("names",setUserName);
            profilemap.put("status",newstatus);
            RootRef.child("Users").child(currentUserId).updateChildren(profilemap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {

                        loadingBar.setTitle("Welcome");
                        loadingBar.setMessage("Please Wait...");
                        loadingBar.setCanceledOnTouchOutside(true);
                        loadingBar.show();


                        Toast.makeText(UserNameActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        Intent mainintent = new Intent(UserNameActivity.this,MainActivity.class);
                        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainintent);
                        finish();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        String msg = task.getException().toString();
                        Toast.makeText(getApplicationContext(),"Error : "+msg ,Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainintent = new Intent(UserNameActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }
}
