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
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;

import java.io.File;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.models.ZooperWidget;
import jahirfiquitiva.iconshowcase.utilities.Preferences;

public class ZooperWidgetHolder extends RecyclerView.ViewHolder {

    private final ImageView background;
    private final ImageView widget;
    private final Drawable wallpaper;

    public ZooperWidgetHolder(View itemView, Drawable nWallpaper) {
        super(itemView);
        background = (ImageView) itemView.findViewById(R.id.wall);
        widget = (ImageView) itemView.findViewById(R.id.preview);
        wallpaper = nWallpaper;
    }

    public void setItem(Context context, ZooperWidget item) {
        if (background != null && wallpaper != null)
            background.setImageDrawable(wallpaper);
        if (widget != null) {
            Preferences mPrefs = new Preferences(context);
            if (mPrefs.getAnimationsEnabled()) {
                Glide.with(context)
                        .load(new File(item.getPreviewPath()))
                        .priority(Priority.IMMEDIATE)
                        .into(widget);
            } else {
                Glide.with(context)
                        .load(new File(item.getPreviewPath()))
                        .priority(Priority.IMMEDIATE)
                        .dontAnimate()
                        .into(widget);
            }
        }
    }

}