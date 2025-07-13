package ddtradeup.ddtradeup2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

import ddtradeup.ddtradeup2.BuyerTransactionsActivity;
import ddtradeup.ddtradeup2.EditProfileActivity;
import ddtradeup.ddtradeup2.ItemModel;
import ddtradeup.ddtradeup2.ListingsAdapter;
import ddtradeup.ddtradeup2.LoginActivity;
import ddtradeup.ddtradeup2.R;

public class ProfileFragment extends Fragment {
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private RecyclerView recyclerView;
    private ListingsAdapter adapter;
    private ArrayList<ItemModel> itemList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userNameTextView = view.findViewById(R.id.userNameTextView);
        userEmailTextView = view.findViewById(R.id.userEmailTextView);
        recyclerView = view.findViewById(R.id.recyclerView);
        Button btnEditProfile = view.findViewById(R.id.editProfileButton);
        Button btnLogout = view.findViewById(R.id.logoutButton);
        Button btnHistory = view.findViewById(R.id.historyButton); // ← Thêm nút này vào layout XML

        itemList = new ArrayList<>();
        adapter = new ListingsAdapter(getContext(), itemList, true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            loadUserInfo(uid);
            loadUserItems(uid);
        }

        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), EditProfileActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), BuyerTransactionsActivity.class));
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

    private void loadUserInfo(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        userNameTextView.setText(name != null ? name : "Unknown");
                        userEmailTextView.setText(email != null ? email : "Unknown");
                    } else {
                        userNameTextView.setText("Unknown");
                        userEmailTextView.setText("Unknown");
                    }
                })
                .addOnFailureListener(e -> {
                    userNameTextView.setText("Error");
                    userEmailTextView.setText("Error");
                });
    }

    private void loadUserItems(String uid) {
        db.collection("items")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ItemModel item = doc.toObject(ItemModel.class);
                        item.setId(doc.getId());
                        itemList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
