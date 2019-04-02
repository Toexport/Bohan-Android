package cn.mioto.bohan.fragment;

import java.util.ArrayList;
import java.util.List;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.utils.LogUtilNIU;

import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.os.Bundle;

/** 
 * 类说明：UDPfragment基类，区别于另一个UDPFragment 这个在初始化的时候没有初始化ID
 * 用于多个设备同时查询的情况。例如是在线列表
 * 
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年6月29日 下午2:58:07 
 */
public class BaseUDPNoCurrentFragment extends BaseFragment {
	protected IntentFilter filter;
	protected BApplication app;
	/**********************************************************/
	protected Boolean udpok;//判断发出的UDP有没有收到返回数据
	protected Boolean udpok2;//判断发出的UDP有没有收到返回数据,并列发送数据时使用
	protected List<String> devicesIds = new ArrayList<>();
	
	/***************************************************/
	protected long loadOnceDur;//每次一轮加载相隔的时间
	protected long udpSendDis;//UDP之间间隔的时间
	protected long udpNoneRecieveTime;//UDP之间间隔的时间
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app=((BApplication) getContext());
		devicesIds = app.getCurrentDeviceIds();
		/******得到app用于调用app里面发UDP的方法*********************************************/
		/*******初始化接收频道********************************************/
		filter = new IntentFilter();
		filter.addAction(Constant.SOCKET_BROCAST_ONRECEIVED);
		/***************************************************/
		//每一轮加载的时间，视设备个数来定，设备个数越多，加载的时间就越长，每发一次UDP需要间隔n毫秒的话，就需要间隔
		udpSendDis = 1000;
		loadOnceDur = devicesIds.size()*udpSendDis*2+2000;//每组数据间隔，2条指令，如每条相隔1秒，每组需要2秒
		udpNoneRecieveTime = 2000;//2秒内都没有收到1条UDP数据,就从服务器查询
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
	protected Boolean checkUDPMessage(String content,String reqCode,String mId){
		Boolean checkUDPMessageOK = false;
		LogUtilNIU.e("返回的数据总位数为"+content.length());
		//		if(content.length()<87){
		//			LogUtilNIU.e("返回的数据位数出错，总位数为");
		//			return;//不向下执行
		//		}
		String statCode = content.substring(14,16);//状态码
		String id =content.substring(2, 14);// ID
		if(id.equals(mId)){//判断接收到的信息的ID
			if(statCode.equals("00")){//状态是否成功
				if(content.substring(16,20).equals(reqCode)){//请求码
					checkUDPMessageOK=true;
				}
			}else if (statCode.equals("03")){
				LogUtilNIU.e("发送指令有错，返回03");
			}
		}else{
			//在这个设备的查询界面接收到其他Id的数据
			LogUtilNIU.e("在"+mId+"的界面接收到其他设备"+id+"的数据");
		}
		return checkUDPMessageOK;//如果没有在if内赋值，会return false
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
	protected Boolean checkUDPMessage(String content,String mId){
		Boolean checkUDPMessageOK = false;
		LogUtilNIU.e("返回的数据总位数为"+content.length());
		//		if(content.length()<87){
		//			LogUtilNIU.e("返回的数据位数出错，总位数为");
		//			return;//不向下执行
		//		}
		String statCode = content.substring(14,16);//状态码
		String id =content.substring(2, 14);// ID
		if(id.equals(mId)){//判断接收到的信息的ID
			if(statCode.equals("00")){//状态是否成功
					checkUDPMessageOK=true;
			}else if (statCode.equals("03")){
				LogUtilNIU.e("发送指令有错，返回03");
			}
		}else{
			//在这个设备的查询界面接收到其他Id的数据
			LogUtilNIU.e("在"+mId+"的界面接收到其他设备"+id+"的数据");
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
		//3A 691283749267 00 0002 0001 01 6F 0D
		Boolean isReqCodeOK = false;
		if(message.substring(16,20).equals(reqCode)){//请求码
			isReqCodeOK=true;
		}
		return isReqCodeOK;
	}
}
