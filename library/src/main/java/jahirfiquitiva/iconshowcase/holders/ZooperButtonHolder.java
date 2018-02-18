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
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.utilities.utils.IconUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.Utils;
import jahirfiquitiva.iconshowcase.views.DebouncedClickListener;

public class ZooperButtonHolder extends RecyclerView.ViewHolder {

    private final ImageView icon;
    private final TextView text;
    private final OnZooperButtonClickListener listener;
    private int buttonId = -1;

    public ZooperButtonHolder(View itemView, OnZooperButtonClickListener nListener) {
        super(itemView);
        CardView card = (CardView) itemView.findViewById(R.id.zooper_btn_card);
        icon = (ImageView) itemView.findViewById(R.id.zooper_btn_icon);
        text = (TextView) itemView.findViewById(R.id.zooper_btn_title);
        listener = nListener;
        if (card != null)
            card.setOnClickListener(new DebouncedClickListener() {
                @Override
                public void onDebouncedClick(View v) {
                    if (listener != null)
                        listener.onClick(buttonId);
                }
            });
    }

    public void setItem(Context context, int id) {
        buttonId = id;
        if (id < 0 || id > 1) return;
        if (icon != null)
            icon.setImageDrawable(IconUtils.getTintedDrawable(context, id == 0 ?
                    "ic_store_download" : "ic_assets"));
        if (text != null)
            text.setText(Utils.getStringFromResources(context, id == 0 ? R.string.install_apps : R
                    .string.install_assets));
    }

    public interface OnZooperButtonClickListener {
        void onClick(int position);
    }

}