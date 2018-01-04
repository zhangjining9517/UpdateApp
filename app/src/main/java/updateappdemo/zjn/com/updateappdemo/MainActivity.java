package updateappdemo.zjn.com.updateappdemo;

import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zjn.updateapputils.util.CheckVersionRunnable;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkVersion();
    }

    private void checkVersion() {
        try {
            CheckVersionRunnable runnable = CheckVersionRunnable.from(MainActivity.this)
                    .setApkPath("")//文件存储路径
                    .setDownLoadUrl("")//文件下载路径
                    .setServerUpLoadLocalVersion("")//服务器中本地版本
                    .setServerVersion("")//服务器最新版本
                    .setUpdateMsg("")//更新信息
                    .isUseCostomDialog(true)//是否使用自定义对话框
                    .setToast(true) //弹出toast
                    .setNotifyTitle("海拳")
                    .setVersionShow("");//是否app展示版本

            //设置不再提醒的监听回调
            runnable.setListener(new CheckVersionRunnable.OnEventCallbackListener() {
                @Override
                public void noRemindListener() {
                }
            });
            //启动通知，去下载
            new Thread(runnable).start();
        } catch (Exception e) {
        }
    }
}
