package ddtradeup.ddtradeup2;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseUtil {

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    /** Lấy FirebaseAuth toàn cục */
    public static FirebaseAuth getAuth() {
        return mAuth;
    }

    /** Kiểm tra người dùng đã xác minh email chưa */
    public static boolean isEmailVerified() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    /** Trả về GoogleSignInClient đã cấu hình ID token */
    public static GoogleSignInClient getGoogleSignInClient(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id)) // Lấy từ google‑services.json
                .requestEmail()
                .build();
        return GoogleSignIn.getClient(context, gso);
    }
}
