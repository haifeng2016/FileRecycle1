package com.samsung.android.filerecycle.terms;

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

public class TermsFragment extends BaseFragment {
    private static final String TAG = TermsFragment.class.getSimpleName();

    protected View view;

    public TermsFragment() {
        // required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_terms,container,false);
        title = mContext.getString(R.string.termsandconditions);

        WebView webView = (WebView) view.findViewById(R.id.termsvw);
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

        webView.loadUrl("file:///android_asset/terms.html");

        updateActionBar();

        return view;
    }
}
