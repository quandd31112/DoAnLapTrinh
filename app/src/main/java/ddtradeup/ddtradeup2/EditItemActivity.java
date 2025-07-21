package ddtradeup.ddtradeup2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditItemActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int LOCATION_PERMISSION_REQUEST = 2;

    private ImageView itemImageView;
    private EditText titleEditText, descriptionEditText, priceEditText, locationEditText, tagsEditText;
    private ProgressBar progressBar;
    private Button previewButton;
    private String itemId;

    private ArrayList<Uri> imageUris = new ArrayList<>();
    private ArrayList<String> imageUrls = new ArrayList<>();
    private FirebaseFirestore db;
    private FusedLocationProviderClient locationClient;

    private String selectedCategory = "Chung";
    private String selectedCondition = "Mới";
    private String selectedStatus = "Available";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        db = FirebaseFirestore.getInstance();
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        itemImageView = findViewById(R.id.itemImageView);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        priceEditText = findViewById(R.id.priceEditText);
        locationEditText = findViewById(R.id.locationEditText);
        tagsEditText = findViewById(R.id.tagsEditText);
        previewButton = findViewById(R.id.previewButton);
        progressBar = findViewById(R.id.progressBar);

        Button saveButton = findViewById(R.id.saveBtn);
        Button getLocationButton = findViewById(R.id.getLocationButton);
        Spinner statusSpinner = findViewById(R.id.sSpinner);

        itemId = getIntent().getStringExtra("itemId");
        loadItemData();

        getLocationButton.setOnClickListener(v -> getGpsLocation());
        saveButton.setOnClickListener(v -> {
            if (imageUris.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 ảnh!", Toast.LENGTH_SHORT).show();
            } else {
                uploadImagesToCloudinary();
            }
        });

        previewButton.setOnClickListener(v -> {
            if (imageUris.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ảnh!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, PreviewActivity.class);
            intent.putExtra("title", titleEditText.getText().toString());
            intent.putExtra("description", descriptionEditText.getText().toString());
            intent.putExtra("price", priceEditText.getText().toString());
            intent.putExtra("location", locationEditText.getText().toString());
            intent.putExtra("category", selectedCategory);
            intent.putExtra("condition", selectedCondition);
            intent.putExtra("imageUri", imageUris.get(0).toString());
            intent.putExtra("tags", tagsEditText.getText().toString());
            startActivity(intent);
        });

        itemImageView.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
        });

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Available", "Sold", "Paused"});
        statusSpinner.setAdapter(statusAdapter);
        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadItemData() {
        db.collection("items").document(itemId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        titleEditText.setText(documentSnapshot.getString("title"));
                        descriptionEditText.setText(documentSnapshot.getString("description"));

                        // FIX CRASH: handle price safely
                        Object priceObj = documentSnapshot.get("price");
                        double price = 0;
                        if (priceObj instanceof Number) {
                            price = ((Number) priceObj).doubleValue();
                        } else if (priceObj instanceof String) {
                            try {
                                price = Double.parseDouble((String) priceObj);
                            } catch (NumberFormatException e) {
                                price = 0;
                            }
                        }
                        priceEditText.setText(String.valueOf(price));

                        locationEditText.setText(documentSnapshot.getString("location"));
                        selectedCategory = documentSnapshot.getString("category");
                        selectedCondition = documentSnapshot.getString("condition");
                        selectedStatus = documentSnapshot.getString("status");

                        Object tagsObj = documentSnapshot.get("tags");
                        if (tagsObj instanceof ArrayList) {
                            tagsEditText.setText(TextUtils.join(",", (ArrayList<?>) tagsObj));
                        }

                        imageUrls = (ArrayList<String>) documentSnapshot.get("imageUrls");
                        if (imageUrls != null && !imageUrls.isEmpty()) {
                            Glide.with(this).load(imageUrls.get(0)).into(itemImageView);
                        }
                    }
                });
    }

    private void getGpsLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    locationEditText.setText(location.getLatitude() + "," + location.getLongitude());
                } else {
                    Toast.makeText(this, "Không lấy được vị trí!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }
    }

    private void uploadImagesToCloudinary() {
        progressBar.setVisibility(View.VISIBLE);
        imageUrls.clear();
        int[] uploadCount = {0};
        for (Uri uri : imageUris) {
            String publicId = "ddtradeup_items/" + UUID.randomUUID();
            MediaManager.get().upload(uri)
                    .option("public_id", publicId)
                    .callback(new UploadCallback() {
                        @Override public void onStart(String requestId) {}
                        @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                        @Override public void onSuccess(String requestId, Map resultData) {
                            imageUrls.add((String) resultData.get("secure_url"));
                            uploadCount[0]++;
                            if (uploadCount[0] == imageUris.size()) {
                                saveToFirestore();
                            }
                        }

                        @Override public void onError(String requestId, ErrorInfo error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(EditItemActivity.this, "Lỗi upload ảnh: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        }

                        @Override public void onReschedule(String requestId, ErrorInfo error) {}
                    }).dispatch();
        }
    }

    private void saveToFirestore() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String priceStr = priceEditText.getText().toString().trim();
        String tags = tagsEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        Map<String, Object> item = new HashMap<>();
        item.put("title", title);
        item.put("description", description);
        item.put("price", price);
        item.put("imageUrls", imageUrls);
        item.put("category", selectedCategory);
        item.put("condition", selectedCondition);
        item.put("location", locationEditText.getText().toString());
        item.put("tags", tags.isEmpty() ? new ArrayList<String>() : Arrays.asList(tags.split(",")));
        item.put("status", selectedStatus);

        db.collection("items").document(itemId)
                .update(item)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật bài đăng thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> progressBar.setVisibility(View.GONE));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            imageUris.clear();
            imageUris.add(imageUri);
            Glide.with(this).load(imageUri).into(itemImageView);
        }
    }
}
