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

package jahirfiquitiva.iconshowcase.holders;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Vibrator;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.models.WallpaperItem;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.color.ColorUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.ThemeUtils;

public class WallpaperHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnLongClickListener {

    private final View view;
    private final ImageView wall;
    private final TextView name;
    private final TextView authorName;
    private final LinearLayout titleBg;
    private final OnWallpaperClickListener listener;
    private WallpaperItem item;
    private int lastPosition = -1;
    private boolean clickable = true;

    public WallpaperHolder(View v, OnWallpaperClickListener mListener) {
        super(v);
        listener = mListener;
        view = v;
        wall = (ImageView) view.findViewById(R.id.wall);
        name = (TextView) view.findViewById(R.id.name);
        authorName = (TextView) view.findViewById(R.id.author);
        titleBg = (LinearLayout) view.findViewById(R.id.titleBg);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        titleBg.setBackgroundColor(
                ColorUtils.changeAlpha(ThemeUtils.darkOrLight(view.getContext(),
                        R.color.card_light_background, R.color.card_dark_background), 0.65f));
        name.setTextColor(ColorUtils.getMaterialPrimaryTextColor(!ThemeUtils.isDarkTheme()));
        authorName.setTextColor(ColorUtils.getMaterialSecondaryTextColor(
                !ThemeUtils.isDarkTheme()));
    }

    public void setItem(WallpaperItem item) {
        this.item = item;
        ViewCompat.setTransitionName(wall, "transition" + getAdapterPosition());
        name.setText(item.getWallName());
        authorName.setText(item.getWallAuthor());

        final String wallURL = item.getWallURL();
        final String wallThumbURL = item.getWallThumbURL();
        final Preferences mPrefs = new Preferences(view.getContext());

        BitmapImageViewTarget target = new BitmapImageViewTarget(wall) {
            @Override
            protected void setResource(Bitmap bitmap) {
                Palette.Swatch wallSwatch = ColorUtils.getPaletteSwatch(bitmap);
                if (mPrefs.getAnimationsEnabled() && (getAdapterPosition() > lastPosition)) {
                    wall.setAlpha(0f);
                    titleBg.setAlpha(0f);
                    wall.setImageBitmap(bitmap);
                    if (wallSwatch != null) setColors(wallSwatch.getRgb());
                    wall.animate().setDuration(250).alpha(1f).start();
                    titleBg.animate().setDuration(250).alpha(1f).start();
                    lastPosition = getAdapterPosition();
                } else {
                    wall.setImageBitmap(bitmap);
                    if (wallSwatch != null) setColors(wallSwatch.getRgb());
                }
            }
        };

        //TODO: Find a way to simplify the code when animations are disabled.
        if (!(wallThumbURL.equals("null"))) {
            if (mPrefs.getAnimationsEnabled()) {
                Glide.with(view.getContext())
                        .load(wallURL)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .priority(Priority.HIGH)
                        .thumbnail(
                                Glide.with(view.getContext())
                                        .load(wallThumbURL)
                                        .asBitmap()
                                        .priority(Priority.IMMEDIATE)
                                        .thumbnail(0.3f))
                        .into(target);
            } else {
                Glide.with(view.getContext())
                        .load(wallURL)
                        .asBitmap()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .priority(Priority.HIGH)
                        .thumbnail(
                                Glide.with(view.getContext())
                                        .load(wallThumbURL)
                                        .asBitmap()
                                        .priority(Priority.IMMEDIATE)
                                        .thumbnail(0.3f))
                        .into(target);
            }
        } else {
            if (mPrefs.getAnimationsEnabled()) {
                Glide.with(view.getContext())
                        .load(wallURL)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .priority(Priority.HIGH)
                        .thumbnail(0.5f)
                        .into(target);
            } else {
                Glide.with(view.getContext())
                        .load(wallURL)
                        .asBitmap()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .priority(Priority.HIGH)
                        .thumbnail(0.5f)
                        .into(target);
            }
        }
    }

    private void setColors(int color) {
        if (titleBg != null && color != 0) {
            titleBg.setBackgroundColor(color);
            if (name != null) {
                name.setTextColor(ColorUtils.getMaterialPrimaryTextColor(
                        !ColorUtils.isLightColor(color)));
            }
            if (authorName != null) {
                authorName.setTextColor(ColorUtils.getMaterialSecondaryTextColor(
                        !ColorUtils.isLightColor(color)));
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (clickable) {
            clickable = false;
            if (listener != null)
                listener.onSimpleClick(wall, item);
            reset();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (clickable) {
            clickable = false;
            Vibrator vibrator = (Vibrator) v.getContext().getSystemService(Context
                    .VIBRATOR_SERVICE);
            vibrator.vibrate(30);
            if (listener != null)
                listener.onLongClick(view.getContext(), item);
            reset(); //TODO shouldn't all clicks be paused when applying?
        }
        return false;
    }

    private void reset() {
        clickable = true;
    }

    public interface OnWallpaperClickListener {
        void onSimpleClick(ImageView wall, WallpaperItem item);

        void onLongClick(Context context, WallpaperItem item);
    }

}