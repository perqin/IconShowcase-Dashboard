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

package jahirfiquitiva.iconshowcase.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.AltWallpaperViewerActivity;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.activities.WallpaperViewerActivity;
import jahirfiquitiva.iconshowcase.config.Config;
import jahirfiquitiva.iconshowcase.holders.WallpaperHolder;
import jahirfiquitiva.iconshowcase.models.WallpaperItem;
import jahirfiquitiva.iconshowcase.tasks.ApplyWallpaper;
import jahirfiquitiva.iconshowcase.utilities.utils.Utils;
import timber.log.Timber;

public class WallpapersAdapter extends RecyclerView.Adapter<WallpaperHolder> {

    private final FragmentActivity activity;
    private final ArrayList<WallpaperItem> wallsList;
    private MaterialDialog applyDialog;

    public WallpapersAdapter(FragmentActivity activity, ArrayList<WallpaperItem> wallsList) {
        this.activity = activity;
        this.wallsList = wallsList;
    }

    @Override
    public WallpaperHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WallpaperHolder(LayoutInflater.from(
                parent.getContext()).inflate(R.layout.item_wallpaper, parent, false),
                new WallpaperHolder.OnWallpaperClickListener() {
                    @Override
                    public void onSimpleClick(ImageView wall, WallpaperItem item) {
                        onWallClick(wall, item);
                    }

                    @Override
                    public void onLongClick(Context context, WallpaperItem item) {
                        showApplyWallpaperDialog(context, item);
                    }
                });
    }

    @Override
    public void onBindViewHolder(final WallpaperHolder holder, int position) {
        holder.setItem(wallsList.get(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return wallsList != null ? wallsList.size() : 0;
    }

    private void showApplyWallpaperDialog(final Context context, final WallpaperItem item) {
        new MaterialDialog.Builder(context)
                .title(R.string.apply)
                .content(R.string.confirm_apply)
                .positiveText(R.string.apply)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull final
                    DialogAction dialogAction) {
                        try {
                            if (applyDialog != null) {
                                applyDialog.dismiss();
                            }

                            final boolean[] enteredApplyTask = {false};

                            applyDialog = new MaterialDialog.Builder(context)
                                    .content(R.string.downloading_wallpaper)
                                    .progress(true, 0)
                                    .cancelable(false)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull
                                                DialogAction which) {
                                            try {
                                                ((ShowcaseActivity) context)
                                                        .getSupportLoaderManager().getLoader(2)
                                                        .cancelLoad();
                                                ((ShowcaseActivity) context)
                                                        .getSupportLoaderManager().destroyLoader(2);
                                            } catch (Exception ignored) {
                                            }
                                            applyDialog.dismiss();
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
                                        public void onResourceReady(final Bitmap resource,
                                                                    GlideAnimation<? super Bitmap>
                                                                            glideAnimation) {
                                            if (resource != null && applyDialog.isShowing()) {
                                                enteredApplyTask[0] = true;

                                                if (applyDialog != null) {
                                                    applyDialog.dismiss();
                                                }
                                                showWallpaperApplyOptionsDialogAndExecuteTask
                                                        (context, resource);
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
                                                applyDialog.setContent(newContent);
                                                applyDialog.setActionButton(DialogAction.POSITIVE,
                                                        android.R.string.cancel);
                                            }
                                        }
                                    });
                                }
                            }, 10000);
                        } catch (Exception e) {
                            // Ignored
                        }
                    }
                })
                .show();
    }

    private void showWallpaperApplyOptionsDialogAndExecuteTask(final Context context,
                                                               final Bitmap resource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            applyDialog = new MaterialDialog.Builder(context)
                    .title(R.string.set_wall_to)
                    .listSelector(android.R.color.transparent)
                    .items(R.array.wall_options)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View itemView, int position,
                                                CharSequence text) {
                            dialog.dismiss();
                            if (applyDialog != null) {
                                applyDialog.dismiss();
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

                            applyDialog = new MaterialDialog.Builder(context)
                                    .content(context.getResources().getString(
                                            R.string.setting_wall_title, extra.toLowerCase()))
                                    .progress(true, 0)
                                    .cancelable(false)
                                    .show();

                            executeApplyTask(context, resource, position == 0, position == 1,
                                    position == 2);
                        }
                    })
                    .show();
        } else {
            applyDialog = new MaterialDialog.Builder(context)
                    .content(R.string.setting_wall_title)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();

            executeApplyTask(context, resource, false, false, true);
        }
    }

    private void executeApplyTask(final Context context, Bitmap resource, boolean setHome,
                                  boolean setLock, boolean setBoth) {
        ((ShowcaseActivity) context).executeApplyTask(
                new ApplyWallpaper.ApplyWallpaperCallback() {
                    @Override
                    public void onPreExecute(Context context) {
                        // Do nothing
                    }

                    @Override
                    public void onSuccess() {
                        if (applyDialog != null) {
                            applyDialog.dismiss();
                        }

                        applyDialog = new MaterialDialog.Builder(context)
                                .content(R.string.set_as_wall_done)
                                .positiveText(android.R.string.ok)
                                .show();

                        applyDialog.setOnDismissListener(
                                new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        if (((ShowcaseActivity) context).getPickerKey() ==
                                                Config.WALLS_PICKER) {
                                            ((ShowcaseActivity) context).finish();
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onError() {
                        if (applyDialog != null) {
                            applyDialog.dismiss();
                        }
                        if (((ShowcaseActivity) context).getPickerKey() == Config.WALLS_PICKER) {
                            ((ShowcaseActivity) context).finish();
                        }
                    }
                }, resource, null, setHome, setLock, setBoth);
    }

    private Handler handler(Context context) {
        return new Handler(context.getMainLooper());
    }

    private void runOnUIThread(Context context, Runnable r) {
        handler(context).post(r);
    }

    private void onWallClick(ImageView wall, WallpaperItem item) {
        if (((ShowcaseActivity) activity).getPickerKey() == Config.WALLS_PICKER) {
            showApplyWallpaperDialog(activity, item);
        } else {
            final Intent intent = new Intent(activity,
                    activity.getResources().getBoolean(R.bool.alternative_viewer) ?
                            AltWallpaperViewerActivity.class :
                            WallpaperViewerActivity.class);

            intent.putExtra("item", item);
            intent.putExtra("transitionName", ViewCompat.getTransitionName(wall));

            if (wall.getDrawable() != null) {
                Bitmap bitmap = Utils.drawableToBitmap(wall.getDrawable());

                try {
                    String filename = "temp.png";
                    FileOutputStream stream = activity.openFileOutput(filename, Context
                            .MODE_PRIVATE);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();
                    intent.putExtra("image", filename);
                } catch (Exception e) {
                    Timber.e("Error getting drawable", e.getLocalizedMessage());
                }

                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(activity, wall, ViewCompat
                                .getTransitionName(wall));
                activity.startActivity(intent, options.toBundle());
            } else {
                activity.startActivity(intent);
            }
        }
    }

}