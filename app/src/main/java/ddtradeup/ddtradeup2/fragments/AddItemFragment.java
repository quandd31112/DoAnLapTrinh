package ddtradeup.ddtradeup2.fragments;

import ddtradeup.ddtradeup2.R;
import ddtradeup.ddtradeup2.PreviewActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ddtradeup.ddtradeup2.CloudinaryManager;

public class AddItemFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView itemImageView;
    private EditText titleEditText, descriptionEditText, priceEditText, locationEditText;
    private ProgressBar progressBar;
    private Button previewButton;

    private Uri imageUri;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String selectedCategory = "Chung"; // mặc định
    private String selectedCondition = "Mới"; // mặc định

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_item, container, false);

        CloudinaryManager.init(requireContext());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        itemImageView = view.findViewById(R.id.itemImageView);
        titleEditText = view.findViewById(R.id.titleEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        priceEditText = view.findViewById(R.id.priceEditText);
        locationEditText = view.findViewById(R.id.locationEditText);
        previewButton = view.findViewById(R.id.previewButton);

        Button chooseImageButton = view.findViewById(R.id.chooseImageButton);
        Button uploadButton = view.findViewById(R.id.uploadButton);

        progressBar = new ProgressBar(requireContext());
        progressBar.setVisibility(View.GONE);

        chooseImageButton.setOnClickListener(v -> openImagePicker());

        uploadButton.setOnClickListener(v -> {
            if (imageUri == null) {
                Toast.makeText(getContext(), "Vui lòng chọn ảnh!", Toast.LENGTH_SHORT).show();
            } else {
                uploadImageToCloudinary();
            }
        });

        // ✅ Preview bài đăng
        previewButton.setOnClickListener(v -> {
            if (imageUri == null) {
                Toast.makeText(getContext(), "Vui lòng chọn ảnh!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(getActivity(), PreviewActivity.class);
            intent.putExtra("title", titleEditText.getText().toString());
            intent.putExtra("description", descriptionEditText.getText().toString());
            intent.putExtra("price", priceEditText.getText().toString());
            intent.putExtra("location", locationEditText.getText().toString());
            intent.putExtra("category", selectedCategory);
            intent.putExtra("condition", selectedCondition);
            intent.putExtra("imageUri", imageUri.toString());
            startActivity(intent);
        });

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(itemImageView);
        }
    }

    private void uploadImageToCloudinary() {
        progressBar.setVisibility(View.VISIBLE);
        String publicId = "ddtradeup_items/" + UUID.randomUUID();

        MediaManager.get().upload(imageUri)
                .option("public_id", publicId)
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        saveToFirestore(imageUrl);
                    }
                    @Override public void onError(String requestId, ErrorInfo error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Lỗi upload ảnh: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void saveToFirestore(String imageUrl) {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String priceStr = priceEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Giá không hợp lệ!", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> item = new HashMap<>();
        item.put("title", title);
        item.put("description", description);
        item.put("price", price);
        item.put("imageUrl", imageUrl);
        item.put("userId", userId);
        item.put("timestamp", System.currentTimeMillis());

        db.collection("items")
                .add(item)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                    resetForm();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                })
                .addOnCompleteListener(task -> progressBar.setVisibility(View.GONE));
    }

    private void resetForm() {
        titleEditText.setText("");
        descriptionEditText.setText("");
        priceEditText.setText("");
        locationEditText.setText("");
        itemImageView.setImageResource(android.R.color.darker_gray);
        imageUri = null;
    }
}
