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

package jahirfiquitiva.iconshowcase.activities.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.dialogs.ISDialogs;
import jahirfiquitiva.iconshowcase.models.WallpaperItem;
import jahirfiquitiva.iconshowcase.tasks.ApplyWallpaper;
import jahirfiquitiva.iconshowcase.tasks.WallpaperToCrop;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.color.ColorUtils;
import jahirfiquitiva.iconshowcase.utilities.color.ToolbarColorizer;
import jahirfiquitiva.iconshowcase.utilities.utils.PermissionsUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.ThemeUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.Utils;

@SuppressLint("Registered")
public class BaseWallpaperViewerActivity extends AppCompatActivity {

    private static final int NAV_BAR_VISIBILITY_CHANGE_DELAY = 2000;
    private boolean isFullScreen = false;
    private String transitionName = "";
    private MaterialDialog dialogApply;
    private MaterialDialog downloadDialog;
    private WallpaperItem item;
    private Preferences mPrefs;
    private ViewGroup layout;
    private File downloadsFolder;
    private WallpaperDialogsCallback callback;
    private WallpaperToCrop cropTask;
    private View toHide1;
    private View toHide2;
    private Timer mTimer;
    private boolean shouldShowNavBar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.onActivityCreateSetTheme(this);
        if (isFullScreen) {
            setupFullScreen();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            makeStatusBarIconsWhite();
        }
        super.onCreate(savedInstanceState);

