package ddtradeup.ddtradeup2;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextWatcher watcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String email = emailEditText.getText().toString().trim();
                String pass = passwordEditText.getText().toString().trim();
                registerButton.setEnabled(Patterns.EMAIL_ADDRESS.matcher(email).matches() && pass.length() >= 6);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        emailEditText.addTextChangedListener(watcher);
        passwordEditText.addTextChangedListener(watcher);

        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String pass = passwordEditText.getText().toString().trim();

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification();
                                Toast.makeText(this, "Đăng ký thành công! Vui lòng xác minh email.", Toast.LENGTH_LONG).show();

                                // Lưu hồ sơ vào Firestore
                                Map<String, Object> userProfile = new HashMap<>();
                                userProfile.put("displayName", "Người dùng mới");
                                userProfile.put("email", user.getEmail());
                                userProfile.put("bio", "");
                                userProfile.put("contact", "");
                                userProfile.put("profileImageUrl", "");
                                userProfile.put("rating", 0);
                                userProfile.put("transactions", 0);

                                db.collection("users").document(user.getUid())
                                        .set(userProfile)
                                        .addOnSuccessListener(aVoid -> Log.d("Register", "Hồ sơ đã lưu"))
                                        .addOnFailureListener(e -> Log.w("Register", "Lỗi lưu hồ sơ", e));
                            }
                        } else {
                            Toast.makeText(this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
