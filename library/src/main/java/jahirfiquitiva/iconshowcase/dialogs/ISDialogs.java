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

package jahirfiquitiva.iconshowcase.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.piracychecker.enums.PirateApp;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.events.BlankEvent;
import jahirfiquitiva.iconshowcase.fragments.WallpapersFragment;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.utils.IconUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.RequestUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.Utils;

/**
 * This Class was created by Patrick Jung on 07.01.16. For more Details and Licensing have a look at
 * the README.md
 */

public final class ISDialogs {

    public static MaterialDialog buildLicenseSuccessDialog(Context context,
                                                           MaterialDialog.SingleButtonCallback
                                                                   onPositive,
                                                           MaterialDialog.OnDismissListener
                                                                   onDismiss,
                                                           MaterialDialog.OnCancelListener
                                                                   onCancel) {

        String message = context.getResources().getString(R.string.license_success,
                context.getResources().getString(R.string.app_name));

        MaterialDialog licenseSuccessDialog = new MaterialDialog.Builder(context)
                .title(R.string.license_success_title)
                .content(message)
                .positiveText(R.string.close)
                .onPositive(onPositive)
                .build();

        licenseSuccessDialog.setOnCancelListener(onCancel);
        licenseSuccessDialog.setOnDismissListener(onDismiss);
        return licenseSuccessDialog;
    }

    public static MaterialDialog buildShallNotPassDialog(Context context,
                                                         PirateApp app,
                                                         MaterialDialog.SingleButtonCallback
                                                                 onPositive,
                                                         MaterialDialog.SingleButtonCallback
                                                                 onNegative,
                                                         MaterialDialog.OnDismissListener onDismiss,
                                                         MaterialDialog.OnCancelListener onCancel) {
        String extra1 = "";
        String extra2 = "";
        if (app != null) {
            extra1 = context.getResources().getString(R.string.license_failed_extra,
                    app.getName());
            extra2 = context.getResources().getString(R.string.license_failed_extra_sec);
        }
        String message = context.getResources().getString(R.string.license_failed,
                context.getResources().getString(R.string.app_name), extra1, extra2);

        MaterialDialog.Builder shallNotPassDialogBuilder = new MaterialDialog.Builder(context)
                .title(R.string.license_failed_title)
                .content(message)
                .autoDismiss(false);

        if (onPositive != null) {
            shallNotPassDialogBuilder.positiveText(R.string.download);
            shallNotPassDialogBuilder.onPositive(onPositive);
        }
        if (onNegative != null) {
            shallNotPassDialogBuilder.negativeText(R.string.exit);
            shallNotPassDialogBuilder.onNegative(onNegative);
        }

        MaterialDialog shallNotPassDialog = shallNotPassDialogBuilder.build();
        if (onCancel != null)
            shallNotPassDialog.setOnCancelListener(onCancel);
        if (onDismiss != null)
            shallNotPassDialog.setOnDismissListener(onDismiss);

        return shallNotPassDialog;
    }

    public static MaterialDialog buildLicenseErrorDialog(Context context,
                                                         MaterialDialog.SingleButtonCallback
                                                                 onPositive,
                                                         MaterialDialog.SingleButtonCallback
                                                                 onNegative,
                                                         MaterialDialog.OnDismissListener onDismiss,
                                                         MaterialDialog.OnCancelListener onCancel) {

        MaterialDialog.Builder licenseErrorDialogBuilder = new MaterialDialog.Builder(context)
                .title(R.string.error)
                .content(R.string.license_error)
                .autoDismiss(false);

        if (onPositive != null) {
            licenseErrorDialogBuilder.positiveText(R.string.retry);
            licenseErrorDialogBuilder.onPositive(onPositive);
        }
        if (onNegative != null) {
            licenseErrorDialogBuilder.negativeText(R.string.exit);
            licenseErrorDialogBuilder.onNegative(onNegative);
        }

        MaterialDialog licenseErrorDialog = licenseErrorDialogBuilder.build();
        if (onCancel != null)
            licenseErrorDialog.setOnCancelListener(onCancel);
        if (onDismiss != null)
            licenseErrorDialog.setOnDismissListener(onDismiss);
        return licenseErrorDialog;
    }

    /*
    WallpaperViewerActivity Dialogs
     */

