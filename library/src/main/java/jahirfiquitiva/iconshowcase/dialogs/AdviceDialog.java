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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.utilities.Preferences;

/**
 * @author Allan Wang
 */
@SuppressWarnings("SameParameterValue")
public class AdviceDialog extends DialogFragment {

    private static final String TAG = "advice_dialog";

    public static void show(final FragmentActivity context) {
        Preferences prefs = new Preferences(context);

        switch (Type.WALLPAPER) {
            case WALLPAPER:
                if (prefs.getWallsDialogDismissed()) return;
        }

        Fragment frag = context.getSupportFragmentManager().findFragmentByTag(TAG);
        if (frag != null) ((AdviceDialog) frag).dismiss();
        AdviceDialog.newInstance(Type.WALLPAPER).show(context.getSupportFragmentManager(), TAG);
    }

    public static void dismiss(final FragmentActivity context) {
        Fragment frag = context.getSupportFragmentManager().findFragmentByTag(TAG);
        if (frag != null) ((AdviceDialog) frag).dismiss();
    }

    @SuppressWarnings("UnusedParameters")
    private static AdviceDialog newInstance(@NonNull final Type type) {
        AdviceDialog f = new AdviceDialog();
        Bundle args = new Bundle();
        args.putSerializable("type", Type.WALLPAPER);
        f.setArguments(args);
        return f;
    }

    private Preferences getPrefs() {
        return new Preferences(getActivity());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Type type = (Type) getArguments().getSerializable("type");
        if (type == null) type = Type.ERROR;

        final MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());

        builder.title(R.string.advice)
                .positiveText(R.string.close)
                .neutralText(R.string.dontshow);

        switch (type) {
            case WALLPAPER:
                builder.content(R.string.walls_advice)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull
                                    DialogAction which) {
                                getPrefs().setWallsDialogDismissed(true);
                            }
                        });
                break;
            default:
                builder.content(R.string.error);
                break;
        }

        return builder.build();
    }

    public enum Type {
        WALLPAPER, ERROR
    }

}