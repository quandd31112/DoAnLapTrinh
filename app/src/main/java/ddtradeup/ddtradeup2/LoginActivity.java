package ddtradeup.ddtradeup2;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private EditText emailLogin, passwordLogin;
    private Button loginButton;
    private Button googleSignInButton;
    private TextView toRegisterText, forgotPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailLogin = findViewById(R.id.emailEditText);
        passwordLogin = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        toRegisterText = findViewById(R.id.registerButton);
        forgotPassword = findViewById(R.id.forgotPasswordButton);

        mAuth = FirebaseAuth.getInstance();
        loginButton.setEnabled(false); // Mặc định disabled

        // Kiểm tra input hợp lệ
        TextWatcher watcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                checkInputValidity();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        emailLogin.addTextChangedListener(watcher);
        passwordLogin.addTextChangedListener(watcher);

        loginButton.setOnClickListener(v -> {
            String email = emailLogin.getText().toString().trim();
            String pass = passwordLogin.getText().toString().trim();

            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                mAuth.signOut();
                                Toast.makeText(this, "Vui lòng xác minh email trước khi đăng nhập.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        toRegisterText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        // GOOGLE SIGN-IN
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // lấy từ google-services.json
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    private void checkInputValidity() {
        String email = emailLogin.getText().toString().trim();
        String password = passwordLogin.getText().toString().trim();

        boolean isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean isValidPassword = password.length() >= 6;

        loginButton.setEnabled(isValidEmail && isValidPassword);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account == null) {
                    Toast.makeText(this, "Google Sign-In thất bại: account null", Toast.LENGTH_LONG).show();
                    return;
                }

                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(authTask -> {
                            if (authTask.isSuccessful()) {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });

            } catch (ApiException e) {
                int statusCode = e.getStatusCode();
                Toast.makeText(this, "Lỗi Google Sign-In, code: " + statusCode, Toast.LENGTH_LONG).show();
                e.printStackTrace(); // in chi tiết logcat để debug
            }
        }
    }
}
