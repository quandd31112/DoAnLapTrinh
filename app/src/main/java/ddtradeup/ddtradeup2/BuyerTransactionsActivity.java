package ddtradeup.ddtradeup2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
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
        adapter = new TransactionAdapter(transactionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadBuyerTransactions();
    }

    private void loadBuyerTransactions() {
        String currentUserId = mAuth.getCurrentUser().getUid();

        db.collection("transactions")
                .whereEqualTo("buyerId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    transactionList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        TransactionModel transaction = doc.toObject(TransactionModel.class);
                        transactionList.add(transaction);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
