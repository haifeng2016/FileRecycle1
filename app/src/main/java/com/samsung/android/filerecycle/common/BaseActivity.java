package com.samsung.android.filerecycle.common;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.samsung.android.filerecycle.R;

public class BaseActivity extends AppCompatActivity {

    private final String TAG = BaseActivity.class.getSimpleName();

    protected Toolbar mToolbar;

    protected void setActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.back_btn_mirrored);
            actionBar.setHomeActionContentDescription(getResources().getString(R.string.action_bar_back_button_navigatate_up));
        }
    }

    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //call this method only for single fragment
    protected void attachFragmentAsSingle(Fragment fragment) {
        if (fragment == null) {
            return;
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        String tag = fragment.getClass().getSimpleName() + "_" + fragment.hashCode();
        fragmentTransaction.replace(R.id.container,fragment,tag);
        fragmentTransaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
    }

    protected void popFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        try {
            fragmentManager.popBackStackImmediate();
        } catch (IllegalStateException | NullPointerException e) {
            Log.e(TAG,e.getMessage());
        }
    }


}
