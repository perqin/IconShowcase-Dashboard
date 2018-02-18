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
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;

import java.util.Locale;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.models.LauncherItem;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.color.ColorUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.IconUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.ThemeUtils;
import jahirfiquitiva.iconshowcase.views.DebouncedClickListener;

public class LauncherHolder extends RecyclerView.ViewHolder {

    private final ImageView icon;
    private final TextView launcherName;
    private final LinearLayout itemBG;
    private final OnLauncherClickListener listener;
    private LauncherItem launcher;

    public LauncherHolder(View view, OnLauncherClickListener nListener) {
        super(view);
        itemBG = (LinearLayout) view.findViewById(R.id.itemBG);
        icon = (ImageView) view.findViewById(R.id.launcherIcon);
        launcherName = (TextView) view.findViewById(R.id.launcherName);
        listener = nListener;
        view.setOnClickListener(new DebouncedClickListener() {
            @Override
            public void onDebouncedClick(View v) {
                if (listener != null)
                    listener.onLauncherClick(launcher);
            }
        });
    }

    public void setItem(Context context, LauncherItem item) {
        this.launcher = item;

        String iconName = "ic_" + launcher.getName().toLowerCase().replace(" ", "_");
        int iconResource = IconUtils.getIconResId(context.getResources(), context.getPackageName(),
                iconName);

        Preferences mPrefs = new Preferences(context);
        if (mPrefs.getAnimationsEnabled()) {
            Glide.with(context)
                    .load(iconResource != 0 ? iconResource : IconUtils.getIconResId(context
                            .getResources(), context.getPackageName(), "ic_na_launcher"))
                    .priority(Priority.IMMEDIATE)
                    .into(icon);
        } else {
            Glide.with(context)
                    .load(iconResource != 0 ? iconResource : IconUtils.getIconResId(context
                            .getResources(), context.getPackageName(), "ic_na_launcher"))
                    .priority(Priority.IMMEDIATE)
                    .dontAnimate()
                    .into(icon);
        }

        launcherName.setText(launcher.getName().toUpperCase(Locale.getDefault()));

        if (launcher.isInstalled(context)) {
            icon.setColorFilter(null);
            itemBG.setBackgroundColor(launcher.getColor());
            launcherName.setTextColor(ColorUtils.getMaterialPrimaryTextColor(
                    !(ColorUtils.isLightColor(launcher.getColor()))));
        } else {
            int bgColor = ThemeUtils.darkLightOrTransparent(context,
                    R.color.card_dark_background, R.color.card_light_background,
                    R.color.card_clear_background);
            icon.setColorFilter(bnwFilter());
            itemBG.setBackgroundColor(bgColor);
            launcherName.setTextColor(ColorUtils.getMaterialPrimaryTextColor(
                    !(ColorUtils.isLightColor(bgColor))));
        }
    }

    private ColorFilter bnwFilter() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        return new ColorMatrixColorFilter(matrix);
    }

    public interface OnLauncherClickListener {
        void onLauncherClick(LauncherItem item);
    }

}