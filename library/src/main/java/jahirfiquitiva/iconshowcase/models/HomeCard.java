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
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import jahirfiquitiva.iconshowcase.utilities.utils.Utils;

public class HomeCard implements Parcelable {

    public static final Creator<HomeCard> CREATOR = new Creator<HomeCard>() {
        @Override
        public HomeCard createFromParcel(Parcel in) {
            return new HomeCard(in);
        }

        @Override
        public HomeCard[] newArray(int size) {
            return new HomeCard[size];
        }
    };
    private final String title;
    private final String desc;
    private final String onClickLink;
    private final boolean imgEnabled;
    private final boolean isAnApp;
    private final boolean isInstalled;
    private final Intent intent;
    private Drawable img;

    private HomeCard(Builder builder) {
        this.title = builder.title;
        this.desc = builder.desc;
        this.img = builder.img;
        this.imgEnabled = builder.imgEnabled;
        this.onClickLink = builder.onClickLink;
        this.isInstalled = builder.isInstalled;
        this.intent = builder.intent;
        this.isAnApp = builder.isAnApp;
    }

    private HomeCard(Parcel in) {
        title = in.readString();
        desc = in.readString();
        imgEnabled = in.readByte() != 0;
        onClickLink = in.readString();
        isInstalled = in.readByte() != 0;
        intent = in.readParcelable(Intent.class.getClassLoader());
        isAnApp = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(desc);
        dest.writeByte((byte) (imgEnabled ? 1 : 0));
        dest.writeString(onClickLink);
        dest.writeByte((byte) (isInstalled ? 1 : 0));
        dest.writeParcelable(intent, flags);
        dest.writeByte((byte) (isAnApp ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getOnClickLink() {
        return onClickLink;
    }

    public boolean hasImgEnabled() {
        return imgEnabled;
    }

    public boolean isAnApp() {
        return isAnApp;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public Drawable getImg() {
        return img;
    }

    public Intent getIntent() {
        return intent;
    }

    public static class Builder {

        public String title, desc, onClickLink, packageName;
        public Drawable img;
        public Context context;
        public boolean imgEnabled = false, isAnApp = false, isInstalled = false;
        public Intent intent;

        public Builder() {
            this.title = "Insert title here";
            this.desc = "Insert description here";
            this.img = null;
        }

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String desc) {
            this.desc = desc;
            return this;
        }

        public Builder icon(Drawable img) {
            this.img = img;
            this.imgEnabled = img != null;
            return this;
        }

        public Builder onClickLink(String s) {
            this.onClickLink = s;
            this.isAnApp = s.toLowerCase()
                    .contains("play.google.com/store/apps/details?id=");
            if (isAnApp) {
                this.packageName = s.substring(s.lastIndexOf("=") + 1, s.length());
                this.isInstalled = Utils.isAppInstalled(context, packageName);
                if (isInstalled) {
                    this.intent = context.getPackageManager().getLaunchIntentForPackage
                            (packageName);
                }
            }
            return this;
        }

        public HomeCard build() {
            return new HomeCard(this);
        }
    }
}