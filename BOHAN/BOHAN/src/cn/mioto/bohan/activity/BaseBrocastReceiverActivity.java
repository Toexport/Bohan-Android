package cn.mioto.bohan.activity;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.utils.LogUtilNIU;

import com.vilyever.socketclient.SocketClient;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

/** 
 * 此类可以作废或保留，是之前socket连接方案的类
 * 类说明：接收socket广播的activity类
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年6月15日 下午1:19:27 
 */
public class BaseBrocastReceiverActivity extends SteedAppCompatActivity {
	/*****初始化设备id,ip,名称**********************************************/
	protected String deviceId="";
	protected String deviceName="";
	protected SingleDevice singleDevice;
	/***************************************************/
	protected int port=3333;
	protected String deviceIp ="";
	protected SocketClient socketClient;
	protected IntentFilter filter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/******基类初始化设备id,ip,名称*********************************************/
		singleDevice =((BApplication)getApplication()).getCurrentDevice();//得到当前设备
		deviceIp=singleDevice.getDeviceIp();
		LogUtilNIU.e("deviceIp"+deviceIp);
//		deviceIp="192.168.31.231";
		deviceName = singleDevice.getDeviceName();
		/******得到client*********************************************/
//		socketClient=((BApplication) getApplication()).getSocketClient(deviceIp);
		/*******初始化接收频道********************************************/
		filter = new IntentFilter();
		filter.addAction(Constant.SOCKET_BROCAST_ONCONNECTED);
		filter.addAction(Constant.SOCKET_BROCAST_DISCONNECT);
		filter.addAction(Constant.SOCKET_BROCAST_ONRECEIVED);
	}
	/*
	 * Description: 当每个需要socket数据的Activity再次回到界面时，重新连接
	 *  
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

}
