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

package jahirfiquitiva.iconshowcase.config;

import android.support.annotation.ArrayRes;
import android.support.annotation.BoolRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

/**
 * Created by Allan Wang on 2016-08-19.
 * <p/>
 * With reference to Polar https://github
 * .com/afollestad/polar-dashboard/blob/master/app/src/main/java/com/afollestad/polar/config
 * /IConfig.java
 */
public interface IConfig {

    //General Functions

    boolean bool(@BoolRes int id);

    String string(@StringRes int id);

    String[] stringArray(@ArrayRes int id);

    int integer(@IntegerRes int id);

    boolean hasString(@StringRes int id);

    boolean hasArray(@ArrayRes int id);

    //Main Configs

    boolean allowDebugging();

    int appTheme();

    boolean hasDonations();

    boolean hasGoogleDonations();

    boolean hasPaypal();

    @NonNull
    String getPaypalCurrency();

    boolean devOptions();

    //Home Configs

    boolean shuffleToolbarIcons();

    boolean userWallpaperInToolbar();

    boolean hidePackInfo();

    int getIconResId(String iconName);

}