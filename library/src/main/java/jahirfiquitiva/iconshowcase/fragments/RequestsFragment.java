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

import android.Manifest;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pitchedapps.butler.iconrequest.IconRequest;
import com.pitchedapps.butler.iconrequest.events.AppLoadedEvent;
import com.pitchedapps.butler.iconrequest.events.OnRequestProgress;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ca.allanwang.capsule.library.event.CFabEvent;
import ca.allanwang.capsule.library.fragments.CapsuleFragment;
import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.activities.base.DrawerActivity;
import jahirfiquitiva.iconshowcase.adapters.RequestsAdapter;
import jahirfiquitiva.iconshowcase.dialogs.ISDialogs;
import jahirfiquitiva.iconshowcase.utilities.color.ColorUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.IconUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.PermissionsUtils;
import jahirfiquitiva.iconshowcase.views.CounterFab;
import jahirfiquitiva.iconshowcase.views.GridSpacingItemDecoration;
import timber.log.Timber;

public class RequestsFragment extends CapsuleFragment {

    private RequestsAdapter mAdapter;
    private ViewGroup mViewGroup;
    private RelativeLayout mLoadingView;
    //private TextView mLoadingText;
    private RecyclerView mRecyclerView;
    private boolean subscribed = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View layout = inflater.inflate(R.layout.icon_request_section, container, false);

        int gridSpacing = getResources().getDimensionPixelSize(R.dimen.lists_padding);
        int columnsNumber = getResources().getInteger(R.integer.requests_grid_width);

        setHasOptionsMenu(true);

        hideFab();

        mViewGroup = (ViewGroup) layout.findViewById(R.id.viewgroup);
        mLoadingView = (RelativeLayout) layout.findViewById(R.id.loading_view);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.appsToRequestList);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnsNumber));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(
                new GridSpacingItemDecoration(columnsNumber,
                        gridSpacing,
                        true));
        RecyclerFastScroller mFastScroller = (RecyclerFastScroller) layout.findViewById(R.id
                .rvFastScroller);
        mFastScroller.attachRecyclerView(mRecyclerView);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    hideFab();
                } else {
                    showFab();
                }
            }
        });
        if (!IconRequest.get().isLoading()) {
            switchToLoadedView();
            subscribed = false;
            Timber.d("Requests already loaded");
        } else {
            Timber.d("Requests still loading; subscribing to events");
            //            AppLoadingEvent stickyEvent = EventBus.getDefault().removeStickyEvent
            // (AppLoadingEvent.class);
            //            if (stickyEvent != null) onAppsLoading(stickyEvent);
        }
        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (subscribed) EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        postEvent(updateFab());
    }

    @Override
    public int getTitleId() {
        return DrawerActivity.DrawerItem.REQUESTS.getTitleID();
    }

    @Nullable
    @Override
    protected CFabEvent updateFab() {
        updateFabNumber();
        Drawable icon = IconUtils.getTintedDrawableCheckingForColorDarkness(
                getActivity(), "ic_email", ColorUtils.getAccentColor(getActivity()));
        if (icon != null) {
            return new CFabEvent(icon, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startRequestProcess();
                }
            });
        } else {
            return new CFabEvent(R.drawable.ic_email, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startRequestProcess();
                }
            });
        }
    }

    private void updateFabNumber() {
        if (getAdapter() != null && getAdapter().getSelectedApps() != null) {
            try {
                ((CounterFab) capsuleActivity().getFab()).setCount(
                        getAdapter().getSelectedApps().size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.requests, menu);
    }

    @SuppressWarnings("UnusedParameters")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppsLoaded(AppLoadedEvent event) { //TODO make use of exceptions provided in event
        switchToLoadedView();
        //        EventBus.getDefault().unregister(AppLoadingEvent.class);
    }

    // TODO: Show a dialog with the progress of apps load
    //    @Subscribe(threadMode = ThreadMode.MAIN)
    //    public void onAppsLoading(AppLoadingEvent event) {
    //        EventBus.getDefault().removeStickyEvent(AppLoadingEvent.class);
    //        if (loaded) return;
    //        mLoadingText.setText(event.getString());
    //    }

    private void switchToLoadedView() {
        mViewGroup.removeView(mLoadingView);
        mLoadingView = null;
        mRecyclerView.setVisibility(View.VISIBLE);
        mAdapter = new RequestsAdapter(new RequestsAdapter.OnItemsChanged() {
            @Override
            public void doOnItemsChanged() {
                if (getAdapter() != null && getAdapter().getSelectedApps() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ((CounterFab) capsuleActivity().getFab()).setCount(getAdapter()
                                        .getSelectedApps().size());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        showFab();
    }

    public void startRequestProcess() {
        if (mAdapter != null && ((mAdapter.getSelectedApps() != null) &&
                (mAdapter.getSelectedApps().size() <= 0))) {
            try {
                ISDialogs.showNoSelectedAppsDialog(getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showRequestsFilesCreationDialog(getActivity());
        }
    }

    private void showRequestsFilesCreationDialog(final Context context) {
        PermissionsUtils.checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                new PermissionsUtils.PermissionRequestListener() {
                    @Override
                    public void onPermissionRequest() {
                        PermissionsUtils.requestStoragePermission((ShowcaseActivity) context);
                    }

                    @Override
                    public void onPermissionDenied() {
                        ISDialogs.showPermissionNotGrantedDialog(context);
                    }

                    @Override
                    public void onPermissionCompletelyDenied() {
                        ISDialogs.showPermissionNotGrantedDialog(context);
                    }

                    @Override
                    public void onPermissionGranted() {
                        final MaterialDialog dialog = ISDialogs.showBuildingRequestDialog
                                (context);
                        IconRequest.get().send(new OnRequestProgress() {
                            @Override
                            public void doWhenStarted() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.show();
                                    }
                                });
                            }

                            @Override
                            public void doOnError() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        new MaterialDialog.Builder(getActivity())
                                                .title(R.string.error)
                                                .content(R.string.requests_error)
                                                .positiveText(android.R.string.ok)
                                                .show();
                                    }
                                });
                            }

                            @Override
                            public void doWhenReady() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        if (mAdapter != null)
                                            mAdapter.selectOrDeselectAll();
                                    }
                                });
                            }
                        });
                    }
                });
    }

    public RequestsAdapter getAdapter() {
        return mAdapter;
    }

}