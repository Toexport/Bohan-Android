package cn.mioto.bohan.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import cn.mioto.bohan.BApplication;

/** 
 * 类说明：统一处理异常
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年5月26日 上午11:11:35 
 */
public class ExceptionUtil {

	public static void handleException(Exception e)
	{   
		//如果已经发布了软件
		if (BApplication.isRelease)
		{
			//把异常信息变成字符串
			StringWriter stringWriter=new StringWriter();
			PrintWriter printWriter=new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);
			String string=stringWriter.toString();
			//可以选择性地把异常发到服务器。发邮件，发到服务器

			//在后台打印异常
			//LogUtilNIU.i("", string);
		}else
		{
			StringWriter stringWriter=new StringWriter();
			PrintWriter printWriter=new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);
			String string=stringWriter.toString();
			LogUtilNIU.i("异常信息必须看", "  ");
			LogUtilNIU.i("异常信息必须看", "  ");
			LogUtilNIU.i("异常信息必须看", string);
			e.printStackTrace();
		}
	}


}
