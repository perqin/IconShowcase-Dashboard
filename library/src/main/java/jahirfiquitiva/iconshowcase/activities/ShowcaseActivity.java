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

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.piracychecker.PiracyChecker;
import com.github.javiersantos.piracychecker.enums.InstallerID;
import com.github.javiersantos.piracychecker.enums.PiracyCheckerCallback;
import com.github.javiersantos.piracychecker.enums.PiracyCheckerError;
import com.github.javiersantos.piracychecker.enums.PirateApp;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.sufficientlysecure.donations.google.util.IabHelper;
import org.sufficientlysecure.donations.google.util.IabResult;
import org.sufficientlysecure.donations.google.util.Inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import ca.allanwang.capsule.library.changelog.ChangelogDialog;
import ca.allanwang.capsule.library.interfaces.CCollapseListener;
import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.base.TasksActivity;
import jahirfiquitiva.iconshowcase.config.Config;
import jahirfiquitiva.iconshowcase.dialogs.ISDialogs;
import jahirfiquitiva.iconshowcase.fragments.KustomFragment;
import jahirfiquitiva.iconshowcase.fragments.PreviewsFragment;
import jahirfiquitiva.iconshowcase.fragments.RequestsFragment;
import jahirfiquitiva.iconshowcase.fragments.SettingsFragment;
import jahirfiquitiva.iconshowcase.fragments.WallpapersFragment;
import jahirfiquitiva.iconshowcase.fragments.ZooperFragment;
import jahirfiquitiva.iconshowcase.holders.lists.FullListHolder;
import jahirfiquitiva.iconshowcase.models.IconItem;
import jahirfiquitiva.iconshowcase.models.WallpaperItem;
import jahirfiquitiva.iconshowcase.tasks.DownloadJSONTask;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.color.ToolbarColorizer;
import jahirfiquitiva.iconshowcase.utilities.utils.PermissionsUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.ThemeUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.Utils;
import timber.log.Timber;

public class ShowcaseActivity extends TasksActivity {

    private IabHelper mHelper;
    private Drawer drawer;

    private String shortcut;
    private boolean allowShuffle = true;
    private boolean shuffleIcons = true;
    private long currentItem = -1;
    private int numOfIcons = 4;
    private int wallpaper = -1;
    private int pickerKey = 0;

    //TODO do not save Dialog instance; use fragment tags
    private MaterialDialog dialog;
    //TODO perhaps dynamically load the imageviews rather than predefining 8
    private ImageView icon1, icon2, icon3, icon4, icon5, icon6, icon7, icon8;
    private ImageView toolbarHeader;
    private Drawable wallpaperDrawable;

    private PiracyChecker checker = null;

    public void startShowcase(Bundle savedInstance, Bundle configuration) {
        ThemeUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstance);

        shortcut = configuration.getString("shortcut");
        boolean openWallpapers = configuration.getBoolean("open_wallpapers", false) ||
                (shortcut != null && shortcut.equals("wallpapers_shortcut"));

        //TODO remove all this; donations will exist if they are configured
        WITH_DONATIONS_SECTION = configuration.getBoolean("enableDonations", false);
        DONATIONS_GOOGLE = configuration.getBoolean("enableGoogleDonations", false);
        DONATIONS_PAYPAL = configuration.getBoolean("enablePayPalDonations", false);

        WITH_LICENSE_CHECKER = configuration.getBoolean("enableLicenseCheck", false);
        WITH_INSTALLED_FROM_AMAZON = configuration.getBoolean("enableAmazonInstalls", false);
        CHECK_LPF = configuration.getBoolean("checkLPF", false);
        CHECK_STORES = configuration.getBoolean("checkStores", false);

        GOOGLE_PUBKEY = configuration.getString("googlePubKey");

        pickerKey = configuration.getInt("picker");

        shuffleIcons = getResources().getBoolean(R.bool.shuffle_toolbar_icons);

        try {
            String installer = Utils.getAppInstaller(this);
            if (installer.matches(Config.PLAY_STORE_INSTALLER) ||
                    installer.matches(Config.PLAY_STORE_PACKAGE)) {
                installedFromPlayStore = true;
            }
        } catch (Exception ignored) {
        }

        setupDonations();

        //TODO dynamically add icons rather than defining 8 and hiding those that aren't used
        numOfIcons = getResources().getInteger(R.integer.toolbar_icons);

