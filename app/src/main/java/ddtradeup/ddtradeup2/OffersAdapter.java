package ddtradeup.ddtradeup2;

import android.content.Context;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.ViewHolder> {
    private Context context;
    private ArrayList<OfferModel> offerList;
    private String currentUserId;

    public OffersAdapter(Context context, ArrayList<OfferModel> offerList, String currentUserId) {
        this.context = context;
        this.offerList = offerList;
        this.currentUserId = currentUserId;
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

        if (currentUserId.equals(offer.getSellerId())) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnCounter.setVisibility(View.VISIBLE);
        } else {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnCounter.setVisibility(View.GONE);
        }

        holder.btnAccept.setOnClickListener(v -> {
            updateStatus(offer, "accepted", position);
            FirebaseFirestore.getInstance().collection("items").document(offer.getItemId())
                    .update("status", "Sold");
        });
        holder.btnReject.setOnClickListener(v -> updateStatus(offer, "rejected", position));
        holder.btnCounter.setOnClickListener(v -> showCounterOfferDialog(offer, position));
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

    private void showCounterOfferDialog(OfferModel offer, int position) {
        EditText edt = new EditText(context);
        edt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        edt.setHint("Nhập giá đối đề nghị");

        new AlertDialog.Builder(context)
                .setTitle("Đối đề nghị")
                .setView(edt)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    String input = edt.getText().toString();
                    if (input.isEmpty()) {
                        Toast.makeText(context, "Vui lòng nhập giá!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double price = Double.parseDouble(input);
                    String newOfferId = FirebaseFirestore.getInstance().collection("offers").document().getId();
                    OfferModel counterOffer = new OfferModel(
                            newOfferId, offer.getItemId(), offer.getBuyerId(), offer.getSellerId(),
                            price, "pending", System.currentTimeMillis());
                    FirebaseFirestore.getInstance().collection("offers").document(newOfferId)
                            .set(counterOffer)
                            .addOnSuccessListener(aVoid -> {
                                updateStatus(offer, "rejected", position);
                                Toast.makeText(context, "Đã gửi đối đề nghị!", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return offerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView offerPrice, offerStatus;
        Button btnAccept, btnReject, btnCounter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            offerPrice = itemView.findViewById(R.id.offerPrice);
            offerStatus = itemView.findViewById(R.id.offerStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnCounter = itemView.findViewById(R.id.btnCounter);
        }
    }
}