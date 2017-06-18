package com.samsung.android.filerecycle.main;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.about.AboutActivity;
import com.samsung.android.filerecycle.common.BaseActivity;
import com.samsung.android.filerecycle.doc.ShowDocActivity;
import com.samsung.android.filerecycle.otherfiles.ShowOtherFilesActivity;
import com.samsung.android.filerecycle.setting.SettingsActivity;
import com.samsung.android.recoveryfile.mainpresenter.FileBean;
import com.samsung.android.recoveryfile.mainpresenter.IMainPresenter;
import com.samsung.android.recoveryfile.mainpresenter.MainPresenter;

import java.util.List;

public class MainActivity extends BaseActivity {

    private IMainPresenter mPresenter = null;

    private static final String smpackagename = "com.samsung.android.voc"; //Samsung Members包名
    private boolean isQuit = false;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        mPresenter = MainPresenter.getInstance(getApplicationContext());
        initBasicView();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof Integer) {
                int type = (int) v.getTag();
                Intent mIntent;
                if (type == FileBean.TYPE.IMG || type == FileBean.TYPE.VIDEO) {
                    mIntent = new Intent(MainActivity.this, ShowImageActivity.class);
                } else if (type == FileBean.TYPE.MUSIC || type == FileBean.TYPE.DOC) {
                    mIntent = new Intent(MainActivity.this, ShowDocActivity.class);
                } else {
                    mIntent = new Intent(MainActivity.this, ShowOtherFilesActivity.class);
                }
                if(mPresenter.isReady()) {
                    mIntent.putExtra("type", type);
                    startActivity(mIntent);
                }
            }
        }
    };

    private void initBasicView() {
        loadLinearView(R.id.picture_layout, getString(R.string.picture), R.drawable.picture, FileBean.TYPE.IMG);
        loadLinearView(R.id.video_layout, getString(R.string.video), R.drawable.video, FileBean.TYPE.VIDEO);
        loadLinearView(R.id.audio_layout, getString(R.string.audio), R.drawable.audio, FileBean.TYPE.MUSIC);
        loadLinearView(R.id.doc_layout, getString(R.string.doc), R.drawable.doc, FileBean.TYPE.DOC);
        loadLinearView(R.id.app_layout, getString(R.string.applications), R.drawable.app, FileBean.TYPE.APP);
        loadLinearView(R.id.others_layout, getString(R.string.other_files), R.drawable.others, FileBean.TYPE.OTHER);
    }

    private void loadLinearView(@IdRes int itemViewIntRes, @NonNull String title, @DrawableRes int imgRes, int type) {
        ViewGroup itemView = (ViewGroup) findViewById(itemViewIntRes);
        TextView textView = (TextView) itemView.findViewById(R.id.title);
        ImageView icon = (ImageView) itemView.findViewById(R.id.icon);
        textView.setText(title);
        icon.setImageDrawable(getDrawable(imgRes));
        itemView.setTag(type);
        itemView.setOnClickListener(onClickListener);
    }

    //菜单栏的添加
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.navigation,menu);
        return true;
    }

    //点击菜单栏的响应事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId())
        {
            case R.id.setting:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.contactus:
                //startActivity(new Intent(this, ContactUsActivity.class));
                doStartApplicationWithPackageName(smpackagename);//调用盖乐世空间APP
                break;
        }
        return true;
    }

    //在首页“联系我们”，跳转到盖乐世空间APP
    private void doStartApplicationWithPackageName(String packagename) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //如果盖乐世空间被卸载或者不存在
        if (packageinfo == null) {
            Toast.makeText(getApplicationContext(),"请在三星应用商店下载并安装盖乐世空间",Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);

            intent.setComponent(cn);
            startActivity(intent);
        }
    }

//    连续按两次back键退出应用
//    @Override
//    public void onBackPressed() {
//        if (!isQuit) {
//            Toast.makeText(MainActivity.this,"再按一次退出应用",Toast.LENGTH_SHORT).show();
//            isQuit = true;
//
//            //这段代码意思是,在两秒钟之后isQuit会变成false
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } finally {
//                        isQuit = false;
//                    }
//                }
//            }).start();
//        } else {
//            System.exit(0);
//        }
//    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("确定要退出S 回收站吗？退出后将无法进行文件的备份。");
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        System.exit(0);
                    }
                });
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        builder.show();
    }
}
