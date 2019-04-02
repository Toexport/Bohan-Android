package cn.mioto.bohan.brocast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** 
 * 类说明：广播接收者
 * 用于接收socket的广播
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年6月15日 上午10:51:40 
 */
public class SocketConnectedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
//		//区分接收到的是哪种广播
//		String action = intent.getAction();
//		if(action.equals("socketConnected")){
//			
//		}
//		else if(action.equals("socketReceiveMessage")){
//			
//		}
//		else if(action.equals("socketDisConnected")){
//			
//		}
	}

}
