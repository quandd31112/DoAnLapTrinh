package ddtradeup.ddtradeup2;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PreviewActivity extends AppCompatActivity {
    private TextView previewTitle, previewDescription, previewTags, previewPrice;
    private ImageView previewImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        previewTitle = findViewById(R.id.previewTitle);
        previewDescription = findViewById(R.id.previewDescription);
        previewTags = findViewById(R.id.previewTags);
        previewPrice = findViewById(R.id.previewPrice);
        previewImage = findViewById(R.id.previewImage);

        // Lấy dữ liệu từ Intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String tags = getIntent().getStringExtra("tags");
        String price = getIntent().getStringExtra("price");
        Uri imageUri = getIntent().getParcelableExtra("imageUri");

        // Đặt dữ liệu vào view
        previewTitle.setText(title);
        previewDescription.setText(description);
        previewTags.setText(tags);
        previewPrice.setText(price);
        if (imageUri != null) {
            previewImage.setImageURI(imageUri);
        }
    }
}