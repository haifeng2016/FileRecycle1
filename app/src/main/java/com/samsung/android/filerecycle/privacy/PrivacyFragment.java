package com.samsung.android.filerecycle.privacy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.common.BaseFragment;

/**
 * Created by haifeng on 2017/5/5.
 */

public class PrivacyFragment extends BaseFragment {
    private static final String TAG = PrivacyFragment.class.getSimpleName();

    protected View view;

    public PrivacyFragment() {
        // required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_privacy,container,false);
        title = mContext.getString(R.string.privacy_policy);

        WebView webView = (WebView) view.findViewById(R.id.privacyvw);
        WebSettings settings = webView.getSettings();
        settings.setTextZoom(200);
        settings.setAllowFileAccess(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setGeolocationEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        webView.loadUrl("file:///android_asset/privacy.html");

        updateActionBar();

        return view;
    }
}
