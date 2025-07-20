package ddtradeup.ddtradeup2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

import ddtradeup.ddtradeup2.BuyerTransactionsActivity;
import ddtradeup.ddtradeup2.ChatActivity;
import ddtradeup.ddtradeup2.EditProfileActivity;
import ddtradeup.ddtradeup2.ItemModel;
import ddtradeup.ddtradeup2.ItemsAdapter;          // << dùng adapter mới
import ddtradeup.ddtradeup2.LoginActivity;
import ddtradeup.ddtradeup2.R;

public class ProfileFragment extends Fragment {

    private TextView userNameTextView, userEmailTextView;
    private RecyclerView recyclerView;
    private ItemsAdapter adapter;
    private final ArrayList<ItemModel> itemList = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // View binding
        userNameTextView  = view.findViewById(R.id.userNameTextView);
        userEmailTextView = view.findViewById(R.id.userEmailTextView);
        recyclerView      = view.findViewById(R.id.recyclerView);

        Button btnEditProfile = view.findViewById(R.id.editProfileButton);
        Button btnHistory     = view.findViewById(R.id.historyButton);
        Button btnLogout      = view.findViewById(R.id.logoutButton);
        Button btnChatList    = view.findViewById(R.id.chatButton);   // << mới

        // Adapter hiển thị “Sửa” vì đây là item của chính user (isEditable = true)
        adapter = new ItemsAdapter(requireContext(), itemList, /*isEditable*/ true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(getContext(), EditProfileActivity.class)));

        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(getContext(), BuyerTransactionsActivity.class)));

        btnChatList.setOnClickListener(v ->
                startActivity(new Intent(getContext(), ChatActivity.class)));   // mở màn chat chính

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent i = new Intent(getContext(), LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            loadUserInfo(uid);
            loadUserItems(uid);
        }
    }

    /* ---------------- helper ---------------- */

    private void loadUserInfo(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    userNameTextView.setText(doc.getString("name")  != null ? doc.getString("name")  : "Unknown");
                    userEmailTextView.setText(doc.getString("email") != null ? doc.getString("email") : "Unknown");
                });
    }

    private void loadUserItems(String uid) {
        db.collection("items")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    itemList.clear();
                    qs.forEach(d -> {
                        ItemModel item = d.toObject(ItemModel.class);
                        item.setId(d.getId());
                        itemList.add(item);
                    });
                    adapter.notifyDataSetChanged();
                });
    }
}
