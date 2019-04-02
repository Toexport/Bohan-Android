package cn.mioto.bohan.fragment;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;

import com.vilyever.socketclient.SocketClient;

import android.content.IntentFilter;
import android.os.Bundle;

/** 
 * 
 * 此类可以作废或保留，是之前socket连接方案的类
 * 类说明：接收广播的fragment基类
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年6月16日 下午9:40:29 
 */
public class BaseBrocastFragment extends BaseFragment {
	protected int port=3333;
	protected String deviceIp ="";
	protected SocketClient socketClient;
	protected IntentFilter filter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		deviceIp=((BApplication)getContext()).getCurrentDevice().getDeviceIp();
		/******得到client*********************************************/
//		socketClient=((BApplication) getContext()).getSocketClient(deviceIp);
		/*******初始化接收频道********************************************/
		filter = new IntentFilter();
		filter.addAction(Constant.SOCKET_BROCAST_ONCONNECTED);
		filter.addAction(Constant.SOCKET_BROCAST_DISCONNECT);
		filter.addAction(Constant.SOCKET_BROCAST_ONRECEIVED);
	}
	

}
