/*
 * Copyright (c) 2017 Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Special thanks to the project contributors and collaborators
 * 	https://github.com/jahirfiquitiva/IconShowcase#special-thanks
 */

package jahirfiquitiva.iconshowcase.utilities.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.util.Calendar;

import jahirfiquitiva.iconshowcase.R;

@SuppressWarnings("ResourceAsColor")
public class ThemeUtils {

    public static final int LIGHT = 0;
    public static final int DARK = 1;
    public static final int CLEAR = 2;
    public static final int AUTO = 3;

    private static boolean darkTheme;
    private static boolean transparent;

    @ColorRes
    public static int darkOrLight(@ColorRes int dark, @ColorRes int light) {
        return darkTheme ? dark : light;
    }

    @ColorInt
    public static int darkOrLight(@NonNull Context context, @ColorRes int dark,
                                  @ColorRes int light) {
        return ContextCompat.getColor(context, darkOrLight(dark, light));
    }

    @ColorInt
    public static int darkLightOrTransparent(@NonNull Context context, @ColorRes int dark,
                                             @ColorRes int light, @ColorRes int transparentColor) {
        if (transparent) return ContextCompat.getColor(context, transparentColor);
        return darkOrLight(context, dark, light);
    }

    public static int getCurrentTheme() {
        return darkTheme ? transparent ? CLEAR : DARK : LIGHT;
    }

    public static boolean isDarkTheme() {
        return darkTheme;
    }

    public static void onActivityCreateSetTheme(Activity activity) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        int mTheme = sp.getInt("theme",
                (activity.getResources().getInteger(R.integer.app_theme) - 1));
        switch (mTheme) {
            default:
            case LIGHT:
                activity.setTheme(R.style.AppTheme);
                darkTheme = false;
                transparent = false;
                break;
            case DARK:
                activity.setTheme(R.style.AppThemeDark);
                darkTheme = true;
                transparent = false;
                break;
            case CLEAR:
                activity.setTheme(R.style.AppThemeClear);
                darkTheme = true;
                transparent = true;
                break;
            case AUTO:
                Calendar c = Calendar.getInstance();
                transparent = false;
                int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
                if (timeOfDay >= 7 && timeOfDay < 20) {
                    activity.setTheme(R.style.AppTheme);
                    darkTheme = false;
                } else {
                    activity.setTheme(R.style.AppThemeDark);
                    darkTheme = true;
                }
                break;
        }
        onActivityCreateSetNavBar(activity);
    }

    private static void onActivityCreateSetNavBar(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        activity.getWindow().setNavigationBarColor(darkTheme ?
                ContextCompat.getColor(activity, R.color.dark_theme_navigation_bar) :
                ContextCompat.getColor(activity, R.color.light_theme_navigation_bar));
    }

    public static void restartActivity(Activity activity) {
        activity.recreate();
    }

}