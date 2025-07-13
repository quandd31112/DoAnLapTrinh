package ddtradeup.ddtradeup2.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

import ddtradeup.ddtradeup2.ItemModel;
import ddtradeup.ddtradeup2.ItemsAdapter;
import ddtradeup.ddtradeup2.R;

public class HomeFragment extends Fragment {
    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView recyclerView;
    private ItemsAdapter adapter;
    private ArrayList<ItemModel> itemList;
    private FirebaseFirestore db;
    private FusedLocationProviderClient locationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private double userLatitude;
    private double userLongitude;
    private boolean hasLocationPermission;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        searchEditText = view.findViewById(R.id.searchEditText);
        searchButton = view.findViewById(R.id.searchButton);
        recyclerView = view.findViewById(R.id.recyclerView);
        itemList = new ArrayList<>();
        adapter = new ItemsAdapter(getContext(), itemList, false);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        db = FirebaseFirestore.getInstance();
        locationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        searchButton.setOnClickListener(v -> searchItems());
        checkLocationPermission();
        return view;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            hasLocationPermission = true;
            getUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasLocationPermission = true;
                getUserLocation();
            } else {
                hasLocationPermission = false;
                Toast.makeText(requireContext(), "Không có quyền vị trí, chỉ tìm theo tag!", Toast.LENGTH_SHORT).show();
                loadItems(null, null);
            }
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLatitude = location.getLatitude();
                        userLongitude = location.getLongitude();
                        loadItems(null, null);
                    } else {
                        Toast.makeText(requireContext(), "Không lấy được vị trí, chỉ tìm theo tag!", Toast.LENGTH_SHORT).show();
                        loadItems(null, null);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Lỗi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadItems(null, null);
                });
    }

    private void loadItems(String query, Double maxDistanceKm) {
        Query firestoreQuery = db.collection("items")
                .orderBy("timestamp", Query.Direction.DESCENDING);
        if (query != null && !query.isEmpty()) {
            firestoreQuery = firestoreQuery.whereArrayContains("tags", query);
        }
        firestoreQuery.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ItemModel item = doc.toObject(ItemModel.class);
                        item.setId(doc.getId());
                        // Lọc theo khoảng cách nếu có vị trí và maxDistanceKm
                        if (hasLocationPermission && maxDistanceKm != null && item.getLatitude() != null && item.getLongitude() != null) {
                            double distance = calculateDistance(userLatitude, userLongitude,
                                    item.getLatitude(), item.getLongitude());
                            if (distance <= maxDistanceKm) {
                                itemList.add(item);
                            }
                        } else {
                            itemList.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void searchItems() {
        String query = searchEditText.getText().toString().trim();
        // Lọc trong bán kính 10km nếu có vị trí
        Double maxDistanceKm = hasLocationPermission ? 10.0 : null;
        loadItems(query.isEmpty() ? null : query, maxDistanceKm);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Bán kính Trái Đất (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Khoảng cách tính bằng km
    }
}