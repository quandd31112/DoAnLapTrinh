package ddtradeup.ddtradeup2;

import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class PublicProfileActivity extends AppCompatActivity {

    private TextView nameText, emailText, bioText, contactText;
    private CircleImageView profileImage;
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile);

        nameText = findViewById(R.id.displayNameText);
        emailText = findViewById(R.id.emailText);
        bioText = findViewById(R.id.bioText);
        contactText = findViewById(R.id.contactText);
        profileImage = findViewById(R.id.profileImage);
        ratingBar = findViewById(R.id.ratingBar);

        String userId = getIntent().getStringExtra("userId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        nameText.setText(snapshot.getString("displayName"));
                        emailText.setText(snapshot.getString("email"));
                        bioText.setText(snapshot.getString("bio"));
                        contactText.setText(snapshot.getString("contact"));
                        Double rating = snapshot.getDouble("rating");
                        if (rating != null) ratingBar.setRating(rating.floatValue());
                        Glide.with(this).load(snapshot.getString("photoUrl")).into(profileImage);
                    } else {
                        Toast.makeText(this, "Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}