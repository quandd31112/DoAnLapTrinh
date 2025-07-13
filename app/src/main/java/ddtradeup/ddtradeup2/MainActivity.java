package ddtradeup.ddtradeup2;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import ddtradeup.ddtradeup2.fragments.AddItemFragment;
import ddtradeup.ddtradeup2.fragments.HomeFragment;
import ddtradeup.ddtradeup2.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigationView);

        // Load Home mặc định khi mở app
        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();

            if (id == R.id.menu_home) {
                selected = new HomeFragment();
            } else if (id == R.id.menu_add) {
                selected = new AddItemFragment();
            } else if (id == R.id.menu_profile) {
                selected = new ProfileFragment();
            }

            if (selected != null) {
                loadFragment(selected);
                return true;
            }
            if (!FirebaseUtil.isEmailVerified()) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Vui lòng xác minh email trước khi truy cập ứng dụng.", Toast.LENGTH_LONG).show();
                finish();
            }

            return false;

        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .commit();
    }
}
