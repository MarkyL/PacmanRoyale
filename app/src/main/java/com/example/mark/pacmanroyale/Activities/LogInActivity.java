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
import android.widget.TextView;
import android.widget.Toast;

import com.example.mark.pacmanroyale.R;
import com.example.mark.pacmanroyale.User.Ghost;
import com.example.mark.pacmanroyale.User.Pacman;
import com.example.mark.pacmanroyale.UserPresence;
import com.example.mark.pacmanroyale.Utils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;


public class LogInActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LogInActivity";

    protected EditText emailEditText;
    protected EditText passwordEditText;
    protected Button logInButton;
    protected TextView signUpTextView;

    protected LoginButton facebookSignUp;
    private FirebaseAuth mFirebaseAuth;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_log_in);
        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();
        FacebookSdk.setApplicationId(getResources().getString(R.string.facebook_application_id));

        callbackManager = CallbackManager.Factory.create();

        signUpTextView = findViewById(R.id.signUpText);
        emailEditText = findViewById(R.id.emailField);
        passwordEditText = findViewById(R.id.passwordField);
        logInButton = findViewById(R.id.loginButton);
        logInButton.setOnClickListener(this);
        facebookSignUp = findViewById(R.id.facebook_signup_btn);
        facebookSignUp.setOnClickListener(this);
        facebookSignUp.setReadPermissions("email","public_profile");

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.loginButton): {
                initiateLoginAction();
            } break;
            case (R.id.facebook_signup_btn): {
                facebookSignUp();
            } break;
        }
    }

    private void initiateLoginAction() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        email = email.trim();
        password = password.trim();

        if (email.isEmpty() || password.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
            builder.setMessage(R.string.login_error_message)
                    .setTitle(R.string.login_error_title)
                    .setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            mFirebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
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

    private void facebookSignUp() {
        Log.d(TAG, "facebookSignUp: im here");
        facebookSignUp.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(LogInActivity.this, "Logged in Successfully!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Login success, "+loginResult.getAccessToken().getUserId()+
                        "\n" + loginResult.getAccessToken().getToken());

                handleFacebookAccessToken(loginResult.getAccessToken());


            }

            @Override
            public void onCancel() {
                Toast.makeText(LogInActivity.this, "Login cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LogInActivity.this, "Error facebook Signup", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        Log.d(TAG, "handleFacebookAccessToken:" + accessToken);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            //FirebaseUser user = mFirebaseAuth.getCurrentUser();

                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                            Log.d(TAG, "signInWithCredential: " + (isNew ? "new user" : "old user"));
                            if (isNew) {
                                initUserDefaultData();
                            }
                            Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LogInActivity.this, "auth failed", Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void initUserDefaultData() {

        Ghost ghost = new Ghost(1,1,0,0);
        Pacman pacman = new Pacman(1,1,0,0);
        DatabaseReference mDatabase = Utils.getFireBaseDataBase();
        String mUserId = FirebaseAuth.getInstance().getUid();
        DatabaseReference userReference = mDatabase.child(getString(R.string.users_node)).child(mUserId);
        DatabaseReference pacmanReference = userReference.child(getString(R.string.pacman_node));
        DatabaseReference ghostReference = userReference.child(getString(R.string.ghost_node));

        userReference.child(getString(R.string.user_presence)).setValue(UserPresence.ONLINE);
        pacmanReference.setValue(new Pacman(1,0,-1,-1));
        ghostReference.setValue(new Pacman(1,0,-1,-1));

    }



}
