package com.example.sairamkrishna.instagblogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class SignInActivity extends AppCompatActivity {
    private EditText SignInEmail,SignInPass;
    private Button SignInBtn;
    private TextView sign_up_text;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth=FirebaseAuth.getInstance();
        SignInEmail=findViewById(R.id.sign_in_email);
        SignInPass=findViewById(R.id.sign_in_pass);
        SignInBtn=findViewById(R.id.sign_in_button);
        sign_up_text=findViewById(R.id.sign_up_text_id);
        sign_up_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this,SignUpActivity.class));
            }
        });
        SignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=SignInEmail.getText().toString();
                String pass=SignInPass.getText().toString();
                if(!email.isEmpty() && !pass.isEmpty())
                {
                    mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(SignInActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignInActivity.this,MainActivity.class));
                                finish();
                            }
                            else{
                                Toast.makeText(SignInActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(SignInActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}