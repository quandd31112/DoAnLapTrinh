package ddtradeup.ddtradeup2;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemViewHolder> {

    private final Context context;
    private final List<ItemModel> itemList;
    private final boolean isEditable;

    public ItemsAdapter(Context context, List<ItemModel> itemList, boolean isEditable) {
        this.context    = context;
        this.itemList   = itemList;
        this.isEditable = isEditable;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listing, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemModel item = itemList.get(position);

        holder.title.setText(item.getTitle());

        /* ---------- FIX NumberFormatException ---------- */
        long priceLong = 0;
        String priceStr = item.getPrice();
        if (priceStr != null && !priceStr.isEmpty()) {
            try {
                // Loại bỏ ký tự không phải số (dấu phẩy, khoảng trắng…)
                priceLong = Long.parseLong(priceStr.replaceAll("[^\\d]", ""));
            } catch (NumberFormatException ignored) {
                priceLong = 0;
            }
        }
        holder.price.setText(String.format("₫%,d", priceLong));
        /* ---------------------------------------------- */

        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(item.getDescription());
        } else {
            holder.description.setVisibility(View.GONE);
        }

        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.itemImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("itemId", item.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, price;
        ImageView itemImage;
        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            title       = itemView.findViewById(R.id.item_title);
            description = itemView.findViewById(R.id.item_description);
            price       = itemView.findViewById(R.id.item_price);
            itemImage   = itemView.findViewById(R.id.item_image);
        }
    }
}
