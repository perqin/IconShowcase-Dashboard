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

package jahirfiquitiva.iconshowcase.fragments;
/*
 * Copyright (C) 2011-2015 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.sufficientlysecure.donations.google.util.IabHelper;
import org.sufficientlysecure.donations.google.util.IabResult;
import org.sufficientlysecure.donations.google.util.Purchase;

import ca.allanwang.capsule.library.event.CFabEvent;
import ca.allanwang.capsule.library.fragments.CapsuleFragment;
import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.base.DrawerActivity;
import jahirfiquitiva.iconshowcase.views.DebouncedClickListener;
import timber.log.Timber;

public class DonationsFragment extends CapsuleFragment {

    private static final String ARG_DEBUG = "debug";

    private static final String ARG_GOOGLE_ENABLED = "googleEnabled";
    private static final String ARG_GOOGLE_PUBKEY = "googlePubkey";
    private static final String ARG_GOOGLE_CATALOG = "googleCatalog";
    private static final String ARG_GOOGLE_CATALOG_VALUES = "googleCatalogValues";

    private static final String ARG_PAYPAL_ENABLED = "paypalEnabled";
    private static final String ARG_PAYPAL_USER = "paypalUser";
    private static final String ARG_PAYPAL_CURRENCY_CODE = "paypalCurrencyCode";
    private static final String ARG_PAYPAL_ITEM_NAME = "mPaypalItemName";

    private static final String ARG_FLATTR_ENABLED = "flattrEnabled";
    private static final String ARG_FLATTR_PROJECT_URL = "flattrProjectUrl";
    private static final String ARG_FLATTR_URL = "flattrUrl";

    private static final String ARG_BITCOIN_ENABLED = "bitcoinEnabled";
    private static final String ARG_BITCOIN_ADDRESS = "bitcoinAddress";

    // http://developer.android.com/google/play/billing/billing_testing.html
    private static final String[] CATALOG_DEBUG = new String[]{"android.test.purchased",
            "android.test.canceled", "android.test.refunded", "android.test.item_unavailable"};

    private Spinner mGoogleSpinner;

    // Google Play helper object
    private IabHelper mHelper;

    private boolean mDebug = false;
    // Called when consumption is complete
    private final IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper
            .OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (mDebug)
                Timber.d("Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isSuccess()) {
                if (mDebug)
                    Timber.d("Consumption successful. Provisioning.");
            }
            if (mDebug)
                Timber.d("End consumption flow.");
        }
    };
    // Callback for when a purchase is finished
    private final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new
            IabHelper.OnIabPurchaseFinishedListener() {
                public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                    if (mDebug)
                        Timber.d("Purchase finished: " + result + ", purchase: " + purchase);

                    // if we were disposed of in the meantime, quit.
                    if (mHelper == null) return;

                    if (result.isSuccess()) {
                        if (mDebug)
                            Timber.d("Purchase successful.");

                        // directly consume in-app purchase, so that people can donate multiple
                        // times
                        mHelper.consumeAsync(purchase, mConsumeFinishedListener);

                        // show thanks openDialog
                        openDialog(android.R.drawable.ic_dialog_info, R.string
                                        .donations__thanks_dialog_title,
                                getString(R.string.donations__thanks_dialog));
                    }
                }
            };
    private boolean mGoogleEnabled = false;
    private String mGooglePubkey = "";
    private String[] mGgoogleCatalog = new String[]{};
    private String[] mGoogleCatalogValues = new String[]{};
    private boolean mPaypalEnabled = false;
    private String mPaypalUser = "";
    private String mPaypalCurrencyCode = "";
    private String mPaypalItemName = "";
    private boolean mFlattrEnabled = false;
    private String mFlattrProjectUrl = "";
    private String mFlattrUrl = "";
    private boolean mBitcoinEnabled = false;
    private String mBitcoinAddress = "";

    /**
     * Instantiate DonationsFragment.
     *
     * @param googleEnabled       Enabled Google Play donations
     * @param googlePubkey        Your Google Play public key
     * @param googleCatalog       Possible item names that can be purchased from Google Play
     * @param googleCatalogValues Values for the names
     * @param paypalEnabled       Enable PayPal donations
     * @param paypalUser          Your PayPal email address
     * @param paypalCurrencyCode  Currency code like EUR. See here for other codes:
     *                            https://developer.paypal
     *                            .com/webapps/developer/docs/classic/api/currency_codes
     *                            /#id09A6G0U0GYK
     * @param paypalItemName      Display item name on PayPal, like "Donation for NTPSync"
     * @param flattrEnabled       Enable Flattr donations
     * @param bitcoinEnabled      Enable bitcoin donations
     * @return DonationsFragment
     */
    @SuppressWarnings("SameParameterValue")
    public static DonationsFragment newInstance(boolean googleEnabled, String googlePubkey,
                                                String[] googleCatalog,
                                                String[] googleCatalogValues, boolean
                                                        paypalEnabled, String paypalUser,
                                                String paypalCurrencyCode, String paypalItemName,
                                                boolean flattrEnabled,
                                                boolean bitcoinEnabled) {
        DonationsFragment donationsFragment = new DonationsFragment();
        Bundle args = new Bundle();

        args.putBoolean(ARG_DEBUG, jahirfiquitiva.iconshowcase.BuildConfig.DEBUG);

        args.putBoolean(ARG_GOOGLE_ENABLED, googleEnabled);
        args.putString(ARG_GOOGLE_PUBKEY, googlePubkey);
        args.putStringArray(ARG_GOOGLE_CATALOG, googleCatalog);
        args.putStringArray(ARG_GOOGLE_CATALOG_VALUES, googleCatalogValues);

        args.putBoolean(ARG_PAYPAL_ENABLED, paypalEnabled);
        args.putString(ARG_PAYPAL_USER, paypalUser);
        args.putString(ARG_PAYPAL_CURRENCY_CODE, paypalCurrencyCode);
        args.putString(ARG_PAYPAL_ITEM_NAME, paypalItemName);

        args.putBoolean(ARG_FLATTR_ENABLED, flattrEnabled);
        args.putString(ARG_FLATTR_PROJECT_URL, null);
        args.putString(ARG_FLATTR_URL, null);

        args.putBoolean(ARG_BITCOIN_ENABLED, bitcoinEnabled);
        args.putString(ARG_BITCOIN_ADDRESS, null);

        donationsFragment.setArguments(args);
        return donationsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO remove debug and all other bundle vars and use Config
        mDebug = getArguments().getBoolean(ARG_DEBUG);

        mGoogleEnabled = getArguments().getBoolean(ARG_GOOGLE_ENABLED);
        mGooglePubkey = getArguments().getString(ARG_GOOGLE_PUBKEY);
        mGgoogleCatalog = getArguments().getStringArray(ARG_GOOGLE_CATALOG);
        mGoogleCatalogValues = getArguments().getStringArray(ARG_GOOGLE_CATALOG_VALUES);

        mPaypalEnabled = getArguments().getBoolean(ARG_PAYPAL_ENABLED);
        mPaypalUser = getArguments().getString(ARG_PAYPAL_USER);
        mPaypalCurrencyCode = getArguments().getString(ARG_PAYPAL_CURRENCY_CODE);
        mPaypalItemName = getArguments().getString(ARG_PAYPAL_ITEM_NAME);

        mFlattrEnabled = getArguments().getBoolean(ARG_FLATTR_ENABLED);
        mFlattrProjectUrl = getArguments().getString(ARG_FLATTR_PROJECT_URL);
        mFlattrUrl = getArguments().getString(ARG_FLATTR_URL);

        mBitcoinEnabled = getArguments().getBoolean(ARG_BITCOIN_ENABLED);
        mBitcoinAddress = getArguments().getString(ARG_BITCOIN_ADDRESS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.donations__fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /* Flattr */
        if (mFlattrEnabled) {
            // inflate flattr view into stub
            ViewStub flattrViewStub = (ViewStub) getActivity().findViewById(
                    R.id.donations__flattr_stub);
            flattrViewStub.inflate();

            buildFlattrView();
        }

        /* Google */
        if (mGoogleEnabled) {
            // inflate google view into stub
            ViewStub googleViewStub = (ViewStub) getActivity().findViewById(
                    R.id.donations__google_stub);
            googleViewStub.inflate();

            // choose donation amount
            mGoogleSpinner = (Spinner) getActivity().findViewById(
                    R.id.donations__google_android_market_spinner);
            ArrayAdapter<CharSequence> adapter;
            if (mDebug) {
                adapter = new ArrayAdapter<CharSequence>(getActivity(),
                        android.R.layout.simple_spinner_item, CATALOG_DEBUG);
            } else {
                adapter = new ArrayAdapter<CharSequence>(getActivity(),
                        android.R.layout.simple_spinner_item, mGoogleCatalogValues);
            }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mGoogleSpinner.setAdapter(adapter);

            Button btGoogle = (Button) getActivity().findViewById(
                    R.id.donations__google_android_market_donate_button);
            btGoogle.setOnClickListener(new DebouncedClickListener() {
                @Override
                public void onDebouncedClick(View v) {
                    donateGoogleOnClick();
                }
            });

            // Create the helper, passing it our context and the public key to verify signatures
            // with
            if (mDebug)
                Timber.d("Creating IAB helper.");
            mHelper = new IabHelper(getActivity(), mGooglePubkey);

            // enable debug logging (for a production application, you should set this to false).
            mHelper.enableDebugLogging(mDebug);

            // Start setup. This is asynchronous and the specified listener
            // will be called once setup completes.
            if (mDebug)
                Timber.d("Starting setup.");
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (mDebug)
                        Timber.d("Setup finished.");

                    if (!result.isSuccess()) {
                        // Oh noes, there was a problem.
                        openDialog(android.R.drawable.ic_dialog_alert, R.string
                                        .donations__google_android_market_not_supported_title,
                                getString(R.string.donations__google_android_market_not_supported));
                    }

                }
            });
        }

        /* PayPal */
        if (mPaypalEnabled) {
            // inflate paypal view into stub
            ViewStub paypalViewStub = (ViewStub) getActivity().findViewById(
                    R.id.donations__paypal_stub);
            paypalViewStub.inflate();

            Button btPayPal = (Button) getActivity().findViewById(
                    R.id.donations__paypal_donate_button);
            btPayPal.setOnClickListener(new DebouncedClickListener() {
                @Override
                public void onDebouncedClick(View v) {
                    donatePayPalOnClick();
                }
            });
        }

        /* Bitcoin */
        if (mBitcoinEnabled) {
            // inflate bitcoin view into stub
            ViewStub bitcoinViewStub = (ViewStub) getActivity().findViewById(R.id
                    .donations__bitcoin_stub);
            bitcoinViewStub.inflate();

            Button btBitcoin = (Button) getActivity().findViewById(R.id.donations__bitcoin_button);
            btBitcoin.setOnClickListener(new DebouncedClickListener() {
                @Override
                public void onDebouncedClick(View v) {
                    donateBitcoinOnClick(v);
                }
            });
            btBitcoin.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    // http://stackoverflow.com/a/11012443/832776
                    if (Build.VERSION.SDK_INT >= 11) {
                        ClipboardManager clipboard =
                                (ClipboardManager) getActivity().getSystemService(Context
                                        .CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(mBitcoinAddress, mBitcoinAddress);
                        clipboard.setPrimaryClip(clip);
                    } else {
                        @SuppressWarnings("deprecation")
                        android.text.ClipboardManager clipboard =
                                (android.text.ClipboardManager) getActivity().getSystemService
                                        (Context.CLIPBOARD_SERVICE);
                        clipboard.setText(mBitcoinAddress);
                    }
                    Toast.makeText(getActivity(), R.string.donations__bitcoin_toast_copy, Toast
                            .LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }

    @Override
    public int getTitleId() {
        return DrawerActivity.DrawerItem.DONATE.getTitleID();
    }

    @Nullable
    @Override
    protected CFabEvent updateFab() {
        return new CFabEvent(false);
    }

    /**
     * Open dialog
     */
    private void openDialog(int icon, int title, String message) {
        new MaterialDialog.Builder(getActivity())
                .icon(ContextCompat.getDrawable(getActivity(), icon))
                .title(title)
                .content(message)
                .cancelable(true)
                .positiveText(R.string.donations__button_close)
                .show();
    }

    /**
     * Donate button executes donations based on selection in spinner
     */
    private void donateGoogleOnClick() {
        final int index;
        index = mGoogleSpinner.getSelectedItemPosition();
        if (mDebug)
            Timber.d("selected item in spinner: " + index);

        if (mDebug) {
            // when debugging, choose android.test.x item
            mHelper.launchPurchaseFlow(getActivity(),
                    CATALOG_DEBUG[index], IabHelper.ITEM_TYPE_INAPP,
                    0, mPurchaseFinishedListener, null);
        } else {
            mHelper.launchPurchaseFlow(getActivity(),
                    mGgoogleCatalog[index], IabHelper.ITEM_TYPE_INAPP,
                    0, mPurchaseFinishedListener, null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mDebug)
            Timber.d("onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the fragment result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            if (mDebug)
                Timber.d("onActivityResult handled by IABUtil.");
        }
    }

    /**
     * Donate button with PayPal by opening browser with defined URL For possible parameters see:
     * https://developer.paypal.com/webapps/developer/docs/classic/paypal-payments-standard
     * /integration-guide/Appx_websitestandard_htmlvariables/
     */
    private void donatePayPalOnClick() {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("https").authority("www.paypal.com").path("cgi-bin/webscr");
        uriBuilder.appendQueryParameter("cmd", "_donations");

        uriBuilder.appendQueryParameter("business", mPaypalUser);
        uriBuilder.appendQueryParameter("lc", "US");
        uriBuilder.appendQueryParameter("item_name", mPaypalItemName);
        uriBuilder.appendQueryParameter("no_note", "1");
        uriBuilder.appendQueryParameter("no_shipping", "1");
        uriBuilder.appendQueryParameter("currency_code", mPaypalCurrencyCode);
        Uri payPalUri = uriBuilder.build();

        if (mDebug)
            Timber.d("Opening the browser with the url: " + payPalUri.toString());

        // Start your favorite browser
        try {
            Intent viewIntent = new Intent(Intent.ACTION_VIEW, payPalUri);
            startActivity(viewIntent);
        } catch (ActivityNotFoundException e) {
            openDialog(android.R.drawable.ic_dialog_alert, R.string.donations__alert_dialog_title,
                    getString(R.string.donations__alert_dialog_no_browser));
        }
    }

    /**
     * Donate with bitcoin by opening a bitcoin: intent if available.
     */
    private void donateBitcoinOnClick(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("bitcoin:" + mBitcoinAddress));

        if (mDebug)
            Timber.d("Attempting to donate bitcoin using URI: " + i.getDataString());

        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            view.findViewById(R.id.donations__bitcoin_button).performLongClick();
        }
    }

    /**
     * Build view for Flattr. see Flattr API for more information: http://developers.flattr
     * .net/button/
     */
    @SuppressLint({"SetJavaScriptEnabled", "SetTextI18n"})
    @TargetApi(11)
    private void buildFlattrView() {
        final FrameLayout mLoadingFrame;
        final WebView mFlattrWebview;

        mFlattrWebview = (WebView) getActivity().findViewById(R.id.donations__flattr_webview);
        mLoadingFrame = (FrameLayout) getActivity().findViewById(R.id.donations__loading_frame);

        // disable hardware acceleration for this webview to get transparent background working
        if (Build.VERSION.SDK_INT >= 11) {
            mFlattrWebview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        // define own webview client to override loading behaviour
        mFlattrWebview.setWebViewClient(new WebViewClient() {
            /**
             * Open all links in browser, not in webview
             */
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                try {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(urlNewString)));
                } catch (ActivityNotFoundException e) {
                    openDialog(android.R.drawable.ic_dialog_alert, R.string
                                    .donations__alert_dialog_title,
                            getString(R.string.donations__alert_dialog_no_browser));
                }
                return false;
            }

            /**
             * Links in the flattr iframe should load in the browser not in the iframe itself,
             * http:/
             * /stackoverflow.com/questions/5641626/how-to-get-webview-iframe-link-to-launch-the
             * -browser
             */
            @Override
            public void onLoadResource(WebView view, String url) {
                if (url.contains("flattr")) {
                    WebView.HitTestResult result = view.getHitTestResult();
                    if (result != null && result.getType() > 0) {
                        try {
                            view.getContext().startActivity(
                                    new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        } catch (ActivityNotFoundException e) {
                            openDialog(android.R.drawable.ic_dialog_alert, R.string
                                            .donations__alert_dialog_title,
                                    getString(R.string.donations__alert_dialog_no_browser));
                        }
                        view.stopLoading();
                    }
                }
            }

            /**
             * After loading is done, remove frame with progress circle
             */
            @Override
            public void onPageFinished(WebView view, String url) {
                // remove loading frame, show webview
                if (mLoadingFrame.getVisibility() == View.VISIBLE) {
                    mLoadingFrame.setVisibility(View.GONE);
                    mFlattrWebview.setVisibility(View.VISIBLE);
                }
            }
        });

        // make text white and background transparent
        String htmlStart = "<html> <head><style type='text/css'>*{color: #FFFFFF; " +
                "background-color: transparent;}</style>";

        // https is not working in android 2.1 and 2.2
        String flattrScheme;
        if (Build.VERSION.SDK_INT >= 9) {
            flattrScheme = "https://";
        } else {
            flattrScheme = "http://";
        }

        // set url of flattr link
        TextView mFlattrUrlTextView = (TextView) getActivity().findViewById(R.id
                .donations__flattr_url);
        mFlattrUrlTextView.setText(flattrScheme + mFlattrUrl);

        String flattrJavascript = "<script type='text/javascript'>"
                + "/* <![CDATA[ */"
                + "(function() {"
                + "var s = document.createElement('script'), t = document.getElementsByTagName" +
                "('script')[0];"
                + "s.type = 'text/javascript';" + "s.async = true;" + "s.src = '" + flattrScheme
                + "api.flattr.com/js/0.6/load.js?mode=auto';" + "t.parentNode.insertBefore(s, t);"
                + "})();" + "/* ]]> */" + "</script>";
        String htmlMiddle = "</head> <body> <div align='center'>";
        String flattrHtml = "<a class='FlattrButton' style='display:none;' href='"
                + mFlattrProjectUrl
                + "' target='_blank'></a> <noscript><a href='"
                + flattrScheme
                + mFlattrUrl
                + "' target='_blank'> <img src='"
                + flattrScheme
                + "api.flattr.com/button/flattr-badge-large.png' alt='Flattr this' title='Flattr " +
                "this' border='0' /></a></noscript>";
        String htmlEnd = "</div> </body> </html>";

        String flattrCode = htmlStart + flattrJavascript + htmlMiddle + flattrHtml + htmlEnd;

        mFlattrWebview.getSettings().setJavaScriptEnabled(true);

        mFlattrWebview.loadData(flattrCode, "text/html", "utf-8");

        // disable scroll on touch
        mFlattrWebview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // already handled (returns true) when moving
                return (motionEvent.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        // make background of webview transparent
        // has to be called AFTER loadData
        // http://stackoverflow.com/questions/5003156/android-webview-style-background
        // -colortransparent-ignored-on-android-2-2
        mFlattrWebview.setBackgroundColor(0x00000000);
    }
}
