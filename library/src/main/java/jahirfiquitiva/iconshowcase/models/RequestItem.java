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

import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class RequestItem implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RequestItem> CREATOR = new Parcelable
            .Creator<RequestItem>() {
        @Override
        public RequestItem createFromParcel(Parcel in) {
            return new RequestItem(in);
        }

        @Override
        public RequestItem[] newArray(int size) {
            return new RequestItem[size];
        }
    };
    private final Drawable normalIcon;
    private final ResolveInfo resolveInfo;
    private final String appName;
    private final String packageName;
    private final String className;
    private boolean selected = false;

    public RequestItem(@NonNull String appName, @NonNull String packageName, @NonNull String
            className,
                       @Nullable Drawable normalIcon, @Nullable ResolveInfo resolveInfo) {
        this.appName = appName;
        this.packageName = packageName;
        this.className = className;
        this.normalIcon = normalIcon;
        this.resolveInfo = resolveInfo;
    }

    private RequestItem(Parcel in) {
        appName = in.readString();
        packageName = in.readString();
        className = in.readString();
        normalIcon = in.readParcelable(Drawable.class.getClassLoader());
        resolveInfo = (ResolveInfo) in.readValue(ResolveInfo.class.getClassLoader());
        selected = in.readByte() != 0x00;
    }

    public String getClassName() {
        return className;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getNormalIcon() {
        return normalIcon;
    }

    public ResolveInfo getResolveInfo() {
        return resolveInfo;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Used to compare object to object
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof RequestItem)) {
            return false;
        }
        RequestItem that = (RequestItem) other;
        return this.appName.equals(that.appName)
                && this.packageName.equals(that.packageName)
                && this.className.equals(that.className);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appName);
        dest.writeString(packageName);
        dest.writeString(className);
        dest.writeValue(normalIcon);
        dest.writeValue(resolveInfo);
        dest.writeByte((byte) (selected ? 0x01 : 0x00));
    }
}