package com.zjn.updateapputils.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.BuildConfig;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zjn.updateapputils.R;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 类名:CheckVersionRunnable 说明:检查版本号线程
 *
 * @author zhang
 */
public class CheckVersionRunnable implements Runnable {

    /**
     * 检查更新
     */
    private static final int CHECK_UPDATE = 1;
    /**
     * 安装文件
     */
    private static final int INSTALL_FILE = 2;
    /**
     * 弹出信息
     */
    private static final int TOAST_MESSAGE = 3;


    public static final int CHECK_BY_VERSION_NAME = 1001;
    public static final int CHECK_BY_VERSION_CODE = 1002;
    public static final int DOWNLOAD_BY_APP = 1003;
    public static final int DOWNLOAD_BY_BROWSER = 1004;

    private int downloadBy = DOWNLOAD_BY_APP;
    private Activity m_ctx;
    /**
     * 文件下载路径
     */
    private String m_StrApkFileUrl;
    /**
     * 服务器版本
     */
    private String m_ServerVersion;
    /**
     * 服务器传本地版本
     */
    private String m_ServerUpLoadLocalVersion;
    /**
     * 更新信息
     */
    private String m_UpdateMsg;
    /**
     * apk的下载路径
     */
    private String m_ApkStoragePath;
    /**
     * apk展示版本
     */
    private String m_Version_show;
    /**
     * 自定义对话框 1
     */
    private boolean isUseCustomDialog = false;
    /** 是否手动强制更新 */
    private boolean isHandleQzgx = false;
    /** 是否弹出toast */
    private boolean isToast = false;
    /** 设置通知提示信息 */
    private String notifyTitle = "";

    private OnEventCallbackListener mListener;

    public OnEventCallbackListener getListener() {
        return mListener;
    }

    public void setListener(OnEventCallbackListener listener) {
        this.mListener = listener;
    }

    public CheckVersionRunnable setHandleQzgx(boolean handleQzgx) {
        isHandleQzgx = handleQzgx;
        return this;
    }

    public CheckVersionRunnable setToast(boolean toast) {
        isToast = toast;
        return this;
    }

    //事件回调监听
    public interface OnEventCallbackListener{
        //不在提醒的点击事件回调
        void noRemindListener();
    }

