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

package jahirfiquitiva.iconshowcase.activities.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.CallSuper;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.pitchedapps.butler.iconrequest.IconRequest;
import com.pitchedapps.butler.iconrequest.events.RequestsCallback;

import java.io.File;
import java.util.ArrayList;

import jahirfiquitiva.iconshowcase.BuildConfig;
import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.config.Config;
import jahirfiquitiva.iconshowcase.dialogs.ISDialogs;
import jahirfiquitiva.iconshowcase.fragments.MainFragment;
import jahirfiquitiva.iconshowcase.fragments.WallpapersFragment;
import jahirfiquitiva.iconshowcase.holders.lists.FullListHolder;
import jahirfiquitiva.iconshowcase.logging.CrashReportingTree;
import jahirfiquitiva.iconshowcase.models.WallpaperItem;
import jahirfiquitiva.iconshowcase.tasks.ApplyWallpaper;
import jahirfiquitiva.iconshowcase.tasks.DownloadJSONTask;
import jahirfiquitiva.iconshowcase.tasks.LoadIconsLists;
import jahirfiquitiva.iconshowcase.tasks.LoadKustomFiles;
import jahirfiquitiva.iconshowcase.tasks.LoadZooperWidgets;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import timber.log.Timber;

import static com.pitchedapps.butler.iconrequest.IconRequest.STATE_TIME_LIMITED;

/**
 * Created by Allan Wang on 2016-08-20.
 */
public abstract class TasksActivity extends DrawerActivity {

    protected Preferences mPrefs;
    private boolean tasksExecuted = false;

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Config.init(this);

        if (BuildConfig.DEBUG || Config.get().allowDebugging()) {
            Timber.plant(new Timber.DebugTree());
        } else {
            //Disable debug & verbose logging on release
            Timber.plant(new CrashReportingTree());
        }

