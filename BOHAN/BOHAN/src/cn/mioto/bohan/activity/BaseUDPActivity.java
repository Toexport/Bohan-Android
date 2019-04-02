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

/** 
 * 类说明：UDP Activity 基类
 * 进行操作UDP 数据的Activity的初始化
 * 
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年6月23日 下午6:08:25 
 */
public class BaseUDPActivity extends SteedAppCompatActivity {
	/*****初始化设备id,ip,名称**********************************************/
	protected String deviceName="";
	protected SingleDevice currentDevice = new SingleDevice();
	/**************************************************/
	//初始化广播意图
	protected IntentFilter filter;
	protected String deviceId = "";
	/**********************************************************/
	protected Boolean udpok;//判断发出的UDP有没有收到返回数据
	protected Boolean udpok2;//判断发出的UDP有没有收到返回数据,并列发送数据时使用
	protected Boolean udpok3;//判断发出的UDP有没有收到返回数据,并列发送数据时使用
	protected BApplication app;
	protected ProgressDialog p;
	protected String deviceDBssid="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/******基类初始化设备id,ip,名称*********************************************/
		app=((BApplication)getApplication());
		currentDevice =app.getCurrentDevice();//得到当前设备
		if(currentDevice!=null){
			deviceName = currentDevice.getDeviceName();
			deviceId= currentDevice.getDeviceID();
			deviceDBssid = currentDevice.getDeviceWIFIBSSID();
		}
		/***************************************************/
		p = new ProgressDialog(this);
		/*******初始化接收频道********************************************/
		filter = new IntentFilter();
		filter.addAction(Constant.SOCKET_BROCAST_ONRECEIVED);
		filter.addAction(Constant.BROCAST_CHART_SHOW_DIALOG);
		filter.addAction(Constant.BROCAST_CHART_DIMISS_DIALOG);

		filter.addAction(Constant.BROCAST_ONLINE_LIST_SHOW_CLOSE_DIALOG);
		filter.addAction(Constant.BROCAST_ONLINE_LIST_SHOW_OPEN_DIALOG);
		filter.addAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);

		//多重发机制的广播
		filter.addAction(Constant.BROCAST_BEGIN_LOAD_SHOW_DIALOG);
		filter.addAction(Constant.BROCAST_BEGIN_LOAD_SUS);
		filter.addAction(Constant.BROCAST_BEGIN_LOAD_FAIL);
		filter.addAction(Constant.BROCAST_BEGIN_LOAD_LOAD_BY_SERVICE);
		//设置对取消的监听
		p.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				LogUtilNIU.value("点击取消了，取消线程");
				BApplication.instance.setResendTaskShowBreak(true);
				progressGettingDataDismissNoToast();
			}
		});
	}
	
	/***************************************************/
	protected Boolean refreshingOk = true;
	protected void progressShow(String message) {
		refreshingOk = false;
		p.setMessage(message);
		p.setCancelable(false);
		p.setCanceledOnTouchOutside(true);
		p.show();
	}

	protected void progressDismiss(String message) {
		if(!refreshingOk){
			refreshingOk = true;
			p.dismiss();
			ToastUtils.shortToast(this,message);
		}
	}

	protected void progressDismissNoToast() {
		if(!refreshingOk){
			refreshingOk = true;
			p.dismiss();
		}
	}

	/***************************************************/
	//获取数据的dialog效果
	protected Boolean gettedDataOK = true;
	protected void progressGettingDataShow(String message) {
		if(refreshingOk){//如果正在刷新，就不出现获取数据提示
			gettedDataOK = false;
			p.setMessage(message);
			p.setCancelable(true);
			p.setCanceledOnTouchOutside(false);
			p.show();
		}
	}

	protected void progressGettingDataDismiss(String message) {
		if(!gettedDataOK){
			gettedDataOK = true;
			p.dismiss();
			ToastUtils.shortToast(this,message);
		}
	}

	protected void progressGettingDataDismissNoToast() {
		if(!gettedDataOK){
			gettedDataOK = true;
			p.dismiss();
		}
	}
	/***************************************************/

	/**
	 * 需要传入的参数:UDP返回的数据，（带请求码判断）
	 * 
	 * @Title: checkUDPMessage 
	 * @Description: 判断 id 状态码 请求码 是否符合
	 * 
	 * @return Boolean    
	 * @throws
	 */
	protected Boolean checkUDPMessage(String content,String reqCode){
		Boolean checkUDPMessageOK = false;
		LogUtilNIU.e("返回的数据总位数为"+content.length()+"内容-->"+content);
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
	 * @Description: 判断 id 状态码 是否符合返回的数据格式
	 * 
	 * @return Boolean    
	 * @throws
	 */
	protected Boolean checkUDPMessage(String content){
		Boolean checkUDPMessageOK = false;
		LogUtilNIU.value("返回的数据总位数为"+content.length()+"内容"+content);
		String statCode = content.substring(14,16);//状态码
		LogUtilNIU.value("返回的状态码---"+statCode+"--id"+content.substring(2, 14)+"deviceId--"+deviceId);
		String id =content.substring(2, 14);// ID
		if(id.equals(deviceId)){//判断接收到的信息的ID
			LogUtilNIU.value("id正确");
			if(statCode.equals("00")){//状态是否成功
				LogUtilNIU.value("状态正确");
				checkUDPMessageOK=true;
			}else if (statCode.equals("03")){
				LogUtilNIU.value("id正确，但发送指令有错，返回03");
				checkUDPMessageOK = false;
			}
		}else{
			//在这个设备的查询界面接收到其他Id的数据
			LogUtilNIU.e("在"+deviceId+"的界面接收到其他设备"+id+"的数据");
			checkUDPMessageOK = false;
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


}
