package com.samsung.android.filerecycle.otherfiles;

import android.os.Bundle;

import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.common.BaseActivity;

public class ShowOtherFilesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolbar_container);
        attachFragmentAsSingle(new ShowOtherFilesFragment());
        setActionBar();
    }
}
