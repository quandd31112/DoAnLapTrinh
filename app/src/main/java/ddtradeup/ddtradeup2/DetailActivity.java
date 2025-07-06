package ddtradeup.ddtradeup2;

import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import android.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {
    private ImageView imgItem;
    private TextView tvTitle, tvDescription, tvPrice, tvMyOfferStatus;
    private Button btnOffer;
    private RecyclerView recyclerOffers;

    private ItemModel item;
    private String itemId;
    private String sellerId;
    private String currentUserId;

    private ArrayList<OfferModel> offerList;
    private OffersAdapter offersAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private boolean hasPendingOffer = false;
    private OfferModel myLastOffer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imgItem = findViewById(R.id.imgItem);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvPrice = findViewById(R.id.tvPrice);
        btnOffer = findViewById(R.id.btnOffer);
        recyclerOffers = findViewById(R.id.recyclerOffers);
        tvMyOfferStatus = findViewById(R.id.tvMyOfferStatus); // Thêm TextView này vào XML

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();
        itemId = getIntent().getStringExtra("itemId");

        offerList = new ArrayList<>();
        offersAdapter = new OffersAdapter(this, offerList, currentUserId);
        recyclerOffers.setAdapter(offersAdapter);
        recyclerOffers.setLayoutManager(new LinearLayoutManager(this));

        loadItemDetail();

        btnOffer.setOnClickListener(v -> showOfferDialog());
    }

    private void loadItemDetail() {
        db.collection("items").document(itemId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    item = documentSnapshot.toObject(ItemModel.class);
                    if (item == null) return;
                    item.setId(documentSnapshot.getId());
                    sellerId = item.getUserId();

                    tvTitle.setText(item.getTitle());
                    tvDescription.setText(item.getDescription());
                    tvPrice.setText(String.valueOf(item.getPrice()));

                    Glide.with(this)
                            .load(item.getImageUrl())
                            .placeholder(R.drawable.placeholder)
                            .into(imgItem);

                    // Seller thấy tất cả offer, Buyer thì chỉ gửi offer & xem trạng thái của mình
                    if (currentUserId.equals(sellerId)) {
                        btnOffer.setVisibility(Button.GONE);
                        tvMyOfferStatus.setVisibility(TextView.GONE);
                        loadOffersForSeller();
                    } else {
                        btnOffer.setVisibility(Button.VISIBLE);
                        recyclerOffers.setVisibility(RecyclerView.GONE);
                        loadMyOffers();
                    }
                });
    }

    // Bổ sung hàm này để Buyer chỉ gửi 1 offer pending và hiện trạng thái
    private void loadMyOffers() {
        db.collection("offers")
                .whereEqualTo("itemId", itemId)
                .whereEqualTo("buyerId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    hasPendingOffer = false;
                    myLastOffer = null;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        OfferModel offer = doc.toObject(OfferModel.class);
                        if (myLastOffer == null) myLastOffer = offer; // Lấy offer mới nhất
                        if ("pending".equals(offer.getStatus())) hasPendingOffer = true;
                    }

                    if (myLastOffer != null) {
                        tvMyOfferStatus.setVisibility(TextView.VISIBLE);
                        tvMyOfferStatus.setText("Trạng thái offer: " + myLastOffer.getStatus() +
                                " (Giá: " + myLastOffer.getPrice() + ")");
                    } else {
                        tvMyOfferStatus.setVisibility(TextView.GONE);
                    }

                    // Nếu đã có offer pending thì ẩn nút gửi offer
                    btnOffer.setEnabled(!hasPendingOffer);
                    if (hasPendingOffer) {
                        btnOffer.setText("Đang chờ phản hồi");
                    } else {
                        btnOffer.setText("Đề nghị giá");
                    }
                });
    }

    private void showOfferDialog() {
        EditText edt = new EditText(this);
        edt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        edt.setHint("Nhập giá bạn muốn đề nghị");

        new AlertDialog.Builder(this)
                .setTitle("Đề nghị giá")
                .setView(edt)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    String input = edt.getText().toString();
                    if (input.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập giá!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double price = Double.parseDouble(input);
                    sendOffer(price);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void sendOffer(double price) {
        String offerId = db.collection("offers").document().getId();
        OfferModel offer = new OfferModel(
                offerId,
                itemId,
                currentUserId,
                sellerId,
                price,
                "pending",
                System.currentTimeMillis()
        );
        db.collection("offers").document(offerId)
                .set(offer)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã gửi đề nghị!", Toast.LENGTH_SHORT).show();
                    loadMyOffers(); // Refresh trạng thái sau khi gửi
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gửi đề nghị thất bại!", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadOffersForSeller() {
        recyclerOffers.setVisibility(RecyclerView.VISIBLE);
        db.collection("offers")
                .whereEqualTo("itemId", itemId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    offerList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        OfferModel offer = doc.toObject(OfferModel.class);
                        offer.setId(doc.getId());
                        offerList.add(offer);
                    }
                    offersAdapter.notifyDataSetChanged();
                });
    }
}
