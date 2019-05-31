# UpdateApp [![](https://jitpack.io/v/zhangjining9517/UpdateApp.svg)](https://jitpack.io/#zhangjining9517/UpdateApp)
版本更新库，支持下载，断点下载，下载完成安装等功能

# Gradle [![](https://jitpack.io/v/zhangjining9517/UpdateApp.svg)](https://jitpack.io/#zhangjining9517/UpdateApp)
allprojects {    
	repositories {  
		...  
		maven {  
			url 'https://jitpack.io'  
		}  
	}  
}
  
dependencies {
	   implementation 'com.github.zhangjining9517:UpdateApp:Tag'
}

# Usage
基本用法
默认从服务器请求版本更新信息，拿到下载路径，传入文件存储路径，更新版本信息，服务器版本信息，更新内容，是否弹出自定义对话框，设置通知title信息，等内容
CheckVersionRunnable runnable = CheckVersionRunnable.from(MainActivity.this)
                                .setApkPath(SystemDir.DIR_UPDATE_APK)//文件存储路径
                                .setDownLoadUrl(mbean.getDownUrl())
                                .setServerUpLoadLocalVersion(mbean.getUpdateVersion())
                                .setServerVersion(mbean.getVersion())
                                .setUpdateMsg(mbean.getMsg())
                                .isUseCostomDialog(true)
                                .setNotifyTitle(getResources().getString(R.string.app_name))
                                .setVersionShow(mbean.getVersion_show());
//启动通知，去下载
ThreadPoolUtils.newInstance().execute(runnable);
