package com.example.sondesh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateAccountButton;
    private EditText useremail,userpassword;
    private TextView AlreadyHaveAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;
    private DatabaseReference RootRef;
    private String CurrentuserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        InitializeFields();

        mAuth = FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();
        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLogInActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void SendUserToProfileImageActivity() {
        Intent Imageintent = new Intent(RegisterActivity.this,ProfileImageActivity.class);
        Imageintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(Imageintent);
        finish();
    }

    private void CreateNewAccount() {
        String email=useremail.getText().toString();
        String  password=userpassword.getText().toString();
        if(email.isEmpty())
        {
            useremail.setError("Enter an email address");
            useremail.requestFocus();
            return;
        }

        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            useremail.setError("Enter a valid email address");
            useremail.requestFocus();
            return;
        }

        //checking the validity of the password
        if(password.isEmpty())
        {
            userpassword.setError("Enter a password");
            userpassword.requestFocus();
            return;
        }

        if(password.length()<6)
        {
            userpassword.setError("Minimum length of the passowrd should be 6");
            userpassword.requestFocus();
            return;
        }
        else
        {
            loadingbar.setTitle("Creating New Account");
            loadingbar.setMessage("Please Wait...");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                String DeviceToken= FirebaseInstanceId.getInstance().getToken();

                                CurrentuserId= mAuth.getCurrentUser().getUid();
                                RootRef.child("Users").child(CurrentuserId).setValue("");
                                RootRef.child("Users").child(CurrentuserId).child("device_token").setValue(DeviceToken);

                                Log.d("kkkkkey","111111111111");
                                SendUserToProfileImageActivity();
                                Log.d("kkkkkey","222222222222222");
                                Toast.makeText(getApplicationContext(),"Account Created Successfull.. ",Toast.LENGTH_LONG).show();
                                loadingbar.dismiss();
                            } else {
                                String msg = task.getException().toString();
                                Toast.makeText(getApplicationContext(),"Error : "+msg ,Toast.LENGTH_LONG).show();
                                loadingbar.dismiss();
                            }

                        }
                    });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainintent = new Intent(RegisterActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }
    private void InitializeFields() {
        CreateAccountButton=(Button)findViewById(R.id.register_button);
        useremail=(EditText)findViewById(R.id.register_email);
        userpassword=(EditText)findViewById(R.id.register_password);
        AlreadyHaveAccountLink=(TextView)findViewById(R.id.already_have_account_link);
        loadingbar= new ProgressDialog(this);

    }
    private void SendUserToLogInActivity() {

        Intent intent = new Intent(RegisterActivity.this , LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
