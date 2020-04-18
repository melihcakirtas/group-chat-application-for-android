package com.melih.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStreamReader;

public class SignUpActivity extends AppCompatActivity {
    EditText useremail,userpassword;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        useremail = findViewById(R.id.editText_useremail);
        userpassword = findViewById(R.id.editText_user_password);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if(firebaseUser != null){
            Intent intent = new Intent(SignUpActivity.this,ChatActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void signup(View view){
        firebaseAuth.createUserWithEmailAndPassword(useremail.getText().toString(),userpassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SignUpActivity.this, "User created,succesfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this,ChatActivity.class);
                    startActivity(intent);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void signin(View view){
        firebaseAuth.signInWithEmailAndPassword(useremail.getText().toString(),userpassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(SignUpActivity.this,ChatActivity.class);
                    startActivity(intent);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
