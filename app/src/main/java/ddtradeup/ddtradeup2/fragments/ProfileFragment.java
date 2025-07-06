package ddtradeup.ddtradeup2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import ddtradeup.ddtradeup2.LoginActivity;
import ddtradeup.ddtradeup2.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private TextView nameText, emailText, bioText, contactText;
    private CircleImageView profileImage;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nameText = view.findViewById(R.id.displayNameText);
        emailText = view.findViewById(R.id.emailText);
        bioText = view.findViewById(R.id.bioText);
        contactText = view.findViewById(R.id.contactText);
        profileImage = view.findViewById(R.id.profileImage);

        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnSwitchAccount = view.findViewById(R.id.btnSwitchAccount);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        nameText.setText(snapshot.getString("displayName"));
                        emailText.setText(snapshot.getString("email"));
                        bioText.setText(snapshot.getString("bio"));
                        contactText.setText(snapshot.getString("contact"));
                        Glide.with(this).load(snapshot.getString("photoUrl")).into(profileImage);
                    }
                });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnSwitchAccount.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
        });
    }
}
