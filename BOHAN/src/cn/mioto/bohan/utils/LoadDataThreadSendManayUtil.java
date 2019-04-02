package cn.mioto.bohan.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/** 
 * 类说明：封装了的，用于发送和接收数据数据还没有收到数据期间，显示dialog的框架
 * 发送2条数据的线程
 * 
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年8月10日 上午11:50:21 
 */
public class LoadDataThreadSendManayUtil extends Thread{
	/*
	 * 构造方法，使用这个线程是，需要传入需要发送的指令
	 */
	private String msg;
	private Handler handler = new Handler();
	private List<String> mMsgs = new ArrayList<String>();
	private int kind = 0;
	private Context context;
	//当前所查询的设备的WIFIBSSID
	private String deviceWIFIBSSID="";

	public LoadDataThreadSendManayUtil(List<String> msgs,Context context,int kind,String deviceWIFIBSSID){//需要发送的指令
		this.mMsgs  = msgs;
		this.context = context;//把主线程的handler传进来
		this.deviceWIFIBSSID = deviceWIFIBSSID;
		BApplication.instance.setResendTaskShowBreak(false);
		this.kind = kind;
	}

	@Override
	public void run() {
		/*
		 *  说明
		 *  Constant.RESENDCOUNT  重发次数
		 */
		for(int i = 0 ; i <= 8 ; i ++){
			//Constant.RESENDCOUNT为重发次数

			//全局变量表面如果重发机制需要停止
			if(BApplication.instance.getResendTaskShowBreak()){
				Intent intent = new Intent();
				intent.setAction(Constant.BROCAST_BEGIN_LOAD_SUS);
				context.sendBroadcast(intent);
				break;//停止循环
			}

			if(i == 0){//如果是第一次，显示dialog
				Intent intent = new Intent();
				intent.setAction(Constant.BROCAST_BEGIN_LOAD_SHOW_DIALOG);
				context.sendBroadcast(intent);
				LogUtilNIU.value("isInSameWIFI()--->"+isInSameWIFI());
				if(isInSameWIFI()){
					sendUDP();
				}else{
//					ToastUtils.shortToast(context, "通过服务器发送数据");
					checkByService();
				}
			}

			if(i==1){
				if(isInSameWIFI()){
					sendUDP();
				}else{
					
				}
			}

			if(i == 2){
				if(isInSameWIFI()){
					sendUDP();
				}else{
					
				}
			}

			if(i==3){//启动服务器查询
				if(isInSameWIFI()){//在家
//					ToastUtils.shortToast(context, "通过服务器发送数据");
					checkByService();
				}else{
					
				}
			}

			if(i==4){

			}

			if(i==5){

			}

			if(i==6){

			}

			if(i==7){

			}

			if(i == 8){//发送加载失败的广播
				Intent intent = new Intent();
				intent.setAction(Constant.BROCAST_BEGIN_LOAD_FAIL);
				context.sendBroadcast(intent);
				break;//停止循环
			}

			LogUtilNIU.value("循环在运行-->"+i);

			try {
				Thread.sleep(Constant.RESENDTIME);//重发间隔3秒，每隔3秒再执行一次发生
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		super.run();
	}

	private void checkByService() {
		Intent intent = new Intent();
		intent.setAction(Constant.BROCAST_BEGIN_LOAD_LOAD_BY_SERVICE);
		Bundle b = new Bundle();
		b.putStringArrayList(Constant.BROCAST_BEGIN_LOAD_LOAD_BY_SERVICE_MSG_KEY, (ArrayList<String>) mMsgs);
		intent.putExtras(b);
		context.sendBroadcast(intent);
	}

	
	//这里间隔2秒，发送完第一条数据，再发送第二条数据
	private void sendUDP() {
		
		try {
			BApplication.instance.sendUDPMsg(this.mMsgs.get(0));
		} catch (IOException e) {
			e.printStackTrace();
		}//发送UDP数据

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			BApplication.instance.sendUDPMsg(this.mMsgs.get(1));
		} catch (IOException e) {
			e.printStackTrace();
		}//发送UDP数据
	}

	/**
	 * 
	 * @Description:判断设备和app当前是否在同一个局域网内
	 */
	private Boolean isInSameWIFI(){
		String appBSSID=BApplication.instance.getmWIFIRouterSSID();
		if(this.deviceWIFIBSSID.equals(appBSSID)){
			return true;
		}else{
			return false;
		}
	}
}
