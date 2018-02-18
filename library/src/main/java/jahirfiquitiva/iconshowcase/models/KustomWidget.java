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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class KustomWidget {

    private final String widgetName;
    private final String previewPath;
    private final String previewPathLand;

    public KustomWidget(String widgetName, String previewPath, String previewPathLand) {
        this.widgetName = widgetName;
        this.previewPath = previewPath;
        this.previewPathLand = previewPathLand;
    }

    public String getPreviewPath() {
        return previewPath;
    }

    public String getPreviewPathLand() {
        return previewPathLand;
    }

    public Intent getKWGTIntent(Context context) {
        Intent kwgtIntent = new Intent();
        kwgtIntent.setComponent(new ComponentName("org.kustom.widget", "org.kustom.widget.picker" +
                ".WidgetPicker"));
        kwgtIntent.setData(Uri.parse("kfile://" + context.getPackageName() + "/widgets/" +
                widgetName));
        return kwgtIntent;
    }

}