    public static void showApplyWallpaperDialog(final Context context,
                                                MaterialDialog.SingleButtonCallback onPositive,
                                                MaterialDialog.SingleButtonCallback onNeutral,
                                                MaterialDialog.SingleButtonCallback onNegative,
                                                MaterialDialog.OnDismissListener onDismiss) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.apply)
                .content(R.string.confirm_apply)
                .positiveText(R.string.apply)
                .neutralText(R.string.crop)
                .negativeText(android.R.string.cancel)
                .onPositive(onPositive)
                .onNeutral(onNeutral)
                .onNegative(onNegative)
                .build();
        dialog.setOnDismissListener(onDismiss);
        dialog.show();
    }

    public static void showWallpaperDetailsDialog(final Context context, String wallName,
                                                  String wallAuthor, String wallDimensions,
                                                  String wallCopyright, MaterialDialog
                                                          .OnDismissListener listener) {

        MaterialDialog dialog = new MaterialDialog.Builder(context).title(wallName)
                .customView(R.layout.wallpaper_details, false)
                .positiveText(context.getResources().getString(R.string.close))
                .build();

        dialog.setOnDismissListener(listener);

        View v = dialog.getCustomView();

        ImageView authorIcon;
        ImageView dimensIcon;
        ImageView copyrightIcon;

        if (v != null) {
            authorIcon = (ImageView) v.findViewById(R.id.icon_author);
            dimensIcon = (ImageView) v.findViewById(R.id.icon_dimensions);
            copyrightIcon = (ImageView) v.findViewById(R.id.icon_copyright);
            authorIcon.setImageDrawable(IconUtils.getTintedDrawable(context, "ic_person"));
            dimensIcon.setImageDrawable(IconUtils.getTintedDrawable(context, "ic_dimensions"));
            copyrightIcon.setImageDrawable(IconUtils.getTintedDrawable(context, "ic_copyright"));

            LinearLayout author = (LinearLayout) v.findViewById(R.id.authorName);
            LinearLayout dimensions = (LinearLayout) v.findViewById(R.id.wallDimensions);
            LinearLayout copyright = (LinearLayout) v.findViewById(R.id.wallCopyright);

            TextView authorText = (TextView) v.findViewById(R.id.wallpaper_author_text);
            TextView dimensionsText = (TextView) v.findViewById(R.id.wallpaper_dimensions_text);
            TextView copyrightText = (TextView) v.findViewById(R.id.wallpaper_copyright_text);

            if ("null".equals(wallAuthor) || wallAuthor.isEmpty()) {
                author.setVisibility(View.GONE);
            } else {
                authorText.setText(wallAuthor);
            }

            if ("null".equals(wallDimensions) || wallDimensions.isEmpty()) {
                dimensions.setVisibility(View.GONE);
            } else {
                dimensionsText.setText(wallDimensions);
            }

            if ("null".equals(wallCopyright) || wallCopyright.isEmpty()) {
                copyright.setVisibility(View.GONE);
            } else {
                copyrightText.setText(wallCopyright);
            }
        }

        dialog.show();
    }


    /*
    Apply Fragment Dialogs
     */

    public static void showOpenInPlayStoreDialog(Context context, String title, String content,
                                                 MaterialDialog.SingleButtonCallback onPositive) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive(onPositive)
                .show();
    }

    public static void showGoogleNowLauncherDialog(Context context, MaterialDialog
            .SingleButtonCallback onPositive) {
        new MaterialDialog.Builder(context)
                .title(R.string.gnl_title)
                .content(R.string.gnl_content)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive(onPositive)
                .show();
    }

    public static void showApplyAdviceDialog(Context context, MaterialDialog.SingleButtonCallback
            callback) {
        new MaterialDialog.Builder(context)
                .title(R.string.advice)
                .content(R.string.apply_advice)
                .positiveText(R.string.close)
                .neutralText(R.string.dontshow)
                .onAny(callback)
                .show();
    }

    /*
    Request Fragment Dialogs
     */
    public static void showPermissionNotGrantedDialog(Context context) {
        String appName = Utils.getStringFromResources(context, R.string.app_name);
        new MaterialDialog.Builder(context)
                .title(R.string.md_error_label)
                .content(context.getResources().getString(R.string.md_storage_perm_error, appName))
                .positiveText(android.R.string.ok)
                .show();
    }

    public static MaterialDialog showBuildingRequestDialog(Context context) {
        return new MaterialDialog.Builder(context)
                .content(R.string.building_request_dialog)
                .progress(true, 0)
                .cancelable(false)
                .build();
    }

    public static void showNoSelectedAppsDialog(Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.no_selected_apps_title)
                .content(R.string.no_selected_apps_content)
                .positiveText(android.R.string.ok)
                .show();
    }

    public static void showRequestLimitDialog(Context context, int maxApps) {
        if (context.getResources().getInteger(R.integer.max_apps_to_request) > -1) {
            String content;
            if (maxApps == context.getResources().getInteger(R.integer.max_apps_to_request)) {
                content = context.getResources().getString(R.string.apps_limit_dialog, String
                        .valueOf(maxApps));
            } else {
                content = context.getResources().getString(R.string.apps_limit_dialog_more,
                        String.valueOf(maxApps));
            }
            new MaterialDialog.Builder(context)
                    .title(R.string.section_icon_request)
                    .content(content)
                    .positiveText(android.R.string.ok)
                    .show();
        }
    }

    public static void showRequestTimeLimitDialog(Context context, long minutes, long millisLeft) {
        String content = context.getResources().getString(R.string.apps_limit_dialog_day,
                RequestUtils.getTimeTextFromMillis(context, TimeUnit.MINUTES.toMillis(minutes)));

        String contentExtra;
        if (TimeUnit.MILLISECONDS.toSeconds(millisLeft) >= 60) {
            contentExtra = context.getResources().getString(R.string.apps_limit_dialog_day_extra,
                    RequestUtils.getTimeTextFromMillis(context, millisLeft));
        } else {
            contentExtra = Utils.getStringFromResources(context, R.string
                    .apps_limit_dialog_day_extra_sec);
        }

        String finalContent = content + " " + contentExtra;
        new MaterialDialog.Builder(context)
                .title(R.string.section_icon_request)
                .content(finalContent)
                .positiveText(android.R.string.ok)
                .show();
    }

    public static void showHideIconErrorDialog(final Activity context) {
        String content = context.getResources().getString(R.string.launcher_icon_restorer_error,
                Utils.getStringFromResources(context, R.string.app_name));
        new MaterialDialog.Builder(context)
                .title(R.string.pref_title_launcher_icon)
                .content(content)
                .positiveText(android.R.string.ok)
                .show();
    }

    public static void showColumnsSelectorDialog(final Context context,
                                                 final WallpapersFragment wallsFragment) {
        Preferences mPrefs = new Preferences(context);
        final int current = mPrefs.getWallsColumnsNumber();
        new MaterialDialog.Builder(context)
                .title(R.string.columns)
                .content(R.string.columns_desc)
                .items(R.array.columns_options)
                .itemsCallbackSingleChoice(current - 1, new MaterialDialog
                        .ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int position,
                                               CharSequence text) {
                        int newSelected = position + 1;
                        if (newSelected != current) {
                            EventBus.getDefault().post(new BlankEvent(newSelected));
                            wallsFragment.updateRecyclerView(newSelected);
                        }
                        return true;
                    }
                })
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .show();
    }

    public static void showZooperAppsDialog(final Context context,
                                            final ArrayList<String> appsNames) {
        final String storePrefix = "https://play.google.com/store/apps/details?id=",
                muLink = "com.batescorp.notificationmediacontrols.alpha",
                koloretteLink = "com.arun.themeutil.kolorette";
        new MaterialDialog.Builder(context)
                .title(R.string.install_apps)
                .content(R.string.install_apps_content)
                .items(appsNames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which,
                                            CharSequence text) {
                        if (appsNames.get(which).equals("Zooper Widget Pro")) {
                            showZooperDownloadDialog(context);
                        } else if (appsNames.get(which).equals("Media Utilities")) {
                            Utils.openLinkInChromeCustomTab(context,
                                    storePrefix + muLink);
                        } else if (appsNames.get(which).equals("Kolorette")) {
                            Utils.openLinkInChromeCustomTab(context,
                                    storePrefix + koloretteLink);
                        }
                    }
                })
                .show();
    }

    public static void showKustomAppsDownloadDialog(final Context context,
                                                    final ArrayList<String> appsNames) {
        final String storePrefix = "https://play.google.com/store/apps/details?id=",
                klwpLink = "org.kustom.wallpaper",
                kwgtLink = "org.kustom.widget",
                koloretteLink = "com.arun.themeutil.kolorette";
        new MaterialDialog.Builder(context)
                .title(R.string.install_apps)
                .content(R.string.install_kustom_apps_content)
                .items(appsNames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which,
                                            CharSequence text) {
                        if (appsNames.get(which).equals("Kustom Live Wallpaper")) {
                            Utils.openLinkInChromeCustomTab(context,
                                    storePrefix + klwpLink);
                        } else if (appsNames.get(which).equals("Kustom Widget")) {
                            Utils.openLinkInChromeCustomTab(context,
                                    storePrefix + kwgtLink);
                        } else if (appsNames.get(which).equals("Kolorette")) {
                            Utils.openLinkInChromeCustomTab(context,
                                    storePrefix + koloretteLink);
                        }
                    }
                })
                .show();
    }

    private static void showZooperDownloadDialog(final Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.zooper_download_dialog_title)
                .items(R.array.zooper_download_dialog_options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int selection,
                                            CharSequence text) {
                        switch (selection) {
                            case 0:
                                Utils.openLinkInChromeCustomTab(context,
                                        "https://play.google.com/store/apps/details?id=org.zooper" +
                                                ".zwpro");
                                break;
                            case 1:
                                if (Utils.isAppInstalled(context, "com.amazon.venezia")) {
                                    Utils.openLinkInChromeCustomTab(context,
                                            "amzn://apps/android?p=org.zooper.zwpro");
                                } else {
                                    Utils.openLinkInChromeCustomTab(context,
                                            "http://www.amazon.com/gp/mas/dl/android?p=org.zooper" +
                                                    ".zwpro");
                                }
                                break;
                        }
                    }
                })
                .show();
    }

    public static void showSherryDialog(final Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.sherry_title)
                .content(R.string.sherry_dialog)
                .neutralText(R.string.follow_her)
                .positiveText(R.string.close)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction
                            which) {
                        Utils.openLinkInChromeCustomTab(context,
                                context.getResources().getString(R.string.sherry_link));
                    }
                })
                .show();
    }

    public static void showUICollaboratorsDialog(final Context context,
                                                 final String[] uiCollaboratorsLinks) {
        new MaterialDialog.Builder(context)
                .title(R.string.ui_design)
                .negativeText(R.string.close)
                .items(R.array.ui_collaborators_names)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view,
                                            final int i, CharSequence charSequence) {
                        Utils.openLinkInChromeCustomTab(context, uiCollaboratorsLinks[i]);
                    }
                }).show();
    }

    public static void showLibrariesDialog(final Context context, final String[] libsLinks) {
        new MaterialDialog.Builder(context)
                .title(R.string.implemented_libraries)
                .negativeText(R.string.close)
                .items(R.array.libs_names)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view,
                                            final int i, CharSequence charSequence) {
                        Utils.openLinkInChromeCustomTab(context, libsLinks[i]);
                    }
                }).show();
    }

    public static void showContributorsDialog(final Context context, final String[]
            contributorsLinks) {
        new MaterialDialog.Builder(context)
                .title(R.string.contributors)
                .negativeText(R.string.close)
                .items(R.array.contributors_names)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view,
                                            final int i, CharSequence charSequence) {
                        Utils.openLinkInChromeCustomTab(context, contributorsLinks[i]);
                    }
                }).show();
    }

    public static void showTranslatorsDialogs(final Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.translators)
                .negativeText(R.string.close)
                .items(R.array.translators_names)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which,
                                            CharSequence text) {
                        //Do nothing
                    }
                })
                .listSelector(android.R.color.transparent)
                .show();
    }

    /*
    Settings Fragment Dialogs
     */

    public static void showClearCacheDialog(Context context, MaterialDialog.SingleButtonCallback
            singleButtonCallback) {
        new MaterialDialog.Builder(context)
                .title(R.string.clearcache_dialog_title)
                .content(R.string.clearcache_dialog_content)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive(singleButtonCallback)
                .show();
    }

    public static MaterialDialog showHideIconDialog(Context context, MaterialDialog
            .SingleButtonCallback positive, MaterialDialog.SingleButtonCallback negative,
                                                    DialogInterface.OnDismissListener
                                                            dismissListener) {
        return new MaterialDialog.Builder(context)
                .title(R.string.hideicon_dialog_title)
                .content(R.string.hideicon_dialog_content)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive(positive)
                .onNegative(negative)
                .dismissListener(dismissListener)
                .show();
    }

}