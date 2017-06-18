package com.samsung.android.filerecycle.launcher;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.lypeer.fcpermission.FcPermissionsB;
import com.lypeer.fcpermission.impl.OnPermissionsDeniedListener;
import com.lypeer.fcpermission.impl.OnPermissionsGrantedListener;
import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.about.AboutActivity;
import com.samsung.android.filerecycle.common.BaseActivity;
import com.samsung.android.filerecycle.main.MainActivity;
import com.samsung.android.filerecycle.setting.SettingsActivity;
import com.samsung.android.recoveryfile.mainpresenter.FileBean;
import com.samsung.android.recoveryfile.mainpresenter.IMainPresenter;
import com.samsung.android.recoveryfile.mainpresenter.MainPresenter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LauncherActivity extends BaseActivity {

    //动态获取权限
    private static final int RC_STORAGE = 2333;
    private FcPermissionsB mFcPermissionsB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        requestExternalStoragePermission();
    }

    private void requestExternalStoragePermission() {
        mFcPermissionsB = new FcPermissionsB.Builder(this)
                .onGrantedListener(new OnPermissionsGrantedListener() {
                    @Override
                    public void onPermissionsGranted(int requestCode, List<String> perms) {
                        startActivity(new Intent(LauncherActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .onDeniedListener(new OnPermissionsDeniedListener() {
                    @Override
                    public void onPermissionsDenied(int requestCode, List<String> perms) {
                        System.exit(0);
                    }
                })
                .positiveBtn4ReqPer(android.R.string.ok)
                .negativeBtn4ReqPer(R.string.cancel)
                .positiveBtn4NeverAskAgain(R.string.setting)
                .negativeBtn4NeverAskAgain(R.string.cancel)
                .rationale4ReqPer(getString(R.string.prompt_request_camara))//必需
                .rationale4NeverAskAgain(getString(R.string.prompt_we_need_camera))//必需
                .requestCode(RC_STORAGE)//必需
                .build();
        mFcPermissionsB.requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mFcPermissionsB.onRequestPermissionsResult(requestCode , permissions , grantResults , this);
    }

}
