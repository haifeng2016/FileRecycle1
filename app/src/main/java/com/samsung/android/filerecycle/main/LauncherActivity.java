package com.samsung.android.filerecycle.main;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.lypeer.fcpermission.FcPermissionsB;
import com.lypeer.fcpermission.impl.OnPermissionsDeniedListener;
import com.lypeer.fcpermission.impl.OnPermissionsGrantedListener;
import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.common.BaseActivity;

import java.util.List;

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
