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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pitchedapps.butler.iconrequest.utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.utils.ThemeUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.Utils;
import timber.log.Timber;

public class WallpaperToCrop extends AsyncTask<Void, String, Boolean> {
    
    private final MaterialDialog dialog;
    private final Bitmap resource;
    private final View layout;
    private final String wallName;
    private final WeakReference<Activity> wrActivity;
    private Uri wallUri;
    private Context context;
    private View toHide1;
    private View toHide2;
    private volatile boolean wasCancelled = false;
    
    public WallpaperToCrop(Activity activity, MaterialDialog dialog, Bitmap resource,
                           View layout, String wallName, View toHide1, View toHide2) {
        this(activity, dialog, resource, layout, wallName);
        this.toHide1 = toHide1;
        this.toHide2 = toHide2;
    }
    
    private WallpaperToCrop(Activity activity, MaterialDialog dialog, Bitmap resource,
                            View layout, String wallName) {
        this.wrActivity = new WeakReference<>(activity);
        this.dialog = dialog;
        this.resource = resource;
        this.layout = layout;
        this.wallName = wallName;
    }
    
    @Override
    protected void onPreExecute() {
        final Activity a = wrActivity.get();
        if (a != null) {
            this.context = a.getApplicationContext();
        }
    }
    
    @Override
    protected Boolean doInBackground(Void... params) {
        Boolean worked = false;
        if (!wasCancelled) {
            if (wallUri != null) {
                wallUri = null;
            }
            try {
                if (resource == null) return false;
                wallUri = getImageUri(resource);
                worked = wallUri != null;
            } catch (Exception e) {
                worked = false;
            }
        }
        return worked;
    }
    
    @Override
    protected void onPostExecute(Boolean worked) {
        if (!wasCancelled) {
            if (toHide1 != null && toHide2 != null) {
                toHide1.setVisibility(View.GONE);
                toHide2.setVisibility(View.GONE);
            }
            if (worked) {
                dialog.dismiss();
                Intent setWall = new Intent(Intent.ACTION_ATTACH_DATA);
                setWall.setDataAndType(wallUri, "image/*");
                setWall.putExtra("png", "image/*");
                wrActivity.get().startActivityForResult(Intent.createChooser(setWall,
                                                                             context.getResources()
                                                                                     .getString(
                                                                                             R.string.crop_and_set_as)),
                                                        1);
            } else {
                dialog.dismiss();
                Snackbar snackbar = Snackbar.make(layout,
                                                  context.getResources().getString(R.string.error),
                                                  Snackbar.LENGTH_SHORT);
                ViewGroup snackbarView = (ViewGroup) snackbar.getView();
                snackbarView.setBackgroundColor(ThemeUtils.darkOrLight(context, R.color
                        .snackbar_light, R.color.snackbar_dark));
                snackbarView.setPadding(snackbarView.getPaddingLeft(),
                                        snackbarView.getPaddingTop(),
                                        snackbarView.getPaddingRight(),
                                        Utils.getNavigationBarHeight((Activity) context));
                snackbar.show();
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        if (toHide1 != null && toHide2 != null) {
                            toHide1.setVisibility(View.VISIBLE);
                            toHide2.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }
    }
    
    @Override
    protected void onCancelled() {
        wasCancelled = true;
    }
    
    private Uri getImageUri(Bitmap inImage) {
        Preferences mPrefs = new Preferences(context);
        File downloadsFolder;
        
        Bitmap picture;
        picture = inImage.isRecycled() ? inImage.copy(Bitmap.Config.ARGB_8888, false) : inImage;
        
        if (mPrefs.getDownloadsFolder() != null) {
            downloadsFolder = new File(mPrefs.getDownloadsFolder());
        } else {
            downloadsFolder = new File(context.getString(R.string.walls_save_location,
                                                         Environment.getExternalStorageDirectory()
                                                                 .getAbsolutePath()));
        }
        
        //noinspection ResultOfMethodCallIgnored
        downloadsFolder.mkdirs();
        
        File destFile = new File(downloadsFolder, wallName + ".png");
        
        if (!destFile.exists() && picture != null) {
            try {
                FileOutputStream fos = new FileOutputStream(destFile);
                picture.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (final Exception e) {
                Timber.e("WallpaperToCrop", e.getLocalizedMessage());
            }
        }
        return FileUtil.getUriFromFile(context, destFile);
    }
    
}