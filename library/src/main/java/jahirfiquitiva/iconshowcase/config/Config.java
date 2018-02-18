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

package jahirfiquitiva.iconshowcase.config;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ArrayRes;
import android.support.annotation.BoolRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import jahirfiquitiva.iconshowcase.BuildConfig;
import jahirfiquitiva.iconshowcase.R;
import timber.log.Timber;

/**
 * Created by Allan Wang on 2016-08-19.
 * <p/>
 * With reference to Polar https://github
 * .com/afollestad/polar-dashboard/blob/master/app/src/main/java/com/afollestad/polar/config
 * /Config.java
 */
public class Config implements IConfig {

    public static final String MARKET_URL = "https://play.google.com/store/apps/details?id=";
    public static final String PLAY_STORE_INSTALLER = "com.google.android.feedback";
    public static final String PLAY_STORE_PACKAGE = "com.android.vending";
    public static final String ADW_ACTION = "org.adw.launcher.icons.ACTION_PICK_ICON";
    public static final String TURBO_ACTION =
            "com.phonemetra.turbo.launcher.icons.ACTION_PICK_ICON";
    public static final String NOVA_ACTION = "com.novalauncher.THEME";
    public static final String APPLY_ACTION = "jahirfiquitiva.iconshowcase.APPLY_ACTION";

    public static final int ICONS_PICKER = 1, IMAGE_PICKER = 2, WALLS_PICKER = 3, ICONS_APPLIER = 4;

    @SuppressLint("StaticFieldLeak")
    private static Config mConfig;
    private Context context;
    private Resources mR;

    private Config(@Nullable Context context) {
        mR = null;
        this.context = context;
        if (context != null)
            mR = context.getResources();
    }

    public static void init(@NonNull Context context) {
        mConfig = new Config(context);
    }

    public static void setContext(Context context) {
        if (mConfig != null) {
            mConfig.context = context;
            if (context != null)
                mConfig.mR = context.getResources();
        }
    }

    public static void deinit() {
        if (mConfig != null) {
            mConfig.destroy();
            mConfig = null;
        }
    }

    @NonNull
    public static IConfig get() {
        if (mConfig == null)
            return new Config(null); // shouldn't ever happen, but avoid crashes
        return mConfig;
    }

    @NonNull
    public static IConfig get(@NonNull Context context) {
        if (mConfig == null)
            return new Config(context);
        return mConfig;
    }

    private void destroy() {
        context = null;
        mR = null;
    }

    // Getters

    @Override
    public boolean bool(@BoolRes int id) {
        return mR != null && mR.getBoolean(id);
    }

    @Override
    @Nullable
    public String string(@StringRes int id) {
        if (mR == null) return null;
        return mR.getString(id);
    }

    @Override
    @Nullable
    public String[] stringArray(@ArrayRes int id) {
        if (mR == null) return null;
        return mR.getStringArray(id);
    }

    @Override
    public int integer(@IntegerRes int id) {
        if (mR == null) return 0;
        return mR.getInteger(id);
    }

    @Override
    public boolean hasString(@StringRes int id) {
        String s = string(id);
        return (s != null && !s.isEmpty());
    }

    @Override
    public boolean hasArray(@ArrayRes int id) {
        String[] s = stringArray(id);
        return (s != null && s.length != 0);
    }

    @Override
    public boolean allowDebugging() {
        return BuildConfig.DEBUG || mR == null || mR.getBoolean(R.bool.debugging);
    }

    @Override
    public int appTheme() {
        return integer(R.integer.app_theme);
    }

    @Override
    public boolean hasDonations() {
        return hasGoogleDonations() || hasPaypal();
    }

    @Override
    public boolean hasGoogleDonations() { //Also check donation key from java
        return hasArray(R.array.google_donations_catalog) && hasArray(R.array
                .google_donations_items);
    }

    @Override
    public boolean hasPaypal() {
        return hasString(R.string.paypal_email);
    }

    @NonNull
    @Override
    public String getPaypalCurrency() {
        String s = string(R.string.paypal_currency_code);
        if (s == null || s.length() != 3) {
            Timber.e("Invalid currency $s; switching to USD", s);
            return "USD";
        }
        return s;
    }

    @Override
    public boolean devOptions() {
        return bool(R.bool.dev_options);
    }

    @Override
    public boolean shuffleToolbarIcons() {
        return bool(R.bool.shuffle_toolbar_icons);
    }

    @Override
    public boolean userWallpaperInToolbar() {
        return bool(R.bool.enable_user_wallpaper_in_toolbar);
    }

    @Override
    public boolean hidePackInfo() {
        return bool(R.bool.hide_pack_info);
    }

    @Override
    public int getIconResId(String iconName) {
        if (context == null) return 0;
        return mR.getIdentifier(iconName, "drawable", context.getPackageName());
    }
}
