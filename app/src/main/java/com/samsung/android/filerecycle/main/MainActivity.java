package com.samsung.android.filerecycle.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.filerecycle.R;
import com.samsung.android.filerecycle.about.AboutActivity;
import com.samsung.android.filerecycle.app.ShowAppActivity;
import com.samsung.android.filerecycle.common.BaseActivity;
import com.samsung.android.filerecycle.doc.ShowDocActivity;
import com.samsung.android.filerecycle.image.ShowImageActivity;
import com.samsung.android.filerecycle.music.ShowMusicActivity;
import com.samsung.android.filerecycle.otherfiles.ShowOtherFilesActivity;
import com.samsung.android.filerecycle.video.ShowVideoActivity;
import com.samsung.android.recoveryfile.mainpresenter.FileBean;
import com.samsung.android.recoveryfile.mainpresenter.IMainPresenter;
import com.samsung.android.recoveryfile.mainpresenter.MainPresenter;
import com.samsung.android.recoveryfile.mainpresenter.OnMainPresenterListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity {

    private IMainPresenter mPresenter = null;
    private static final String smpackagename = "com.samsung.android.voc"; //Samsung Members包名
    private Handler mHandler = new Handler(Looper.getMainLooper());

    int notifyId = 100;
    public NotificationManager notificationManager;
    NotificationCompat.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initIconMap();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");//自定义Toolbar标题，所以需要设置默认的title为空字符
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        mPresenter = MainPresenter.getInstance(getApplicationContext());
        initBasicView();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        initNotify();
    }

    public PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }

    /** 初始化通知栏 */
    private void initNotify() {
        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("S 回收站")
                .setContentText("点击体验更多功能吧")
                .setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL))
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setOngoing(false)
                .setDefaults(Notification.FLAG_SHOW_LIGHTS)
                .setLights(Color.GREEN, 1000, 1000)
                .setSmallIcon(R.mipmap.notify);
    }

    public void showIntentActivityNotify() {
        builder.setAutoCancel(true)//点击后让通知消失
                .setContentTitle("超级实用的文件回收应用")
                .setContentText("点击进入app一起体验吧");
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        notificationManager.notify(notifyId, builder.build());
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof Integer) {
                int type = (int) v.getTag();
                Intent mIntent;
                if (type == FileBean.TYPE.IMG) {
                    mIntent = new Intent(MainActivity.this, ShowImageActivity.class);
                } else if (type == FileBean.TYPE.VIDEO) {
                    mIntent = new Intent(MainActivity.this, ShowVideoActivity.class);
                } else if (type == FileBean.TYPE.MUSIC) {
                    mIntent = new Intent(MainActivity.this, ShowMusicActivity.class);
                } else if (type == FileBean.TYPE.DOC) {
                    mIntent = new Intent(MainActivity.this, ShowDocActivity.class);
                } else if (type == FileBean.TYPE.APP) {
                    mIntent = new Intent(MainActivity.this, ShowAppActivity.class);
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

    private final String KEY_VIEW = "view";
    private final String KEY_TITLE = "title";
    private final String KEY_ICON = "icon";
    private final String KEY_TYPE = "type";
    List<HashMap<String, Object>> mIconMap = new ArrayList<>();

    private void initIconMap() {
        if (mIconMap == null) {
            mIconMap = new ArrayList<>();
        }
        mIconMap.add(0, pushMapValue(R.id.picture_layout, getString(R.string.picture), R.drawable.fileholder, FileBean.TYPE.IMG));
        mIconMap.add(1, pushMapValue(R.id.video_layout, getString(R.string.video), R.drawable.fileholder, FileBean.TYPE.VIDEO));
        mIconMap.add(2, pushMapValue(R.id.audio_layout, getString(R.string.audio), R.drawable.fileholder, FileBean.TYPE.MUSIC));
        mIconMap.add(3, pushMapValue(R.id.doc_layout, getString(R.string.doc), R.drawable.fileholder, FileBean.TYPE.DOC));
        mIconMap.add(4, pushMapValue(R.id.app_layout, getString(R.string.applications), R.drawable.fileholder, FileBean.TYPE.APP));
        mIconMap.add(5, pushMapValue(R.id.others_layout, getString(R.string.other_files), R.drawable.fileholder, FileBean.TYPE.OTHER));
    }

    private HashMap<String, Object> pushMapValue(int view, String title, int draw, int type) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(KEY_VIEW, view);
        map.put(KEY_TITLE, title);
        map.put(KEY_ICON, draw);
        map.put(KEY_TYPE, type);
        return map;
    }

    private void initBasicView() {
        for (int i = 0; i < mIconMap.size(); i++) {
            HashMap<String, Object> map = mIconMap.get(i);
            loadLinearView((int)map.get(KEY_VIEW), (String)map.get(KEY_TITLE), (int)map.get(KEY_ICON), (int)map.get(KEY_TYPE));
        }
    }

    private void loadLinearView(int itemViewIntRes, @NonNull String title, @DrawableRes int imgRes, int type) {
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
//            case R.id.setting:
//                startActivity(new Intent(this, SettingsActivity.class));
//                break;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.contactus:
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
        List<ResolveInfo> resolveinfoList = getPackageManager().queryIntentActivities(resolveIntent, 0);

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

    @Override
    protected void onResume() {
        initFilesSizeMap();
        super.onResume();
    }

    private void initFilesSizeMap() {
        mPresenter = MainPresenter.getInstance(getApplicationContext());
        mPresenter.setPresenterListener(new OnMainPresenterListener() {
            @Override
            public void onReady(boolean ready) {
                for (int i = 0; i < mIconMap.size(); i++) {
                    HashMap<String, Object> map = mIconMap.get(i);
                    int type = (int)map.get(KEY_TYPE);
                    updateFileSize((int)map.get(KEY_VIEW), mPresenter.getBackupFiles(type), type);
                }
            }

            @Override
            public void onDeleteFiles(int success) {}

            @Override
            public void onRecoverFiles(int success) {}

            @Override
            public void onBackupFiles(FileBean m) {}
        });
        for (int i = 0; i < mIconMap.size(); i++) {
            HashMap<String, Object> map = mIconMap.get(i);
            int type = (int)map.get(KEY_TYPE);
            updateFileSize((int)map.get(KEY_VIEW), mPresenter.getBackupFiles(type), type);
        }
    }

    private void updateFileSize(int containerView, List<FileBean> fileBeans, final int type) {
        final ViewGroup viewGroup = (ViewGroup) findViewById(containerView);
        if (viewGroup == null) {
            return;
        }
        final int count = (fileBeans == null ? 0: fileBeans.size());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(MainActivity.class.getSimpleName(), "type " + type + " " + count);
                ((TextView)viewGroup.findViewById(R.id.count)).setText(String.valueOf(count));
            }
        });

    }

    //按返回键退出时，点击“确定”按键，app实际并没有真正退出，还在后台跑着，这个功能类似于按Home键的效果
    private void backToLauncher() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(),e);
        }
        showIntentActivityNotify();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("确定要退出S 回收站吗？退出后可能会影响文件的备份。");
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //System.exit(0);
                        backToLauncher();
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
