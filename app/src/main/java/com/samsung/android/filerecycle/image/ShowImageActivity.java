package com.samsung.android.filerecycle.image;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.common.BaseActivity;

/**
 * Created by haifeng on 2017/5/9.
 */

public class ShowImageActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolbar_container);

        setActionBar();

        Intent intent = getIntent();

        if (intent == null) {
            finish();
            return;
        }
        Fragment fragment = new ShowImageFragment();
        fragment.setArguments(intent.getExtras());
        attachFragmentAsSingle(fragment);
    }

}