        icon1 = (ImageView) findViewById(R.id.iconOne);
        icon2 = (ImageView) findViewById(R.id.iconTwo);
        icon3 = (ImageView) findViewById(R.id.iconThree);
        icon4 = (ImageView) findViewById(R.id.iconFour);
        icon5 = (ImageView) findViewById(R.id.iconFive);
        icon6 = (ImageView) findViewById(R.id.iconSix);
        icon7 = (ImageView) findViewById(R.id.iconSeven);
        icon8 = (ImageView) findViewById(R.id.iconEight);

        capsulate().toolbar(R.id.toolbar).appBarLayout(R.id.appbar).coordinatorLayout(R.id
                .mainCoordinatorLayout);

        cCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        toolbarHeader = (ImageView) findViewById(R.id.toolbarHeader);

        thaAppName = getResources().getString(R.string.app_name);

        cCollapsingToolbarLayout.setTitle(thaAppName);
        setupDrawer(savedInstance);

        //Setup donations
        if (DONATIONS_GOOGLE) {
            final IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper
                    .QueryInventoryFinishedListener() {

                public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                    if (inventory != null) {
                        Timber.i("IAP inventory exists");
                        for (String aGOOGLE_CATALOG : GOOGLE_CATALOG) {
                            Timber.i(aGOOGLE_CATALOG, "is", inventory.hasPurchase(aGOOGLE_CATALOG));
                            if (inventory.hasPurchase(aGOOGLE_CATALOG)) { //at least one donation
                                // value found, now premium
                                mIsPremium = true;
                            }
                        }
                    }
                    if (isPremium()) {
                        mGoogleCatalog = GOOGLE_CATALOG;
                    }
                }
            };

