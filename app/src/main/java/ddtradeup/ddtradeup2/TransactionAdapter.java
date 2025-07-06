package ddtradeup.ddtradeup2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<TransactionModel> transactionList;

    public TransactionAdapter(List<TransactionModel> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionAdapter.ViewHolder holder, int position) {
        TransactionModel tx = transactionList.get(position);
        holder.info.setText("Người mua: " + tx.getBuyerId() + "\nTrạng thái: " + tx.getStatus());

        holder.accept.setOnClickListener(v -> updateStatus(tx, "accepted", v));
        holder.decline.setOnClickListener(v -> updateStatus(tx, "declined", v));
    }

    private void updateStatus(TransactionModel tx, String status, View view) {
        FirebaseFirestore.getInstance()
                .collection("transactions")
                .document(tx.getId())
                .update("status", status)
                .addOnSuccessListener(unused ->
                        Toast.makeText(view.getContext(), "Đã cập nhật: " + status, Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView info;
        Button accept, decline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            info = itemView.findViewById(R.id.transactionInfo);
            accept = itemView.findViewById(R.id.acceptButton);
            decline = itemView.findViewById(R.id.declineButton);
        }
    }
}
