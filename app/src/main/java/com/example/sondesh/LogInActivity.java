package com.example.sondesh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

import java.time.chrono.ThaiBuddhistEra;

public class LogInActivity extends AppCompatActivity {

    private Button loginbutton,phoneloginbutton;
    private EditText useremail,userpassword;
    private TextView NeedNewAccountLink,ForgetPasswordLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;
    private DatabaseReference RootRef,UserRef;
    private String CurrentuserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        InitializeFields();
        mAuth = FirebaseAuth.getInstance();

        RootRef= FirebaseDatabase.getInstance().getReference();
        UserRef=FirebaseDatabase.getInstance().getReference().child("Users");

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });

        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });
//        phoneloginbutton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent phoneLoginintent= new Intent(LogInActivity.this,PhoneLogInActivity.class);
//                startActivity(phoneLoginintent);
//            }
//        });
    }

    private void AllowUserToLogin() {
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

            loadingbar.setTitle("Opening Your Account");
            loadingbar.setMessage("Please Wait...");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {


                                 CurrentuserId= mAuth.getCurrentUser().getUid();

                               String DeviceToken= FirebaseInstanceId.getInstance().getToken();

                               UserRef.child(CurrentuserId).child("device_token").setValue(DeviceToken)
                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task) {

                                               if(task.isSuccessful())
                                               {
                                                   RootRef.child("Users").child(CurrentuserId);
                                                   SendUserToMainActivity();
                                                   Toast.makeText(getApplicationContext(),"Logged in Successfull",Toast.LENGTH_LONG).show();
                                                   loadingbar.dismiss();
                                               }
                                           }
                                       });


                            }
                            else {
                                String msg = task.getException().toString();
                                Toast.makeText(getApplicationContext(),"Error : "+msg ,Toast.LENGTH_LONG).show();
                                loadingbar.dismiss();
                            }


                        }
                    });
        }
    }

    private void InitializeFields() {
        loginbutton=(Button)findViewById(R.id.login_button);
//        phoneloginbutton=(Button)findViewById(R.id.phone_login_button);
        useremail=(EditText)findViewById(R.id.login_email);
        userpassword=(EditText)findViewById(R.id.login_password);
        NeedNewAccountLink=(TextView)findViewById(R.id.need_new_account_link);
        ForgetPasswordLink=(TextView)findViewById(R.id.forget_password_link);
        loadingbar= new ProgressDialog(this);
    }



    private void SendUserToMainActivity() {
        Intent mainintent = new Intent(LogInActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }
    private void SendUserToRegisterActivity() {

        Intent registerintent = new Intent(LogInActivity.this,RegisterActivity.class);
        startActivity(registerintent);
    }
}
