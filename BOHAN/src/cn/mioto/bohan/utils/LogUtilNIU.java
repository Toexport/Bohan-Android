package cn.mioto.bohan.utils;


import cn.mioto.bohan.BApplication;

import android.util.Log;

/** 
* 类说明：LogUtil
*/
public class LogUtilNIU {
	public static void i(String tag,Object msg) {
		if (BApplication.isRelease) {//需要自定义tag的信息
			return;
		}
		Log.i(tag, String.valueOf(msg));
	}
	
	public static void e(Object msg) {
		if (BApplication.isRelease) {//打印各种值
			return;
		}
		Log.d("DEBUG", String.valueOf(msg));
	}

	public static void v(Object msg) {//网络数据信息
		if (BApplication.isRelease) {
			return;
		}
		Log.v("WEB", String.valueOf(msg));
	}
	
	public static void web(Object msg) {//网络数据信息
		if (BApplication.isRelease) {
			return;
		}
		Log.i("发指令给服务器", String.valueOf(msg));
	}
	
	public static void other(Object msg) {//网络数据信息
		if (BApplication.isRelease) {
			return;
		}
		Log.v("OTHER", String.valueOf(msg));
	}
	
	public static void j(Object msg) {//打印Json信息
		if (BApplication.isRelease) {
			return;
		}
		Log.d("JSON", String.valueOf(msg));
	}
	
	public static void interfaceWrong(Object msg) {//打印各种小的错误信息，非catch的
		if (BApplication.isRelease) {
			return;
		}
		Log.i("INTERFACEWRONG", String.valueOf(msg));
	}
	
	public static void circle(Object msg) {//打印生命周期或跟踪执行轨迹
		if (BApplication.isRelease) {
			return;
		}
		Log.w("CIRCLE", String.valueOf(msg));
	}

	public static void value(Object msg) {//打印生命周期或跟踪执行轨迹
		if (BApplication.isRelease) {
			return;
		}
		Log.w("VALUE", String.valueOf(msg));
	}
	
	public static void online(Object msg) {//打印生命周期或跟踪执行轨迹
		if (BApplication.isRelease) {
			return;
		}
		Log.w("在线设备", String.valueOf(msg));
	}
}
