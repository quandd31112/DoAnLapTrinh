package ddtradeup.ddtradeup2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String currentUserId;
    private String chatWithUserId;
    private DatabaseReference messagesRef;
    private List<Message> messages = new ArrayList<>();
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Lấy UID người gửi và người nhận từ Intent
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatWithUserId = getIntent().getStringExtra("chatWithUserId");

        if (chatWithUserId == null) {
            Toast.makeText(this, "Thiếu người nhận!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        adapter = new MessageAdapter(messages, currentUserId);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Lắng nghe tin nhắn
        messagesRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Message msg = snap.getValue(Message.class);
                    if (msg == null) continue;

                    if ((msg.senderId.equals(currentUserId) && msg.receiverId.equals(chatWithUserId)) ||
                            (msg.senderId.equals(chatWithUserId) && msg.receiverId.equals(currentUserId))) {
                        messages.add(msg);
                    }
                }
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messages.size() - 1);
            }

            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Gửi tin nhắn
        Button sendBtn = findViewById(R.id.sendBtn);
        EditText input = findViewById(R.id.messageInput);

        sendBtn.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                Message msg = new Message(currentUserId, chatWithUserId, text, System.currentTimeMillis());
                messagesRef.push().setValue(msg);
                input.setText("");
            }
        });
    }
}
