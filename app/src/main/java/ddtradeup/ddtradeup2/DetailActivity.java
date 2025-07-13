// File: DetailActivity.java
package ddtradeup.ddtradeup2;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
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
    private TextView tvTitle, tvDescription, tvPrice, tvMyOfferStatus;
    private Button   btnOffer, btnRate, btnBuyNow;
    private RecyclerView recyclerOffers;

    private ItemModel item;
    private String itemId, sellerId, currentUserId;

    private final ArrayList<OfferModel> offerList = new ArrayList<>();
    private OffersAdapter offersAdapter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth      mAuth = FirebaseAuth.getInstance();

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_detail);

        imgItem         = findViewById(R.id.imgItem);
        tvTitle         = findViewById(R.id.tvTitle);
        tvDescription   = findViewById(R.id.tvDescription);
        tvPrice         = findViewById(R.id.tvPrice);
        tvMyOfferStatus = findViewById(R.id.tvMyOfferStatus);
        btnOffer        = findViewById(R.id.btnOffer);
        btnRate         = findViewById(R.id.btnRate);
        btnBuyNow       = findViewById(R.id.btnBuyNow);
        recyclerOffers  = findViewById(R.id.recyclerOffers);

        currentUserId = mAuth.getCurrentUser().getUid();
        itemId        = getIntent().getStringExtra("itemId");
        if (itemId == null) { finish(); return; }

        offersAdapter = new OffersAdapter(this, offerList, currentUserId);
        recyclerOffers.setLayoutManager(new LinearLayoutManager(this));
        recyclerOffers.setAdapter(offersAdapter);

        btnOffer .setOnClickListener(v -> showOfferDialog());
        btnRate  .setOnClickListener(v -> showRatingDialog());
        btnBuyNow.setOnClickListener(v -> handleBuyNow());

        loadItemDetail();
    }

    /* --------- LOAD ITEM --------- */
    private void loadItemDetail() {
        db.collection("items").document(itemId).get().addOnSuccessListener(doc -> {
            item = doc.toObject(ItemModel.class);
            if (item == null) { finish(); return; }

            item.setId(doc.getId());
            sellerId = item.getUserId();

            tvTitle.setText(item.getTitle());
            tvDescription.setText(item.getDescription());
            tvPrice.setText(item.getPrice());

            String url = (item.getImageUrls()!=null && !item.getImageUrls().isEmpty())
                    ? item.getImageUrls().get(0) : item.getImageUrl();
            if (url!=null && url.startsWith("http://")) url = url.replace("http://","https://");
            Glide.with(this).load(url!=null?url:R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder).into(imgItem);

            if (currentUserId.equals(sellerId)) {      // người bán
                btnOffer.setVisibility(View.GONE);
                btnRate .setVisibility(View.GONE);
                btnBuyNow.setVisibility(View.GONE);
                tvMyOfferStatus.setVisibility(View.GONE);
                loadOffersForSeller();
            } else {                                   // người mua
                btnOffer .setVisibility(Boolean.TRUE.equals(item.isNegotiable())?View.VISIBLE:View.GONE);
                btnBuyNow.setVisibility(item.getStatus()==null
                        || item.getStatus().equalsIgnoreCase("available") ? View.VISIBLE : View.GONE);
                recyclerOffers.setVisibility(View.GONE);
                loadMyOffers();
            }
        });
    }

    /* --------- BUY NOW --------- */
    private void handleBuyNow() {
        HashMap<String,Object> tx = new HashMap<>();
        tx.put("itemId", itemId);
        tx.put("buyerId", currentUserId);
        tx.put("sellerId", sellerId);
        tx.put("status", "completed");
        tx.put("timestamp", FieldValue.serverTimestamp());

        db.collection("transactions").add(tx).addOnSuccessListener(r ->
                db.collection("items").document(itemId).update("status","sold")
                        .addOnSuccessListener(u -> {
                            Toast.makeText(this,"Mua thành công",Toast.LENGTH_SHORT).show();
                            btnBuyNow.setEnabled(false);
                            btnBuyNow.setText("Đã mua");
                        })
        ).addOnFailureListener(e ->
                Toast.makeText(this,"Lỗi mua: "+e.getMessage(),Toast.LENGTH_SHORT).show());
    }

    /* --------- OFFER (BUYER) --------- */
    private void showOfferDialog() {
        EditText edt = new EditText(this);
        edt.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        edt.setHint("Nhập giá đề nghị");
        new AlertDialog.Builder(this)
                .setTitle("Đề nghị giá")
                .setView(edt)
                .setPositiveButton("Gửi",(d,w)->{
                    String v=edt.getText().toString();
                    if(v.isEmpty()){ Toast.makeText(this,"Nhập giá!",Toast.LENGTH_SHORT).show(); return; }
                    sendOffer(Double.parseDouble(v));
                })
                .setNegativeButton("Hủy",null).show();
    }

    private void sendOffer(double price) {
        String offerId = db.collection("offers").document().getId();
        OfferModel o = new OfferModel(offerId,itemId,currentUserId,sellerId,price,"pending",
                System.currentTimeMillis());
        db.collection("offers").document(offerId).set(o)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this,"Đã gửi đề nghị!",Toast.LENGTH_SHORT).show();
                    loadMyOffers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,"Lỗi gửi: "+e.getMessage(),Toast.LENGTH_SHORT).show());
    }

    private void loadMyOffers() {
        db.collection("offers")
                .whereEqualTo("itemId", itemId)
                .whereEqualTo("buyerId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    boolean pending = false;
                    OfferModel last = null;
                    for(DocumentSnapshot d:snap){
                        OfferModel o=d.toObject(OfferModel.class);
                        if(last==null) last=o;
                        if("pending".equals(o.getStatus())) pending=true;
                    }
                    if(last!=null){
                        tvMyOfferStatus.setVisibility(View.VISIBLE);
                        tvMyOfferStatus.setText("Trạng thái offer: "+last.getStatus()+
                                " (Giá: "+last.getPrice()+")");
                        btnRate.setVisibility("accepted".equals(last.getStatus())?View.VISIBLE:View.GONE);
                    }else{
                        tvMyOfferStatus.setVisibility(View.GONE);
                        btnRate.setVisibility(View.GONE);
                    }
                    btnOffer.setEnabled(!pending && item.isNegotiable());
                });
    }

    /* --------- OFFER LIST (SELLER) --------- */
    private void loadOffersForSeller() {
        recyclerOffers.setVisibility(View.VISIBLE);
        db.collection("offers").whereEqualTo("itemId",itemId).get()
                .addOnSuccessListener(snap -> {
                    offerList.clear();
                    for(DocumentSnapshot d:snap){
                        OfferModel o=d.toObject(OfferModel.class);
                        o.setId(d.getId());
                        offerList.add(o);
                    }
                    offersAdapter.notifyDataSetChanged();
                });
    }

    /* --------- RATING --------- */
    private void showRatingDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_rating,null);
        RatingBar rb = v.findViewById(R.id.ratingBar);

        new AlertDialog.Builder(this)
                .setTitle("Đánh giá người bán")
                .setView(v)
                .setPositiveButton("Gửi",(d,w)->{
                    float rate = rb.getRating();
                    if(rate==0){
                        Toast.makeText(this,"Vui lòng chọn sao!",Toast.LENGTH_SHORT).show(); return;
                    }
                    saveRating(rate);
                })
                .setNegativeButton("Hủy",null).show();
    }

    private void saveRating(float rating) {
        HashMap<String,Object> r = new HashMap<>();
        r.put("userId", sellerId);
        r.put("itemId", itemId);
        r.put("rating", rating);
        r.put("timestamp", System.currentTimeMillis());
        db.collection("ratings").add(r).addOnSuccessListener(v -> {
            Toast.makeText(this,"Đã gửi đánh giá "+rating+" sao",Toast.LENGTH_SHORT).show();
            updateUserRating();
        });
    }

    private void updateUserRating() {
        db.collection("ratings").whereEqualTo("userId",sellerId).get()
                .addOnSuccessListener(snap -> {
                    double total=0; int cnt=0;
                    for(DocumentSnapshot d:snap){ total+=d.getDouble("rating"); cnt++; }
                    if(cnt>0){
                        double avg = total/cnt;
                        db.collection("users").document(sellerId).update("rating",avg);
                    }
                });
    }
}
