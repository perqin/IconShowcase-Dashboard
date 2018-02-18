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
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.dialogs.ISDialogs;
import jahirfiquitiva.iconshowcase.models.CreditsItem;
import jahirfiquitiva.iconshowcase.models.DetailedCreditsItem;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.utils.IconUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.Utils;
import jahirfiquitiva.iconshowcase.views.DebouncedClickListener;
import jahirfiquitiva.iconshowcase.views.SplitButtonsLayout;

public class CreditsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<DetailedCreditsItem> detailedCredits = new ArrayList<>();
    private final ArrayList<CreditsItem> credits;
    private final Context context;
    private final Preferences mPrefs;

    public CreditsAdapter(Context context) {
        this.context = context;
        this.mPrefs = new Preferences(context);

        Resources r = context.getResources();

        final String[] titles = r.getStringArray(R.array.credits_titles);
        final String[] contents = r.getStringArray(R.array.credits_contents);
        final String[] photos = r.getStringArray(R.array.credits_photos);
        final String[] banners = r.getStringArray(R.array.credits_banners);

        final String[] buttonsNames = r.getStringArray(R.array.credits_buttons);
        final String[][] buttonsNames2 = new String[buttonsNames.length][];
        for (int i = 0; i < buttonsNames.length; i++)
            buttonsNames2[i] = buttonsNames[i].split("\\|");

        final String[] buttonsLinks = r.getStringArray(R.array.credits_links);
        final String[][] buttonsLinks2 = new String[buttonsLinks.length][];
        for (int i = 0; i < buttonsLinks.length; i++)
            buttonsLinks2[i] = buttonsLinks[i].split("\\|");

        for (int i = 0; i < titles.length; i++) {
            detailedCredits.add(new DetailedCreditsItem(banners[i], photos[i], titles[i],
                    contents[i],
                    buttonsNames2[i], buttonsLinks2[i]));
        }

        final String[] jahirBtns = {
                r.getString(R.string.visit_website),
                r.getString(R.string.google_plus),
                r.getString(R.string.play_store)
        };

        final String[] jahirLinks = {
                r.getString(R.string.dashboard_author_website),
                r.getString(R.string.dashboard_author_gplus),
                "http://play.google.com/store/apps/dev?id=7438639276314720952"
        };

        final String[] allanBtns = {
                r.getString(R.string.github),
                r.getString(R.string.google_plus),
                r.getString(R.string.play_store)
        };

        final String[] allanLinks = {
                r.getString(R.string.allan_github),
                r.getString(R.string.allan_gplus),
                r.getString(R.string.allan_play)
        };

        detailedCredits.add(new DetailedCreditsItem(r.getString(R.string.dashboard_author_banner),
                r.getString(R.string.dashboard_author_photo),
                r.getString(R.string.dashboard_author_name),
                r.getString(R.string.dashboard_author_copyright),
                jahirBtns,
                jahirLinks));

        detailedCredits.add(new DetailedCreditsItem(
                r.getString(R.string.allan_banner),
                r.getString(R.string.allan_photo),
                r.getString(R.string.allan),
                r.getString(R.string.allan_description),
                allanBtns,
                allanLinks));

        final String[] extraCreditsTitles = r.getStringArray(R.array.more_credits_titles);
        final String[] extraCreditsDrawablesNames = r.getStringArray(R.array.credits_drawables);

        credits = new ArrayList<>(extraCreditsTitles.length);
        for (int j = 0; j < extraCreditsTitles.length; j++) {
            credits.add(new CreditsItem(extraCreditsTitles[j],
                    IconUtils.getTintedDrawable(context, extraCreditsDrawablesNames[j])));
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (position < detailedCredits.size()) {
            return new DetailedCreditsHolder(inflater.inflate(R.layout.item_detailed_credit,
                    parent, false));
        }
        if (position >= detailedCredits.size()) {
            return new CreditsHolder(inflater.inflate(R.layout.item_credit, parent, false),
                    position - detailedCredits.size());
        }
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position < detailedCredits.size()) {
            DetailedCreditsItem item = detailedCredits.get(holder.getAdapterPosition());
            DetailedCreditsHolder detailedCreditsHolder = (DetailedCreditsHolder) holder;

            detailedCreditsHolder.title.setText(item.getTitle());
            detailedCreditsHolder.content.setText(item.getContent());

            if (mPrefs != null && mPrefs.getAnimationsEnabled()) {
                Glide.with(context).load(item.getPhotoLink()).diskCacheStrategy(DiskCacheStrategy
                        .SOURCE)
                        .priority(Priority.IMMEDIATE).into(detailedCreditsHolder.photo);
                Glide.with(context).load(item.getBannerLink()).diskCacheStrategy
                        (DiskCacheStrategy.SOURCE)
                        .priority(Priority.HIGH).into(detailedCreditsHolder.banner);
            } else {
                Glide.with(context).load(item.getPhotoLink()).dontAnimate().diskCacheStrategy
                        (DiskCacheStrategy.SOURCE)
                        .priority(Priority.IMMEDIATE).into(detailedCreditsHolder.photo);
                Glide.with(context).load(item.getBannerLink()).dontAnimate().diskCacheStrategy
                        (DiskCacheStrategy.SOURCE)
                        .priority(Priority.HIGH).into(detailedCreditsHolder.banner);
            }

            if (item.getBtnTexts().length > 0) {
                detailedCreditsHolder.buttons.setButtonCount(item.getBtnTexts().length);
                if (!detailedCreditsHolder.buttons.hasAllButtons()) {
                    if (item.getBtnTexts().length != item.getBtnLinks().length)
                        throw new IllegalStateException(
                                "Button names and button links must have the same number of items" +
                                        ".");
                    for (int i = 0; i < item.getBtnTexts().length; i++)
                        detailedCreditsHolder.buttons.addButton(item.getBtnTexts()[i], item
                                .getBtnLinks()[i]);
                }
            } else {
                detailedCreditsHolder.buttons.setVisibility(View.GONE);
            }

            for (int i = 0; i < detailedCreditsHolder.buttons.getChildCount(); i++)
                detailedCreditsHolder.buttons.getChildAt(i).setOnClickListener(new DebouncedClickListener() {
                    @Override
                    public void onDebouncedClick(View view) {
                        if (view.getTag() instanceof String) {
                            try {
                                Utils.openLinkInChromeCustomTab(context, (String) view.getTag());
                            } catch (Exception e) {
                                Toast.makeText(context, e.getLocalizedMessage(), Toast
                                        .LENGTH_SHORT).show();
                            }
                        }

                    }
                });
        }

        if (position >= detailedCredits.size()) {
            CreditsItem item = credits.get(holder.getAdapterPosition() - detailedCredits.size());
            CreditsHolder creditsHolder = (CreditsHolder) holder;
            creditsHolder.text.setText(item.getText());
            creditsHolder.icon.setImageDrawable(item.getIcon());
        }

    }

    @Override
    public int getItemCount() {
        int count = credits.size();
        if (detailedCredits != null) count += detailedCredits.size();
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class CreditsHolder extends RecyclerView.ViewHolder {

        private final View view;
        private final TextView text;
        private final ImageView icon;

        public CreditsHolder(View itemView, final int position) {
            super(itemView);
            view = itemView;
            text = (TextView) view.findViewById(R.id.title);
            icon = (ImageView) view.findViewById(R.id.icon);
            view.setOnClickListener(new DebouncedClickListener() {
                @Override
                public void onDebouncedClick(View v) {
                    switch (position) {
                        case 0:
                            ISDialogs.showSherryDialog(context);
                            break;
                        case 1:
                            ISDialogs.showContributorsDialog(context,
                                    context.getResources().getStringArray(R.array
                                            .contributors_links));
                            break;
                        case 2:
                            ISDialogs.showUICollaboratorsDialog(context,
                                    context.getResources().getStringArray(R.array
                                            .ui_collaborators_links));
                            break;
                        case 3:
                            ISDialogs.showTranslatorsDialogs(context);
                            break;
                        case 4:
                            ISDialogs.showLibrariesDialog(context,
                                    context.getResources().getStringArray(R.array.libs_links));
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }

    class DetailedCreditsHolder extends RecyclerView.ViewHolder {

        private final View view;
        private final TextView title;
        private final TextView content;
        private final ImageView photo;
        private final ImageView banner;
        private final SplitButtonsLayout buttons;

        public DetailedCreditsHolder(View itemView) {
            super(itemView);
            view = itemView;
            title = (TextView) view.findViewById(R.id.title);
            content = (TextView) view.findViewById(R.id.content);
            photo = (ImageView) view.findViewById(R.id.photo);
            banner = (ImageView) view.findViewById(R.id.banner);
            buttons = (SplitButtonsLayout) view.findViewById(R.id.buttons);
        }
    }

}
