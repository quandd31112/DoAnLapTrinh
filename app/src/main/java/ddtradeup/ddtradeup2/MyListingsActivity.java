package ddtradeup.ddtradeup2;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;

public class MyListingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ListingsAdapter adapter;
    private ArrayList<ItemModel> itemList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);

        recyclerView = findViewById(R.id.recyclerViewMyListings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemList = new ArrayList<>();
        adapter = new ListingsAdapter(this, itemList, false);

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadMyItems();
    }

    private void loadMyItems() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("items")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ItemModel item = doc.toObject(ItemModel.class);
                        item.setId(doc.getId());
                        itemList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
