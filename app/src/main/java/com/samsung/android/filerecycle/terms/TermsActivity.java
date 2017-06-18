package com.samsung.android.filerecycle.terms;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.common.BaseActivity;
import com.samsung.android.filerecycle.license.LicenseFragment;

public class TermsActivity extends BaseActivity {

    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolbar_container);

        setActionBar();
        Intent intent = getIntent();

        if (intent == null) {
            // do something?
        }

        Fragment fragment = new TermsFragment();
        attachFragmentAsSingle(fragment);
    }

}
