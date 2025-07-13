package ddtradeup.ddtradeup2;

// File: BuyNowButtonHandler.java (nên dùng trong DetailActivity)

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BuyNowButtonHandler {

    public static void handleBuyNow(Context context, String itemId, String sellerId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String buyerId = auth.getCurrentUser().getUid();

        // Tạo transaction mới
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("itemId", itemId);
        transaction.put("buyerId", buyerId);
        transaction.put("sellerId", sellerId);
        transaction.put("status", "completed");
        transaction.put("timestamp", FieldValue.serverTimestamp());

        db.collection("transactions")
                .add(transaction)
                .addOnSuccessListener(documentReference -> {
                    // Cập nhật trạng thái sản phẩm
                    db.collection("items").document(itemId)
                            .update("status", "sold")
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(context, "Mua thành công", Toast.LENGTH_SHORT).show();
                            });
                });
    }
}

