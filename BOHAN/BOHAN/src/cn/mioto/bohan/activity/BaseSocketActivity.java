package cn.mioto.bohan.activity;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.entity.SingleDevice;

import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.helper.SocketClientAddress;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

/** 
 * 类说明：需要连接socket的activity基类
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年6月8日 下午2:23:45 
 */
public class BaseSocketActivity extends SteedAppCompatActivity{
	/*****初始化设备id,ip,名称**********************************************/
	protected String deviceIp ="";
	protected String deviceId="";
	protected String deviceName="";
	protected SingleDevice singleDevice;
	protected int port=3333;
	protected SocketClient socketClient;
	protected Boolean canshowDisConnectMsg=true;//可否显示断连提示
	protected Boolean disConnectByHand = false;
//	protected SocketBrocastReceiver socketBrocastReceiver;
	protected IntentFilter filter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * TODO 测试，一下初始化数据暂时抹去
		 */
		/******基类初始化设备id,ip,名称*********************************************/
//		Intent intent = getIntent();
//		deviceId=intent.getStringExtra(Constant.DEVICE_LIST_POSITION_INTENT_KEY);
//		singleDevice = BApplication.getItemByItemID(deviceId);
//		deviceIp=singleDevice.getDeviceIp();
//		deviceName = singleDevice.getDeviceName();
		
		/*  TODO
		 * 单个activity测试
		 * 
		 */
		deviceIp="192.192.1.103";
		/******得到client*********************************************/
//		socketClient=((BApplication) getApplication()).getSocketClient(deviceIp);
		/*******初始化接收频道********************************************/
		filter = new IntentFilter();
		filter.addAction(Constant.SOCKET_BROCAST_ONCONNECTED);
		filter.addAction(Constant.SOCKET_BROCAST_DISCONNECT);
		filter.addAction(Constant.SOCKET_BROCAST_ONRECEIVED);
	}
	
	@Override
	protected void onDestroy() {
		disConnectByHand=true;
		socketClient.disconnect();
		super.onDestroy();
	}
	
	
}
