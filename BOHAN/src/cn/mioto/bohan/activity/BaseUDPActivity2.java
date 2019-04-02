package cn.mioto.bohan.activity;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ToastUtils;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

/** 
 * 类说明：UDP Activity 基类
 * 进行操作UDP 数据的Activity的初始化
 * 
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年6月23日 下午6:08:25 
 */
public class BaseUDPActivity2 extends BaseActivity {
	/*****初始化设备id,ip,名称**********************************************/
	protected String deviceName="";
	protected SingleDevice currentDevice;
	/**************************************************/
	//初始化广播意图
	protected IntentFilter filter;
	protected String deviceId;
	/**********************************************************/
	protected Boolean udpok;//判断发出的UDP有没有收到返回数据
	protected Boolean udpok2;//判断发出的UDP有没有收到返回数据,并列发送数据时使用
	protected Boolean udpok3;//判断发出的UDP有没有收到返回数据,并列发送数据时使用
	protected BApplication app;
	protected Handler handler= new Handler();
	protected ProgressDialog p;
	protected String deviceBSSID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/******基类初始化设备id,ip,名称*********************************************/
		app=((BApplication)getApplication());
		currentDevice =app.getCurrentDevice();//得到当前设备
		deviceName = currentDevice.getDeviceName();
		deviceId= currentDevice.getDeviceID();
		deviceBSSID = currentDevice.getDeviceWIFIBSSID();
		/***************************************************/
		p = new ProgressDialog(this);
		/****得到app用于调用app里面发UDP的方法******************************/
		/*******初始化接收频道********************************************/
		filter = new IntentFilter();
		filter.addAction(Constant.SOCKET_BROCAST_ONRECEIVED);
		p.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				BApplication.instance.setResendTaskShowBreak(true);
				progressGettingDataDismissNoToast();
			}
		});
	}

	/***************************************************/
	//父类的progressDialog方法，可在子类调用
	protected Boolean refreshingOk = true;
	protected void progressShow(String message) {
		refreshingOk = false;
		p.setMessage(message);
		p.setCancelable(false);
		p.setCanceledOnTouchOutside(false);
		p.show();

	}

	protected void progressDismiss(String message) {
		if(!refreshingOk){
			refreshingOk = true;
			p.dismiss();
			ToastUtils.shortToast(this,message);
		}
	}

	/***************************************************/
	//获取数据的dialog效果
	protected Boolean gettingDataOk = true;
	protected void progressGettingDataShow(String message) {
		gettingDataOk = false;
		p.setMessage(message);
		p.setCancelable(false);
		p.setCanceledOnTouchOutside(false);
		p.show();
	}
	
	protected void progressGettingDataShow(String message,boolean show) {
		gettingDataOk = false;
		p.setMessage(message);
		p.setCancelable(show);
		p.setCanceledOnTouchOutside(show);
		p.show();
	}

	protected void progressGettingDataDismiss(String message) {
		if(!gettingDataOk){
			gettingDataOk = true;
			p.dismiss();
			ToastUtils.shortToast(this,message);
		}
	}

	protected void progressGettingDataDismissNoToast() {
		if(!gettingDataOk){
			gettingDataOk = true;
			p.dismiss();
		}
	}
	/***************************************************/


	/**
	 * 需要传入的参数:UDP返回的数据，（带请求码判断）
	 * 
	 * @Title: checkUDPMessage 
	 * @Description: 判断 长度 id 状态码 请求码 是否符合
	 * 
	 * @return Boolean    
	 * @throws
	 */
	protected Boolean checkUDPMessage(String content,String reqCode){
		Boolean checkUDPMessageOK = false;
		LogUtilNIU.e("返回的数据总位数为"+content.length());
		//		if(content.length()<87){
		//			LogUtilNIU.e("返回的数据位数出错，总位数为");
		//			return;//不向下执行
		//		}
		String statCode = content.substring(14,16);//状态码
		String id =content.substring(2, 14);// ID
		if(id.equals(deviceId)){//判断接收到的信息的ID
			if(statCode.equals("00")){//状态是否成功
				if(content.substring(16,20).equals(reqCode)){//请求码
					checkUDPMessageOK=true;
				}
			}else if (statCode.equals("03")){
				LogUtilNIU.e("发送指令有错，返回03");
			}
		}else{
			//在这个设备的查询界面接收到其他Id的数据
			LogUtilNIU.e("在"+deviceId+"的界面接收到其他设备"+id+"的数据");
		}
		return checkUDPMessageOK;//如果没有在if内赋值，会return false
	}

	/**
	 * 需要传入的参数:UDP返回的数据(不带请求码判断)
	 * 
	 * @Title: checkUDPMessage 
	 * @Description: 判断 长度 id 状态码是否符合返回的数据格式
	 * 
	 * @return Boolean    
	 * @throws
	 */
	protected Boolean checkUDPMessage(String content){
		Boolean checkUDPMessageOK = false;
		LogUtilNIU.value("返回的数据总位数为"+content.length());
		String statCode = content.substring(14,16);//状态码
		LogUtilNIU.value("返回的状态码---"+statCode+"--id"+content.substring(2, 14)+"deviceId--"+deviceId);
		String id =content.substring(2, 14);// ID
		if(id.equals(deviceId)){//判断接收到的信息的ID
			LogUtilNIU.value("id正确");
			if(statCode.equals("00")){//状态是否成功
				LogUtilNIU.value("状态正确");
				checkUDPMessageOK=true;
			}else if (statCode.equals("03")){
				LogUtilNIU.e("id正确，但发送指令有错，返回03");
			}
		}else{
			//在这个设备的查询界面接收到其他Id的数据
			LogUtilNIU.e("在"+deviceId+"的界面接收到其他设备"+id+"的数据");
		}
		LogUtilNIU.value("状态："+checkUDPMessageOK);
		return checkUDPMessageOK;//如果没有在if内赋值，会return false
	}

	/**
	 * @Title: isReqCodeEqual 
	 * @Description:判断请求码是否相等
	 * @return Boolean    
	 * @throws
	 */
	protected Boolean isReqCodeEqual(String message,String reqCode){
		Boolean isReqCodeOK = false;
		if(message.substring(16,20).equals(reqCode)){//请求码
			isReqCodeOK=true;
		}
		return isReqCodeOK;
	}

	//	@Override
	//	public boolean onKeyDown(int keyCode, KeyEvent event) {
	//		//捕获返回键按下的事件
	//		if(keyCode == KeyEvent.KEYCODE_BACK){
	//			LogUtilNIU.value("线程停止执行-------");
	//			BApplication.instance.setResendTaskShowBreak(true);
	//			progressGettingDataDismissNoToast();
	//			return true;
	//		}
	//		return super.onKeyDown(keyCode, event);
	//	}

}
