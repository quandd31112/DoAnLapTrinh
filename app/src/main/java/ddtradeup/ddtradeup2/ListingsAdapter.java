package ddtradeup.ddtradeup2;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ListingsAdapter extends RecyclerView.Adapter<ListingsAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<ItemModel> itemList;
    private final boolean isEditable;     // true = hiển thị item của chủ sở hữu (Profile)

    public ListingsAdapter(Context context, ArrayList<ItemModel> itemList, boolean isEditable) {
        this.context  = context;
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

        holder.itemTitle.setText(item.getTitle());          // hiển thị tiêu đề
        holder.itemDescription.setText(item.getDescription());

        // load ảnh – KHÔNG phụ thuộc isEditable
        Glide.with(context)
                .load(item.getImageUrl())                      // phải là https:// → đã fix ở AddItemFragment
                .placeholder(R.drawable.placeholder)           // ảnh tạm
                .error(R.drawable.image_error)                 // ảnh khi lỗi
                .into(holder.itemImage);

        // mở chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, DetailActivity.class);
            i.putExtra("itemId", item.getId());
            context.startActivity(i);
        });
    }

    @Override public int getItemCount() { return itemList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView  itemTitle, itemDescription;

        ViewHolder(View itemView) {
            super(itemView);
            itemImage       = itemView.findViewById(R.id.item_image);    // ← BỔ SUNG
            itemTitle       = itemView.findViewById(R.id.item_title);
            itemDescription = itemView.findViewById(R.id.item_description);
        }
    }
}
