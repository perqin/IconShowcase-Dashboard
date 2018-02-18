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

package jahirfiquitiva.iconshowcase.tasks;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.DisplayMetrics;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.greenrobot.eventbus.EventBus;

import jahirfiquitiva.iconshowcase.events.WallpaperEvent;
import timber.log.Timber;

public class ApplyWallpaper extends BasicTaskLoader<Boolean> {

    private boolean setToHomeScreen;
    private boolean setToLockScreen;
    private boolean setToBoth;

    private Context context;
    private Bitmap resource;
    private String url;

    ApplyWallpaper(Context context) {
        super(context);
        this.context = context;
    }

    public ApplyWallpaper(Context context, Bitmap resource, String url, boolean setToHomeScreen,
                          boolean setToLockScreen, boolean setToBoth) {
        super(context);
        this.context = context;
        this.resource = resource;
        this.url = url;
        this.setToHomeScreen = setToHomeScreen;
        this.setToLockScreen = setToLockScreen;
        this.setToBoth = setToBoth;
    }

    @Override
    public Boolean loadInBackground() {
        if (resource != null) {
            EventBus.getDefault().post(new WallpaperEvent(url, true, WallpaperEvent.Step.APPLYING));
            return applyWallpaper(resource);
        } else if (url != null) {
            final boolean[] worked = {false};
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(context)
                            .load(url)
                            .asBitmap()
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(final Bitmap resource,
                                                            GlideAnimation<? super Bitmap>
                                                                    glideAnimation) {
                                    if (resource != null) {
                                        try {
                                            Thread.sleep(500);
                                            EventBus.getDefault().post(new WallpaperEvent(url,
                                                    true, WallpaperEvent.Step.APPLYING));
                                            worked[0] = applyWallpaper(resource);
                                        } catch (InterruptedException ignored) {
                                        }
                                    }
                                }
                            });
                }
            });
            return worked[0];
        }
        return false;
    }

    private boolean applyWallpaper(Bitmap resource) {
        WallpaperManager wm = WallpaperManager.getInstance(context);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (setToHomeScreen) {
                    wm.setBitmap(scaleToActualAspectRatio(resource), null, true,
                            WallpaperManager.FLAG_SYSTEM);
                } else if (setToLockScreen) {
                    wm.setBitmap(scaleToActualAspectRatio(resource), null, true,
                            WallpaperManager.FLAG_LOCK);
                } else if (setToBoth) {
                    wm.setBitmap(scaleToActualAspectRatio(resource), null, true);
                }
            } else {
                wm.setBitmap(scaleToActualAspectRatio(resource));
            }
            EventBus.getDefault().postSticky(new WallpaperEvent(url, true,
                    WallpaperEvent.Step.FINISH));
            return true;
        } catch (Exception ex) {
            Timber.e("Exception %s", ex.getLocalizedMessage());
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    private Bitmap scaleToActualAspectRatio(Bitmap bitmap) {
        if (bitmap != null) {
            boolean flag = true;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay()
                    .getMetrics(displayMetrics);

            int deviceWidth = displayMetrics.widthPixels;
            int deviceHeight = displayMetrics.heightPixels;

            int bitmapHeight = bitmap.getHeight();
            int bitmapWidth = bitmap.getWidth();
            if (bitmapWidth > deviceWidth) {
                flag = false;
                int scaledHeight = deviceHeight;
                int scaledWidth = (scaledHeight * bitmapWidth) / bitmapHeight;
                try {
                    if (scaledHeight > deviceHeight) { //TODO check; this is always false?
                        scaledHeight = deviceHeight;
                    }
                    bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth,
                            scaledHeight, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (flag && bitmapHeight > deviceHeight) {
                int scaledWidth = (deviceHeight * bitmapWidth) / bitmapHeight;
                try {
                    if (scaledWidth > deviceWidth)
                        scaledWidth = deviceWidth;
                    bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth,
                            deviceHeight, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    public interface ApplyWallpaperCallback {
        void onPreExecute(Context context);

        void onSuccess();

        void onError();
    }

}