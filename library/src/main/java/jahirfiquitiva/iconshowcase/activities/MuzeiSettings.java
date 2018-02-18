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

package jahirfiquitiva.iconshowcase.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Locale;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.config.Config;
import jahirfiquitiva.iconshowcase.dialogs.ISDialogs;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.color.ColorUtils;
import jahirfiquitiva.iconshowcase.utilities.color.ToolbarColorizer;
import jahirfiquitiva.iconshowcase.utilities.utils.IconUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.ThemeUtils;

@SuppressWarnings("ResourceAsColor")
public class MuzeiSettings extends AppCompatActivity {

    private static final int SEEKBAR_STEPS = 1;
    private static final int SEEKBAR_MAX_VALUE = 13;
    private static final int SEEKBAR_MIN_VALUE = 0;

    private Preferences mPrefs;

    private AppCompatSeekBar seekBar;
    private AppCompatCheckBox checkBox;

    private MaterialDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.muzei_settings);

        mPrefs = new Preferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ToolbarColorizer.colorizeToolbar(toolbar, ThemeUtils.darkOrLight(this,
                R.color.toolbar_text_dark, R.color.toolbar_text_light));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(ColorUtils.getAccentColor(this)));
        fab.setImageDrawable(IconUtils.getTintedDrawable(this, "ic_save",
                ColorUtils.getAccentColor(this)));

        TextView everyTitle = (TextView) findViewById(R.id.every_title);
        everyTitle.setTextColor(ColorUtils.getMaterialPrimaryTextColor(ThemeUtils.isDarkTheme()));

        final TextView everySummary = (TextView) findViewById(R.id.every_summary);
        everySummary.setTextColor(ColorUtils.getMaterialSecondaryTextColor(
                ThemeUtils.isDarkTheme()));
        everySummary.setText(getResources().getString(R.string.every_x, textFromProgress
                (mPrefs.getMuzeiRefreshInterval()).toLowerCase(Locale.getDefault())));

        seekBar = (AppCompatSeekBar) findViewById(R.id.every_seekbar);
        seekBar.incrementProgressBy(SEEKBAR_STEPS);
        seekBar.setMax((SEEKBAR_MAX_VALUE - SEEKBAR_MIN_VALUE) / SEEKBAR_STEPS);
        seekBar.setProgress(mPrefs.getMuzeiRefreshInterval());

        View divider = findViewById(R.id.divider);
        divider.setBackground(new ColorDrawable(ColorUtils.getMaterialDividerColor(
                ThemeUtils.isDarkTheme())));

        TextView wifiOnlyTitle = (TextView) findViewById(R.id.wifi_only_title);
        wifiOnlyTitle.setTextColor(ColorUtils.getMaterialPrimaryTextColor(
                ThemeUtils.isDarkTheme()));

        TextView wifiOnlySummary = (TextView) findViewById(R.id.wifi_only_summary);
        wifiOnlySummary.setTextColor(ColorUtils.getMaterialSecondaryTextColor(
                ThemeUtils.isDarkTheme()));

        checkBox = (AppCompatCheckBox) findViewById(R.id.wifi_checkbox);
        checkBox.setChecked(mPrefs.getMuzeiRefreshOnWiFiOnly());

        LinearLayout wifiOnly = (LinearLayout) findViewById(R.id.wifi_only);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = SEEKBAR_MIN_VALUE + (progress * SEEKBAR_STEPS);
                everySummary.setText(getResources().getString(R.string.every_x, textFromProgress
                        (value).toLowerCase(Locale.getDefault())));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        wifiOnly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBox.toggle();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveValues();
                finish();
            }
        });

        if (!(mPrefs.isDashboardWorking())) showShallNotPassDialog();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showConfirmDialog();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        showConfirmDialog();
    }

    private void showShallNotPassDialog() {
        if (dialog != null) dialog.dismiss();
        dialog = ISDialogs.buildShallNotPassDialog(this, null,
                new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction
                            which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(Config.MARKET_URL + getPackageName()));
                        startActivity(browserIntent);
                    }
                }, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction
                            which) {
                        finish();
                    }
                }, new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        finish();
                    }
                }, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                });
    }

    private void showConfirmDialog() {
        if (dialog != null) dialog.dismiss();
        dialog = new MaterialDialog.Builder(this)
                .title(R.string.sure_to_exit)
                .content(R.string.sure_to_exit_content)
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .neutralText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction
                            which) {
                        saveValues();
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction
                            which) {
                        finish();
                    }
                })
                .build();
        dialog.show();
    }

    private void saveValues() {
        if (seekBar != null)
            mPrefs.setMuzeiRefreshInterval(seekBar.getProgress());
        mPrefs.setMuzeiRefreshOnWiFiOnly(checkBox != null && checkBox.isChecked());
    }

    private String textFromProgress(int progress) {
        switch (progress) {
            case 0:
                return 15 + " " + getResources().getString(R.string.minutes);
            case 1:
                return 30 + " " + getResources().getString(R.string.minutes);
            case 2:
                return 45 + " " + getResources().getString(R.string.minutes);
            case 3:
                return 1 + " " + getResources().getString(R.string.hours);
            case 4:
                return 2 + " " + getResources().getString(R.string.hours);
            case 5:
                return 3 + " " + getResources().getString(R.string.hours);
            case 6:
                return 6 + " " + getResources().getString(R.string.hours);
            case 7:
                return 9 + " " + getResources().getString(R.string.hours);
            case 8:
                return 12 + " " + getResources().getString(R.string.hours);
            case 9:
                return 18 + " " + getResources().getString(R.string.hours);
            case 10:
                return 1 + " " + getResources().getString(R.string.days);
            case 11:
                return 3 + " " + getResources().getString(R.string.days);
            case 12:
                return 7 + " " + getResources().getString(R.string.days);
            case 13:
                return 14 + " " + getResources().getString(R.string.days);
        }
        return "";
    }
}