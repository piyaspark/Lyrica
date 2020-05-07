package com.example.lyrica_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText email, password;
    private TextView signup;
    private Button signin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        email = (EditText) findViewById(R.id.emailText2);
        password = (EditText) findViewById(R.id.passwordText);
        signup = (TextView) findViewById(R.id.signupText);
        signin = (Button) findViewById(R.id.loginBtn);

        if(mAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(getApplicationContext(),Menu.class));
        }

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currUsername = email.getText().toString();
                String currPassword = password.getText().toString();
                callSignIn(currUsername,currPassword);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });
    }

    private void callSignIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("Test","Sign in success:" + task.isSuccessful());

                if(!task.isSuccessful()){
                    Log.d("Test","Sign In with username failed:" + task.getException());
                    Toast.makeText(MainActivity.this, "Login failed.",
                            Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(MainActivity.this, Menu.class);
                    finish();
                    startActivity(intent);
                }
            }
        });
    }
}