        mPrefs = new Preferences(this);
        if (savedInstanceState != null)
            IconRequest.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Config.deinit();
        try {
            getSupportLoaderManager().getLoader(0).cancelLoad();
            getSupportLoaderManager().destroyLoader(0);
        } catch (Exception ignored) {
        }
        cancelApplyTask();
    }

    //TODO fix up booleans
    protected void startTasks() {
        Timber.d("Starting tasks");
        if (tasksExecuted)
            Timber.w("startTasks() executed more than once; please remove duplicates");
        tasksExecuted = true;
        if (drawerHas(DrawerItem.PREVIEWS))
            new LoadIconsLists(this).execute();
        if (drawerHas(DrawerItem.WALLPAPERS)) {
            executeJsonTask(new DownloadJSONTask.JSONDownloadCallback() {
                @Override
                public void onPreExecute(Context context) {
                    FullListHolder.get().walls().clearList();
                    if (getCurrentFragment() instanceof WallpapersFragment) {
                        ((WallpapersFragment) getCurrentFragment()).refreshContent(context);
                    }
                }

                @Override
                public void onSuccess(ArrayList<WallpaperItem> wallpapers) {
                    FullListHolder.get().walls().createList(wallpapers);
                    if (getCurrentFragment() instanceof MainFragment) {
                        ((MainFragment) getCurrentFragment()).updateAppInfoData();
                    } else if (getCurrentFragment() instanceof WallpapersFragment) {
                        ((WallpapersFragment) getCurrentFragment()).setupContent();
                    }
                }
            });
        }
        if (drawerHas(DrawerItem.REQUESTS)) {
            Preferences mPrefs = new Preferences(this);
            IconRequest.start(this)
                    .withAppName(getString(R.string.app_name))
                    .withFooter("IconShowcase lib version: %s", BuildConfig.VERSION_NAME)
                    .withSubject(s(R.string.request_title))
                    .toEmail(s(R.string.email_id))
                    .saveDir(new File(getString(R.string.request_save_location, Environment
                            .getExternalStorageDirectory())))
                    .generateAppFilterJson(false)
                    .debugMode(Config.get().allowDebugging())
                    .filterXmlId(R.xml.appfilter)
                    //.filterOff() //TODO switch
                    .withTimeLimit(getResources().getInteger(R.integer.time_limit_in_minutes),
                            mPrefs.getPrefs())
                    .maxSelectionCount(getResources().getInteger(R.integer.max_apps_to_request))
                    .setCallback(new RequestsCallback() {
                        @Override
                        public void onRequestLimited(Context context, int reason, int appsLeft,
                                                     long millis) {
                            try {
                                if (reason == STATE_TIME_LIMITED && millis > 0) {
                                    ISDialogs.showRequestTimeLimitDialog(context, getResources()
                                            .getInteger(R.integer.time_limit_in_minutes), millis);
                                } else {
                                    ISDialogs.showRequestLimitDialog(context, appsLeft);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onRequestEmpty(Context context) {
                            try {
                                ISDialogs.showNoSelectedAppsDialog(context);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    })
                    .build().loadApps();
        }
        if (drawerHas(DrawerItem.ZOOPER)) {
            WITH_ZOOPER_SECTION = true;
            new LoadZooperWidgets(this).execute();
        }
        if (drawerHas(DrawerItem.KUSTOM)) {
            new LoadKustomFiles(this).execute();
        }
    }

    private boolean drawerHas(DrawerItem item) {
        return mDrawerMap.containsKey(item);
    }

    public void executeJsonTask(final DownloadJSONTask.JSONDownloadCallback callback) {
        final Context c = this;
        try {
            getSupportLoaderManager().getLoader(0).cancelLoad();
            getSupportLoaderManager().destroyLoader(0);
        } catch (Exception ignored) {
        }
        if (callback != null) callback.onPreExecute(this);
        getSupportLoaderManager().initLoader(0, null, new LoaderManager
                .LoaderCallbacks<ArrayList<WallpaperItem>>() {
            @Override
            public Loader<ArrayList<WallpaperItem>> onCreateLoader(int id, Bundle args) {
                return new DownloadJSONTask(c);
            }

            @Override
            @SuppressWarnings("unchecked")
            public void onLoadFinished(Loader<ArrayList<WallpaperItem>> loader,
                                       ArrayList<WallpaperItem> data) {
                if ((data != null) && (callback != null)) {
                    callback.onSuccess(data);
                }
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<WallpaperItem>> loader) {
                // Do nothing
            }
        });
    }

    public void executeApplyTask(final ApplyWallpaper.ApplyWallpaperCallback callback,
                                 final Bitmap resource, final String url,
                                 final boolean setToHomeScreen, final boolean setToLockScreen,
                                 final boolean setToBoth) {
        final Context c = this;
        cancelApplyTask();
        if (callback != null) callback.onPreExecute(this);
        getSupportLoaderManager().initLoader(2, null,
                new LoaderManager.LoaderCallbacks<Boolean>() {
                    @Override
                    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
                        return new ApplyWallpaper(c, resource, url, setToHomeScreen,
                                setToLockScreen, setToBoth);
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void onLoadFinished(Loader<Boolean> loader, Boolean success) {
                        if (callback != null) {
                            if (success) callback.onSuccess();
                            else callback.onError();
                        }
                    }

                    @Override
                    public void onLoaderReset(Loader<Boolean> loader) {
                        // Do nothing
                    }
                });
    }

    public void cancelApplyTask() {
        try {
            getSupportLoaderManager().getLoader(2).cancelLoad();
            getSupportLoaderManager().destroyLoader(2);
        } catch (Exception ignored) {
        }
    }

    //    @Subscribe
    //    public void onAppsLoaded(AppLoadedEvent event) {
    //        IconRequest.get().loadHighResIcons(); //Takes too much memory
    //    }

    @Override
    @CallSuper
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        IconRequest.saveInstanceState(outState);
    }

    //    @Override
    //    public void onStart() {
    //        super.onStart();
    //        EventBus.getDefault().register(this);
    //    }
    //
    //    @Override
    //    public void onStop() {
    //        EventBus.getDefault().unregister(this);
    //        super.onStop();
    //    }

}