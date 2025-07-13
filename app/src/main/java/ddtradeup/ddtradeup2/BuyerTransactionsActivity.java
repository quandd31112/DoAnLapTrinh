package ddtradeup.ddtradeup2;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class BuyerTransactionsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<TransactionModel> transactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_transactions);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recyclerViewBuyerTransactions);
        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(transactionList, false); // false = buyer
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Nếu chưa đăng nhập, quay về màn hình đăng nhập
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        loadBuyerTransactions(currentUser.getUid());
    }

    private void loadBuyerTransactions(String userId) {
        db.collection("transactions")
                .whereEqualTo("buyerId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    transactionList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        TransactionModel tx = doc.toObject(TransactionModel.class);
                        tx.setId(doc.getId()); // Gán ID document
                        transactionList.add(tx);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
