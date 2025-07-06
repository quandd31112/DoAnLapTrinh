package ddtradeup.ddtradeup2;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.ViewHolder> {
    private Context context;
    private ArrayList<OfferModel> offerList;

    public OffersAdapter(Context context, ArrayList<OfferModel> offerList, String currentUserId) {
        this.context = context;
        this.offerList = offerList;
    }

    @NonNull
    @Override
    public OffersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.offer_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OffersAdapter.ViewHolder holder, int position) {
        OfferModel offer = offerList.get(position);
        holder.offerPrice.setText("Giá: " + offer.getPrice());
        holder.offerStatus.setText("Trạng thái: " + offer.getStatus());

        holder.btnAccept.setOnClickListener(v -> updateStatus(offer, "accepted", position));
        holder.btnReject.setOnClickListener(v -> updateStatus(offer, "rejected", position));
    }

    private void updateStatus(OfferModel offer, String status, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("offers").document(offer.getId())
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    offer.setStatus(status);
                    notifyItemChanged(position);
                    Toast.makeText(context, "Đã cập nhật!", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return offerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView offerPrice, offerStatus;
        Button btnAccept, btnReject;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            offerPrice = itemView.findViewById(R.id.offerPrice);
            offerStatus = itemView.findViewById(R.id.offerStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