            mHelper = new IabHelper(ShowcaseActivity.this, GOOGLE_PUBKEY);
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        Timber.d("In-app Billing setup failed: ", result);
                        clearDialog();
                        dialog = new MaterialDialog.Builder(ShowcaseActivity.this)
                                .title(R.string.donations_error_title)
                                .content(R.string.donations_error_content)
                                .positiveText(android.R.string.ok)
                                .build();
                        dialog.show();
                    } else {
                        mHelper.queryInventoryAsync(false, mGotInventoryListener);
                    }

                }
            });
        }

        if (savedInstance == null) {
            if ((pickerKey == Config.ICONS_PICKER || pickerKey == Config.IMAGE_PICKER)
                    && mDrawerMap.containsKey(DrawerItem.PREVIEWS)) {
                drawerItemClick(mDrawerMap.get(DrawerItem.PREVIEWS));
            } else if (((pickerKey == Config.WALLS_PICKER) || openWallpapers)
                    && mPrefs.isDashboardWorking()
                    && mDrawerMap.containsKey(DrawerItem.WALLPAPERS)) {
                drawerItemClick(mDrawerMap.get(DrawerItem.WALLPAPERS));
            } else if (pickerKey == Config.ICONS_APPLIER && mPrefs.isDashboardWorking()
                    && mDrawerMap.containsKey(DrawerItem.APPLY)) {
                drawerItemClick(mDrawerMap.get(DrawerItem.APPLY));
            } else if ((shortcut != null && shortcut.equals("request_shortcut"))
                    && mDrawerMap.containsKey(DrawerItem.REQUESTS)) {
                drawerItemClick(mDrawerMap.get(DrawerItem.REQUESTS));
            } else {
                if (mPrefs.getSettingsModified()) {
                    //TODO remove this from preferences; this can be sent via bundle itself
                    //TODO check this
                    long settingsIdentifier = 0;
                    drawerItemClick(settingsIdentifier);
                } else {
                    currentItem = -1;
                    drawerItemClick(0);
                }
            }
        }

        //Load last, load all other data first
        startTasks();
    }

    private Activity getActivity() {
        return this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pickerKey == 0) {
            setupToolbarHeader();
        }
        new CustomizeToolbar().setCollapseListener(new CCollapseListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                // Do nothing
            }

            @Override
            public void onVerticalOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                ToolbarColorizer.setCollapsingToolbarColorForOffset(getActivity(),
                        cToolbar, verticalOffset);
            }
        });
        ToolbarColorizer.setupCollapsingToolbarTextColors(this, cCollapsingToolbarLayout, true);
        runLicenseChecker(WITH_LICENSE_CHECKER, GOOGLE_PUBKEY, WITH_INSTALLED_FROM_AMAZON,
                CHECK_LPF, CHECK_STORES);
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearDialog();
        destroyChecker();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearDialog();
        destroyChecker();
    }

    private void clearDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    private void destroyChecker() {
        if (checker != null) {
            checker.destroy();
            checker = null;
        }
    }

    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(getFragmentId());
    }

    private void reloadFragment(DrawerItem dt) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(getFragmentId(), dt.getFragment(), dt.getName());
        if (mPrefs.getAnimationsEnabled())
            fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter,
                    R.anim.pop_exit);
        fragmentTransaction.commit();
    }

    @SuppressWarnings("ResourceAsColor")
    private void setupDrawer(Bundle savedInstanceState) {
        getDrawerItems();

        DrawerBuilder drawerBuilder = new DrawerBuilder().withActivity(this)
                .withToolbar(cToolbar)
                // .withFireOnInitialOnClick(true)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {

                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            drawerItemClick(drawerItem.getIdentifier());
                        }
                        return false;
                    }
                });

        for (int position = 0; position < mDrawerItems.size(); position++) {
            DrawerItem item = mDrawerItems.get(position);
            if (item == DrawerItem.CREDITS) {
                drawerBuilder.addDrawerItems(new DividerDrawerItem());
            }
            mDrawerMap.put(item, position);
            drawerBuilder.addDrawerItems(
                    !item.isSecondary() ? DrawerItem.getPrimaryDrawerItem(this, item, position) :
                            DrawerItem.getSecondaryDrawerItem(this, item, position));
        }

        drawerBuilder.withSavedInstance(savedInstanceState);

        String headerAppName = "", headerAppVersion = "";

        boolean withDrawerTexts;

        withDrawerTexts = Config.get().devOptions() ? mPrefs.getDevDrawerTexts() :
                getResources().getBoolean(R.bool.with_drawer_texts);

        if (withDrawerTexts) {
            headerAppName = getResources().getString(R.string.app_long_name);
            headerAppVersion = "v " + Utils.getAppVersion(this);
        }

        AccountHeader drawerHeader = new AccountHeaderBuilder().withActivity(this)
                .withHeaderBackground(R.drawable.drawer_header)
                .withSelectionFirstLine(headerAppName)
                .withSelectionSecondLine(headerAppVersion)
                .withProfileImagesClickable(false)
                .withResetDrawerOnProfileListClick(false)
                .withSelectionListEnabled(false)
                .withSelectionListEnabledForSingleProfile(false)
                .withSavedInstance(savedInstanceState)
                .build();

        TextView drawerTitle = (TextView)
                drawerHeader.getView().findViewById(R.id.material_drawer_account_header_name);
        TextView drawerSubtitle = (TextView)
                drawerHeader.getView().findViewById(R.id.material_drawer_account_header_email);
        setTextAppearance(drawerTitle, R.style.DrawerTextsWithShadow);
        setTextAppearance(drawerSubtitle, R.style.DrawerTextsWithShadow);

        drawerBuilder.withAccountHeader(drawerHeader);

        drawer = drawerBuilder.build();
    }

    public void drawerItemClick(long id) {
        switchFragment(id, mDrawerItems.get((int) id));
    }

    private void switchFragment(long itemId, DrawerItem dt) {

        // Don't allow re-selection of the currently active item
        if (currentItem == itemId) return;

        currentItem = itemId;

        // TODO Make sure this works fine even after configuration changes
        if ((dt == DrawerItem.HOME) && (pickerKey == 0) &&
                (shortcut == null || shortcut.length() < 1)) {
            expandAppBar(mPrefs != null && mPrefs.getAnimationsEnabled());
        } else {
            collapseAppBar(mPrefs != null && mPrefs.getAnimationsEnabled());
        }

        if (dt == DrawerItem.HOME) {
            icon1.setVisibility(View.INVISIBLE);
            icon2.setVisibility(View.INVISIBLE);
            icon3.setVisibility(View.INVISIBLE);
            icon4.setVisibility(View.INVISIBLE);
        }

        //Fragment Switcher
        reloadFragment(dt);

        cCollapsingToolbarLayout.setTitle(dt.getName().equals("Home")
                                          ? Config.get().string(R.string.app_name)
                                          : getString(dt.getTitleID()));

        drawer.setSelection(itemId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (drawer != null) {
            outState = drawer.saveInstanceState(outState);
        }
        if (cCollapsingToolbarLayout != null && cCollapsingToolbarLayout.getTitle() != null) {
            outState.putString("toolbarTitle", cCollapsingToolbarLayout.getTitle().toString());
        }
        outState.putInt("currentSection", (int) currentItem);
        outState.putInt("pickerKey", pickerKey);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (cCollapsingToolbarLayout != null) {
            ToolbarColorizer.setupCollapsingToolbarTextColors(this, cCollapsingToolbarLayout, true);
            cCollapsingToolbarLayout.setTitle(savedInstanceState.getString("toolbarTitle",
                    thaAppName));
            pickerKey = savedInstanceState.getInt("pickerKey", 0);
        }
        drawerItemClick(savedInstanceState.getInt("currentSection"));
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else if (drawer != null && currentItem != 0 && (pickerKey == 0) &&
                (shortcut == null || shortcut.length() < 1)) {
            drawer.setSelection(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionsUtils.PERMISSION_REQUEST_CODE) {
            if (permissionGranted(grantResults)) {
                if (getCurrentFragment() instanceof RequestsFragment) {
                    ((RequestsFragment) getCurrentFragment()).startRequestProcess();
                } else if (getCurrentFragment() instanceof SettingsFragment) {
                    ((SettingsFragment) getCurrentFragment()).showFolderChooserDialog();
                } else if (getCurrentFragment() instanceof ZooperFragment) {
                    ((ZooperFragment) getCurrentFragment()).getAdapter().installAssets();
                }
            } else if (PermissionsUtils.getListener() != null) {
                PermissionsUtils.getListener().onPermissionGranted();
            } else {
                ISDialogs.showPermissionNotGrantedDialog(this);
            }
        } else {
            ISDialogs.showPermissionNotGrantedDialog(this);
        }
    }

    private boolean permissionGranted(int[] results) {
        return results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int i = item.getItemId();
        if (i == R.id.changelog) {
            showChangelogDialog();
        } else if (i == R.id.refresh) {
            if (getCurrentFragment() instanceof WallpapersFragment) {
                FullListHolder.get().walls().clearList();
                executeJsonTask(new DownloadJSONTask.JSONDownloadCallback() {
                    @Override
                    public void onPreExecute(Context context) {
                        ((WallpapersFragment) getCurrentFragment()).refreshContent(context);
                    }

                    @Override
                    public void onSuccess(ArrayList<WallpaperItem> wallpapers) {
                        FullListHolder.get().walls().createList(wallpapers);
                        ((WallpapersFragment) getCurrentFragment()).setupContent();
                    }
                });
            } else {
                Toast.makeText(this, "Can't perform this action from here.", Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (i == R.id.columns) {
            if (getCurrentFragment() instanceof WallpapersFragment) {
                ISDialogs.showColumnsSelectorDialog(this,
                        ((WallpapersFragment) getCurrentFragment()));
            } else {
                Toast.makeText(this, "Can't perform this action from here.", Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (i == R.id.select_all) {
            if (getCurrentFragment() instanceof RequestsFragment) {
                if (((RequestsFragment) getCurrentFragment()).getAdapter() != null && (
                        (RequestsFragment) getCurrentFragment()).getAdapter().getItemCount() > 0) {
                    ((RequestsFragment) getCurrentFragment()).getAdapter().selectOrDeselectAll();
                }
            } else {
                Toast.makeText(this, "Can't perform this action from here.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("donationsFragment");
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void resetFragment(DrawerItem item) {
        switch (item) {
            case PREVIEWS:
                if (getCurrentFragment() instanceof PreviewsFragment) {
                    reloadFragment(item);
                }
                break;
            case ZOOPER:
                if (getCurrentFragment() instanceof ZooperFragment) {
                    reloadFragment(item);
                }
                break;
            case KUSTOM:
                if (getCurrentFragment() instanceof KustomFragment) {
                    reloadFragment(item);
                }
                break;
        }
    }

    @Override
    protected int getFragmentId() {
        return R.id.main;
    }

    /**
     * Gets the fab ID
     *
     * @return fabID
     */
    @Override
    protected int getFabId() {
        return R.id.fab;
    }

    /**
     * Gets your layout ID for the activity
     *
     * @return layoutID
     */
    @Override
    protected int getContentViewId() {
        return R.layout.showcase_activity;
    }

    @SuppressWarnings("deprecation")
    private void setTextAppearance(TextView text, @StyleRes int style) {
        if (text != null) {
            if (Build.VERSION.SDK_INT < 23) {
                text.setTextAppearance(this, style);
            } else {
                text.setTextAppearance(style);
            }
        }
    }

    public void setupIcons(int delay) {
        if (FullListHolder.get().home() == null ||
                FullListHolder.get().home().isEmpty() ||
                !includesIcons()) return;

        if (FullListHolder.get().home().getList().size() < 8) {
            Log.e(getString(R.string.app_name),
                    "You didn't set at least 8 icons for the toolbar previews." +
                            "No icons will be shown there until you do so.");
            return;
        }

        ArrayList<IconItem> finalIconsList = new ArrayList<>();

        if (allowShuffle && shuffleIcons) {
            Collections.shuffle(FullListHolder.get().home().getList());
        }

        for (int i = 0; i < numOfIcons; i++) {
            finalIconsList.add(FullListHolder.get().home().getList().get(i));
        }

        if (delay >= 0)
            animateIcons(delay, finalIconsList);

        allowShuffle = false;
    }

    private void animateIcons(int delay, final ArrayList<IconItem> resources) {
        if (!(includesIcons())) return;
        if (mPrefs.getAnimationsEnabled()) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    playIconsAnimations(500, resources);
                }
            }, (long) (delay / 1.5f));
        }
    }

    private void playIconsAnimations(int duration, ArrayList<IconItem> resources) {
        if (!(includesIcons())) return;
        if (pickerKey == 0) {
            try {
                switch (numOfIcons) {
                    case 8:
                        animateIcon(icon7, duration, resources.get(6).getResId());
                        animateIcon(icon8, duration, resources.get(7).getResId());
                    case 6:
                        animateIcon(icon5, duration, resources.get(4).getResId());
                        animateIcon(icon6, duration, resources.get(5).getResId());
                    case 4:
                        animateIcon(icon1, duration, resources.get(0).getResId());
                        animateIcon(icon2, duration, resources.get(1).getResId());
                        animateIcon(icon3, duration, resources.get(2).getResId());
                        animateIcon(icon4, duration, resources.get(3).getResId());
                        break;
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void animateIcon(ImageView icon, int duration, @DrawableRes int resource) {
        if (icon.getAnimation() != null) icon.clearAnimation();
        icon.setScaleX(0.0f);
        icon.setScaleY(0.0f);
        icon.setAlpha(0.0f);
        icon.setImageResource(resource);
        if (icon.getVisibility() != View.VISIBLE) icon.setVisibility(View.VISIBLE);
        expandIcon(icon, duration);
        /*
        icon.animate()
                .alpha(0.0f)
                .scaleX(0.0f)
                .scaleY(0.0f)
                .setStartDelay(10)
                .setDuration(clicked ? duration : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        expandIcon(icon, duration);
                    }
                })
                .start(); */
    }

    private void expandIcon(ImageView icon, int duration) {
        icon.animate()
                .alpha(1.0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setStartDelay(100)
                .setDuration(duration)
                .start();
    }

    public void setupToolbarHeader() {
        try {
            if ((pickerKey == 0) && (shortcut == null || shortcut.length() < 1)) {
                if (Config.get().userWallpaperInToolbar() &&
                        mPrefs.getWallpaperAsToolbarHeaderEnabled()) {
                    WallpaperManager wm = WallpaperManager.getInstance(this);
                    if (wm != null) {
                        Drawable currentWallpaper = wm.getFastDrawable();
                        if (currentWallpaper != null) {
                            wallpaperDrawable = currentWallpaper;
                            toolbarHeader.setAlpha(0.95f);
                            toolbarHeader.setImageDrawable(wallpaperDrawable);
                        }
                    }
                } else {
                    String defPicture = getResources().getString(R.string.toolbar_picture);
                    if (defPicture.length() > 0) {
                        int res = getResources().getIdentifier(defPicture, "drawable",
                                getPackageName
                                        ());
                        if (res != 0) {
                            wallpaperDrawable = ContextCompat.getDrawable(this, res);
                            toolbarHeader.setImageDrawable(wallpaperDrawable);
                        }
                    } else {
                        String[] wallpapers = getResources().getStringArray(R.array.wallpapers);
                        if (wallpapers.length > 0) {
                            int res;
                            ArrayList<Integer> wallpapersArray = new ArrayList<>();
                            for (String wallpaper : wallpapers) {
                                res = getResources().getIdentifier(wallpaper, "drawable",
                                        getPackageName());
                                if (res != 0) {
                                    wallpapersArray.add(res);
                                }
                            }
                            Random random = new Random();
                            if (wallpaper == -1) {
                                wallpaper = random.nextInt(wallpapersArray.size());
                            }
                            wallpaperDrawable = ContextCompat.getDrawable(this, wallpapersArray.get
                                    (wallpaper));
                            toolbarHeader.setImageDrawable(wallpaperDrawable);
                        }
                    }
                }
            }
            toolbarHeader.setVisibility(View.VISIBLE);
        } catch (Exception ignored) {
        }
    }

    private void setupDonations() {
        //donations stuff

        if (installedFromPlayStore) {
            // Disable donation methods not allowed by Google
            DONATIONS_PAYPAL = false;
        }

        //google
        if (DONATIONS_GOOGLE) {
            GOOGLE_CATALOG = getResources().getStringArray(R.array.google_donations_items);
            mGoogleCatalog = GOOGLE_CATALOG;
            GOOGLE_CATALOG_VALUES = getResources().getStringArray(R.array.google_donations_catalog);

            try {
                if (GOOGLE_PUBKEY.length() < 50 || GOOGLE_CATALOG_VALUES.length <= 0 ||
                        (GOOGLE_CATALOG.length != GOOGLE_CATALOG_VALUES.length)) {
                    DONATIONS_GOOGLE = false; //google donations setup is incorrect
                }
            } catch (Exception e) {
                DONATIONS_GOOGLE = false;
            }

        }

        //paypal
        if (DONATIONS_PAYPAL) {
            PAYPAL_USER = getResources().getString(R.string.paypal_email);
            PAYPAL_CURRENCY_CODE = getResources().getString(R.string.paypal_currency_code);
            if (PAYPAL_USER.length() <= 5 || PAYPAL_CURRENCY_CODE.length() <= 1) {
                DONATIONS_PAYPAL = false; //paypal content incorrect
            }
        }

        if (WITH_DONATIONS_SECTION) {
            WITH_DONATIONS_SECTION = DONATIONS_GOOGLE || DONATIONS_PAYPAL;
            //if one of the donations is enabled, then the section is enabled
        }
    }

    private void runLicenseChecker(boolean ch, String lic, boolean allAma,
                                   boolean checkLPF, boolean checkStores) {
        Preferences mPrefs = new Preferences(this);
        mPrefs.setSettingsModified(false);
        if (ch) {
            if (Utils.isNewVersion(this) || (!(mPrefs.isDashboardWorking()))) {
                if (Utils.hasNetwork(this)) {
                    try {
                        checkLicense(lic, allAma, checkLPF, checkStores);
                    } catch (Exception e) {
                        showSimpleLicenseCheckErrorDialog();
                    }
                } else {
                    showSimpleLicenseCheckErrorDialog();
                }
            }
        } else {
            mPrefs.setDashboardWorking(true);
            showNewVersionChangelogDialog();
        }
    }

    private void showNewVersionChangelogDialog() {
        if (Utils.isNewVersion(this)) showChangelogDialog();
    }

    private void showChangelogDialog() {
        clearDialog();
        if (includesIcons()) {
            ChangelogDialog.show(this, R.xml.changelog,
                    getResources().getBoolean(R.bool.show_check_new_icons_button)
                            ? new ChangelogDialog.OnChangelogNeutralButtonClick() {
                        @Override
                        public int getNeutralText() {
                            return R.string.changelog_neutral_text;
                        }

                        @Override
                        public void onNeutralButtonClick(@NonNull MaterialDialog dialog) {
                            dialog.dismiss();
                            openPreviews();
                        }
                    } : null);
        } else {
            ChangelogDialog.show(this, R.xml.changelog, null);
        }
    }

    private void checkLicense(final String lic, final boolean allAma, final boolean checkLPF,
                              final boolean checkStores) {
        destroyChecker();
        checker = new PiracyChecker(this);
        checker.enableInstallerId(InstallerID.GOOGLE_PLAY);
        if (lic != null && lic.length() > 50) checker.enableGooglePlayLicensing(lic);
        if (allAma) checker.enableInstallerId(InstallerID.AMAZON_APP_STORE);
        if (checkLPF) checker.enableUnauthorizedAppsCheck();
        if (checkStores) checker.enableStoresCheck();
        checker.enableEmulatorCheck(false)
                .enableDebugCheck()
                .callback(new PiracyCheckerCallback() {
                    @Override
                    public void allow() {
                        showLicensedDialog();
                    }

                    @Override
                    public void dontAllow(@NonNull PiracyCheckerError piracyCheckerError,
                                          @Nullable final PirateApp pirateApp) {
                        showNotLicensedDialog(pirateApp);
                    }

                    @Override
                    public void onError(@NonNull PiracyCheckerError error) {
                        showLicenseCheckErrorDialog(lic, allAma, checkLPF, checkStores);
                    }
                });
        checker.start();
    }

    private void showLicensedDialog() {
        clearDialog();
        dialog = ISDialogs.buildLicenseSuccessDialog(this,
                new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                        mPrefs.setDashboardWorking(true);
                        showNewVersionChangelogDialog();
                    }
                }, new MaterialDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mPrefs.setDashboardWorking(true);
                        showNewVersionChangelogDialog();
                    }
                }, new MaterialDialog.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mPrefs.setDashboardWorking(true);
                        showNewVersionChangelogDialog();
                    }
                });
        dialog.show();
    }

    private void showLicenseCheckErrorDialog(final String lic, final boolean allAma,
                                             final boolean checkLPF, final boolean checkStores) {
        clearDialog();
        dialog = ISDialogs.buildLicenseErrorDialog(this,
                new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                        dialog.dismiss();
                        checkLicense(lic, allAma, checkLPF, checkStores);
                    }
                }, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                        finish();
                    }
                }, new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        finish();
                    }
                }, new MaterialDialog.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                });
        dialog.show();
    }

    private void showSimpleLicenseCheckErrorDialog() {
        clearDialog();
        dialog = ISDialogs.buildLicenseErrorDialog(this, null,
                new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull
                            DialogAction which) {
                        finish();
                    }
                }, new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        finish();
                    }
                }, new MaterialDialog.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                });
        dialog.show();
    }

    private void showNotLicensedDialog(PirateApp app) {
        mPrefs.setDashboardWorking(false);
        clearDialog();
        dialog = ISDialogs.buildShallNotPassDialog(this, app,
                new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull
                            DialogAction
                            dialogAction) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(Config.MARKET_URL + getPackageName())));
                    }
                }, new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull
                            DialogAction
                            dialogAction) {
                        finish();
                    }
                }, new MaterialDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                }, new MaterialDialog.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
        dialog.show();
    }

    private boolean isPremium() {
        return mIsPremium;
    }

    public void setSettingsDialog(MaterialDialog settingsDialog) {
        clearDialog();
        this.dialog = settingsDialog;
    }

    public Toolbar getToolbar() {
        return cToolbar;
    }

    public Drawer getDrawer() {
        return drawer;
    }

    public boolean includesZooper() {
        return mDrawerMap.containsKey(DrawerItem.ZOOPER);
    }

    public boolean includesWidgets() {
        return mDrawerMap.containsKey(DrawerItem.ZOOPER) ||
                (mDrawerMap.containsKey(DrawerItem.KUSTOM) &&
                        FullListHolder.get().kustomWidgets() != null &&
                        FullListHolder.get().kustomWidgets().getList() != null &&
                        FullListHolder.get().kustomWidgets().getList().size() > 0);
    }

    public boolean includesIcons() {
        return mDrawerMap.containsKey(DrawerItem.PREVIEWS);
    }

    public boolean includesWallpapers() {
        return mDrawerMap.containsKey(DrawerItem.WALLPAPERS);
    }

    public boolean allowShuffle() {
        return allowShuffle;
    }

    @SuppressWarnings("SameParameterValue")
    public void setAllowShuffle(boolean newValue) {
        this.allowShuffle = newValue;
    }

    public int getPickerKey() {
        return pickerKey;
    }

    public Drawable getWallpaperDrawable() {
        return wallpaperDrawable;
    }

    public void openPreviews() {
        drawerItemClick(mDrawerMap.get(DrawerItem.PREVIEWS));
    }

    public void openFAQs() {
        drawerItemClick(mDrawerMap.get(DrawerItem.FAQS));
    }
}
