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

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.pitchedapps.butler.iconrequest.App;
import com.pitchedapps.butler.iconrequest.IconRequest;

import jahirfiquitiva.iconshowcase.R;

public class RequestHolder extends RecyclerView.ViewHolder {

    private final ImageView imgIcon;
    private final TextView txtName;
    private final AppCompatCheckBox checkBox;
    private final OnAppClickListener listener;
    private App item;

    public RequestHolder(View v, OnAppClickListener nListener) {
        super(v);
        imgIcon = (ImageView) v.findViewById(R.id.imgIcon);
        txtName = (TextView) v.findViewById(R.id.txtName);
        checkBox = (AppCompatCheckBox) v.findViewById(R.id.chkSelected);
        listener = nListener;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onClick(checkBox, item);
            }
        });
    }

    public void setItem(App app) {
        item = app;
        app.loadIcon(imgIcon, Priority.IMMEDIATE);
        txtName.setText(app.getName());
        final IconRequest ir = IconRequest.get();
        itemView.setActivated(ir != null && ir.isAppSelected(app));
        checkBox.setChecked(ir != null && ir.isAppSelected(app));
    }

    public interface OnAppClickListener {
        void onClick(AppCompatCheckBox checkBox, App item);
    }
}