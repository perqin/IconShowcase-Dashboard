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
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jahirfiquitiva.iconshowcase.R;

public class FeaturesAdapter extends RecyclerView.Adapter<FeaturesAdapter.FeatureHolder> {

    private final String[][] mFeatures;

    public FeaturesAdapter(Context context, int featuresArray) {
        // Populate the two-dimensional array
        TypedArray typedArray = context.getResources().obtainTypedArray(featuresArray);
        mFeatures = new String[typedArray.length()][];
        for (int i = 0; i < typedArray.length(); i++) {
            int id = typedArray.getResourceId(i, 0);
            if (id > 0) {
                mFeatures[i] = context.getResources().getStringArray(id);
            }
        }
        typedArray.recycle();
    }

    @Override
    public FeatureHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new FeatureHolder(inflater.inflate(R.layout.features_content, parent, false));
    }

    @Override
    public void onBindViewHolder(FeatureHolder holder, int position) {
        String nameStr = mFeatures[position][0];
        String contentStr = "";

        for (int i = 1; i < mFeatures[position].length; i++) {
            if (i > 1) {
                // No need for new line on the first item
                contentStr += "\n";
            }
            contentStr += "\u2022 ";
            contentStr += mFeatures[position][i];
        }

        holder.title.setText(nameStr);
        holder.content.setText(contentStr);
    }

    @Override
    public int getItemCount() {
        return mFeatures == null ? 0 : mFeatures.length;
    }

    class FeatureHolder extends RecyclerView.ViewHolder {

        final View view;
        final TextView title;
        final TextView content;

        FeatureHolder(View v) {
            super(v);
            view = v;
            title = (TextView) view.findViewById(R.id.features_title);
            content = (TextView) view.findViewById(R.id.features_content);
        }
    }

}