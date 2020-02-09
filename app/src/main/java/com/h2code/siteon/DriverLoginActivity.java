package com.h2code.siteon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DriverLoginActivity extends AppCompatActivity {
    private EditText mPhoneNumber, mCode , mEmail, mPassword;
    private Button mSend , mLogin, mRegistration;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    String mVerificationId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        FirebaseApp.initializeApp(this);

        userIsLoggedIn();

        mPhoneNumber = findViewById(R.id.phonenumber);
        mCode = findViewById(R.id.code);

        mSend = findViewById(R.id.send);


        mEmail =  findViewById(R.id.email);
        mPassword =  findViewById(R.id.password);

        mLogin =  findViewById(R.id.login);
        mRegistration =  findViewById(R.id.registration);

        // bedayet el no cick listeners













        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mVerificationId != null)
                    verifyPhoneNumberWithCode();
                else
                    startPhoneNumberVerification();
            }
        });


        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEmail.getText().toString().isEmpty() ||mPassword.getText().toString().isEmpty() ){
                    Toast.makeText(DriverLoginActivity.this, "please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(!task.isSuccessful()){
                                    Toast.makeText(DriverLoginActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                                }else{
                                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                    if (user != null) {
                                        final DatabaseReference mUserDB = FirebaseDatabase.getInstance()
                                                .getReference().child("user").child(user.getUid());
                                        mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (!dataSnapshot.exists()) {
                                                    Map<String, Object> userMap = new HashMap<>();
                                                    userMap.put("email", user.getEmail());
                                                    userMap.put("passwords", password);
                                                    mUserDB.updateChildren(userMap);
                                                }
                                                userIsLoggedIn();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }
                        });
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mEmail.getText().toString().isEmpty() ||mPassword.getText().toString().isEmpty() ){
                    Toast.makeText(DriverLoginActivity.this, "please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }



                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(DriverLoginActivity.this, "sign in error", Toast.LENGTH_SHORT).show();
                        }
                        else{

                            userIsLoggedIn();
                        }
                    }
                });

            }
        });








// nehayet ta3refat el zarayer we wazayefha



        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
            }


            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);

                mVerificationId = verificationId;
                mSend.setText("Verify Code");
            }
        };

    }

    // nehayet onCreate



    private void verifyPhoneNumberWithCode(){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, mCode.getText().toString());
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            // final FirebaseUser user = task.getResult().getUser();



                            if (user != null) {
                                final DatabaseReference mUserDB = FirebaseDatabase.getInstance()
                                        .getReference().child("user").child(user.getUid());
                                mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.exists()) {
                                            Map<String, Object> userMap = new HashMap<>();
                                            userMap.put("phone", user.getPhoneNumber());
                                            userMap.put("name", user.getPhoneNumber());
                                            mUserDB.updateChildren(userMap);
                                        }
                                        userIsLoggedIn();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                        }

                    }

                });
    }


    private void userIsLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            startActivity(new Intent(getApplicationContext(), DriverMapActivity.class));
            finish();
            return;
        }
    }

    private void startPhoneNumberVerification() {

        if (mPhoneNumber.getText().toString().isEmpty())
        {
            Toast.makeText(this, "please enter phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mPhoneNumber.getText().toString(),
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks);
    }
}
