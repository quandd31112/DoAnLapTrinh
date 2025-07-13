package ddtradeup.ddtradeup2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class PaymentActivity extends AppCompatActivity {

    private String itemId, sellerId, price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        TextView tvAmount = findViewById(R.id.tvAmount);
        Button btnConfirm = findViewById(R.id.btnConfirmPayment);

        Intent intent = getIntent();
        itemId   = intent.getStringExtra("itemId");
        sellerId = intent.getStringExtra("sellerId");
        price    = intent.getStringExtra("price");

        if (itemId == null || sellerId == null || price == null) {
            Toast.makeText(this, "Thiếu dữ liệu thanh toán!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvAmount.setText("Total: ₫" + price);

        btnConfirm.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        String buyerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String, Object> tx = new HashMap<>();
        tx.put("itemId", itemId);
        tx.put("buyerId", buyerId);
        tx.put("sellerId", sellerId);
        tx.put("price", price);
        tx.put("status", "completed");
        tx.put("timestamp", FieldValue.serverTimestamp());

        db.collection("transactions").add(tx).addOnSuccessListener(docRef -> {
            db.collection("items").document(itemId).update("status", "sold")
                    .addOnSuccessListener(u -> {
                        Toast.makeText(this, "Payment successful", Toast.LENGTH_SHORT).show();
                        Intent result = new Intent();
                        result.putExtra("paid", true);
                        setResult(RESULT_OK, result);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Không thể cập nhật trạng thái sản phẩm!", Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Payment failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
