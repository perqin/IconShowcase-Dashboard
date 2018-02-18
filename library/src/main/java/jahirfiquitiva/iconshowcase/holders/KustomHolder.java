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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.SectionedViewHolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;

import java.io.File;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.config.Config;
import jahirfiquitiva.iconshowcase.utilities.Preferences;

public class KustomHolder extends SectionedViewHolder {
    private final ImageView widget;
    private final TextView sectionTitle;
    private final OnKustomItemClickListener listener;
    private int section = -1;
    private int position = -1;

    public KustomHolder(View itemView, Drawable wallpaper, OnKustomItemClickListener nListener) {
        super(itemView);
        ImageView background = (ImageView) itemView.findViewById(R.id.wall);
        widget = (ImageView) itemView.findViewById(R.id.preview);
        sectionTitle = (TextView) itemView.findViewById(R.id.kustom_section_title);
        listener = nListener;
        if (background != null)
            background.setImageDrawable(wallpaper);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onKustomItemClick(section, position);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void setSectionTitle(int section) {
        switch (section) {
            case 0:
                sectionTitle.setText("Komponents");
                break;
            case 1:
                sectionTitle.setText(Config.get().string(R.string.section_wallpapers));
                break;
            case 2:
                sectionTitle.setText("Widgets");
                break;
            default:
                sectionTitle.setText("Empty Assets");
                break;
        }
    }

    public void setItem(Context context, int section, String filePath, int relPosition) {
        this.section = section;
        this.position = relPosition;
        if (filePath != null) {
            Preferences mPrefs = new Preferences(context);
            if (mPrefs.getAnimationsEnabled()) {
                Glide.with(context)
                        .load(new File(filePath))
                        .priority(Priority.IMMEDIATE)
                        .into(widget);
            } else {
                Glide.with(context)
                        .load(new File(filePath))
                        .dontAnimate()
                        .priority(Priority.IMMEDIATE)
                        .into(widget);
            }
        }
    }

    public interface OnKustomItemClickListener {
        void onKustomItemClick(int section, int position);
    }

}