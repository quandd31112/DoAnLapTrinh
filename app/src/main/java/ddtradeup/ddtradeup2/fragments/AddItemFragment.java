package ddtradeup.ddtradeup2.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.cloudinary.android.MediaManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ddtradeup.ddtradeup2.R;

public class AddItemFragment extends Fragment {
    private EditText itemTitleEditText, itemDescriptionEditText, itemTagsEditText, itemPriceEditText, latitudeEditText, longitudeEditText;
    private ImageView itemImageView;
    private Button addItemButton, selectImageButton, previewButton, getLocationButton;
    private CheckBox checkboxNegotiable;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Uri imageUri;
    private String imageUrl;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private FusedLocationProviderClient locationClient;
    private Double latitude;
    private Double longitude;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_item, container, false);
        itemTitleEditText = view.findViewById(R.id.itemTitleEditText);
        itemDescriptionEditText = view.findViewById(R.id.itemDescriptionEditText);
        itemTagsEditText = view.findViewById(R.id.itemTagsEditText);
        itemPriceEditText = view.findViewById(R.id.itemPriceEditText);
        itemImageView = view.findViewById(R.id.itemImageView);
        addItemButton = view.findViewById(R.id.addItemButton);
        selectImageButton = view.findViewById(R.id.selectImageButton);
        previewButton = view.findViewById(R.id.previewButton);
        latitudeEditText = view.findViewById(R.id.latitudeEditText);
        longitudeEditText = view.findViewById(R.id.longitudeEditText);
        getLocationButton = view.findViewById(R.id.getLocationButton);
        checkboxNegotiable = view.findViewById(R.id.checkboxNegotiable);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        locationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                imageUri = result.getData().getData();
                itemImageView.setImageURI(imageUri);
            }
        });

        selectImageButton.setOnClickListener(v -> selectImage());
        addItemButton.setOnClickListener(v -> addItem());
        previewButton.setOnClickListener(v -> showPreview());
        getLocationButton.setOnClickListener(v -> fetchLocation());
        return view;
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                latitudeEditText.setText(String.valueOf(latitude));
                longitudeEditText.setText(String.valueOf(longitude));
            } else {
                Toast.makeText(requireContext(), "Không lấy được vị trí. Hãy thử lại.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Lỗi vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void addItem() {
        String title = itemTitleEditText.getText().toString().trim();
        String description = itemDescriptionEditText.getText().toString().trim();
        String tags = itemTagsEditText.getText().toString().trim();
        String price = itemPriceEditText.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || tags.isEmpty() || price.isEmpty() || imageUri == null) {
            Toast.makeText(requireContext(), "Điền đủ thông tin và chọn ảnh nha!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        addItemButton.setEnabled(false);

        if (!latitudeEditText.getText().toString().isEmpty() && !longitudeEditText.getText().toString().isEmpty()) {
            latitude = Double.parseDouble(latitudeEditText.getText().toString());
            longitude = Double.parseDouble(longitudeEditText.getText().toString());
            uploadImage(title, description, tags, price, userId);
        } else {
            fetchLocation();
            uploadImage(title, description, tags, price, userId);
        }
    }

    private void showPreview() {
        String title = itemTitleEditText.getText().toString().trim();
        String description = itemDescriptionEditText.getText().toString().trim();
        String tags = itemTagsEditText.getText().toString().trim();
        String price = itemPriceEditText.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || tags.isEmpty() || price.isEmpty() || imageUri == null) {
            Toast.makeText(requireContext(), "Điền đủ thông tin và chọn ảnh để xem trước nha!", Toast.LENGTH_SHORT).show();
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View previewView = LayoutInflater.from(requireContext()).inflate(R.layout.activity_preview, null);
        TextView previewTitle = previewView.findViewById(R.id.previewTitle);
        TextView previewDescription = previewView.findViewById(R.id.previewDescription);
        TextView previewTags = previewView.findViewById(R.id.previewTags);
        TextView previewPrice = previewView.findViewById(R.id.previewPrice);
        ImageView previewImage = previewView.findViewById(R.id.previewImage);
        previewTitle.setText(title);
        previewDescription.setText(description);
        previewTags.setText(tags);
        previewPrice.setText(price);
        previewImage.setImageURI(imageUri);
        builder.setView(previewView);
        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void uploadImage(String title, String description, String tags, String price, String userId) {
        MediaManager.get().upload(imageUri).unsigned("ddtradeup_preset").callback(new com.cloudinary.android.callback.UploadCallback() {
            @Override
            public void onStart(String requestId) {
                Toast.makeText(requireContext(), "Đang upload ảnh...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {}

            @Override
            public void onSuccess(String requestId, Map resultData) {
                imageUrl = (String) resultData.get("secure_url");
                saveItemToFirestore(title, description, tags, price, userId, imageUrl);
            }

            @Override
            public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                Toast.makeText(requireContext(), "Lỗi upload ảnh: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                addItemButton.setEnabled(true);
            }

            @Override
            public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
        }).dispatch();
    }

    private void saveItemToFirestore(String title, String description, String tags, String price, String userId, String imageUrl) {
        Map<String, Object> item = new HashMap<>();
        item.put("title", title);
        item.put("description", description);
        item.put("tags", Arrays.asList(tags.split(",")));
        item.put("userId", userId);
        item.put("timestamp", System.currentTimeMillis());
        item.put("price", price);
        item.put("imageUrl", imageUrl);
        item.put("status", "Available");
        item.put("isNegotiable", checkboxNegotiable.isChecked());

        if (latitude != null && longitude != null) {
            item.put("latitude", latitude);
            item.put("longitude", longitude);
        }

        db.collection("items")
                .add(item)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(requireContext(), "Đăng sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                    itemTitleEditText.setText("");
                    itemDescriptionEditText.setText("");
                    itemTagsEditText.setText("");
                    itemPriceEditText.setText("");
                    latitudeEditText.setText("");
                    longitudeEditText.setText("");
                    itemImageView.setImageResource(0);
                    checkboxNegotiable.setChecked(false);
                    addItemButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    addItemButton.setEnabled(true);
                });
    }
}
