package cn.mioto.bohan.utils;

import cn.mioto.bohan.BApplication;

import android.content.Context;
import android.widget.Toast;

/** 
 * 类说明：Toast工具类
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年5月26日 下午8:53:32 
 */
public class ToastUtils {
	public ToastUtils() {
	}

	public static void longToast(Context context,String text) {
		Toast.makeText(context, text, 1).show();
	}
	
	public static void longToast(Context context,int resId) {
		Toast.makeText(context, resId, 1).show();
	}

	public static void shortToast(Context context,String text) {
		Toast.makeText(context, text, 0).show();
	}
	
	public static void testToast(Context context,String text) {
		if(!BApplication.isRelease){
			Toast.makeText(context, text, 0).show();
		}
	}
}
