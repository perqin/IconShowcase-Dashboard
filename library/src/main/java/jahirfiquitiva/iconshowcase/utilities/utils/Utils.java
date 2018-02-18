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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import timber.log.Timber;

/**
 * With a little help from Aidan Follestad (afollestad)
 */
public class Utils {
    
    public static String getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // this should never happen
            return "Unknown";
        }
    }
    
    public static String getAppPackageName(Context context) {
        return context.getPackageName();
    }
    
    public static boolean hasNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context
                                                                                        .CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    
    public static boolean isConnectedToWiFi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context
                                                                                        .CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && (activeNetwork.getType() == ConnectivityManager
                .TYPE_WIFI) && activeNetwork.isConnectedOrConnecting();
    }
    
    public static boolean isAppInstalled(Context context, String packageName) {
        final PackageManager pm = context.getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }
    
    public static String getDefaultLauncherPackage(@NonNull Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo resolveInfo = context.getPackageManager()
                .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }
    
    public static Snackbar snackbar(@NonNull Context context, @NonNull View view, @NonNull
            CharSequence text, int duration) {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        snackbar.getView().setBackgroundColor(ThemeUtils.darkOrLight(context, R.color
                .snackbar_dark, R.color.snackbar_light));
        return snackbar;
    }
    
    public static void showSimpleSnackbar(Context context, View location, String text) {
        snackbar(context, location, text, Snackbar.LENGTH_SHORT).show();
    }
    
    public static void openLink(Context context, String link) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }
    
    @SuppressWarnings("ResourceAsColor")
    public static void openLinkInChromeCustomTab(Context context, String link) {
        final CustomTabsClient[] mClient = new CustomTabsClient[1];
        final CustomTabsSession[] mCustomTabsSession = new CustomTabsSession[1];
        CustomTabsServiceConnection mCustomTabsServiceConnection;
        CustomTabsIntent customTabsIntent;
        
        mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName,
                                                     CustomTabsClient customTabsClient) {
                mClient[0] = customTabsClient;
                mClient[0].warmup(0L);
                mCustomTabsSession[0] = mClient[0].newSession(null);
            }
            
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mClient[0] = null;
            }
        };
        
        CustomTabsClient.bindCustomTabsService(context, "com.android.chrome",
                                               mCustomTabsServiceConnection);
        customTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession[0])
                .setToolbarColor(ThemeUtils.darkOrLight(context, R.color.dark_theme_primary,
                                                        R.color.light_theme_primary))
                .setShowTitle(true)
                .build();
        
        try {
            customTabsIntent.launchUrl(context, Uri.parse(link));
        } catch (Exception ex) {
            openLink(context, link);
        }
    }
    
    public static String getStringFromResources(Context context, int id) {
        return context.getResources().getString(id);
    }
    
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable
                    .getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    
    @SuppressWarnings("ConstantConditions")
    public static Bitmap getWidgetPreview(@NonNull Bitmap bitmap, @ColorInt int colorToReplace) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        
        int minX = width;
        int minY = height;
        int maxX = -1;
        int maxY = -1;
        
        Bitmap newBitmap = Bitmap.createBitmap(width, height, bitmap.getConfig());
        int pixel;
        
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int index = y * width + x;
                pixel = pixels[index];
                if (pixel == colorToReplace) {
                    pixels[index] = android.graphics.Color.TRANSPARENT;
                }
                if (pixels[index] != android.graphics.Color.TRANSPARENT) {
                    if (x < minX)
                        minX = x;
                    if (x > maxX)
                        maxX = x;
                    if (y < minY)
                        minY = y;
                    if (y > maxY)
                        maxY = y;
                }
            }
        }
        
        newBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        
        return Bitmap.createBitmap(newBitmap, minX, minY, (maxX - minX) + 1, (maxY - minY) + 1);
    }
    
    /**
     * Methods for tasks
     */
    
    public static void copyFiles(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[2048];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush();
    }
    
    public static String getFilenameWithoutExtension(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static int clean(File file) {
        if (!file.exists()) return 0;
        int count = 0;
        if (file.isDirectory()) {
            File[] folderContent = file.listFiles();
            if (folderContent != null && folderContent.length > 0) {
                for (File fileInFolder : folderContent) {
                    count += clean(fileInFolder);
                }
            }
        }
        file.delete();
        return count;
    }
    
    @SuppressWarnings("SameParameterValue")
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    public static int getNavigationBarHeight(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = activity.getResources().getBoolean(R.bool.isTablet)
                               ? metrics.heightPixels :
                               activity.getResources().getConfiguration().orientation ==
                                       Configuration
                                               .ORIENTATION_LANDSCAPE ? metrics.widthPixels
                                                                      : metrics.heightPixels;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = activity.getResources().getBoolean(R.bool.isTablet)
                             ? metrics.heightPixels :
                             activity.getResources().getConfiguration().orientation == Configuration
                                     .ORIENTATION_LANDSCAPE ? metrics.widthPixels
                                                            : metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }
    
    public static int convertMinutesToMillis(int minute) {
        return minute * 60 * 1000;
    }
    
    public static int convertMillisToMinutes(int millis) {
        return millis / 60 / 1000;
    }
    
    public static boolean isNewVersion(Context context) {
        Preferences mPrefs = new Preferences(context);
        int prevVersionCode = mPrefs.getVersionCode();
        int curVersionCode = getAppCurrentVersionCode(context);
        if ((curVersionCode > prevVersionCode) && (curVersionCode > -1)) {
            mPrefs.setVersionCode(curVersionCode);
            return true;
        }
        return false;
    }
    
    public static String getAppInstaller(Context context) {
        return context.getPackageManager().getInstallerPackageName(context.getPackageName());
    }
    
    private static int getAppCurrentVersionCode(Context context) {
        try {
            PackageInfo packageInfo =
                    context.getPackageManager().getPackageInfo(context.getPackageName(),
                                                               0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e("Unable to get version code. Reason:", e.getLocalizedMessage());
            return -1;
        }
    }
    
}