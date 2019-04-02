package cn.mioto.bohan.brocast;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.socket.SocketLong;
import cn.mioto.bohan.socket.SocketLong.ReadThread;
import cn.mioto.bohan.utils.NetWorkStateUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.widget.Toast;

/**
 * 类说明： 作者： jiemai liangminhua 创建时间：2016年8月28日 下午2:18:35
 */
public class NetBoardCastReceiver extends BroadcastReceiver {
	State wifiState = null;
	State mobileState = null;
	public static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (ACTION.equals(intent.getAction())) {
			// 获取手机的连接服务管理器，这里是连接管理器类
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
					.getState();
			mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
					.getState();
			if (wifiState != null && mobileState != null
					&& State.CONNECTED != wifiState
					&& State.CONNECTED == mobileState) {
				Toast.makeText(context,
						context.getString(R.string.connection_successful),
						Toast.LENGTH_SHORT).show();
				BApplication.instance.setIsAtHome(false);
				BApplication.instance.setmWIFIRouterSSID("4G");
				if (SocketLong.mSocket == null) {
					SocketLong.startService(context);
				}
			} else if (wifiState != null && mobileState != null
					&& State.CONNECTED == wifiState
					&& State.CONNECTED != mobileState) {
				Toast.makeText(context,
						context.getString(R.string.WIFI_connection_successful),
						Toast.LENGTH_SHORT).show();
				BApplication.instance.setIsAtHome(true);

				// 设置APP当前网络的BSSID
				BApplication.instance.setmWIFIRouterSSID(NetWorkStateUtil
						.getRouterBSSID(context));
				if (SocketLong.mSocket == null) {
					SocketLong.startService(context);
				}
			} else if (wifiState != null && mobileState != null
					&& State.CONNECTED != wifiState
					&& State.CONNECTED != mobileState) {
				Toast.makeText(context,
						context.getString(R.string.mobile_phone_no_network),
						Toast.LENGTH_SHORT).show();
				BApplication.instance.setIsAtHome(false);
				BApplication.instance.setmWIFIRouterSSID("NONET");// 没有网络是BSSID为""
				if (SocketLong.mSocket != null) {
					SocketLong.stopService(context);
				}
			}
		}
		if (intent.getAction().equals(Constant.BROCAST_SOCKET_FAIL)) {
			Toast.makeText(context,
					context.getString(R.string.socket_disconnected),
					Toast.LENGTH_SHORT).show();
		}
	}

}
