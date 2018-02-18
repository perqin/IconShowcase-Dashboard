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

package jahirfiquitiva.iconshowcase.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.ArrayList;

import ca.allanwang.capsule.library.event.CFabEvent;
import ca.allanwang.capsule.library.fragments.CapsuleFragment;
import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.base.DrawerActivity;
import jahirfiquitiva.iconshowcase.adapters.IconsAdapter;
import jahirfiquitiva.iconshowcase.models.IconItem;
import jahirfiquitiva.iconshowcase.models.IconsCategory;
import jahirfiquitiva.iconshowcase.utilities.utils.IconUtils;

public class IconsFragment extends CapsuleFragment {

    private IconsAdapter mAdapter;
    private ArrayList<IconItem> iconsList, filteredIconsList;

    public static IconsFragment newInstance(IconsCategory icons) {
        IconsFragment fragment = new IconsFragment();
        Bundle args = new Bundle();
        args.putParcelable("icons", icons);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View layout = inflater.inflate(R.layout.icons_grid, container, false);

        RecyclerView iconsGrid = (RecyclerView) layout.findViewById(R.id.iconsGrid);

        iconsGrid.setHasFixedSize(true);
        iconsGrid.setLayoutManager(new GridLayoutManager(getActivity(),
                getResources().getInteger(R.integer.icons_grid_width)));

        iconsList = new ArrayList<>();

        mAdapter = new IconsAdapter(getActivity(), iconsList);

        if (getArguments() != null) {
            IconsCategory category = getArguments().getParcelable("icons");
            if (category != null) {
                iconsList = category.getIconsArray();
            }
            mAdapter.setIcons(iconsList);
        }

        iconsGrid.setAdapter(mAdapter);

        RecyclerFastScroller fastScroller = (RecyclerFastScroller) layout.findViewById(R.id
                .rvFastScroller);
        fastScroller.attachRecyclerView(iconsGrid);

        return layout;
    }

    @Override
    public int getTitleId() {
        return DrawerActivity.DrawerItem.PREVIEWS.getTitleID();
    }

    @Nullable
    @Override
    protected CFabEvent updateFab() {
        return new CFabEvent(false);
    }

    public void performSearch(String query) {
        filter(query, mAdapter);
    }

    private synchronized void filter(CharSequence s, IconsAdapter adapter) {
        if (iconsList != null && iconsList.size() > 0) {
            if (s == null || s.toString().trim().isEmpty()) {
                filteredIconsList = null;
                adapter.clearIconsList();
                adapter.setIcons(iconsList);
            } else {
                if (filteredIconsList != null) {
                    filteredIconsList.clear();
                }
                filteredIconsList = new ArrayList<>();
                String search = s.toString().toLowerCase();
                for (int i = 0; i < iconsList.size(); i++) {
                    String name = IconUtils.formatName(iconsList.get(i).getName());
                    if (name.toLowerCase().contains(search)) {
                        filteredIconsList.add(iconsList.get(i));
                    }
                }
                adapter.clearIconsList();
                adapter.setIcons(filteredIconsList);
            }
            adapter.notifyDataSetChanged();
        }
    }

}