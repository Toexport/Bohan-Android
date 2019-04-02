package cn.mioto.bohan.fragment;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.utils.LogUtilNIU;

import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.os.Bundle;

/** 
 * 类说明：UDPfragment基类
 * 用于单个设备
 * 
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年6月29日 下午2:58:07 
 */
public class BaseUDPFragment extends BaseFragment {
	protected IntentFilter filter;
	protected BApplication app;
	protected String deviceId;
	/**********************************************************/
	protected Boolean udpok = false;//判断发出的UDP有没有收到返回数据
	protected Boolean udpok2 = false;//判断发出的UDP有没有收到返回数据,并列发送数据时使用
	protected String deviceBSSID;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app=((BApplication) getContext());
		deviceId=app.getCurrentDevice().getDeviceID();
		LogUtilNIU.value("deviceId--->"+deviceId);
		deviceBSSID=app.getCurrentDevice().getDeviceWIFIBSSID();
		/******得到app用于调用app里面发UDP的方法*********************************************/
		/*******初始化接收频道********************************************/
		filter = new IntentFilter();
		filter.addAction(Constant.SOCKET_BROCAST_ONRECEIVED);
	}

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
		LogUtilNIU.e("返回的数据总位数为"+content.length()+"内容-->"+content);
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
	
	protected Boolean checkID(String message, String id){
		Boolean idIsSame = false ;
		String messageId =message.substring(2, 14);
		if(messageId.endsWith(id)){
			idIsSame = true;
		}
		return idIsSame;
	}
	
	/**
	 * 需要传入的参数:UDP返回的数据(不带请求码判断)
	 * 
	 * @Title: checkUDPMessage 
	 * @Description: 判断 长度 id 状态码是否符合
	 * 
	 * @return Boolean    
	 * @throws
	 */
	protected Boolean checkUDPMessage(String content){
		Boolean checkUDPMessageOK = false;
		LogUtilNIU.value("指令号"+content.subSequence(16,20)+"***返回的数据总位数为*******"+content.length());
		String statCode = content.substring(14,16);//状态码
		String id =content.substring(2, 14);// ID
		if(id.equals(deviceId)){//判断接收到的信息的ID
			if(statCode.equals("00")){//状态是否成功
					checkUDPMessageOK=true;
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
