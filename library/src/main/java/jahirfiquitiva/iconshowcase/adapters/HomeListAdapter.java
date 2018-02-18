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

package jahirfiquitiva.iconshowcase.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.holders.lists.FullListHolder;
import jahirfiquitiva.iconshowcase.models.HomeCard;
import jahirfiquitiva.iconshowcase.models.IconsCategory;
import jahirfiquitiva.iconshowcase.utilities.utils.IconUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.Utils;
import jahirfiquitiva.iconshowcase.views.DebouncedClickListener;

public class HomeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final ArrayList<HomeCard> homeCards;
    private AppInfoCard hldr;
    private View view;
    private boolean hasAppsList = false;
    private boolean showMoreApps = true;
    private int cards = 3;
    private int icons = 0;
    private int wallpapers = 0;
    private int widgets = 0;

    public HomeListAdapter(ArrayList<HomeCard> homeCards, Context context, boolean hasAppsList) {
        this.context = context;
        this.homeCards = homeCards;
        this.hasAppsList = hasAppsList;
        if (context.getResources().getBoolean(R.bool.hide_pack_info)) {
            this.cards -= 1;
        }
        if (context.getString(R.string.iconpack_author_playstore).length() <= 3) {
            this.showMoreApps = false;
            this.cards -= 1;
        }
        if (hasAppsList && showMoreApps) {
            this.cards -= 1;
        }
        setupAppInfoAmounts();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        switch (i) {
            case 0:
                View welcomeCard = LayoutInflater.from(
                        viewGroup.getContext()).inflate(R.layout.item_welcome_card,
                        viewGroup, false);
                return new WelcomeCard(welcomeCard);
            case 1:
                if (!(context.getResources().getBoolean(R.bool.hide_pack_info))) {
                    View infoCard = LayoutInflater.from(
                            viewGroup.getContext()).inflate(R.layout.item_packinfo_card,
                            viewGroup, false);
                    return new AppInfoCard(infoCard);
                }
            case 2:
                if (!hasAppsList && showMoreApps) {
                    View moreAppsCard = LayoutInflater.from(
                            viewGroup.getContext()).inflate(R.layout.item_moreapps_card,
                            viewGroup, false);
                    return new MoreAppsCard(moreAppsCard);
                }
            default:
                if (hasAppsList) {
                    final View appCard = LayoutInflater.from(
                            viewGroup.getContext()).inflate(R.layout.item_app_card,
                            viewGroup, false);
                    return new AppCard(appCard, i);
                }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof WelcomeCard) {
            WelcomeCard whldr = (WelcomeCard) holder;
            if (context.getResources().getBoolean(R.bool.show_faqs_button)) {
                whldr.faqsbtn.setOnClickListener(new DebouncedClickListener() {
                    @Override
                    public void onDebouncedClick(View v) {
                        ((ShowcaseActivity) context).openFAQs();
                    }
                });
            } else {
                whldr.faqsbtn.setVisibility(View.GONE);
            }
            if (hasAppsList) {
                whldr.ratebtn.setOnClickListener(new DebouncedClickListener() {
                    @Override
                    public void onDebouncedClick(View v) {
                        Intent rate = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=" +
                                        context.getPackageName()));
                        context.startActivity(rate);
                    }
                });
            } else {
                whldr.ratebtn.setVisibility(View.GONE);
            }
            if (hasAppsList && showMoreApps) {
                whldr.moreappsbtn.setOnClickListener(new DebouncedClickListener() {
                    @Override
                    public void onDebouncedClick(View v) {
                        Utils.openLink(context,
                                context.getResources().getString(
                                        R.string.iconpack_author_playstore));
                    }
                });
            } else {
                whldr.moreappsbtn.setVisibility(View.GONE);
            }
        } else if (holder instanceof AppInfoCard) {
            this.hldr = (AppInfoCard) holder;
            setupIcons(context, hldr.iconsIV, hldr.wallsIV, hldr.widgetsIV);
            setupAppInfo();
        } else if (holder instanceof MoreAppsCard) {
            MoreAppsCard mhldr = (MoreAppsCard) holder;
            mhldr.icon.setImageDrawable(IconUtils.getTintedDrawable(context, "ic_play_store"));
            mhldr.lly.setOnClickListener(new DebouncedClickListener() {
                @Override
                public void onDebouncedClick(View v) {
                    Utils.openLink(context,
                            context.getResources().getString(R.string.iconpack_author_playstore));
                }
            });
        } else if (holder instanceof AppCard) {
            String description;
            final AppCard ahldr = (AppCard) holder;
            if (homeCards.get(ahldr.i - cards).isInstalled()) {
                description = context.getResources().getString(R.string.tap_to_open,
                        homeCards.get(ahldr.i - cards).getDesc());
            } else {
                description = context.getResources().getString(R.string.tap_to_download,
                        homeCards.get(ahldr.i - cards).getDesc());
            }
            ahldr.cardTitle.setText(homeCards.get(ahldr.i - cards).getTitle());
            ahldr.cardDesc.setText(homeCards.get(ahldr.i - cards).isAnApp() ? description :
                    homeCards.get(ahldr.i - cards).getDesc());

            if (homeCards.get(ahldr.i - cards).hasImgEnabled()) {
                ahldr.cardIcon.setImageDrawable(homeCards.get(ahldr.i - cards).getImg());
            } else {
                ahldr.subLly.removeView(ahldr.cardIcon);
            }
            view.setOnClickListener(new DebouncedClickListener() {
                @Override
                public void onDebouncedClick(View v) {
                    if (homeCards.get(ahldr.i - cards).isInstalled() && homeCards.get(ahldr.i -
                            cards).getIntent() != null) {
                        context.startActivity(homeCards.get(ahldr.i - cards).getIntent());
                    } else if (view.getVisibility() == View.VISIBLE) {
                        Utils.openLink(context, homeCards.get(ahldr.i - cards).getOnClickLink());
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return homeCards.size() + cards;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void setupIcons(Context context, ImageView iconsIV, ImageView wallsIV,
                            ImageView widgetsIV) {
        iconsIV.setImageDrawable(IconUtils.getTintedDrawable(context, "ic_android"));
        wallsIV.setImageDrawable(IconUtils.getTintedDrawable(context, "ic_multiple_wallpapers"));
        widgetsIV.setImageDrawable(IconUtils.getTintedDrawable(context, "ic_zooper_kustom"));
    }

    private void resetAppInfoValues() {
        this.icons = 0;
        this.wallpapers = 0;
        this.widgets = 0;
    }

    public void setupAppInfoAmounts() {
        resetAppInfoValues();
        int defIconsAmount = context.getResources().getInteger(R.integer.icons_amount);
        if (defIconsAmount == -1) {
            if (FullListHolder.get().iconsCategories() != null &&
                    FullListHolder.get().iconsCategories().getList() != null) {
                for (IconsCategory category : FullListHolder.get().iconsCategories().getList()) {
                    if (category.getCategoryName().equals("All")) {
                        this.icons += category.getIconsArray().size();
                    }
                }
                if (this.icons > 1) {
                    this.icons -= 1;
                }
            }
        } else {
            this.icons += defIconsAmount;
        }
        this.wallpapers += FullListHolder.get().walls() != null ?
                FullListHolder.get().walls().getList() != null
                        ? FullListHolder.get().walls().getList().size() : 0 : 0;
        this.widgets += FullListHolder.get().zooperList() != null ?
                FullListHolder.get().zooperList().getList() != null
                        ? FullListHolder.get().zooperList().getList().size() : 0 : 0;
        this.widgets += FullListHolder.get().kustomWidgets().getList() != null
                ? FullListHolder.get().kustomWidgets().getList().size() : 0;
        if (widgets > 1) {
            this.widgets -= 1;
        }
        if (((ShowcaseActivity) context).includesWidgets() && widgets == 0) {
            this.widgets += 1;
        }
    }

    public void setupAppInfo() {
        if (hldr != null) {
            hldr.icons.setVisibility((!((ShowcaseActivity) context).includesIcons()) || (icons <
                    1) ? View.GONE : View.VISIBLE);
            hldr.wallpapers.setVisibility((!((ShowcaseActivity) context).includesWallpapers()) ||
                    (wallpapers < 1) ? View.GONE : View.VISIBLE);
            hldr.widgets.setVisibility((!((ShowcaseActivity) context).includesWidgets()) ||
                    (widgets < 1) ? View.GONE : View.VISIBLE);
            hldr.iconsT.setText(context.getResources().getString(R.string.themed_icons, String
                    .valueOf(icons)));
            hldr.wallsT.setText(context.getResources().getString(R.string.available_wallpapers,
                    String.valueOf(wallpapers)));
            hldr.widgetsT.setText(context.getResources().getString(R.string.included_widgets,
                    String.valueOf(widgets)));
        }
    }

    public class WelcomeCard extends RecyclerView.ViewHolder {
        private final AppCompatButton faqsbtn;
        private final AppCompatButton ratebtn;
        private final AppCompatButton moreappsbtn;

        public WelcomeCard(View itemView) {
            super(itemView);
            faqsbtn = (AppCompatButton) itemView.findViewById(R.id.faqs_button);
            ratebtn = (AppCompatButton) itemView.findViewById(R.id.rate_button);
            moreappsbtn = (AppCompatButton) itemView.findViewById(R.id.more_apps_button);
        }
    }

    public class AppInfoCard extends RecyclerView.ViewHolder {
        private final ImageView iconsIV;
        private final ImageView wallsIV;
        private final ImageView widgetsIV;
        private final TextView iconsT;
        private final TextView wallsT;
        private final TextView widgetsT;
        private final LinearLayout icons;
        private final LinearLayout wallpapers;
        private final LinearLayout widgets;

        public AppInfoCard(View itemView) {
            super(itemView);
            iconsIV = (ImageView) itemView.findViewById(R.id.icon_themed_icons);
            wallsIV = (ImageView) itemView.findViewById(R.id.icon_available_wallpapers);
            widgetsIV = (ImageView) itemView.findViewById(R.id.icon_included_widgets);
            iconsT = (TextView) itemView.findViewById(R.id.text_themed_icons);
            wallsT = (TextView) itemView.findViewById(R.id.text_available_wallpapers);
            widgetsT = (TextView) itemView.findViewById(R.id.text_included_widgets);
            icons = (LinearLayout) itemView.findViewById(R.id.icons);
            wallpapers = (LinearLayout) itemView.findViewById(R.id.wallpapers);
            widgets = (LinearLayout) itemView.findViewById(R.id.widgets);
        }
    }

    public class MoreAppsCard extends RecyclerView.ViewHolder {
        private final LinearLayout lly;
        private final LinearLayout subLly;
        private final TextView title;
        private final TextView desc;
        private final ImageView icon;

        public MoreAppsCard(View itemView) {
            super(itemView);
            view = itemView;
            lly = (LinearLayout) itemView.findViewById(R.id.more_apps);
            title = (TextView) itemView.findViewById(R.id.more_apps_text);
            desc = (TextView) itemView.findViewById(R.id.more_apps_description);
            icon = (ImageView) itemView.findViewById(R.id.more_apps_icon);
            subLly = (LinearLayout) itemView.findViewById(R.id.more_apps_sub_layout);
        }
    }

    public class AppCard extends RecyclerView.ViewHolder {
        private final LinearLayout subLly;
        private final TextView cardTitle;
        private final TextView cardDesc;
        private final ImageView cardIcon;
        private final int i;

        public AppCard(View itemView, int pos) {
            super(itemView);
            view = itemView;
            i = pos;
            cardTitle = (TextView) itemView.findViewById(R.id.home_card_text);
            cardDesc = (TextView) itemView.findViewById(R.id.home_card_description);
            cardIcon = (ImageView) itemView.findViewById(R.id.home_card_image);
            subLly = (LinearLayout) itemView.findViewById(R.id.home_card_sub_layout);
        }
    }

}