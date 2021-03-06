package com.samsung.android.filerecycle.otherfiles;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.common.BaseActivity;
import com.samsung.android.filerecycle.common.IBasicAction;

public class ShowOtherFilesActivity extends BaseActivity {

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

        Fragment fragment = new ShowOtherFilesFragment();
        fragment.setArguments(intent.getExtras());
        attachFragmentAsSingle(fragment);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
        boolean back = false;
        if (fragment instanceof IBasicAction) {
            back = ((IBasicAction) fragment).onBackAction();
            if (!back) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}
