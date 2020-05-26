package com.example.sondesh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileImageActivity extends AppCompatActivity {

    private CircleImageView userprofileImage;
    private Button skipImage;
    private DatabaseReference RootRef,ImageRef;
    public static final int GalleryPick=1;
    private StorageReference UserProfileImageRef;
    private String currentUserId ;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private StorageTask uploadTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_image);

        mAuth = FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
        UserProfileImageRef= FirebaseStorage.getInstance().getReference("Profile Images");
        loadingBar=new ProgressDialog(this);
        ImageRef=FirebaseDatabase.getInstance().getReference();


        userprofileImage=(CircleImageView)findViewById(R.id.set_image);
        skipImage=(Button)findViewById(R.id.skip_button);

        userprofileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent= new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
            }
        });
//        nextImage.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                Intent intent= new Intent(ProfileImageActivity.this,UserNameActivity.class);
////                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
////                startActivity(intent);
////                finish();
////            }
////        });
        skipImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Uri imageUri = Uri.parse("android.resource://com.example.sondesh/drawable/profile_image");
                uploadData(imageUri);

//                ImageRef.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if(dataSnapshot.hasChild("imageRef"))
//                        {
//                            String newimage= dataSnapshot.child("imageRef").getValue().toString();
//                            RootRef.child("image").setValue(newimage).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if(task.isSuccessful())
//                                    {
//                                        loadingBar.dismiss();
//                                        Intent intent= new Intent(ProfileImageActivity.this,UserNameActivity.class);
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                        startActivity(intent);
//                                        finish();
//
//                                        Toast.makeText(ProfileImageActivity.this, "Image Uploded successfully", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            });
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });


            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null)
        {
            Uri imageUri=data.getData();
//            Uri imageUri = Uri.parse("android.resource://com.example.sondesh/drawable/profile_image");

            if(uploadTask!=null&& uploadTask.isInProgress())
            {
                Toast.makeText(this, "Upload in progress", Toast.LENGTH_SHORT).show();
            }
            else {
                uploadData(imageUri);
            }
            Picasso.get().load(imageUri).into(userprofileImage);
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
                        RootRef.updateChildren(map);
                        skipImage.setVisibility(View.INVISIBLE);
                        loadingBar.dismiss();
                        Intent intent= new Intent(ProfileImageActivity.this,UserNameActivity.class);
                        startActivity(intent);

                    }
                    else {
                        Toast.makeText(ProfileImageActivity.this, "Faild", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileImageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            });


        }
        else
        {
            Toast.makeText(this, "image not selected", Toast.LENGTH_SHORT).show();
        }

    }
}
