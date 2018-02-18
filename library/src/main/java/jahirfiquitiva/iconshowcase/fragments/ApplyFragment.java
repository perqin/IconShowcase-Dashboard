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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.allanwang.capsule.library.event.CFabEvent;
import ca.allanwang.capsule.library.fragments.CapsuleFragment;
import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.base.DrawerActivity;
import jahirfiquitiva.iconshowcase.adapters.LaunchersAdapter;
import jahirfiquitiva.iconshowcase.config.Config;
import jahirfiquitiva.iconshowcase.dialogs.ISDialogs;
import jahirfiquitiva.iconshowcase.holders.LauncherHolder;
import jahirfiquitiva.iconshowcase.models.LauncherItem;
import jahirfiquitiva.iconshowcase.utilities.LauncherIntents;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.sort.InstalledLauncherComparator;
import jahirfiquitiva.iconshowcase.utilities.utils.Utils;
import jahirfiquitiva.iconshowcase.views.GridSpacingItemDecoration;

public class ApplyFragment extends CapsuleFragment {

    private final List<LauncherItem> launchers = new ArrayList<>();
    private String intentString;
    private RecyclerView recyclerView;
    private View layout;
    private Preferences mPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        layout = inflater.inflate(R.layout.apply_section, container, false);

        mPrefs = new Preferences(getActivity());
        showApplyAdviceDialog(getActivity());

        recyclerView = (RecyclerView) layout.findViewById(R.id.launchersList);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getResources().getInteger(R.integer.launchers_grid_width)));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(
                new GridSpacingItemDecoration(getResources().getInteger(R.integer
                        .launchers_grid_width),
                        getResources().getDimensionPixelSize(R.dimen.lists_padding),
                        true));

        updateLaunchersList(layout);

        return layout;
    }

    @Override
    public int getTitleId() {
        return DrawerActivity.DrawerItem.APPLY.getTitleID();
    }

    private void updateLaunchersList(View layout) {

        launchers.clear();

        // Splits all launcher  arrays by the | delimiter {name}|{package}
        final String[] launcherArray = getResources().getStringArray(R.array.launchers);
        final int[] launcherColors = getResources().getIntArray(R.array.launcher_colors);
        for (int i = 0; i < launcherArray.length; i++) {
            launchers.add(new LauncherItem(launcherArray[i].split("\\|"), launcherColors[i]));
        }
        Collections.sort(launchers, new InstalledLauncherComparator(getActivity()));

        LaunchersAdapter adapter = new LaunchersAdapter(getActivity(), launchers, new
                LauncherHolder.OnLauncherClickListener() {
                    @Override
                    public void onLauncherClick(LauncherItem item) {
                        if (item.getName().equals("Google Now")) {
                            gnlDialog();
                        } else if (item.getName().equals("LG Home")) {
                            if (Utils.isAppInstalled(getActivity(), item
                                    .getPackageName())) {
                                openLauncher(item.getName());
                            } else {
                                new MaterialDialog.Builder(getActivity())
                                        .content(R.string.lg_dialog_content)
                                        .positiveText(android.R.string.ok)
                                        .show();
                            }
                        } else if (item.getName().equals("CM Theme Engine")) {
                            if (Utils.isAppInstalled(getActivity(), "com.cyngn.theme.chooser")) {
                                openLauncher("CM Theme Engine");
                            } else if (Utils.isAppInstalled(getActivity(), item.getPackageName())) {
                                openLauncher(item.getName());
                            } else {
                                openInPlayStore(item);
                            }
                        } else if (Utils.isAppInstalled(getActivity(), item
                                .getPackageName())) {
                            openLauncher(item.getName());
                        } else {
                            openInPlayStore(item);
                        }
                    }
                });

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        RecyclerFastScroller fastScroller = (RecyclerFastScroller) layout.findViewById(R.id
                .rvFastScroller);
        fastScroller.attachRecyclerView(recyclerView);
    }

    private void openLauncher(String name) {
        final String launcherName = Character.toUpperCase(name.charAt(0))
                + name.substring(1).toLowerCase().replace(" ", "").replace("launcher", "");
        try {
            new LauncherIntents(getActivity(), launcherName);
        } catch (IllegalArgumentException ex) {
            if (layout != null) {
                Snackbar.make(layout, R.string.no_launcher_intent, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void openInPlayStore(final LauncherItem launcher) {
        intentString = Config.MARKET_URL + launcher.getPackageName();
        final String LauncherName = launcher.getName();
        final String cmName = "CM Theme Engine";
        String dialogContent;
        if (LauncherName.equals(cmName)) {
            dialogContent = getResources().getString(R.string.cm_dialog_content, launcher.getName
                    ());
            intentString = "http://download.cyanogenmod.org/";
        } else {
            dialogContent = getResources().getString(R.string.lni_content, launcher.getName());
            intentString = Config.MARKET_URL + launcher.getPackageName();
        }
        ISDialogs.showOpenInPlayStoreDialog(getContext(), launcher.getName(), dialogContent, new
                MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull
                            DialogAction
                            dialogAction) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(intentString));
                        startActivity(intent);
                    }
                });
    }

    private void gnlDialog() {
        final String appLink = Config.MARKET_URL + getResources().getString(R.string.extraapp);
        ISDialogs.showGoogleNowLauncherDialog(getContext(), new MaterialDialog
                .SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction
                    dialogAction) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(appLink));
                startActivity(intent);
            }
        });
    }

    private void showApplyAdviceDialog(Context dialogContext) {
        if (!mPrefs.getApplyDialogDismissed()) {
            MaterialDialog.SingleButtonCallback singleButtonCallback = new MaterialDialog
                    .SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    if (which.equals(DialogAction.POSITIVE)) {
                        mPrefs.setApplyDialogDismissed(false);
                    } else if (which.equals(DialogAction.NEUTRAL)) {
                        mPrefs.setApplyDialogDismissed(true);
                    }
                }
            };
            ISDialogs.showApplyAdviceDialog(dialogContext, singleButtonCallback);
        }
    }

    @Nullable
    @Override
    protected CFabEvent updateFab() {
        return new CFabEvent(false);
    }

}