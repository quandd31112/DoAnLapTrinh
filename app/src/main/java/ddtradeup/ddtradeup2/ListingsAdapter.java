package ddtradeup.ddtradeup2;

import android.content.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class ListingsAdapter extends RecyclerView.Adapter<ListingsAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ItemModel> itemList;
    private boolean isHome; // Nếu true thì mở DetailActivity khi click

    public ListingsAdapter(Context context, ArrayList<ItemModel> itemList, boolean isHome) {
        this.context = context;
        this.itemList = itemList;
        this.isHome = isHome;
    }

    @NonNull
    @Override
    public ListingsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingsAdapter.ViewHolder holder, int position) {
        ItemModel item = itemList.get(position);
        holder.itemTitle.setText(item.getTitle());
        holder.itemDescription.setText(item.getDescription());
        holder.itemPrice.setText("Giá: " + formatPrice(item.getPrice()) + " VNĐ");

        // Load image
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.itemImage);

        // Nếu là Home, click vào item sẽ mở DetailActivity
        if (isHome) {
            holder.btnDelete.setVisibility(View.GONE); // Home ko cho xoá
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("itemId", item.getId());
                context.startActivity(intent);
            });
        } else {
            // Nếu là MyListings (của tôi), cho phép xoá
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(context)
                        .setTitle("Xác nhận")
                        .setMessage("Bạn có chắc muốn xóa bài đăng này không?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteItem(item, holder.getAdapterPosition()))
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }
    }

    private void deleteItem(ItemModel item, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("items").document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    itemList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Đã xóa thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Xóa thất bại!", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle, itemDescription, itemPrice;
        ImageView itemImage;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemImage = itemView.findViewById(R.id.itemImage);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private String formatPrice(double price) {
        if (price == (long) price)
            return String.format("%d", (long) price);
        else
            return String.format("%s", price);
    }
}
