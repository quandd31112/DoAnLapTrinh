package ddtradeup.ddtradeup2;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;

public class DetailActivity extends AppCompatActivity {

    private ImageView imgItem;
    private TextView tvTitle, tvDescription, tvPrice, tvMyOfferStatus, tvStatus;
    private Button btnOffer, btnRate, btnBuyNow, btnEditItem;
    private RecyclerView recyclerOffers;

    private ItemModel item;
    private String itemId, sellerId, currentUserId;

    private final ArrayList<OfferModel> offerList = new ArrayList<>();
    private OffersAdapter offersAdapter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private ActivityResultLauncher<Intent> payLauncher;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_detail);

        imgItem         = findViewById(R.id.imgItem);
        tvTitle         = findViewById(R.id.tvTitle);
        tvDescription   = findViewById(R.id.tvDescription);
        tvPrice         = findViewById(R.id.tvPrice);
        tvStatus        = findViewById(R.id.tvStatus);
        tvMyOfferStatus = findViewById(R.id.tvMyOfferStatus);
        btnOffer        = findViewById(R.id.btnOffer);
        btnRate         = findViewById(R.id.btnRate);
        btnBuyNow       = findViewById(R.id.btnBuyNow);
        btnEditItem     = findViewById(R.id.btnEditItem);
        recyclerOffers  = findViewById(R.id.recyclerOffers);

        currentUserId = mAuth.getCurrentUser().getUid();
        itemId = getIntent().getStringExtra("itemId");
        if (itemId == null) { finish(); return; }

        offersAdapter = new OffersAdapter(this, offerList, currentUserId);
        recyclerOffers.setLayoutManager(new LinearLayoutManager(this));
        recyclerOffers.setAdapter(offersAdapter);

        btnOffer.setOnClickListener(v -> showOfferDialog());
        btnRate.setOnClickListener(v -> showRatingDialog());

        payLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                r -> {
                    if (r.getResultCode() == RESULT_OK) {
                        if (r.getData() != null && r.getData().getBooleanExtra("paid", false))
                            commitBuyNow();
                    }
                });

        btnBuyNow.setOnClickListener(v -> {
            if (item != null) {
                Intent i = new Intent(this, PaymentActivity.class);
                i.putExtra("itemId", item.getId());
                i.putExtra("sellerId", item.getUserId());
                i.putExtra("price", item.getPrice());
                payLauncher.launch(i);
            }
        });

        loadItemDetail();
    }

    private void loadItemDetail() {
        db.collection("items").document(itemId).get().addOnSuccessListener(doc -> {
            item = doc.toObject(ItemModel.class);
            if (item == null) { finish(); return; }

            item.setId(doc.getId());
            sellerId = item.getUserId();

            Log.d("DEBUG", "itemId=" + item.getId() +
                    ", currentUser=" + currentUserId +
                    ", sellerId=" + sellerId +
                    ", isNegotiable=" + item.getIsNegotiable());

            tvTitle.setText(item.getTitle());
            tvDescription.setText(item.getDescription());
            tvPrice.setText(String.format("\u20ab%,.0f", Double.parseDouble(item.getPrice())));

            if ("sold".equalsIgnoreCase(item.getStatus()))
                tvStatus.setVisibility(View.VISIBLE);
            else
                tvStatus.setVisibility(View.GONE);

            String url = (item.getImageUrls() != null && !item.getImageUrls().isEmpty())
                    ? item.getImageUrls().get(0) : item.getImageUrl();
            if (url != null && url.startsWith("http://")) url = url.replace("http://", "https://");
            Glide.with(this).load(url != null ? url : R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder).into(imgItem);

            if (currentUserId.equals(sellerId)) {
                btnOffer.setVisibility(View.GONE);
                btnRate.setVisibility(View.GONE);
                btnBuyNow.setVisibility(View.GONE);
                tvMyOfferStatus.setVisibility(View.GONE);
                btnEditItem.setVisibility(View.VISIBLE);
                btnEditItem.setOnClickListener(v -> {
                    Intent intent = new Intent(this, EditItemActivity.class);
                    intent.putExtra("itemId", item.getId());
                    startActivity(intent);
                });
                loadOffersForSeller();
            } else {
                boolean isNegotiable = Boolean.TRUE.equals(item.getIsNegotiable());

                Log.d("DEBUG", "Negotiable flag: " + isNegotiable);
                btnOffer.setVisibility(isNegotiable ? View.VISIBLE : View.GONE);
                btnOffer.setEnabled(isNegotiable);

                btnBuyNow.setVisibility(
                        item.getStatus() == null || item.getStatus().equalsIgnoreCase("available")
                                ? View.VISIBLE : View.GONE
                );
                recyclerOffers.setVisibility(View.GONE);
                loadMyOffers();
            }
        });
    }

    private void commitBuyNow() {
        HashMap<String, Object> tx = new HashMap<>();
        tx.put("itemId", itemId);
        tx.put("buyerId", currentUserId);
        tx.put("sellerId", sellerId);
        tx.put("status", "completed");
        tx.put("timestamp", FieldValue.serverTimestamp());
        tx.put("price", item.getPrice());

        db.collection("transactions").add(tx).addOnSuccessListener(r ->
                db.collection("items").document(itemId).update("status", "sold")
                        .addOnSuccessListener(u -> {
                            Toast.makeText(this, "Purchase successful", Toast.LENGTH_SHORT).show();
                            btnBuyNow.setEnabled(false);
                            btnBuyNow.setText("Purchased");
                            btnBuyNow.setVisibility(View.GONE);
                        })
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showOfferDialog() {
        EditText edt = new EditText(this);
        edt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        edt.setHint("Enter your offer");
        new AlertDialog.Builder(this)
                .setTitle("Submit Offer")
                .setView(edt)
                .setPositiveButton("Send", (d, w) -> {
                    String v = edt.getText().toString();
                    if (v.isEmpty()) {
                        Toast.makeText(this, "Enter amount!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendOffer(Double.parseDouble(v));
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void sendOffer(double price) {
        String offerId = db.collection("offer").document().getId();
        OfferModel o = new OfferModel(offerId, itemId, currentUserId, sellerId, price, "pending", System.currentTimeMillis());
        db.collection("offer").document(offerId).set(o)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Offer sent!", Toast.LENGTH_SHORT).show();
                    loadMyOffers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadMyOffers() {
        db.collection("offer")
                .whereEqualTo("itemId", itemId)
                .whereEqualTo("buyerId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    boolean pending = false;
                    OfferModel last = null;
                    for (DocumentSnapshot d : snap) {
                        OfferModel o = d.toObject(OfferModel.class);
                        if (last == null) last = o;
                        if ("pending".equals(o.getStatus())) pending = true;
                    }
                    if (last != null) {
                        tvMyOfferStatus.setVisibility(View.VISIBLE);
                        tvMyOfferStatus.setText("Offer: " + last.getStatus() +
                                " (₫" + last.getPrice() + ")");
                        btnRate.setVisibility("accepted".equals(last.getStatus()) ? View.VISIBLE : View.GONE);
                    } else {
                        tvMyOfferStatus.setVisibility(View.GONE);
                        btnRate.setVisibility(View.GONE);
                    }
                    btnOffer.setEnabled(!pending && Boolean.TRUE.equals(item.getIsNegotiable()));
                });
    }

    private void loadOffersForSeller() {
        recyclerOffers.setVisibility(View.VISIBLE);
        db.collection("offer").whereEqualTo("itemId", itemId).get()
                .addOnSuccessListener(snap -> {
                    offerList.clear();
                    for (DocumentSnapshot d : snap) {
                        OfferModel o = d.toObject(OfferModel.class);
                        o.setId(d.getId());
                        offerList.add(o);
                    }
                    offersAdapter.notifyDataSetChanged();
                });
    }

    private void showRatingDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_rating, null);
        RatingBar rb = v.findViewById(R.id.ratingBar);

        new AlertDialog.Builder(this)
                .setTitle("Rate Seller")
                .setView(v)
                .setPositiveButton("Submit", (d, w) -> {
                    float rate = rb.getRating();
                    if (rate == 0) {
                        Toast.makeText(this, "Please select stars!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveRating(rate);
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void saveRating(float rating) {
        HashMap<String, Object> r = new HashMap<>();
        r.put("userId", sellerId);
        r.put("itemId", itemId);
        r.put("rating", rating);
        r.put("timestamp", System.currentTimeMillis());
        db.collection("ratings").add(r).addOnSuccessListener(v -> {
            Toast.makeText(this, "Rated " + rating + " stars", Toast.LENGTH_SHORT).show();
            updateUserRating();
        });
    }

    private void updateUserRating() {
        db.collection("ratings").whereEqualTo("userId", sellerId).get()
                .addOnSuccessListener(snap -> {
                    double total = 0;
                    int cnt = 0;
                    for (DocumentSnapshot d : snap) {
                        total += d.getDouble("rating");
                        cnt++;
                    }
                    if (cnt > 0) {
                        double avg = total / cnt;
                        db.collection("users").document(sellerId).update("rating", avg);
                    }
                });
    }
}
