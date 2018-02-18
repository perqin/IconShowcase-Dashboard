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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.holders.LauncherHolder;
import jahirfiquitiva.iconshowcase.models.LauncherItem;

public class LaunchersAdapter extends RecyclerView.Adapter<LauncherHolder> {

    private final Context context;
    private final List<LauncherItem> launchers;
    private final LauncherHolder.OnLauncherClickListener listener;

    public LaunchersAdapter(Context context, List<LauncherItem> launchers,
                            LauncherHolder.OnLauncherClickListener listener) {
        this.context = context;
        this.launchers = launchers;
        this.listener = listener;
    }

    @Override
    public LauncherHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new LauncherHolder(inflater.inflate(R.layout.item_launcher, parent, false),
                listener);
    }

    @Override
    public void onBindViewHolder(LauncherHolder holder, int position) {
        // Turns Launcher name "Something Pro" to "ic_something_pro"
        holder.setItem(context, launchers.get(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return launchers == null ? 0 : launchers.size();
    }

}