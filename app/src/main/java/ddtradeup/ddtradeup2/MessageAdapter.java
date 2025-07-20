package ddtradeup.ddtradeup2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Hiển thị 2 kiểu view: tin nhắn mình gửi (canh phải) & tin nhận (canh trái).
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Holder> {

    private static final int VIEW_SENT     = 0;
    private static final int VIEW_RECEIVED = 1;

    private final List<Message> messages;
    private final String currentUid;           // id người dùng hiện tại

    public MessageAdapter(List<Message> messages, String currentUid) {
        this.messages   = messages;
        this.currentUid = currentUid;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).senderId.equals(currentUid) ? VIEW_SENT : VIEW_RECEIVED;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == VIEW_SENT)
                ? R.layout.row_msg_sent
                : R.layout.row_msg_received;

        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        Message m = messages.get(pos);
        h.txtMsg.setText(m.message);

        // ⏰ Hiển thị thời gian gọn gàng (vd: 14:35)
        String time = DateFormat.getTimeInstance(DateFormat.SHORT)
                .format(new Date(m.timestamp));
        h.txtTime.setText(time);
    }

    @Override
    public int getItemCount() { return messages.size(); }

    /* ---------- ViewHolder ---------- */
    static class Holder extends RecyclerView.ViewHolder {
        TextView txtMsg, txtTime;
        Holder(@NonNull View item) {
            super(item);
            txtMsg  = item.findViewById(R.id.textMessage);
            txtTime = item.findViewById(R.id.textTime);
        }
    }
}
