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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import ca.allanwang.capsule.library.event.CFabEvent;
import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.adapters.FeaturesAdapter;
import jahirfiquitiva.iconshowcase.config.Config;
import jahirfiquitiva.iconshowcase.dialogs.FolderSelectorDialog;
import jahirfiquitiva.iconshowcase.dialogs.ISDialogs;
import jahirfiquitiva.iconshowcase.fragments.base.PreferenceFragment;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.utils.PermissionsUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.ThemeUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.Utils;

public class SettingsFragment extends PreferenceFragment implements
        FolderSelectorDialog.FolderSelectionCallback {

    private Preferences mPrefs;
    private PackageManager p;
    private ComponentName componentName;
    private Preference WSL;
    private Preference data;
    private String location;
    private String cacheSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().post(new CFabEvent(false)); //hide fab

        mPrefs = new Preferences(getActivity());

        mPrefs.setSettingsModified(false); //TODO remove this; change to bundle

        location = mPrefs.getDownloadsFolder();

        cacheSize = fullCacheDataSize(getActivity().getApplicationContext());

        p = getActivity().getPackageManager();

        addPreferencesFromResource(R.xml.preferences);

        Class<?> className = null;

        String componentNameString = Utils.getAppPackageName(
                getActivity().getApplicationContext()) + "." + Utils.getStringFromResources(
                getActivity(), R.string.main_activity_name);

        try {
            className = Class.forName(componentNameString);
        } catch (ClassNotFoundException e) {
            try {
                componentNameString = Utils.getStringFromResources(getActivity(),
                        R.string.main_activity_fullname);
                className = Class.forName(componentNameString);
            } catch (ClassNotFoundException e1) {
                //Do nothing
            }
        }

        final PreferenceScreen preferences = (PreferenceScreen) findPreference("preferences");
        final PreferenceCategory launcherIcon = (PreferenceCategory) findPreference
                ("launcherIconPreference");

        setupDevOptions(preferences, getActivity());

        PreferenceCategory uiCategory = (PreferenceCategory) findPreference("uiPreferences");

        WSL = findPreference("wallsSaveLocation");
        WSL.setSummary(getResources().getString(R.string.pref_summary_wsl, location));

        SwitchPreference wallHeaderCheck = (SwitchPreference) findPreference("wallHeader");

        if (Config.get().userWallpaperInToolbar()) {
            wallHeaderCheck.setChecked(mPrefs.getWallpaperAsToolbarHeaderEnabled());
            wallHeaderCheck.setOnPreferenceChangeListener(new Preference
                    .OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mPrefs.setWallpaperAsToolbarHeaderEnabled(newValue.toString().equals("true"));
                    ((ShowcaseActivity) getActivity()).setupToolbarHeader();
                    return true;
                }
            });
        } else {
            uiCategory.removePreference(wallHeaderCheck);
        }

        SwitchPreference animations = (SwitchPreference) findPreference("animations");
        animations.setChecked(mPrefs.getAnimationsEnabled());
        animations.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mPrefs.setAnimationsEnabled(newValue.toString().equals("true"));
                return true;
            }
        });

        data = findPreference("clearData");
        data.setSummary(getResources().getString(R.string.pref_summary_cache, cacheSize));
        data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MaterialDialog.SingleButtonCallback positiveCallback = new MaterialDialog
                        .SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull
                            DialogAction dialogAction) {
                        clearApplicationDataAndCache(getActivity());
                        changeValues(getActivity());
                    }
                };
                ISDialogs.showClearCacheDialog(getActivity(), positiveCallback);
                return true;
            }
        });

        findPreference("wallsSaveLocation").setOnPreferenceClickListener(new Preference
                .OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                PermissionsUtils.checkPermission(getActivity(), Manifest.permission
                                .WRITE_EXTERNAL_STORAGE,
                        new PermissionsUtils.PermissionRequestListener() {
                            @Override
                            public void onPermissionRequest() {
                                // TODO: Show dialog explaining reasons to ask for permission
                                PermissionsUtils.requestStoragePermission(getActivity());
                            }

                            @Override
                            public void onPermissionDenied() {
                                ISDialogs.showPermissionNotGrantedDialog(getActivity());
                            }

                            @Override
                            public void onPermissionCompletelyDenied() {
                                ISDialogs.showPermissionNotGrantedDialog(getActivity());
                            }

                            @Override
                            public void onPermissionGranted() {
                                showFolderChooserDialog();
                            }
                        });
                return true;
            }
        });

        if (getResources().getBoolean(R.bool.allow_user_to_hide_app_icon)) {
            final SwitchPreference hideIcon = (SwitchPreference) findPreference("launcherIcon");
            hideIcon.setChecked(!mPrefs.getLauncherIconShown());

            final Class<?> finalClassName = className;
            final String finalComponentName = componentNameString;

            hideIcon.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            if (finalClassName != null) {
                                componentName = new ComponentName(
                                        Utils.getAppPackageName(getActivity()
                                                .getApplicationContext()),
                                        finalComponentName);
                                if (newValue.toString().equals("true")) {
                                    MaterialDialog.SingleButtonCallback positive = new
                                            MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog
                                                                            materialDialog, @NonNull
                                                                            DialogAction
                                                                            dialogAction) {
                                                    if (mPrefs.getLauncherIconShown()) {
                                                        mPrefs.setIconShown(false);
                                                        p.setComponentEnabledSetting(componentName,
                                                                PackageManager
                                                                        .COMPONENT_ENABLED_STATE_DISABLED,
                                                                PackageManager.DONT_KILL_APP);
                                                        hideIcon.setChecked(true);
                                                    }
                                                }
                                            };

                                    MaterialDialog.SingleButtonCallback negative = new
                                            MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog
                                                                            materialDialog, @NonNull
                                                                            DialogAction
                                                                            dialogAction) {
                                                    if (mPrefs.getLauncherIconShown()) {
                                                        hideIcon.setChecked(false);
                                                    }
                                                }
                                            };

                                    DialogInterface.OnDismissListener dismissListener = new
                                            DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialog) {
                                                    if (mPrefs.getLauncherIconShown()) {
                                                        hideIcon.setChecked(false);
                                                    }
                                                }
                                            };

                                    ((ShowcaseActivity) getActivity()).setSettingsDialog
                                            (ISDialogs.showHideIconDialog(getActivity(),
                                                    positive, negative, dismissListener));
                                } else {
                                    if (!mPrefs.getLauncherIconShown()) {
                                        mPrefs.setIconShown(true);
                                        p.setComponentEnabledSetting(componentName,
                                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                                PackageManager.DONT_KILL_APP);
                                    }
                                }
                                return true;
                            } else {
                                ISDialogs.showHideIconErrorDialog(getActivity());
                                return false;
                            }
                        }
                    }

            );
        } else {
            preferences.removePreference(launcherIcon);
        }
    }

    private void setupDevOptions(PreferenceScreen mainPrefs, final Context context) {
        if (getResources().getBoolean(R.bool.dev_options)) {

            Preference moarOptions;
            SwitchPreference drawerHeaderTexts;
            SwitchPreference listsCards;

            moarOptions = findPreference("moreOptions");

            drawerHeaderTexts = (SwitchPreference) findPreference("drawerHeaderTexts");
            listsCards = (SwitchPreference) findPreference("listsCards");

            drawerHeaderTexts.setChecked(mPrefs.getDevDrawerTexts());
            drawerHeaderTexts.setOnPreferenceChangeListener(new Preference
                    .OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mPrefs.setDevDrawerTexts(newValue.toString().equals("true"));
                    mPrefs.setSettingsModified(true);
                    ThemeUtils.restartActivity((Activity) context);
                    return true;
                }
            });

            listsCards.setChecked(mPrefs.getDevListsCards());
            listsCards.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mPrefs.setDevListsCards(newValue.toString().equals("true"));
                    mPrefs.setSettingsModified(true);
                    ThemeUtils.restartActivity((Activity) context);
                    return true;
                }
            });

            moarOptions.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new MaterialDialog.Builder(context)
                            .title(R.string.dev_more_options_title)
                            .adapter(new FeaturesAdapter(context, R.array.dev_extra_features), null)
                            .positiveText(R.string.great)
                            .listSelector(android.R.color.transparent)
                            .show();
                    return true;
                }
            });

        } else {
            mainPrefs.removePreference(findPreference("devPrefs"));
        }
    }

    private void changeValues(Context context) {
        location = mPrefs.getDownloadsFolder();
        WSL.setSummary(context.getResources().getString(R.string.pref_summary_wsl, location));
        cacheSize = fullCacheDataSize(context);
        data.setSummary(context.getResources().getString(R.string.pref_summary_cache, cacheSize));
    }

    private void clearApplicationDataAndCache(Context context) {
        File cache = context.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                }
            }
        }
        clearCache(context);
        mPrefs.setIconShown(true);
        mPrefs.setDownloadsFolder(null);
        mPrefs.setApplyDialogDismissed(false);
        mPrefs.setWallsDialogDismissed(false);
    }

    private void clearCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            //Do nothing
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                if (!deleteDir(new File(dir, aChildren))) return false;
            }
        }

        return dir != null && dir.delete();
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("DefaultLocale")
    private String fullCacheDataSize(Context context) { //TODO add permission check?
        String finalSize;

        long cache = 0;
        long extCache = 0;
        double finalResult, mbFinalResult;

        File[] fileList = context.getCacheDir().listFiles();
        for (File aFileList : fileList) {
            if (aFileList.isDirectory()) {
                cache += dirSize(aFileList);
            } else {
                cache += aFileList.length();
            }
        }
        try {
            File[] fileExtList = new File[0];
            try {
                fileExtList = context.getExternalCacheDir().listFiles();
            } catch (NullPointerException e) {
                //Do nothing
            }
            if (fileExtList != null) {
                for (File aFileExtList : fileExtList) {
                    if (aFileExtList.isDirectory()) {
                        extCache += dirSize(aFileExtList);
                    } else {
                        extCache += aFileExtList.length();
                    }
                }
            }
        } catch (NullPointerException npe) {
            Log.d("CACHE", Log.getStackTraceString(npe));
        }

        finalResult = (cache + extCache) / 1000;

        if (finalResult > 1001) {
            mbFinalResult = finalResult / 1000;
            finalSize = String.format("%.2f", mbFinalResult) + " MB";
        } else {
            finalSize = String.format("%.2f", finalResult) + " KB";
        }

        return finalSize;
    }

    private long dirSize(File dir) {
        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    result += dirSize(aFileList);
                } else {
                    result += aFileList.length();
                }
            }
            return result;
        }
        return 0;
    }

    public void showFolderChooserDialog() {
        new FolderSelectorDialog().show((AppCompatActivity) getActivity(), this);
    }

    @Override
    public void onFolderSelection(File folder) {
        location = folder.getAbsolutePath();
        mPrefs.setDownloadsFolder(location);
        WSL.setSummary(getString(R.string.pref_summary_wsl, location));
    }

}