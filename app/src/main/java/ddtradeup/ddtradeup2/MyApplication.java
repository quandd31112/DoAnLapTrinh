package ddtradeup.ddtradeup2;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Cấu hình Cloudinary
        Map config = new HashMap();
        config.put("cloud_name", "dredyoths");
        config.put("api_key", "644525417978379");
        config.put("api_secret", "wCaF1IOrTeu0NznCCeYxd7wDiHM");
        config.put("upload_preset", "ddtradeup_preset"); // ← thêm dòng này
        MediaManager.init(this, config);
    }
}
