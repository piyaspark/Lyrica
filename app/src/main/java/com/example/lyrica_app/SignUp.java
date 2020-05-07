package com.example.lyrica_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {
    private EditText email, password;
    private ImageButton backBtn;
    private Button signup;
    private Pattern pattern;
    private Matcher matcher;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up2);
        mAuth = FirebaseAuth.getInstance();
        email = (EditText) findViewById(R.id.emailText_signup);
        password = (EditText) findViewById(R.id.passwordText);
        backBtn = (ImageButton) findViewById(R.id.backBtn);
        signup = (Button) findViewById(R.id.signup);
        pattern = Pattern.compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,})");

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getEmail = email.getText().toString();
                String getPassword = password.getText().toString();
                callSignUp(getEmail,getPassword);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });
    }

    private boolean validatePassword(final String password){
        matcher = pattern.matcher(password);
        return matcher.matches();
    }

    private void callSignUp(String email, String password){
        Log.d("callSignUp","email: "+email);
        Log.d("callSignUp","passw: "+password);

        if(validatePassword(password)){
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("Test", "createUserWithEmail:success");
                                userProfile();
                                Toast.makeText(SignUp.this, "Account created.",
                                        Toast.LENGTH_SHORT).show();
                                FirebaseUser user = mAuth.getCurrentUser();
                                Intent intent = new Intent(SignUp.this, MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("Test", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignUp.this, "Register failed. This email is already existed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else {
            Toast.makeText(SignUp.this, "Password need to contains one capital letter, one lowercase letter, one integer, and contains at least 8 letters",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void userProfile(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(email.getText().toString()).build();
            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d("test","User profile updated");
                    }
                }
            });
        }
    }
}
