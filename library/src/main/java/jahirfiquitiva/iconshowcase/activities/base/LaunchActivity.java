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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.config.Config;
import jahirfiquitiva.iconshowcase.utilities.utils.NotificationUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.Utils;

public class LaunchActivity extends ShowcaseActivity {

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            launchShowcase(savedInstanceState);
        } catch (Exception e) {
            catchException(savedInstanceState, e);
        }
    }

    private void launchShowcase(Bundle savedInstanceState) {
        try {
            Class service = getFirebaseClass();
            if (NotificationUtils.hasNotificationExtraKey(this, getIntent(), "open_link",
                    service)) {
                super.onCreate(savedInstanceState);
                Utils.openLink(this, getIntent().getStringExtra("open_link"));
                finish();
            } else {
                if (service != null) {
                    configureAndLaunch(savedInstanceState, service);
                } else {
                    catchException(savedInstanceState, null);
                }
            }
        } catch (Exception e) {
            catchException(savedInstanceState, e);
        }
    }

    private void catchException(Bundle instance, Exception e) {
        super.onCreate(instance);
        if (e != null)
            e.printStackTrace();
        Toast.makeText(this,
                getResources().getString(R.string.launcher_icon_restorer_error,
                        getResources().getString(R.string.app_name)),
                Toast.LENGTH_LONG).show();
        finish();
    }

    private void configureAndLaunch(Bundle instance, Class service) {
        Bundle configurations = new Bundle();

        if (service != null)
            configurations.putBoolean("open_wallpapers",
                    NotificationUtils.isNotificationExtraKeyTrue(this, getIntent(), "open_walls",
                            service));

        configurations.putBoolean("enableDonations", enableDonations());
        configurations.putBoolean("enableGoogleDonations", enableGoogleDonations());
        configurations.putBoolean("enablePayPalDonations", enablePayPalDonations());
        configurations.putBoolean("enableLicenseCheck", enableLicCheck());
        configurations.putBoolean("enableAmazonInstalls", enableAmazonInstalls());
        configurations.putBoolean("checkLPF", checkLPF());
        configurations.putBoolean("checkStores", checkStores());
        configurations.putString("googlePubKey", licKey());

        if (getIntent() != null) {
            if (getIntent().getDataString() != null &&
                    getIntent().getDataString().contains("_shortcut")) {
                configurations.putString("shortcut", getIntent().getDataString());
            }

            if (getIntent().getAction() != null) {
                switch (getIntent().getAction()) {
                    case Config.APPLY_ACTION:
                        configurations.putInt("picker", Config.ICONS_APPLIER);
                        break;
                    case Config.ADW_ACTION:
                    case Config.TURBO_ACTION:
                    case Config.NOVA_ACTION:
                        configurations.putInt("picker", Config.ICONS_PICKER);
                        break;
                    case Intent.ACTION_PICK:
                    case Intent.ACTION_GET_CONTENT:
                        configurations.putInt("picker", Config.IMAGE_PICKER);
                        break;
                    case Intent.ACTION_SET_WALLPAPER:
                        configurations.putInt("picker", Config.WALLS_PICKER);
                        break;
                    default:
                        configurations.putInt("picker", 0);
                        break;
                }
            }
        }
        startShowcase(instance, configurations);
    }

    protected Class getFirebaseClass() {
        return null;
    }

    protected boolean enableDonations() {
        return false;
    }

    protected boolean enableGoogleDonations() {
        return false;
    }

    protected boolean enablePayPalDonations() {
        return false;
    }

    protected boolean enableLicCheck() {
        return true;
    }

    protected boolean enableAmazonInstalls() {
        return false;
    }

    protected boolean checkLPF() {
        return true;
    }

    protected boolean checkStores() {
        return true;
    }

    protected String licKey() {
        return "key";
    }

}