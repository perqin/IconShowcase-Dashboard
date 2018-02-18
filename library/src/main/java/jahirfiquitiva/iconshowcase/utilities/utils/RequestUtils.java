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

package jahirfiquitiva.iconshowcase.utilities.utils;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import jahirfiquitiva.iconshowcase.R;

public class RequestUtils {

    public static String getTimeTextFromMillis(Context context, long millis) {
        if (TimeUnit.MILLISECONDS.toSeconds(millis) < 60) {
            return (String.valueOf(TimeUnit.MILLISECONDS.toSeconds(millis)) + " " + Utils
                    .getStringFromResources(context, R.string.seconds).toLowerCase());
        } else if (TimeUnit.MILLISECONDS.toMinutes(millis) < 60) {
            return (String.valueOf(TimeUnit.MILLISECONDS.toMinutes(millis)) + " " + Utils
                    .getStringFromResources(context, R.string.minutes).toLowerCase());
        } else if (TimeUnit.MILLISECONDS.toHours(millis) < 24) {
            return (String.valueOf(TimeUnit.MILLISECONDS.toHours(millis)) + " " + Utils
                    .getStringFromResources(context, R.string.hours).toLowerCase());
        } else if (TimeUnit.MILLISECONDS.toDays(millis) < 7) {
            return (String.valueOf(TimeUnit.MILLISECONDS.toDays(millis)) + " " + Utils
                    .getStringFromResources(context, R.string.days).toLowerCase());
        } else if (millisToWeeks(millis) < 4) {
            return (String.valueOf(millisToWeeks(millis)) + " " + Utils.getStringFromResources
                    (context, R.string.weeks).toLowerCase());
        } else {
            return (String.valueOf(millisToMonths(millis)) + " " + Utils.getStringFromResources
                    (context, R.string.months).toLowerCase());
        }
    }

    private static long millisToWeeks(long millis) {
        return TimeUnit.MILLISECONDS.toDays(millis) / 7;
    }

    private static long millisToMonths(long millis) {
        return millisToWeeks(millis) / 4;
    }

}