    private Handler m_Handler = new Handler() {
        public void handleMessage(Message msg) {
            String strInfo = "";
            File apkPath = null;
            switch (msg.what) {
                case CHECK_UPDATE:
                    startDownloadApkService();
                    break;
                case INSTALL_FILE:
                    apkPath = (File) msg.obj;
                    installapk(apkPath);
                    break;
                case TOAST_MESSAGE:
                    strInfo = (String) msg.obj;
                    Toast.makeText(m_ctx,strInfo,Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }

        ;

    };



    private CheckVersionRunnable(Activity ctx) {
        this.m_ctx = ctx;
    }

    public static CheckVersionRunnable from(Activity ctx){
        return new CheckVersionRunnable(ctx);
    }
    //设置下载路径
    public CheckVersionRunnable setDownLoadUrl(String downLoadUrl){
        this.m_StrApkFileUrl = downLoadUrl;
        return this;
    }
    //设置服务器版本
    public CheckVersionRunnable setServerVersion(String sversion){
        this.m_ServerVersion = sversion;
        return this;
    }
    //设置服务器上传本地版本
    public CheckVersionRunnable setServerUpLoadLocalVersion(String sUpLoadVersion){
        this.m_ServerUpLoadLocalVersion = sUpLoadVersion;
        return this;
    }
    //设置更新信息
    public CheckVersionRunnable setUpdateMsg(String updateMsg){
        this.m_UpdateMsg = updateMsg;
        return this;
    }
    //设置apk存储路径
    public CheckVersionRunnable setApkPath(String apkPath){
        this.m_ApkStoragePath = apkPath;
        return this;
    }
    //设置apk存储路径
    public CheckVersionRunnable setVersionShow(String versionShow){
        this.m_Version_show = versionShow;
        return this;
    }

    public CheckVersionRunnable setNotifyTitle(String nTitle){
        this.notifyTitle = nTitle;
        return this;
    }

    public CheckVersionRunnable isUseCostomDialog(boolean isUse){
        this.isUseCustomDialog = isUse;
        return this;
    }



    @Override
    public void run() {
        // TODO Auto-generated method stub
        updateAPKServer();
    }

    /**
     * 功能:更新程序
     */
    private synchronized void updateAPKServer() {
        // TODO Auto-generated method stub
        final Message msg = Message.obtain();
        try {
            // 获取已下载的文件 72370 29680
            File updateFile = new File(m_ApkStoragePath, "_update_temp.apk");
            if (getVersion() < Double.valueOf(m_ServerVersion)) {
                long fileLength = getWebFileLength(m_StrApkFileUrl);
                if (updateFile.exists() && updateFile.length() == fileLength) {
                    msg.obj = updateFile;
                    msg.what = INSTALL_FILE;
                    m_Handler.sendMessage(msg);
                    return;
                }
                // 生成更新的内容描述
                msg.what = CHECK_UPDATE;
                m_Handler.sendMessage(msg);
            } else {
                if(isToast){
                    msg.obj = "当前为最新版本！";
                    msg.what = TOAST_MESSAGE;
                    m_Handler.sendMessage(msg);
                }
                // // 初始化程序下载是否成功的标记
                // UserInfo.setDownLoadSucess("0", m_ctx);
                // 清理更新的文件
                clearUpdateAPKFile(updateFile);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public int getVersion() {
        try {
            PackageManager manager = m_ctx.getPackageManager();
            PackageInfo info = manager
                    .getPackageInfo(m_ctx.getPackageName(), 0);
            int versionCode = Integer.valueOf(info.versionCode);
            return versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 清理更新过的文件
     */
    private void clearUpdateAPKFile(File updateFile) {
        // TODO Auto-generated method stub
        if (updateFile.exists()) {
            // 当不需要的时候，清除之前的下载文件，避免浪费用户空间
            updateFile.delete();
        }
    }

    /**
     * 获取web端文件长度
     *
     * @param strWebFileUrl
     * @return
     */
    private long getWebFileLength(String strWebFileUrl) {
        // TODO Auto-generated method stub
        long updateTotalSize = 0;
        try {
            URL url = new URL(strWebFileUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
            urlConnection.setRequestProperty("User-Agent", "PacificHttpClient");
            urlConnection.setConnectTimeout(100000);
            urlConnection.setReadTimeout(20000);
            updateTotalSize = urlConnection.getContentLength();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return updateTotalSize;
    }

    /**
     * 安装apk文件
     *
     * @param apkPath
     */
    private void installapk(File apkPath) {
        Intent intent;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            String packageName = m_ctx.getPackageName();
            Uri uri = FileProvider.getUriForFile(m_ctx, packageName+".fileprovider", apkPath); //修改  downloadFile 来源于上面下载文件时保存下来的
            //  BuildConfig.APPLICATION_ID + ".fileprovider" 是在manifest中 Provider里的authorities属性定义的值
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //临时授权
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            String type = "application/vnd.android.package-archive";
            intent.setDataAndType(Uri.fromFile(apkPath), type);
        }
        m_ctx.startActivity(intent);
    }

    /**
     * 启动下载apk的服务程序
     */
    protected void startDownloadApkService() {
        // TODO Auto-generated method stub
        try {
            if(!isUseCustomDialog){
                showDefaultDialog();
            }else{
                //展示自定义对话框
                showCustomDialog();
            }
        }catch (NumberFormatException e){
            Toast.makeText(m_ctx,"数字格式异常，请检查服务器传入信息",Toast.LENGTH_SHORT).show();
        }catch (Exception e) {
            // TODO Auto-generated catch block
            // 防止activity销毁时，仍然弹出对话框
            e.printStackTrace();
        }
    }

    /** 展示默认对话框 */
    private void showDefaultDialog(){
        final Dialog normalDialog =
                new Dialog(m_ctx , R.style.CustomDialog);
        View view = View.inflate(m_ctx, R.layout.dialog_version_update, null);
        TextView tv_sjbbm = (TextView) view.findViewById(R.id.tv_sjbbm);
        TextView tv_sjnr = (TextView) view.findViewById(R.id.tv_sjnr);
        tv_sjbbm.setText("发现新版本 " + m_Version_show);
        tv_sjnr.setText(m_UpdateMsg + "");
        RelativeLayout rlyt_gbsj = (RelativeLayout) view.findViewById(R.id.rlyt_gbsj);
        RelativeLayout rlyt_ljsj = (RelativeLayout) view.findViewById(R.id.rlyt_ljsj);
        RelativeLayout rlyt_bzts = (RelativeLayout) view.findViewById(R.id.rlyt_bzts);
        normalDialog.setContentView(view);
        normalDialog.setCancelable(false);
        boolean isQzgx = false;
        if(m_ServerUpLoadLocalVersion != null && getVersion() <= Integer.valueOf(m_ServerUpLoadLocalVersion) ){
            isQzgx = true;
        }
        if(isQzgx){
            rlyt_bzts.setVisibility(View.GONE);
            rlyt_gbsj.setVisibility(View.GONE);
        }
        normalDialog.show();
        rlyt_gbsj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalDialog.cancel();
                normalDialog.dismiss();
            }
        });
        rlyt_ljsj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //启动服务去下载
                if (downloadBy == DOWNLOAD_BY_APP) {
                    DownloadAppUtils.downloadForAutoInstall(m_ctx, m_StrApkFileUrl,m_ApkStoragePath, "_update_temp.apk", notifyTitle);
                }else if (downloadBy == DOWNLOAD_BY_BROWSER){
                    DownloadAppUtils.downloadForWebView(m_ctx,m_StrApkFileUrl);
                }
                normalDialog.cancel();
                normalDialog.dismiss();
            }
        });
        rlyt_bzts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //不在提醒
                if(mListener != null){
                    mListener.noRemindListener();
                }
                normalDialog.cancel();
                normalDialog.dismiss();
            }
        });
    }

    /** 展示自定义对话框 */
    private void showCustomDialog(){
        final BaseDialog normalDialog = new BaseDialog(m_ctx,R.style.BaseDialog,R.layout.custom_dialog_layout);
        View view = View.inflate(m_ctx, R.layout.custom_dialog_layout, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        tv_title.setText("新版本更有料 " + m_Version_show);
        tv_msg.setText(m_UpdateMsg + "");
        TextView btn_update = (TextView) view.findViewById(R.id.btn_update);
//        ImageView btn_close = (ImageView) view.findViewById(R.id.btn_close);
        TextView btn_no_remind = (TextView) view.findViewById(R.id.btn_no_remind);
        normalDialog.setContentView(view);
        normalDialog.setCancelable(false);
        boolean isQzgx = false;
        if(m_ServerUpLoadLocalVersion != null && getVersion() <= Integer.valueOf(m_ServerUpLoadLocalVersion) ){
            isQzgx = true;
        }
        if(isQzgx){
//            btn_close.setVisibility(View.GONE);
            btn_no_remind.setVisibility(View.GONE);
            normalDialog.setCancelable(false);
            normalDialog.setCanceledOnTouchOutside(false);
        }else{
            normalDialog.setCancelable(true);
            normalDialog.setCanceledOnTouchOutside(true);
        }
        normalDialog.show();
//        btn_close.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                normalDialog.cancel();
//                normalDialog.dismiss();
//            }
//        });
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //启动服务去下载
                if (downloadBy == DOWNLOAD_BY_APP) {
                    DownloadAppUtils.downloadForAutoInstall(m_ctx, m_StrApkFileUrl,m_ApkStoragePath, "_update_temp.apk", "黑熊搏击");
                }else if (downloadBy == DOWNLOAD_BY_BROWSER){
                    DownloadAppUtils.downloadForWebView(m_ctx,m_StrApkFileUrl);
                }
                normalDialog.cancel();
                normalDialog.dismiss();
            }
        });
        btn_no_remind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //不在提醒
                if(mListener != null){
                    mListener.noRemindListener();
                }
                normalDialog.cancel();
                normalDialog.dismiss();
            }
        });
    }
}
