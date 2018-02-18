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

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.models.IconItem;

public class IconHolder extends RecyclerView.ViewHolder {

    private final View view;
    private final ImageView icon;
    private final OnIconClickListener listener;
    private IconItem item;
    private int lastPosition = 0;

    public IconHolder(View v, OnIconClickListener nListener) {
        super(v);
        view = v;
        listener = nListener;
        icon = (ImageView) v.findViewById(R.id.icon_img);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onIconClick(item);
            }
        });
    }

    public void setItem(IconItem item, final boolean animate) {
        this.item = item;
        if (item.getResId() == 0) return;
        Glide.with(view.getContext())
                .load(item.getResId())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .priority(Priority.IMMEDIATE)
                .into(new BitmapImageViewTarget(icon) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        if (animate && (getAdapterPosition() > lastPosition)) {
                            icon.setAlpha(0f);
                            icon.setImageBitmap(resource);
                            icon.animate().setDuration(250).alpha(1f).start();
                            lastPosition = getAdapterPosition();
                        } else {
                            icon.setImageBitmap(resource);
                            clearAnimation();
                        }
                    }
                });
    }

    public void clearAnimation() {
        if (view != null) view.clearAnimation();
        if (icon != null) icon.clearAnimation();
    }

    public interface OnIconClickListener {
        void onIconClick(IconItem item);
    }

}