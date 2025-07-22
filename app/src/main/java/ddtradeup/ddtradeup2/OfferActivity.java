package ddtradeup.ddtradeup2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;

public class OfferActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private OffersAdapter adapter;
    private ArrayList<OfferModel> offerList = new ArrayList<>();
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer);

        recyclerView = findViewById(R.id.offersRecyclerView);
        progressBar = findViewById(R.id.offersProgressBar);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new OffersAdapter(this, offerList, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadOffers();
    }

    private void loadOffers() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("offer")
                .whereEqualTo("sellerId", currentUserId)
                .whereEqualTo("status", "pending") // Chỉ load offer đang chờ xác nhận
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    offerList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        OfferModel offer = doc.toObject(OfferModel.class);
                        if (offer != null) {
                            offer.setId(doc.getId());
                            offerList.add(offer);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_ERROR", "Lỗi khi tải đề nghị: ", e);
                    Toast.makeText(this, "Lỗi khi tải đề nghị", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });

    }
}
