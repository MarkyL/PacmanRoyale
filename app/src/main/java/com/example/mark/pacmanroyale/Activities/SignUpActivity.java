package com.example.mark.pacmanroyale.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.mark.pacmanroyale.Enums.UserPresence;
import com.example.mark.pacmanroyale.R;
import com.example.mark.pacmanroyale.User.Ghost;
import com.example.mark.pacmanroyale.User.Pacman;
import com.example.mark.pacmanroyale.Utilities.FireBaseUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignUpActivity";

    protected EditText passwordEditText;
    protected EditText emailEditText;
    protected Button signUpButton;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();

        passwordEditText = findViewById(R.id.passwordField);
        emailEditText = findViewById(R.id.emailField);
        signUpButton = findViewById(R.id.signupButton);
        signUpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.signupButton): {
                beginSignUp();
            } break;
        }
    }

    private void beginSignUp() {
        String password = passwordEditText.getText().toString();
        String email = emailEditText.getText().toString();

        password = password.trim();
        email = email.trim();

        if (password.isEmpty() || email.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
            builder.setMessage(R.string.signup_error_message)
                    .setTitle(R.string.signup_error_title)
                    .setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                                Log.d(TAG, "signInWithCredential: " + (isNew ? "new user" : "old user"));
                                if (isNew) {
                                    initUserDefaultData();
                                }
                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Log.d(TAG, "onComplete: error = " + task.getException());
                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                builder.setMessage(task.getException().getMessage())
                                        .setTitle(R.string.login_error_title)
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
        }
    }

    private void initUserDefaultData() {

        Ghost ghost = new Ghost(1, 1, 0, 0,0,0);
        Pacman pacman = new Pacman(1, 1, 0, 0,0,0);
        DatabaseReference mDatabase = FireBaseUtils.getFireBaseDataBase();
        String mUserId = FirebaseAuth.getInstance().getUid();
        DatabaseReference userReference = mDatabase.child(getString(R.string.users_node)).child(mUserId);
        DatabaseReference pacmanReference = userReference.child(getString(R.string.pacman_node));
        DatabaseReference ghostReference = userReference.child(getString(R.string.ghost_node));

        userReference.child(getString(R.string.user_presence)).setValue(UserPresence.ONLINE);
        userReference.child(getString(R.string.music)).setValue(true);
        userReference.child(getString(R.string.joystick)).setValue(true);
        userReference.child(getString(R.string.SFX)).setValue(true);
        pacmanReference.setValue(pacman);
        ghostReference.setValue(ghost);
    }
}




