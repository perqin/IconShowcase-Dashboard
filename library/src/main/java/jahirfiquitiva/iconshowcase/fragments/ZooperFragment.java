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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import ca.allanwang.capsule.library.event.CFabEvent;
import ca.allanwang.capsule.library.fragments.CapsuleFragment;
import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.activities.base.DrawerActivity;
import jahirfiquitiva.iconshowcase.adapters.ZooperAdapter;
import jahirfiquitiva.iconshowcase.utilities.utils.Utils;
import jahirfiquitiva.iconshowcase.views.GridSpacingItemDecoration;

public class ZooperFragment extends CapsuleFragment {

    private ZooperAdapter zooperAdapter;
    private ViewGroup layout;
    private Context context;
    private GridSpacingItemDecoration space;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        context = getActivity();

        if (layout != null) {
            ViewGroup parent = (ViewGroup) layout.getParent();
            if (parent != null) {
                parent.removeView(layout);
            }
        }
        try {
            layout = (ViewGroup) inflater.inflate(R.layout.zooper_section, container, false);
        } catch (InflateException e) {
            //Do nothing
        }

        setupRV();
        return layout;
    }

    @Override
    public int getTitleId() {
        return DrawerActivity.DrawerItem.ZOOPER.getTitleID();
    }

    @Nullable
    @Override
    protected CFabEvent updateFab() {
        return new CFabEvent(false);
    }

    private void setupRV() {
        if (layout != null) {

            int gridSpacing = getResources().getDimensionPixelSize(R.dimen.lists_padding);
            int columnsNumber = getResources().getInteger(R.integer.zooper_kustom_grid_width);

            RecyclerView mRecyclerView = (RecyclerView) layout.findViewById(R.id.zooper_rv);

            if (space != null) {
                mRecyclerView.removeItemDecoration(space);
            }

            mRecyclerView.setLayoutManager(new GridLayoutManager(context, columnsNumber));

            space = new GridSpacingItemDecoration(columnsNumber,
                    gridSpacing, true);

            mRecyclerView.addItemDecoration(space);
            mRecyclerView.setHasFixedSize(true);

            RecyclerFastScroller fastScroller = (RecyclerFastScroller) layout.findViewById(R.id
                    .rvFastScroller);

            zooperAdapter = new ZooperAdapter(context, layout,
                    ((ShowcaseActivity) context).getWallpaperDrawable(), areAppsInstalled(), this);

            mRecyclerView.setAdapter(zooperAdapter);

            fastScroller.attachRecyclerView(mRecyclerView);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupRV();
    }

    public void showInstalledAppsSnackbar() {
        if (layout != null && context != null) {
            Utils.showSimpleSnackbar(context, layout,
                    Utils.getStringFromResources(context, R.string.apps_installed));
        }
    }

    public void showInstalledAssetsSnackbar() {
        if (layout != null && context != null) {
            Utils.showSimpleSnackbar(context, layout,
                    Utils.getStringFromResources(context, R.string.assets_installed));
        }
    }

    private boolean areAppsInstalled() {

        boolean installed = Utils.isAppInstalled(context, "org.zooper.zwpro");

        if (context.getResources().getBoolean(R.bool.mu_needed) && installed) {
            installed = Utils.isAppInstalled(context, "com.batescorp.notificationmediacontrols" +
                    ".alpha");
        }

        if (context.getResources().getBoolean(R.bool.kolorette_needed) && installed) {
            installed = Utils.isAppInstalled(context, "com.arun.themeutil.kolorette");
        }

        return installed;
    }

    public ZooperAdapter getAdapter() {
        return zooperAdapter;
    }
}
