/**
 * UpdateUtils.java
 * 
 * Version Info:这是自动更新辅助类第一个版本
 * 
 * Copyright (C) 2016 lizhihui
 */
package com.example.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;

import com.example.testmap.R;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;
/**
 * <p>APP自动更新辅助类 </p>
 * <h3><font color="">使用方式:</font></h3>
 * <ul>在主界面调用:
 * 	   <font color="red"> UpdateUtils.getInstance().init(Context context, String url, String filePath);</font>
 * </ul>
 * 
 * <h3>服务器xml文件示例: 可参考 http://www.0102003.com/java/update.png</h3>
 * <img src="update.png" alt="服务器xml文件示例图片,进行jar打包后图片显示不出来,请参考上述网址">
 * 
 * <h3>注意事项:</h3>
 * <ul>
 *    <li>1.1. 代码严格按照标准编码规范编写,相关标准已上传至我的服务器,可参考:http://www.0102003.com/java/CodingRuler.java<br/>
 *    		           或访问官方英文文档<br/>http://www.0102003.com/java/Java-Code-Conventions.pdf
 *    <li>1.2. 以读取服务器xml文件判断是否需要更新
 *    <li>1.3. 具体使用请参考代码注释,内有有详细说明
 *    <li>1.4. 注意添加访问网络、访问网络信息、创建文件、删除文件、写入数据、手机震动等权限
 * </ul>
 *
 * <h3>为了方便使用 ,所需权限就直接贴出来了 ╮(╯▽╰)╭  </h3>
 * 允许程序访问网络的权限<br/>
 * uses-permission android:name="android.permission.INTERNET"<br/>
 * 允许程序访问有关的网络信息<br/>
 * uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"<br/>
 * 在SDCard中创建与删除文件权限<br/>
 * uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"<br/>
 * 往SDCard写入数据权限<br/>
 * uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"<br/>
 * 手机震动权限 <br/>
 * uses-permission android:name="android.permission.VIBRATE"<br/><br/>
 * 
 * @version 1.0.0
 * @author  Lizhihui
 * @since   2016/6/28
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class UpdateUtils {
	
	/**the global UpdateUtils instance*/
    	private static UpdateUtils sInstance = null;
	
	/** 测试标签名称 */
	private final String TAG = "test";
	
	/** 上下文参数 */
	private Context mContext = null;
	
	/** 状态栏通知的管理类，负责发通知、清除通知等操作。 */
	private NotificationManager mNotificationManager = null;
	
	/** 通知栏构造器 NotificationCompat.Builder */
	private NotificationCompat.Builder mBuilder = null;
	
	/** APP更新包本地下载路径 */
	@SuppressWarnings("unused")
	private String mFilePath = "";

	/*******以下字段皆从更新xml文件获取********/
	
	/** 服务器APP更新包文件名	*/
	private String mFileName = "";
	
	/** 服务器APP更新包下载路径	 */
	private String mFileUrl = "";
	
	/** 服务器APP更新包版本号	*/
	private String mVersionCode = "";
	
	/**	服务器APP更新简介  */
	private String mInfo = "";
	
       /**
        * 获取UpdateUtils单实例对象
        * 
        * @return 返回UpdateUtils单实例对象
        */
    	public synchronized static UpdateUtils getInstance() {
        	if(sInstance == null){
        	   sInstance = new UpdateUtils();
	 	}
        	return sInstance;
    	}
    
    /**
     * 初始化APP自动更新辅助类相关参数
     * 
     * @param mContext  上下文
     * @param url 	服务器更新xml文件地址
     * @param filePath 	检测到新版本,APP安装包本地下载路径 <br/>
     *  		(v1.0目前仅支持下载到根目录,此处传""即可,后续更新)
     */
    public synchronized void init(Context context, String url, String filePath) {
    	this.mContext = context;
    	this.mFilePath = filePath;
    	// 执行异步操作 检测APP是否需要更新
    	new MyTask().execute(url);
    }

	/**
	 * 判断APP是否有新版本
	 * 
	 * @param mContext 上下文
	 */
	private void isUpdate() {
		try {
			int vc = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 1).versionCode;
			if (vc >= Integer.parseInt(mVersionCode)) {  // 已是最新版本
				Log.i("TAG", "已是最新版本");
			}
			else{// 有新的版本
				// 创建一个更新提示对话框
				AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
				alertDialog.setCancelable(true);// 设置是否可以通过点击Back键取消(点击Dialog外会取消Dialog进度条 )
				alertDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
				alertDialog.setTitle("检测到新版本");
				alertDialog.setMessage(mInfo);
				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,creatSpannableString("更新", 2), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// 判断是否是手机网络
						if (isMobileConnected(mContext)) {// 是手机网络 再次提示用户是否要进行更新,防止用户流量超标
							// 展示一个二次确认的提示对话框
							creatAlertDialog(mContext);
							return;
						}
						// 判断是否有内存卡并下载更新包
						download();
					}
				});
				alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, creatSpannableString("取消", 1), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// 关闭对话框
						dialog.dismiss();
					}
				});
				alertDialog.show();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	/**
	 * 解析xml
	 * 
	 * @param inputStream xml文件流
	 */
	@SuppressWarnings("static-access")
	private void parseXML(InputStream inputStream) {  
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(inputStream,"UTF-8");
			int type = parser.getEventType();
			while(type != parser.END_DOCUMENT) {
				switch (type) {
				case XmlPullParser.START_TAG:
					// 得到开始标签的名字
					String startName=parser.getName();
					if("versionCode".equals(startName)) {// 服务器APP安装包版本
						mVersionCode = parser.nextText();
					}
					else if("fileName".equals(startName)) {// 服务器APP安装包文件名
						mFileName = parser.nextText();
					}
					else if("fileUrl".equals(startName)) {// 服务器APP安装包下载路径
						mFileUrl = parser.nextText();
					}
					else if("info".equals(startName)) {// 更新简介
						mInfo = parser.nextText();
					}
					break;
				}
				// 重新给type赋值
				type = parser.next();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	/**
	 * 异步获取app更新版本xml
	 */
	 class MyTask extends AsyncTask<Object, Object, Object> {
		URL url;// url
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			// URL
			try {
				url = new URL((String)params[0]);	// java.net包
				// 强制转换成HttpURLConnection类才有一些设置属性的方法
				HttpURLConnection conn=(HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(15000);  
				conn.setRequestMethod("GET");
				int i=conn.getResponseCode();  
				if (i == 200) {
					InputStream in = conn.getInputStream();  
					return in;
				}
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(result!=null){
				InputStream inputStream = (InputStream)result;
				// 解析xml获取服务器上的APP版本信息
				parseXML(inputStream);
				isUpdate();
			}
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}
	}
	 
	 /**
	  * 异步下载app更新包
	  */
	 class DownloadTask extends AsyncTask<Object, Object, Object> {
		 URL url;// url
		 @Override
		 protected void onPreExecute() {
			 // TODO Auto-generated method stub
			 super.onPreExecute();
		 }
		 
		 @Override
		 protected Object doInBackground(Object... params) {
			 // TODO Auto-generated method stub
			 //URL
			 try {
				 url = new URL((String)params[0]);	//java.net包
				 // 强制转换成HttpURLConnection类才有一些设置属性的方法
				 HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				 conn.setConnectTimeout(15000);  
				 conn.setRequestMethod("GET");
				 int i = conn.getResponseCode();  
				 if (i == 200) {
					 try {
						 // 开始下载文件
						 InputStream in = conn.getInputStream();
						 // 下载的更新包
						 File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+mFileName);
						 // 每次读取1kB
						 byte[] buffer = new byte[1024]; 
						 int len = -1;
						 FileOutputStream out = new FileOutputStream(file);
						 while ((len = in.read(buffer)) != -1) {
							 	// 写入文件 
								out.write(buffer, 0, len); 
						 }
						 out.flush(); 
						 out.close(); 
						 in.close();
						 // 重新设置通知栏需要改变的属性
						 mBuilder.setContentText("下载完成,点击安装");
						 /*
						  * ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
						  * setOngoing属性设置为true之后 notification就能够一直停留在系统的通知栏直到cancel或者应用退出
						  */
						 mBuilder.setOngoing(false);
						 PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, installApk(file), 0);
						 // 点击通知栏的触发事件:此处为安装更新包
						 mBuilder.setContentIntent(contentIntent);
						 // 重新发送下载更新包完成的通知栏(通知栏标识一致覆盖原有通知,否则发送新通知)
						 mNotificationManager.notify(100, mBuilder.build());
					  } catch (Exception e) {
						  	// TODO Auto-generated catch block
							e.printStackTrace();
					  } finally {
							 
					  }
				 }
			 } catch (Exception e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
			 } finally {
				 
			 }
			 return null;
		 }
		 
		 @Override
		 protected void onPostExecute(Object result) {
			 // TODO Auto-generated method stub
			 super.onPostExecute(result);
		 }
		 
		 @Override
		 protected void onProgressUpdate(Object... values) {
			 // TODO Auto-generated method stub
			 super.onProgressUpdate(values);
		 }
	 }
	 
	/**
	 * 判断手机是否有内存卡且内存是否大于20MB
	 * 
	 * @return 返回一个Object对象
	 */
	 @SuppressWarnings("deprecation")
	 private Object storageState() {
		// 判断手机是否存在外部存储目录即SDCard(其实手机内部存储跟SD卡存储都是外部存储,真正的内部储存位置是data/data/包名)
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 没有插入SD卡也会返回true,此时拿到的是手机内存路径
			// 得到SD卡的路径 
			String path=Environment.getExternalStorageDirectory().getPath();
	        // 获得一个磁盘状态对象
	        StatFs stat = new StatFs(path);
	        // 获得一个扇区的大小 
			long blockSize = stat.getBlockSize();
	        // 获得可用的扇区数量
	        long totalBlocks = stat.getBlockCount();
	        if((totalBlocks * blockSize) / 1024 / 1024 > 20){// 剩余内存大于20M
	        	return path;
	        }
	        else{
	        	Toast.makeText(mContext, "检测到内存卡剩余空间不足,\n请清理内存后再进行下载更新", Toast.LENGTH_LONG).show();
	        	return false;
	        }
		}
		else{
			Toast.makeText(mContext, "检测到内存卡不存在,\n请重新插入内存卡再进行下载更新", Toast.LENGTH_LONG).show();
			return false;
		}
	 }
	 
	/**
	 * 展示一个二次确认的提示对话框
	 * 
	 * @param context 上下文
	 */
	 private void creatAlertDialog(Context context) {
		 	AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
			alertDialog.setCancelable(true);// 设置是否可以通过点击Back键取消(点击Dialog外会取消Dialog进度条 )
			alertDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
			alertDialog.setTitle("确认更新");
			alertDialog.setMessage("检测到当前是流量上网,更新需要部分流量,是否进行更新?\n土豪请随意...");
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,creatSpannableString("土豪就是那么自信,果断更新", 4), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// 判断是否有内存卡并下载更新包
					download();
				}
			});
			alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,creatSpannableString("本月流量余额不足,取消更新", 3),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// 关闭对话框
					dialog.cancel();
				}
			});
			alertDialog.show();
	 }
	 
	 /**
	  * 创建并初始化NotificationCompat.Builder
	  * 
	  * @return 返回一个NotificationCompat.Builder
	  */
	 private NotificationCompat.Builder initBuilder() {
			//实例化通知栏构造器NotificationCompat.Builder
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);  
			mBuilder.setContentTitle("软件更新");//设置通知栏显示标题 
			mBuilder.setContentText("下载中");//设置通知栏显示内容
			mBuilder.setAutoCancel(false);//点击通知栏信息后是否清除通知栏
			mBuilder.setTicker("开始下载最新安装包..."); //通知首次出现在通知栏，带上升动画效果的 
			mBuilder.setWhen(System.currentTimeMillis());//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间 
			mBuilder.setPriority(Notification.PRIORITY_DEFAULT);//设置该通知优先级 
			// mBuilder.setContentInfo("补充内容");//补充内容
			// mBuilder.setNumber(1);//设置通知集合的数量【默认以int型数值展示在右下角 】
			/*
			 * Notification.DEFAULT_VIBRATE // 添加默认震动提醒  需要  VIBRATE permission
			 * Notification.DEFAULT_SOUND   // 添加默认声音提醒
			 * Notification.DEFAULT_LIGHTS  // 添加默认三色灯提醒
			 * Notification.DEFAULT_ALL     // 添加默认以上3种全部提醒
			 */
			mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
			mBuilder.setSmallIcon(R.drawable.ic_launcher);//设置通知小ICON  
			/*
			 * ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
			 * setOngoing属性设置为true之后 notification就能够一直停留在系统的通知栏直到cancel或者应用退出
			 */
			mBuilder.setOngoing(true);
			/*
			 * 在2.3及更低的版本中，必须给它设置设置contentIntent,否者会报错，如果你点击没有意图，可以在赋值的的Intent中设置为new Intent()既可，切记contentIntent不能为空
			 * 在2.3及以上版本 如果不写这行代码点击通知栏无反应
			 */
			mBuilder.setContentIntent(PendingIntent.getActivity(mContext, 0,new Intent(), 0));//设置ContentIntent
			// 实例化通知栏 
			// Notification notification=mBuilder.build();
			/*
			 * 提醒标志符成员：
			 * Notification.FLAG_SHOW_LIGHTS      	//三色灯提醒，在使用三色灯提醒时候必须加该标志符
			 * Notification.FLAG_ONGOING_EVENT   	//发起正在运行事件（活动中）
			 * Notification.FLAG_INSISTENT   	 	//让声音、振动无限循环，直到用户响应 （取消或者打开）
			 * Notification.FLAG_ONLY_ALERT_ONCE  	//发起Notification后，铃声和震动均只执行一次
			 * Notification.FLAG_AUTO_CANCEL      	//用户单击通知后自动消失
			 * Notification.FLAG_NO_CLEAR          	//只有全部清除时，Notification才会清除 ，不清楚该通知(QQ的通知无法清除，就是用的这个)
			 * Notification.FLAG_FOREGROUND_SERVICE //表示正在运行的服务
			 */
			// notification.flags=Notification.FLAG_AUTO_CANCEL;
			return mBuilder;
	 }
	 
	 /** 
	  * 安装apk 
	  *  
	  * @param  file 需要安装的文件
	  * @return 返回一个Intent
	  */
	 private Intent installApk(File file) {
	  	Intent intent = new Intent(); 
	  	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	  	intent.setAction(android.content.Intent.ACTION_VIEW); 
	  	intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
	  	return intent;
	  }
	 
	 /**
	   * 判断是否是MOBILE网络连接
	   * 
	   * @param  context 上下文
	   * @return 返回boolean值,以此判断是否是手机流量上网
	   */
		private static boolean isMobileConnected(Context context) {
			if (context != null) {
				ConnectivityManager mConnectivityManager = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo mMobileNetworkInfo = mConnectivityManager
						.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				if (mMobileNetworkInfo != null) {
					 if (mMobileNetworkInfo.getState() == State.CONNECTED || mMobileNetworkInfo.getState() == State.CONNECTING) {  
						 return mMobileNetworkInfo.isAvailable();
					 	}
					}
				}
			return false;
		}
		/**
		 * 判断是否有内存卡并下载更新包
		 */
		@SuppressWarnings("static-access")
		private void download(){
			// 判断内存卡是否存在才开始下载 
			Object object = storageState();
			if(object instanceof String){// 内存卡存在,开始下载安装包同时以通知栏形式通知用户
				//***************发送通知栏通知用户正在下载更新安装包**********//
				// 实例化通知栏管理者
				mNotificationManager = (NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
				// 调用方法:初始化通知栏构造器 
				mBuilder = initBuilder();
				// 发送通知
				mNotificationManager.notify(100, mBuilder.build());// 参数一:代表通知栏标识  参数二:通知栏对象 Notification
				// 开始下载更新包
				new DownloadTask().execute(mFileUrl);
				//************************okhttp下载安装包 该方法需要导包 为了简化辅助类使之低耦合  就不采取该方式咯*************************//
				/* OkHttpUtils//
				    .get()//
				    .url("http://m.softbrain.cn:81/update.apk")//
				    .build()//
				    .execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(),mFileName)//
				    {

						@Override
						public void onError(Call call, Exception e, int arg2) {
							// TODO Auto-generated method stub
							Log.e(TAG, "onError :" + e.getMessage());
						}

						@Override
						public void onResponse(File file, int arg1) {
							// TODO Auto-generated method stub
							//重新设置通知栏需要改变的属性
							mBuilder.setContentText("下载完成,点击安装");
							//重新发送下载更新包完成的通知栏(通知栏标识一致覆盖原有,否则发送新通知)
							mNotificationManager.notify(100, mBuilder.build());
							Log.i(TAG, "下载成功:文件下载完之后的路径"+file.getAbsolutePath());
						}
				    });*/
				//************************okhttp下载安装包 该方法需要导包 为了辅助类的简单就不采取该方式咯*************************//
				//Toast.makeText(mContext, "有内存卡,路径为:"+(String)object+"\n安装包将会下载到该目录下", Toast.LENGTH_LONG).show();
				Log.i(TAG, "mVersionCode:"+mVersionCode);
				Log.i(TAG, "mFileUrl:"+mFileUrl);
				Log.i(TAG, "mFileName:"+mFileName);
				Log.i(TAG, "mInfo"+mInfo);
			}
			else if(object instanceof Boolean){// 内存卡不存在或存储空间不够
				
			}
		}
		
		/**
		 * 创建一个可添加属性的文本对象
		 * 
		 * @param string 	需要更改的字符串
		 * @param mode	   	更改标识,通过该标识判断字体需要哪种样式
		 * @return 		   	返回一个可添加属性的文本对象的文本对象SpannableString
		 */
		private SpannableString creatSpannableString(String string ,int mode) {
			SpannableString spannableString = new SpannableString(string);
			AbsoluteSizeSpan sizeSpan = null;
			ForegroundColorSpan colorSpan = null;
			if (mode== 1) {
				sizeSpan = new AbsoluteSizeSpan(18,true);
				colorSpan = new ForegroundColorSpan(Color.LTGRAY);
			}
			else if (mode == 2) {
				sizeSpan = new AbsoluteSizeSpan(18,true);
				colorSpan = new ForegroundColorSpan(Color.GREEN);
			}
			else if (mode == 3) {
				sizeSpan = new AbsoluteSizeSpan(12,true);
				colorSpan = new ForegroundColorSpan(Color.LTGRAY);
			}
			else if (mode == 4) {
				sizeSpan = new AbsoluteSizeSpan(12,true);
				colorSpan = new ForegroundColorSpan(Color.GREEN);
			}
			spannableString.setSpan(sizeSpan,0,string.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
			spannableString.setSpan(colorSpan,0,string.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
			return spannableString;
		}
}
