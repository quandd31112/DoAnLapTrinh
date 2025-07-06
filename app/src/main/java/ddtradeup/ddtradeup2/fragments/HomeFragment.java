package ddtradeup.ddtradeup2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import ddtradeup.ddtradeup2.R;

import ddtradeup.ddtradeup2.ItemModel;
import ddtradeup.ddtradeup2.ListingsAdapter;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private ListingsAdapter adapter;
    private ArrayList<ItemModel> itemList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemList = new ArrayList<>();
        adapter = new ListingsAdapter(getContext(), itemList, true); // true: cho phép click mở Detail
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadAllItems();
        return view;
    }

    private void loadAllItems() {
        db.collection("items").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ItemModel item = doc.toObject(ItemModel.class);
                        item.setId(doc.getId());
                        itemList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
