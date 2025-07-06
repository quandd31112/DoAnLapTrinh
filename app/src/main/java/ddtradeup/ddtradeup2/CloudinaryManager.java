package ddtradeup.ddtradeup2;

import android.content.Context;
import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {

    public static void init(Context context) {
        try {
            MediaManager.get(); // sẽ ném lỗi nếu chưa init
        } catch (IllegalStateException e) {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", "dredyoths");
            config.put("api_key", "182255957121679");
            config.put("api_secret", "y6jhzyhR-tdpKY4vlwLspGD7c88");
            MediaManager.init(context, config);
        }
    }
}
