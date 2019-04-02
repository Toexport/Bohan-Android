package cn.mioto.bohan.service;

import cn.mioto.bohan.utils.LogUtilNIU;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/** 
* 类说明：和服务器保持心跳的服务
* 作者：  jiemai liangminhua 
* 联系方式: elecat@126.com, QQ:349635073
* 创建时间：2016年10月15日 下午8:46:12 
*/
public class TCPAliveService extends Service{
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		LogUtilNIU.value("TCPAliveService的onCreate执行了");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		LogUtilNIU.value("TCPAliveService的onStartCommand执行了");
		return super.onStartCommand(intent, flags, startId);
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		LogUtilNIU.value("TCPAliveService的onBind执行了");
		return null;
	}
	
	@Override
	public void onDestroy() {
		
		// TODO Auto-generated method stub
		super.onDestroy();
		LogUtilNIU.value("TCPAliveService的onDestroy执行了");
	}

}
