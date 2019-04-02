package cn.mioto.bohan.utils;

import java.io.IOException;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * 类说明：封装了的，用于发送和接收数据数据还没有收到数据期间，显示dialog的框架 根据发送的数据是1条还是多条，设置数据重连时间
 * 
 * 作者： jiemai liangminhua 创建时间：2016年8月8日 上午11:50:21
 */
public class LoadDataThreadUtil extends Thread {
	/*
	 * 说明，使用此线程的时候需要传入构造方法的两个参数 需要在代码中编写hanlder的接听消息的方法
	 * 
	 * BApplication.instance.setResendTaskShowBreak(true); 用于控制此线程是进行还是停止
	 * 每次在接收到有效数据就停止，在开启线程时的构造方法中设置为运行状态
	 * 
	 * 注意： 收到数据要停止线程的时候，要调用以下
	 * BApplication.instance.setResendTaskShowBreak(true);//停止重发机制
	 * progressGettingDataDismissNoToast();
	 */
	/*
	 * 构造方法，使用这个线程是，需要传入需要发送的指令
	 */
	private String msg;
	private Handler handler;
	// 用于标记是否用服务器发送过数据的记号
	private int serviceCode = -1;
	// 当前所查询的设备的WIFIBSSID
	private String deviceWIFIBSSID = "";
	private Context context;

	/**
	 * 构造方法
	 * 
	 * @param message
	 *            发送的指令
	 * @param h
	 *            主线程的handler
	 */
	public LoadDataThreadUtil(String message, Handler h,
			String deviceWIFIBSSID, Context context) {// 需要发送的指令
		this.msg = message;// 指令
		this.context = context;
		this.handler = h;// 把主线程的handler传进来
		this.deviceWIFIBSSID = deviceWIFIBSSID;
		BApplication.instance.setResendTaskShowBreak(false);
	}

	int tryTime = 8;

	@Override
	public void run() {
		/*
		 * 说明 Constant.RESENDCOUNT 重发次数
		 */
		for (int i = 0; i <= tryTime; i++) {// Constant.RESENDCOUNT为重发次数
			// 全局变量表面如果重发机制需要停止
			if (BApplication.instance.getResendTaskShowBreak()) {
				Message message = handler.obtainMessage();
				message.what = Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK;// 停止重发线程dialog消失
				// handler向主线程发消息
				handler.sendMessage(message);
				LogUtilNIU.value("全局改变，线程停止");
				break;// 停止循环
			}

			if (i == 0) {// 如果是第一次，显示dialog
				Looper.prepare();
				Message message = handler.obtainMessage();
				message.what = Constant.MSG_WHAT_SHOW_DIALOG_OF_RESEND_TASK;// 显示dialog
				handler.sendMessage(message);
				LogUtilNIU.value("isInSameWIFI()在同一个局域网--->" + isInSameWIFI());
				if (isInSameWIFI()) {
					sendUDP();
					LogUtilNIU.value("循环0UDP发送指令");
				} else {
					checkByService();
					LogUtilNIU.value("循环0,设备不在家，通过服务器发送指令");
				}
			}

			if (i == 1) {
				if (isInSameWIFI()) {
					sendUDP();
					LogUtilNIU.value("循环1UDP发送指令");
				} else {

				}
			}

			if (i == 2) {
				if (isInSameWIFI()) {
					sendUDP();
					LogUtilNIU.value("循环2UDP发送指令");
				} else {

				}
			}

			if (i == 3) {
				if (isInSameWIFI()) {// 在家
					LogUtilNIU.value("循环3，在家但UDP是查不到状态，开启服务器查询");
					checkByService();
				} else {

				}
			}

			if (i == 4) {
				LogUtilNIU.value("循环在运行不做任何操作-->" + i);
			}

			if (i == 5) {
				LogUtilNIU.value("循环在运行不做任何操作-->" + i);
			}

			if (i == 6) {
				LogUtilNIU.value("循环在运行不做任何操作-->" + i);
			}

			if (i == 7) {
				LogUtilNIU.value("循环在运行不做任何操作-->" + i);
			}

			if (i == tryTime) {// 最后一次
				LogUtilNIU.value("最后一次循环运行，停止显示dialog" + i);
				Message message = handler.obtainMessage();
				message.what = Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK_FAIL;// 停止重发线程dialog消失
				// handler向主线程发消息
				// 提示失败
				handler.sendMessage(message);
				break;
			}

			try {
				Thread.sleep(Constant.RESENDTIME);// 重发间隔3秒，每隔3秒再执行一次发生
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		super.run();
	}

	private void checkByService() {
		// 用服务器发送的消息的内容
		LogUtilNIU.value("发handler调用服务器重查机制");
		Message message = handler.obtainMessage();
		message.what = Constant.MSG_WHAT_SEND_MSG_BY_SERVICE_RESEND_TASK_FAIL;// 通过服务器发送消息
		message.obj = msg;
		handler.sendMessage(message);
	}

	private void sendUDP() {
		try {
			BApplication.instance.sendUDPMsg(this.msg);
		} catch (IOException e) {
			e.printStackTrace();
		}// 发送UDP数据
	}

	/**
	 * 
	 * @Description:判断设备和app当前是否在同一个局域网内
	 */
	private Boolean isInSameWIFI() {
		String appBSSID = BApplication.instance.getmWIFIRouterSSID();
		LogUtilNIU.value("手机所在局域网BSSID--->" + appBSSID + "--设备所在局域网BSSID-->"
				+ this.deviceWIFIBSSID);
		if (this.deviceWIFIBSSID.equals(appBSSID)) {
			return true;
		} else {
			return false;
		}
	}

}
