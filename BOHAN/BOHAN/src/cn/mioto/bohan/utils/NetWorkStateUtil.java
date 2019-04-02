package cn.mioto.bohan.utils;

import cn.mioto.bohan.BApplication;

import com.espressif.iot.esptouch.demo_activity.EspWifiAdminSimple;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;

/** 
* 类说明：判断网络的工具类
* 作者：  jiemai liangminhua 
* 创建时间：2016年8月27日 下午11:59:16 
*/
public class NetWorkStateUtil {
	/**
	 * 判断用户网络状态并设定app的BSSID
	 */
	public static int checkNetWorkTypeAndAction(Context context){
		ConnectivityManager	manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		State gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
	    State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
	    if(gprs == State.CONNECTED || gprs == State.CONNECTING){
	    	LogUtilNIU.value("GPS状态");
	    	BApplication.instance.setmWIFIRouterSSID("4G");
	    	//直接用服务器查询
	    	return 0;
	    }
	    if(wifi == State.CONNECTED || wifi == State.CONNECTING){
	    	LogUtilNIU.value("wifi is open!");
			BApplication.instance.setmWIFIRouterSSID(getRouterBSSID(context));
	    	return 1;
	    }
		return -1;//没有网络
	}
	
	/**
	 * 
	 * @Description:获得当前局域网路由器的Bssid
	 */
	public static String getRouterBSSID(Context context){
		EspWifiAdminSimple net = new EspWifiAdminSimple(context);
		return net.getWifiConnectedBssid();
	}
}
