package cn.mioto.bohan.brocast;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.utils.NetWorkStateUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.widget.Toast;

/** 
 * 类说明：
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年8月28日 下午2:18:35 
 */
public class NetBoardCastReceiver  extends BroadcastReceiver{    
	State wifiState = null;  
	State mobileState = null;  
	public static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";  
	
	@Override  
	public void onReceive(Context context, Intent intent) {  
		if (ACTION.equals(intent.getAction())) {  
			//获取手机的连接服务管理器，这里是连接管理器类  
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);    
			wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();      
			mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();  
			if (wifiState != null && mobileState != null && State.CONNECTED != wifiState && State.CONNECTED == mobileState) {  
				Toast.makeText(context, "手机网络连接成功！", Toast.LENGTH_SHORT).show(); 
				BApplication.instance.setIsAtHome(false);
		    	BApplication.instance.setmWIFIRouterSSID("4G");
			} else if (wifiState != null && mobileState != null && State.CONNECTED == wifiState && State.CONNECTED != mobileState) {  
				Toast.makeText(context, "WIFI连接成功！", Toast.LENGTH_SHORT).show();
				BApplication.instance.setIsAtHome(true);
				//设置APP当前网络的BSSID
				BApplication.instance.setmWIFIRouterSSID(NetWorkStateUtil.getRouterBSSID(context));
			} else if (wifiState != null && mobileState != null && State.CONNECTED != wifiState && State.CONNECTED != mobileState) {  
				Toast.makeText(context, "手机没有任何网络...", Toast.LENGTH_SHORT).show(); 
				BApplication.instance.setIsAtHome(false);
				BApplication.instance.setmWIFIRouterSSID("NONET");//没有网络是BSSID为""
			}  
		}  
	} 
}
