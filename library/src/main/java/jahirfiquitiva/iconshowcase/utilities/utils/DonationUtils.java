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
import android.support.annotation.Nullable;

import org.sufficientlysecure.donations.google.util.IabHelper;
import org.sufficientlysecure.donations.google.util.IabResult;
import org.sufficientlysecure.donations.google.util.Inventory;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.config.Config;
import timber.log.Timber;

/**
 * Created by Allan Wang on 2016-08-20.
 */
@SuppressWarnings("WeakerAccess")
public class DonationUtils {

    public static void hasPurchase(final Context context, @Nullable String pubKey, final
    OnPremiumListener listener) {
        if (pubKey == null || pubKey.isEmpty() || !Config.get(context).hasGoogleDonations()) {
            listener.hasNoPurchase();
            return;
        }

        final IabHelper mHelper = new IabHelper(context, pubKey);
        final IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper
                .QueryInventoryFinishedListener() {

            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                if (inventory != null) {
                    Timber.i("IAP inventory exists");
                    for (String key : Config.get().stringArray(R.array.google_donations_items)) {
                        if (inventory.hasPurchase(key)) { //at least one donation value found,
                            // now premium
                            Timber.i("%s is purchased", key);
                            listener.hasPurchase(key);
                            return;
                        }
                    }
                }
                listener.hasNoPurchase();
            }
        };

        mHelper.queryInventoryAsync(mGotInventoryListener);
    }

    public interface OnPremiumListener {
        void hasPurchase(String purchaseCatalogKey);

        void hasNoPurchase();
    }

}
