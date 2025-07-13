package ddtradeup.ddtradeup2;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class SellerTransactionsActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private TransactionAdapter adapter;
    private final List<TransactionModel> list = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_seller_transactions);

        recycler = findViewById(R.id.sellerTransactionsRecyclerView);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(list, true);       // true = seller
        recycler.setAdapter(adapter);

        loadTx();
    }

    private void loadTx(){
        db.collection("transactions")
                .whereEqualTo("sellerId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    list.clear();
                    for(DocumentSnapshot d : snap){
                        TransactionModel tx = d.toObject(TransactionModel.class);
                        if(tx==null) continue;
                        tx.setId(d.getId());
                        list.add(tx);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
