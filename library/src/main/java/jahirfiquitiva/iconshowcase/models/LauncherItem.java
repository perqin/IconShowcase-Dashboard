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

package jahirfiquitiva.iconshowcase.models;

import android.content.Context;

import jahirfiquitiva.iconshowcase.utilities.utils.Utils;

public class LauncherItem {

    private final String name;
    private final String packageName;
    private final int launcherColor;
    private int isInstalled = -1;

    public LauncherItem(String[] values, int color) {
        name = values[0];
        packageName = values[1];
        launcherColor = color;
    }

    public boolean isInstalled(Context context) {
        if (isInstalled == -1) {
            if ("org.cyanogenmod.theme.chooser".equals(packageName)) {
                if (Utils.isAppInstalled(context, "org.cyanogenmod.theme.chooser")
                        || Utils.isAppInstalled(context, "com.cyngn.theme.chooser")) {
                    isInstalled = 1;
                }
            } else {
                isInstalled = Utils.isAppInstalled(context, packageName) ? 1 : 0;
            }
        }

        // Caches this value, checking if a launcher is installed is intensive on processing
        return isInstalled == 1;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getColor() {
        return launcherColor;
    }

}