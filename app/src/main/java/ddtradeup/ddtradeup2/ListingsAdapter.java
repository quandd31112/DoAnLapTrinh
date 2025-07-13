package ddtradeup.ddtradeup2;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ListingsAdapter extends RecyclerView.Adapter<ListingsAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<ItemModel> itemList;
    private final boolean isEditable;  // true = hiển thị item của chủ sở hữu (Profile)

    public ListingsAdapter(Context context, ArrayList<ItemModel> itemList, boolean isEditable) {
        this.context = context;
        this.itemList = itemList;
        this.isEditable = isEditable;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemModel item = itemList.get(position);

        holder.itemTitle.setText(item.getTitle());
        holder.itemDescription.setText(item.getDescription());

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.image_error)
                .into(holder.itemImage);

        // Xử lý click mở chi tiết sản phẩm
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, DetailActivity.class);
            i.putExtra("itemId", item.getId());
            context.startActivity(i);
        });

        // Nếu có quyền chỉnh sửa (isEditable), hiển thị nút edit
        if (isEditable) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.editButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditItemActivity.class);
                intent.putExtra("itemId", item.getId());
                context.startActivity(intent);
            });
        } else {
            holder.editButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemTitle, itemDescription;
        ImageButton editButton;

        ViewHolder(View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_image);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemDescription = itemView.findViewById(R.id.item_description);
            editButton = itemView.findViewById(R.id.edit_button); // Bắt buộc phải có ID này trong XML
        }
    }
}
