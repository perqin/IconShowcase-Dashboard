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

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import ca.allanwang.capsule.library.activities.CapsuleActivity;
import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.fragments.ApplyFragment;
import jahirfiquitiva.iconshowcase.fragments.CreditsFragment;
import jahirfiquitiva.iconshowcase.fragments.DonationsFragment;
import jahirfiquitiva.iconshowcase.fragments.FAQsFragment;
import jahirfiquitiva.iconshowcase.fragments.KustomFragment;
import jahirfiquitiva.iconshowcase.fragments.MainFragment;
import jahirfiquitiva.iconshowcase.fragments.PreviewsFragment;
import jahirfiquitiva.iconshowcase.fragments.RequestsFragment;
import jahirfiquitiva.iconshowcase.fragments.SettingsFragment;
import jahirfiquitiva.iconshowcase.fragments.WallpapersFragment;
import jahirfiquitiva.iconshowcase.fragments.ZooperFragment;
import jahirfiquitiva.iconshowcase.utilities.utils.IconUtils;

/**
 * Created by Allan Wang on 2016-10-09.
 */
public abstract class DrawerActivity extends CapsuleActivity {

    protected static boolean
            WITH_LICENSE_CHECKER = true,
            WITH_INSTALLED_FROM_AMAZON = false,
            CHECK_LPF = true,
            CHECK_STORES = true,

    WITH_DONATIONS_SECTION = false,

    //Donations stuff
    DONATIONS_GOOGLE = false,
            DONATIONS_PAYPAL = false,

    WITH_ZOOPER_SECTION = false;

    protected static String[] mGoogleCatalog = new String[0],
            GOOGLE_CATALOG_VALUES = new String[0],
            GOOGLE_CATALOG;

    protected static String GOOGLE_PUBKEY = "", PAYPAL_USER = "",
            PAYPAL_CURRENCY_CODE = "", thaAppName;

    protected static boolean mIsPremium = false, installedFromPlayStore = false;
    protected final EnumMap<DrawerItem, Integer> mDrawerMap = new EnumMap<>(DrawerItem.class);
    protected List<DrawerItem> mDrawerItems;

    private DrawerItem drawerKeyToType(String s) {
        switch (s.toLowerCase()) {
            case "previews":
                return DrawerItem.PREVIEWS;
            case "wallpapers":
                return DrawerItem.WALLPAPERS;
            case "requests":
                return DrawerItem.REQUESTS;
            case "apply":
                return DrawerItem.APPLY;
            case "faqs":
                return DrawerItem.FAQS;
            case "zooper":
                return DrawerItem.ZOOPER;
            case "kustom":
                return DrawerItem.KUSTOM;
            default:
                throw new UnsupportedOperationException("Invalid drawer key " + s + ".\nPlease " +
                        "check your primary_drawer_items array");
        }
    }

    protected void getDrawerItems() {
        mDrawerItems = new ArrayList<>();
        mDrawerItems.add(DrawerItem.HOME);

        //Convert keys to enums
        String[] configurePrimaryDrawerItems = getResources().getStringArray(
                R.array.drawer_sections);
        for (String s : configurePrimaryDrawerItems) {
            mDrawerItems.add(drawerKeyToType(s));
        }

        mDrawerItems.add(DrawerItem.CREDITS);
        mDrawerItems.add(DrawerItem.SETTINGS);
        if (WITH_DONATIONS_SECTION) mDrawerItems.add(DrawerItem.DONATE);
    }

    /*
     * Drawer item enum class
     */
    public enum DrawerItem {
        //    MAIN("Main", R.string.app_name),
        HOME("Home", R.string.section_home, R.drawable.ic_home) {
            @Override
            public Fragment getFragment() {
                return new MainFragment();
            }
        },
        PREVIEWS("Previews", R.string.section_icons, R.drawable.ic_previews) {
            @Override
            public Fragment getFragment() {
                return new PreviewsFragment();
            }
        },
        WALLPAPERS("Wallpapers", R.string.section_wallpapers, R.drawable.ic_wallpapers) {
            @Override
            public Fragment getFragment() {
                return new WallpapersFragment();
            }
        },
        REQUESTS("Requests", R.string.section_icon_request, R.drawable.ic_request) {
            @Override
            public Fragment getFragment() {
                return new RequestsFragment();
            }
        },
        APPLY("Apply", R.string.section_apply, R.drawable.ic_apply) {
            @Override
            public Fragment getFragment() {
                return new ApplyFragment();
            }
        },
        FAQS("Faqs", R.string.faqs_section, R.drawable.ic_questions) {
            @Override
            public Fragment getFragment() {
                return new FAQsFragment();
            }
        },
        ZOOPER("Zoopers", R.string.zooper_section_title, R.drawable.ic_zooper_kustom) {
            @Override
            public Fragment getFragment() {
                return new ZooperFragment();
            }
        },
        KUSTOM("Kustom", R.string.section_kustom, R.drawable.ic_zooper_kustom) {
            @Override
            public Fragment getFragment() {
                return new KustomFragment();
            }
        },
        CREDITS("Credits", R.string.section_about, 0) {
            @Override
            public Fragment getFragment() {
                return new CreditsFragment();
            }
        },
        SETTINGS("Settings", R.string.title_settings, 0) {
            @Override
            public Fragment getFragment() {
                return new SettingsFragment();
            }
        },
        DONATE("Donate", R.string.section_donate, 0) {
            @Override
            public Fragment getFragment() {
                return DonationsFragment.newInstance(DONATIONS_GOOGLE,
                        GOOGLE_PUBKEY,
                        mGoogleCatalog,
                        GOOGLE_CATALOG_VALUES,
                        DONATIONS_PAYPAL,
                        PAYPAL_USER,
                        PAYPAL_CURRENCY_CODE,
                        "DONATE",
                        false,
                        false);
            }
        };

        private final String name;
        private final int titleID;
        private int iconRes;
        private boolean isSecondary = false;

        DrawerItem(String name, @StringRes int titleID, @DrawableRes int iconRes) {
            this.name = name;
            this.titleID = titleID;
            if (iconRes == 0) {
                isSecondary = true;
            } else {
                this.iconRes = iconRes;
            }
        }

        /**
         * @param context for resource retrieval
         * @param di      drawer type
         * @param i       identifier for drawer item
         */
        public static PrimaryDrawerItem getPrimaryDrawerItem(final Context context, DrawerItem
                di, int i) {
            return new PrimaryDrawerItem().withName(context.getResources().getString(
                    di.getTitleID())).withIdentifier(i).withIcon(
                    IconUtils.getVectorDrawable(context, di.getIconRes()))
                    .withIconTintingEnabled(true);
        }

        /**
         * @param context for resource retrieval
         * @param di      drawer type
         * @param i       identifier for drawer item
         */
        public static SecondaryDrawerItem getSecondaryDrawerItem(final Context context,
                                                                 DrawerItem di, int i) {
            return new SecondaryDrawerItem().withName(context.getResources().getString(
                    di.getTitleID())).withIdentifier(i);
        }

        public int getTitleID() {
            return titleID;
        }

        public int getIconRes() {
            if (isSecondary) {
                throw new RuntimeException("Secondary DrawerTypes do not have icons");
            }
            return iconRes;
        }

        public String getName() {
            return name;
        }

        public boolean isSecondary() {
            return isSecondary;
        }

        public abstract Fragment getFragment();
    }
}