package com.example.sondesh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Transformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import javax.xml.transform.Result;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button UpdateAccountSettings;
    private EditText username,userstatus;
    private CircleImageView UserProfileImage;
    private String currentUserId ;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef,ImageRef;
    public static final int GalleryPick=1;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;
    private StorageTask uploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        Initializefields();
        mAuth = FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");
        ImageRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);





        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updatesettings();


            }
        });

        RetrieveUserInfo();

//        username.setVisibility(View.INVISIBLE);

        UserProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent= new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
            }
        });

    }



    private void RetrieveUserInfo() {
        RootRef.child("Users").child(this.currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists())&& (dataSnapshot.hasChild("names"))&& (dataSnapshot.hasChild("image")))
                {
                    String retrieveUserName=dataSnapshot.child("names").getValue().toString();
                    String retrieveStatus=dataSnapshot.child("status").getValue().toString();
                    String retrieveImage=dataSnapshot.child("image").getValue().toString();
                    username.setText(retrieveUserName);
                    userstatus.setText(retrieveStatus);
                    Picasso.get()
                            .load(retrieveImage)
                            .placeholder(R.drawable.profile_image)
                            .into(UserProfileImage);

                }
                else if((dataSnapshot.exists())&& (dataSnapshot.hasChild("names")))
                {
                    String retrieveUserName=dataSnapshot.child("names").getValue().toString();
                    String retrieveStatus=dataSnapshot.child("status").getValue().toString();

                    username.setText(retrieveUserName);
                    userstatus.setText(retrieveStatus);
                }
                else
                {
//                    username.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Please set and update Information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();

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

    private void Initializefields() {

        UpdateAccountSettings=(Button)findViewById(R.id.update_settings_button);
        username=(EditText)findViewById(R.id.set_user_name);
        userstatus=(EditText)findViewById(R.id.set_profile_status);
        UserProfileImage=(CircleImageView)findViewById(R.id.set_profile_image);
        loadingBar=new ProgressDialog(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null)
        {


            Uri imageUri=data.getData();


            if(uploadTask!=null&& uploadTask.isInProgress())
            {
                Toast.makeText(this, "Upload in progress", Toast.LENGTH_SHORT).show();
            }
            else {
                uploadData(imageUri);
            }
            Picasso.get().load(imageUri).into(UserProfileImage);

        }
    }


    public String getFileExtention(Uri imageUri)
    {
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        String type = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
        return type;
    }
    private void uploadData(Uri imageUri) {
        loadingBar.setTitle("Set Profile Image");
        loadingBar.setMessage("Please Wait...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        if(imageUri!=null)
        {
            final StorageReference filereference=UserProfileImageRef.child(System.currentTimeMillis()+"."+getFileExtention(imageUri));

            uploadTask=filereference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return filereference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if(task.isSuccessful())
                    {
                        Uri downloadUri=task.getResult();
                        String mUri=downloadUri.toString();
                        HashMap<String,Object> map= new HashMap<>();
                        map.put("image",mUri);
                        ImageRef.updateChildren(map);
                        loadingBar.dismiss();


                    }
                    else {
                        Toast.makeText(SettingsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            });


        }
        else
        {
            Toast.makeText(this, "image not selected", Toast.LENGTH_SHORT).show();
        }

    }

    private void SendUserToMainActivity() {
        Intent mainintent = new Intent(SettingsActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SendUserToMainActivity();
    }

}
