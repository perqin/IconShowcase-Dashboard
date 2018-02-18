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

@SuppressWarnings("WeakerAccess")
public class NotificationItem {

    private final String text;
    private final int type;
    private final int ID;

    public NotificationItem(String text, int type, int ID) {
        this.text = text;
        this.type = type;
        this.ID = ID;
    }

    public String getText() {
        return text;
    }

    public int getType() {
        return type;
    }

    public int getID() {
        return ID;
    }
}
