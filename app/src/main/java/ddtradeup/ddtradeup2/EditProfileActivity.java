package ddtradeup.ddtradeup2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editName;
    private EditText editEmail;
    private EditText editPhone;
    private ImageView imageAvatar;
    private Button buttonSave;
    private Button deleteAccountButton;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private Uri imageUri;
    private String avatarUrl;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        imageAvatar = findViewById(R.id.imageAvatar);
        buttonSave = findViewById(R.id.btnSaveProfile);
        deleteAccountButton = findViewById(R.id.btnDelete);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadUserData();

        imageAvatar.setOnClickListener(v -> openImagePicker());

        buttonSave.setOnClickListener(v -> saveProfile());

        deleteAccountButton.setOnClickListener(v -> {
            String uid = auth.getCurrentUser().getUid();

            auth.getCurrentUser().delete()
                    .addOnSuccessListener(aVoid -> {
                        firestore.collection("users").document(uid).delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(this, "Tài khoản đã bị xóa!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Lỗi xóa dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi xóa tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

    }

    private void loadUserData() {
        String uid = auth.getCurrentUser().getUid();
        DocumentReference docRef = firestore.collection("users").document(uid);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                editName.setText(documentSnapshot.getString("name"));
                editEmail.setText(documentSnapshot.getString("email"));
                editPhone.setText(documentSnapshot.getString("phone"));
                avatarUrl = documentSnapshot.getString("avatar");

                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(this).load(avatarUrl).into(imageAvatar);
                }
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageAvatar.setImageURI(imageUri);
            avatarUrl = imageUri.toString();
        }
    }

    private void saveProfile() {
        String name = editName.getText().toString();
        String email = editEmail.getText().toString();
        String phone = editPhone.getText().toString();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        DocumentReference docRef = firestore.collection("users").document(uid);

        Map<String, Object> updates = new HashMap();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("phone", phone);
        updates.put("avatar", avatarUrl);

        docRef.update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}