package ddtradeup.ddtradeup2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class PreviewActivity extends AppCompatActivity {

    ImageView imagePreview;
    TextView titleText, descText, priceText, categoryText, conditionText, locationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);  // ✅ Đảm bảo đúng layout

        imagePreview = findViewById(R.id.imagePreview);
        titleText = findViewById(R.id.textTitle);
        descText = findViewById(R.id.textDescription);
        priceText = findViewById(R.id.textPrice);
        categoryText = findViewById(R.id.textCategory);
        conditionText = findViewById(R.id.textCondition);
        locationText = findViewById(R.id.textLocation);

        Intent intent = getIntent();
        titleText.setText(intent.getStringExtra("title"));
        descText.setText(intent.getStringExtra("description"));
        priceText.setText("Giá: " + intent.getStringExtra("price") + " VNĐ");
        categoryText.setText("Danh mục: " + intent.getStringExtra("category"));
        conditionText.setText("Tình trạng: " + intent.getStringExtra("condition"));
        locationText.setText("Vị trí: " + intent.getStringExtra("location"));

        Uri imageUri = Uri.parse(intent.getStringExtra("imageUri"));
        Glide.with(this).load(imageUri).into(imagePreview);  // ✅ Cloudinary URI cũng ok
    }
}
