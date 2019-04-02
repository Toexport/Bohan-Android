package cn.mioto.bohan.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

/** 
* 类说明：
* 作者：  jiemai liangminhua 
* 创建时间：2016年7月1日 下午2:01:44 
*/
public class ContextUtil2 {
	private static Context applicationContext;
	
	/**********************************************************/
	/**
	 * 获取版本号
	 * @param context
	 * @return
	 */
    public static int getVersionCode(){  
        try {  
            PackageInfo pi = applicationContext.getPackageManager().getPackageInfo(applicationContext.getPackageName(), PackageInfo.INSTALL_LOCATION_AUTO);  
            return pi.versionCode;  
        } catch (NameNotFoundException e) {  
            e.printStackTrace();  
            return 0;  
        }  
    }  

}
