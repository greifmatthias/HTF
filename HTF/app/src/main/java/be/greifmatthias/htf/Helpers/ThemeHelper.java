package be.greifmatthias.htf.Helpers;

import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ThemeHelper {
    public static void setStatusbarWhite(Window window, boolean white){
//        Try to change statusbar to white and icons colortint to black, requires SDK M=+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Change statusbar backcolor
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // Change color
//            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorStatusbar_light));
            // Change icon color tint
            View decor = window.getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            // decor.setSystemUiVisibility(0); revert to light icons
        }
    }
}
