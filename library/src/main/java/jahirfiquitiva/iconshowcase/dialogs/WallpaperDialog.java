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

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.config.Config;
import jahirfiquitiva.iconshowcase.events.WallpaperEvent;
import jahirfiquitiva.iconshowcase.tasks.ApplyWallpaper;

/**
 * @author Allan Wang
 */
public class WallpaperDialog extends BaseEventDialog {

    private static final String TAG = "wallpaper_dialog";

    private boolean setToHome;
    private boolean setToLock;

    public static void show(final FragmentActivity context, final String url) {
        showBase(context, url, WallpaperEvent.Step.START);
    }

    public static void dismiss(final FragmentActivity context) {
        Fragment frag = context.getSupportFragmentManager().findFragmentByTag(TAG);
        if (frag != null) ((WallpaperDialog) frag).dismiss();
    }

    private static void showBase(final FragmentActivity context, final String url, final
    WallpaperEvent.Step step) {
        Fragment frag = context.getSupportFragmentManager().findFragmentByTag(TAG);
        if (frag != null) ((WallpaperDialog) frag).dismiss();
        WallpaperDialog.newInstance(url, step).show(context.getSupportFragmentManager(), TAG);
    }

    private static WallpaperDialog newInstance(@NonNull final String url, final WallpaperEvent
            .Step step) {
        WallpaperDialog f = new WallpaperDialog();
        Bundle args = new Bundle();
        args.putString("wall_url", url);
        args.putSerializable("wall_step", step);
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        WallpaperEvent.Step step = (WallpaperEvent.Step) getArguments().getSerializable
                ("wall_step");
        if (step == null) step = WallpaperEvent.Step.START;

        final MaterialDialog.Builder[] builder = {new MaterialDialog.Builder(getActivity())};
        final boolean[] enteredApplyTask = {false};

        switch (step) {
            case START:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder[0].title(R.string.set_wall_to)
                            .items(R.array.wall_options)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int
                                        position, CharSequence text) {
                                    setToHome = position == 0;
                                    setToLock = position == 1;
                                    if (position == 2) {
                                        setToHome = true;
                                        setToLock = true;
                                    }
                                    showBase(getActivity(), getUrl(), WallpaperEvent.Step.LOADING);
                                }
                            });
                } else {
                    builder[0].title(R.string.apply)
                            .content(R.string.confirm_apply)
                            .positiveText(R.string.apply)
                            .negativeText(android.R.string.cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull
                                final DialogAction dialogAction) {
                                    showBase(getActivity(), getUrl(), WallpaperEvent.Step.LOADING);
                                }
                            });
                }
                break;

            case LOADING:
                try {
                    ((ShowcaseActivity) getContext()).executeApplyTask(new ApplyWallpaper
                            .ApplyWallpaperCallback() {

                        @Override
                        public void onPreExecute(Context context) {

                        }

                        //After downloaded...
                        //showBase(getActivity(), getUrl(), WallpaperEvent.Step.APPLYING);

                        @Override
                        public void onSuccess() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismiss();
                                    builder[0] = new MaterialDialog.Builder(getActivity());
                                    builder[0].content(R.string.set_as_wall_done)
                                            .positiveText(android.R.string.ok)
                                            .show();
                                    if (getActivity() instanceof ShowcaseActivity) {
                                        if (((ShowcaseActivity) getActivity()).getPickerKey() ==
                                                Config.WALLS_PICKER) {
                                            getActivity().finish();
                                        }
                                    }
                                }
                            });
                        }

                        @Override
                        public void onError() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismiss();
                                    builder[0] = new MaterialDialog.Builder(getActivity());
                                    builder[0].content(R.string.error)
                                            .positiveText(android.R.string.ok)
                                            .show();
                                }
                            });
                        }
                    }, null, getUrl(), setToHome, setToLock, setToHome && setToLock);

                } catch (Exception ignored) {
                }

                builder[0].content(R.string.downloading_wallpaper)
                        .progress(true, 0)
                        .cancelable(false)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull
                                    DialogAction which) {
                                ((ShowcaseActivity) getContext()).cancelApplyTask();
                                dismiss();
                            }
                        });

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!enteredApplyTask[0]) {
                                        String newContent = getActivity().getString(
                                                R.string.downloading_wallpaper)
                                                + "\n"
                                                + getActivity().getString(
                                                R.string.download_takes_longer);
                                        builder[0].content(newContent)
                                                .positiveText(android.R.string.cancel);
                                    }
                                }
                            });
                        }
                    }
                }, 10000);

                enteredApplyTask[0] = true;
                break;

            case APPLYING:
                String extra = "";

                if (setToHome)
                    extra = getActivity().getResources().getString(R.string.home_screen);

                if (setToLock)
                    extra = getActivity().getResources().getString(R.string.lock_screen);

                if (setToHome && setToLock)
                    extra = getActivity().getResources().getString(R.string
                            .home_lock_screens);

                builder[0].content(getActivity().getResources().getString(
                        R.string.setting_wall_title, extra.toLowerCase()))
                        .progress(true, 0)
                        .cancelable(false);
                break;

            default:
                builder[0].title(R.string.error);
                break;
        }

        return builder[0].build();
    }

    private String getUrl() {
        return getArguments().getString("wall_url", "error");
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void update(WallpaperEvent event) {
        if (event.getNextStep() == WallpaperEvent.Step.FINISH) {
            dismiss();
            return;
        }
        showBase(getActivity(), event.getUrl(), event.getNextStep());
//        switch (event.getStep()) {
//            case LOADING:
//                break;
//            case APPLYING:
//                break;
//        }
    }


}