        mPrefs = new Preferences(this);
        Intent intent = getIntent();
        transitionName = intent.getStringExtra("transitionName");
        item = intent.getParcelableExtra("item");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFullScreen)
            makeStatusBarIconsWhite();
        ProgressBar spinner = (ProgressBar) findViewById(R.id.progress);
        if (spinner != null) spinner.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        if (dialogApply != null) {
            dialogApply.dismiss();
            dialogApply = null;
        }
        if (downloadDialog != null) {
            downloadDialog.dismiss();
            downloadDialog = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        closeViewer();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                closeViewer();
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResult) {
        if (requestCode == PermissionsUtils.PERMISSION_REQUEST_CODE) {
            if (grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                if (PermissionsUtils.getViewerActivityAction().equals("crop")) {
                    cropWallpaper(this);
                } else if (PermissionsUtils.getViewerActivityAction().equals("save")) {
                    runWallpaperSave(this);
                }
            } else {
                ISDialogs.showPermissionNotGrantedDialog(this);
            }
        }
    }

    private Handler handler(Context context) {
        return new Handler(context.getMainLooper());
    }

    private void runOnUIThread(Context context, Runnable r) {
        handler(context).post(r);
    }

    protected void setFullScreen(boolean fullScreen) {
        this.isFullScreen = fullScreen;
    }

    public void setDialogApply(MaterialDialog dialog) {
        this.dialogApply = dialog;
    }

    protected void setCallback(WallpaperDialogsCallback callback) {
        this.callback = callback;
    }

    protected void setViewsToHide(View toHide1, View toHide2) {
        this.toHide1 = toHide1;
        this.toHide2 = toHide2;
    }

    protected ViewGroup getLayout() {
        return layout;
    }

    protected void setLayout(ViewGroup layout) {
        this.layout = layout;
    }

    protected String getTransitionName() {
        return transitionName;
    }

    protected WallpaperItem getItem() {
        return item;
    }

    protected Preferences getPrefs() {
        return mPrefs;
    }

    protected void closeViewer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportFinishAfterTransition();
        } else {
            finish();
        }
    }

    protected void setupFullScreen() {
        makeStatusBarIconsWhite();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void makeStatusBarIconsWhite() {
        ToolbarColorizer.clearLightStatusBar(this);
    }

    // TODO: Improve this so FABs don't get weird positions
    private void hideNavBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        // | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView()
                    .getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void showNavBar() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        // | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    protected void navBarVisibilityChange() {
        shouldShowNavBar = !shouldShowNavBar;
        if (shouldShowNavBar) {
            showNavBar();
            if (mTimer != null) {
                mTimer.cancel();
                mTimer.purge();
            }
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    shouldShowNavBar = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideNavBar();
                        }
                    });
                }
            }, NAV_BAR_VISIBILITY_CHANGE_DELAY);
        } else hideNavBar();
    }


    protected void runWallpaperSave(Context context) {
        if (Utils.hasNetwork(context)) {
            saveWallpaperAction(context);
        } else {
            showNotConnectedSnackBar(context);
        }
    }

    private void saveWallpaperAction(final Context context) {
        if (downloadDialog != null) {
            downloadDialog.dismiss();
        }

        if (callback != null) {
            callback.onSaveAction();
        }

        final boolean[] enteredDownloadTask = {false};

        downloadDialog = new MaterialDialog.Builder(context)
                .content(R.string.downloading_wallpaper)
                .progress(true, 0)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction
                            which) {
                        if (downloadDialog != null) {
                            downloadDialog.dismiss();
                        }
                    }
                })
                .show();

        Glide.with(context)
                .load(item.getWallURL())
                .asBitmap()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap>
                            glideAnimation) {
                        if (resource != null && downloadDialog.isShowing()) {
                            enteredDownloadTask[0] = true;
                            try {
                                saveWallpaper(((Activity) context), item.getWallName(),
                                        downloadDialog, resource);
                            } catch (Exception e) {
                                // Do nothing
                            }
                        }
                    }
                });

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUIThread(context, new Runnable() {
                    @Override
                    public void run() {
                        if (!enteredDownloadTask[0]) {
                            String newContent = context.getString(R.string.downloading_wallpaper)
                                    + "\n"
                                    + context.getString(R.string.download_takes_longer);
                            downloadDialog.setContent(newContent);
                            downloadDialog.setActionButton(DialogAction.POSITIVE, android.R
                                    .string.cancel);
                        }
                    }
                });
            }
        }, 10000);
    }

    private void saveWallpaper(final Activity context, final String wallName,
                               final MaterialDialog downloadDialog, final Bitmap result) {
        downloadDialog.setContent(context.getString(R.string.saving_wallpaper));
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mPrefs.getDownloadsFolder() != null) {
                    downloadsFolder = new File(mPrefs.getDownloadsFolder());
                } else {
                    downloadsFolder = new File(context.getString(R.string.walls_save_location,
                            Environment.getExternalStorageDirectory().getAbsolutePath()));
                }
                //noinspection ResultOfMethodCallIgnored
                downloadsFolder.mkdirs();
                final File destFile = new File(downloadsFolder, wallName + ".png");

                String snackbarText;
                if (!destFile.exists()) {
                    try {
                        FileOutputStream fos = new FileOutputStream(destFile);
                        result.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        snackbarText = context.getString(R.string.wallpaper_downloaded,
                                destFile.getAbsolutePath());
                        fos.close();
                    } catch (final Exception e) {
                        snackbarText = context.getString(R.string.error);
                    }
                } else {
                    snackbarText = context.getString(R.string.wallpaper_downloaded,
                            destFile.getAbsolutePath());
                }

                final String finalSnackbarText = snackbarText;
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloadDialog.dismiss();

                        if (callback != null) {
                            callback.onDialogShown();
                        }

                        if (isFullScreen) {
                            Snackbar longSnackbar = Utils.snackbar(context, layout,
                                    finalSnackbarText, Snackbar.LENGTH_LONG);
                            ViewGroup snackbarView = (ViewGroup) longSnackbar.getView();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                snackbarView.setPadding(snackbarView.getPaddingLeft(),
                                        snackbarView.getPaddingTop(), snackbarView
                                                .getPaddingRight(),
                                        Utils.getNavigationBarHeight(BaseWallpaperViewerActivity
                                                .this));
                            }
                            longSnackbar.addCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar snackbar, int event) {
                                    super.onDismissed(snackbar, event);
                                    if (callback != null) {
                                        callback.onDialogDismissed();
                                    }
                                }
                            });
                            longSnackbar.show();
                        } else {
                            Utils.snackbar(context, layout, finalSnackbarText,
                                    Snackbar.LENGTH_LONG).addCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar snackbar, int event) {
                                    super.onDismissed(snackbar, event);
                                    if (callback != null) {
                                        callback.onDialogDismissed();
                                    }
                                }
                            }).show();
                        }
                    }
                });
            }
        }).start();
    }

    protected void showApplyWallpaperDialog(final Context context) {
        if (callback != null) {
            callback.onDialogShown();
        }
        ISDialogs.showApplyWallpaperDialog(this,
                new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull
                            DialogAction dialogAction) {
                        onApplyWallpaperClick(context);
                    }
                }, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull
                            DialogAction dialogAction) {
                        PermissionsUtils.checkPermission(context, Manifest.permission
                                        .WRITE_EXTERNAL_STORAGE,
                                new PermissionsUtils.PermissionRequestListener() {
                                    @Override
                                    public void onPermissionRequest() {
                                        PermissionsUtils.setViewerActivityAction("crop");
                                        try {
                                            PermissionsUtils.requestStoragePermission((Activity)
                                                    context);
                                        } catch (Exception e) {
                                            // Do nothing
                                        }
                                    }

                                    @Override
                                    public void onPermissionDenied() {
                                        ISDialogs.showPermissionNotGrantedDialog(context);
                                    }

                                    @Override
                                    public void onPermissionCompletelyDenied() {
                                        ISDialogs.showPermissionNotGrantedDialog(context);
                                    }

                                    @Override
                                    public void onPermissionGranted() {
                                        cropWallpaper(context);
                                    }
                                });
                    }
                }, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction
                            which) {
                        if (callback != null) {
                            callback.onDialogDismissed();
                        }
                    }
                },
                new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (callback != null) {
                            callback.onDialogDismissed();
                        }
                    }
                });
        // WallpaperDialog.show(this, wallUrl);
    }

    private void onApplyWallpaperClick(final Context context) {
        if (dialogApply != null) {
            dialogApply.dismiss();
        }

        if (callback != null) {
            callback.onDialogShown();
        }

        final boolean[] enteredApplyTask = {false};

        dialogApply = new MaterialDialog.Builder(context)
                .content(R.string.downloading_wallpaper)
                .progress(true, 0)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull
                            DialogAction which) {
                        try {
                            getSupportLoaderManager().getLoader(1).cancelLoad();
                            getSupportLoaderManager().destroyLoader(1);
                        } catch (Exception ignored) {
                        }
                        dialogApply.dismiss();

                        if (callback != null) {
                            callback.onDialogDismissed();
                        }
                    }
                })
                .show();

        Glide.with(context)
                .load(item.getWallURL())
                .asBitmap()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(
                            final Bitmap resource,
                            GlideAnimation<? super Bitmap> glideAnimation) {
                        if (resource != null && dialogApply.isShowing()) {
                            enteredApplyTask[0] = true;
                            if (dialogApply != null) {
                                dialogApply.dismiss();
                            }
                            showWallpaperApplyOptionsDialogAndExecuteTask(context, resource);
                        }
                    }
                });

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUIThread(context, new Runnable() {
                    @Override
                    public void run() {
                        if (!enteredApplyTask[0]) {
                            String newContent = context.getString(R.string
                                    .downloading_wallpaper)
                                    + "\n"
                                    + context.getString(R.string
                                    .download_takes_longer);
                            dialogApply.setContent(newContent);
                            dialogApply.setActionButton(DialogAction.POSITIVE,
                                    android.R.string.cancel);
                        }
                    }
                });
            }
        }, 10000);
    }

    private void showWallpaperApplyOptionsDialogAndExecuteTask(final Context context,
                                                               final Bitmap resource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dialogApply = new MaterialDialog.Builder(context)
                    .title(R.string.set_wall_to)
                    .listSelector(android.R.color.transparent)
                    .items(R.array.wall_options)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View itemView, int position,
                                                CharSequence text) {
                            dialog.dismiss();

                            if (dialogApply != null) {
                                dialogApply.dismiss();
                            }

                            String extra = "";

                            switch (position) {
                                case 0:
                                    extra = context.getResources().getString(R.string.home_screen);
                                    break;
                                case 1:
                                    extra = context.getResources().getString(R.string.lock_screen);
                                    break;
                                case 2:
                                    extra = context.getResources().getString(R.string
                                            .home_lock_screens);
                                    break;
                            }

                            dialogApply = new MaterialDialog.Builder(context)
                                    .content(context.getResources().getString(
                                            R.string.setting_wall_title, extra.toLowerCase()))
                                    .progress(true, 0)
                                    .cancelable(false)
                                    .show();

                            executeApplyTask(getApplyCallback(), resource, null, position == 0,
                                    position == 1, position == 2);
                        }
                    })
                    .show();
        } else {
            dialogApply = new MaterialDialog.Builder(context)
                    .content(R.string.setting_wall_title)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
            executeApplyTask(getApplyCallback(), resource, null, false, false, true);
        }
    }

    private void showWallpaperAppliedDialog() {
        if (dialogApply != null) {
            dialogApply.dismiss();
        }

        dialogApply = new MaterialDialog.Builder(this)
                .content(R.string.set_as_wall_done)
                .positiveText(android.R.string.ok)
                .show();

        dialogApply.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (callback != null) {
                    callback.onDialogDismissed();
                }
            }
        });
    }

    protected void showNotConnectedSnackBar(Context context) {
        Utils.snackbar(context, layout, getString(R.string.no_conn_title),
                Snackbar.LENGTH_LONG).show();
    }

    private void cropWallpaper(final Context context) {
        if (dialogApply != null) {
            dialogApply.dismiss();
        }

        final boolean[] enteredCropTask = {false};

        dialogApply = new MaterialDialog.Builder(context)
                .content(R.string.downloading_wallpaper)
                .progress(true, 0)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction
                            which) {
                        if (cropTask != null) {
                            cropTask.cancel(true);
                        }
                        dialogApply.dismiss();
                    }
                })
                .show();

        Glide.with(context)
                .load(item.getWallURL())
                .asBitmap()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(final Bitmap resource, GlideAnimation<? super
                            Bitmap> glideAnimation) {
                        if (resource != null && dialogApply.isShowing()) {
                            enteredCropTask[0] = true;
                            if (dialogApply != null) {
                                dialogApply.dismiss();
                            }

                            dialogApply = new MaterialDialog.Builder(context)
                                    .content(context.getString(R.string.preparing_wallpaper))
                                    .progress(true, 0)
                                    .cancelable(false)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog,
                                                            @NonNull DialogAction which) {
                                            if (cropTask != null) {
                                                cropTask.cancel(true);
                                            }
                                            dialogApply.dismiss();
                                        }
                                    })
                                    .show();

                            cropTask = new WallpaperToCrop((Activity) context, dialogApply,
                                    resource, layout, item.getWallName(), toHide1, toHide2);
                            cropTask.execute();
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUIThread(context, new Runnable() {
                                        @Override
                                        public void run() {
                                            String content = context.getString(R.string
                                                    .preparing_wallpaper)
                                                    + "\n" + context.getString(R.string
                                                    .download_takes_longer);

                                            dialogApply.setContent(content);
                                            dialogApply.setActionButton(DialogAction.POSITIVE,
                                                    android.R.string.cancel);
                                        }
                                    });
                                }
                            }, 7000);
                        }
                    }
                });

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUIThread(context, new Runnable() {
                    @Override
                    public void run() {
                        if (!enteredCropTask[0]) {
                            String newContent = context.getString(R.string.downloading_wallpaper)
                                    + "\n"
                                    + context.getString(R.string.download_takes_longer);
                            dialogApply.setContent(newContent);
                            dialogApply.setActionButton(DialogAction.POSITIVE, android.R.string
                                    .cancel);
                        }
                    }
                });
            }
        }, 10000);
    }

    public void executeApplyTask(final ApplyWallpaper.ApplyWallpaperCallback callback,
                                 final Bitmap resource, final String url,
                                 final boolean setToHomeScreen, final boolean setToLockScreen,
                                 final boolean setToBoth) {
        final Context c = this;
        try {
            getSupportLoaderManager().getLoader(1).cancelLoad();
            getSupportLoaderManager().destroyLoader(1);
        } catch (Exception ignored) {
        }
        if (callback != null) callback.onPreExecute(this);
        getSupportLoaderManager().initLoader(1, null,
                new LoaderManager.LoaderCallbacks<Boolean>() {
                    @Override
                    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
                        return new ApplyWallpaper(c, resource, url, setToHomeScreen,
                                setToLockScreen, setToBoth);
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void onLoadFinished(Loader<Boolean> loader, Boolean success) {
                        if (callback != null) {
                            if (success) callback.onSuccess();
                            else callback.onError();
                        }
                    }

                    @Override
                    public void onLoaderReset(Loader<Boolean> loader) {
                        // Do nothing
                    }
                });
    }

    protected ApplyWallpaper.ApplyWallpaperCallback getApplyCallback() {
        return new ApplyWallpaper.ApplyWallpaperCallback() {
            @Override
            public void onPreExecute(Context context) {

            }

            @Override
            public void onSuccess() {
                showWallpaperAppliedDialog();
            }

            @Override
            public void onError() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dialogApply != null) {
                            dialogApply.dismiss();
                        }
                        dialogApply = new MaterialDialog.Builder(getBaseContext())
                                .content(R.string.error)
                                .positiveText(android.R.string.ok)
                                .show();
                    }
                });
            }
        };
    }

    public void setupPicture(ImageView mPhoto) {
        Bitmap bmp = null;
        String filename = getIntent().getStringExtra("image");
        try {
            if (filename != null) {
                FileInputStream is = openFileInput(filename);
                bmp = BitmapFactory.decodeStream(is);
                is.close();
            } else {
                bmp = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int colorFromCachedPic;

        if (bmp != null) {
            colorFromCachedPic = ColorUtils.getPaletteSwatch(bmp).getBodyTextColor();
        } else {
            colorFromCachedPic = ColorUtils.getMaterialPrimaryTextColor(ThemeUtils.isDarkTheme());
        }

        final ProgressBar spinner = (ProgressBar) findViewById(R.id.progress);
        spinner.getIndeterminateDrawable()
                .setColorFilter(colorFromCachedPic, PorterDuff.Mode.SRC_IN);

        Drawable d;
        if (bmp != null) {
            d = new GlideBitmapDrawable(getResources(), bmp);
        } else {
            d = new ColorDrawable(ContextCompat.getColor(this, android.R.color.transparent));
        }

        if (getPrefs().getAnimationsEnabled()) {
            Glide.with(this)
                    .load(getItem().getWallURL())
                    .placeholder(d)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .fitCenter()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model,
                                                   Target<GlideDrawable> target, boolean
                                                           isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model,
                                                       Target<GlideDrawable> target, boolean
                                                               isFromMemoryCache, boolean
                                                               isFirstResource) {
                            spinner.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(mPhoto);
        } else {
            Glide.with(this)
                    .load(getItem().getWallURL())
                    .placeholder(d)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .fitCenter()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model,
                                                   Target<GlideDrawable> target, boolean
                                                           isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model,
                                                       Target<GlideDrawable> target, boolean
                                                               isFromMemoryCache, boolean
                                                               isFirstResource) {
                            spinner.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(mPhoto);
        }
    }

    public abstract class WallpaperDialogsCallback {
        public void onSaveAction() {

        }

        public abstract void onDialogShown();

        public abstract void onDialogDismissed();
    }

}