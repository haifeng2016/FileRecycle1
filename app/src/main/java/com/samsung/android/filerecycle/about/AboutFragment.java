package com.samsung.android.filerecycle.about;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.common.BaseFragment;
import com.samsung.android.filerecycle.license.LicenseActivity;
import com.samsung.android.filerecycle.privacy.PrivacyActivity;
import com.samsung.android.filerecycle.terms.TermsActivity;

/**
 * Created by haifeng on 2017/5/4.
 */

public class AboutFragment extends BaseFragment {

    private ViewGroup view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_about,container,false);
        title = mContext.getString(R.string.about);
        updateActionBar();
        initLinkLayout();//调用此方法来响应点击事件
        return view;
    }

    private void initLinkLayout() {
        TextView termsAndConditionsTextView = (TextView) view.findViewById(R.id.termsAndConditionsTextView);
        termsAndConditionsTextView.setPaintFlags(termsAndConditionsTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        termsAndConditionsTextView.setTag("termsAndConditionsTextView");
        termsAndConditionsTextView.setFocusable(true);
        termsAndConditionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), TermsActivity.class));
            }
        });

        TextView privacyPolicyTextView = (TextView) view.findViewById(R.id.privacyPolicyTextView);
        privacyPolicyTextView.setPaintFlags(privacyPolicyTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        privacyPolicyTextView.setTag("privacyPolicyTextView");
        privacyPolicyTextView.setFocusable(true);
        privacyPolicyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PrivacyActivity.class));
            }
        });

        TextView openSourceLicenseTextView = (TextView) view.findViewById(R.id.openSourceLicenseTextView);
        openSourceLicenseTextView.setPaintFlags(openSourceLicenseTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        openSourceLicenseTextView.setTag("openSourceLicenseTextView");
        openSourceLicenseTextView.setFocusable(true);
        openSourceLicenseTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LicenseActivity.class));
            }
        });
    }

}
