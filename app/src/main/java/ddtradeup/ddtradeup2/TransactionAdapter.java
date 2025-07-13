package ddtradeup.ddtradeup2;

import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TxVH> {

    private final List<TransactionModel> list;
    private final boolean isSeller;                 // true nếu seller xem lịch sử

    public TransactionAdapter(List<TransactionModel> list, boolean isSeller) {
        this.list = list;
        this.isSeller = isSeller;
    }

    @NonNull @Override
    public TxVH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_transaction, p, false);
        return new TxVH(view);
    }

    @Override public int getItemCount() { return list.size(); }

    @Override
    public void onBindViewHolder(@NonNull TxVH h, int pos) {

        TransactionModel tx = list.get(pos);

        // Lấy thông tin sản phẩm cho UI
        FirebaseFirestore.getInstance().collection("items")
                .document(tx.getItemId()).get()
                .addOnSuccessListener(doc -> {
                    String title   = doc.getString("title");
                    String image   = doc.getString("imageUrl");
                    String price   = doc.getString("price");

                    h.titlePrice.setText(title + " · ₫" + price);
                    Glide.with(h.itemView.getContext())
                            .load(image).placeholder(R.drawable.placeholder)
                            .into(h.img);
                });

        // Ai là đối tác?
        String userRoleText = isSeller
                ? "Người mua: " + tx.getBuyerId()
                : "Người bán: " + tx.getSellerId();
        h.user.setText(userRoleText);

        // Trạng thái + Ngày
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(tx.getTimestamp()));
        h.statusDate.setText("Trạng thái: " + tx.getStatus() + " · " + date);

        // Seller có quyền accept/decline nếu pending
        if (isSeller && "pending".equals(tx.getStatus())) {
            h.accept.setVisibility(View.VISIBLE);
            h.decline.setVisibility(View.VISIBLE);

            h.accept.setOnClickListener(v -> update("accepted", tx, h));
            h.decline.setOnClickListener(v -> update("declined", tx, h));
        } else {
            h.accept.setVisibility(View.GONE);
            h.decline.setVisibility(View.GONE);
        }
    }

    private void update(String newStatus, TransactionModel tx, TxVH h) {
        FirebaseFirestore.getInstance()
                .collection("transactions")
                .document(tx.getId())
                .update("status", newStatus)
                .addOnSuccessListener(v -> {
                    tx.setStatus(newStatus);
                    notifyItemChanged(h.getAdapterPosition());
                    Toast.makeText(h.itemView.getContext(),
                            "Đã cập nhật: " + newStatus, Toast.LENGTH_SHORT).show();
                });
    }

    static class TxVH extends RecyclerView.ViewHolder {
        ImageView img; TextView titlePrice, user, statusDate;
        Button accept, decline;
        TxVH(@NonNull View v){
            super(v);
            img        = v.findViewById(R.id.txImage);
            titlePrice = v.findViewById(R.id.txTitlePrice);
            user       = v.findViewById(R.id.txUser);
            statusDate = v.findViewById(R.id.txStatusDate);
            accept     = v.findViewById(R.id.acceptButton);
            decline    = v.findViewById(R.id.declineButton);
        }
    }
}
