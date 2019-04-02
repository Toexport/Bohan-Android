package cn.mioto.bohan.utils;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * 类说明：用于对字符串做判断
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年6月2日 下午1:10:59 
 */
public class StringUtil {
	/*
	 * 判断是否手机号
	 */
	public static boolean isMobileNO(String mobiles){    

		Pattern p = Pattern.compile("^((17[0-9])|(13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");    

		Matcher m = p.matcher(mobiles);    

		return m.matches();    

	} 
	/*
	 * 判断密码格式是否正确(6位以上数字或字母)
	 */
	public static boolean isPwdRight(String password){    

		if(password.length()>=6){
			Pattern p = Pattern.compile("^([A-Za-z]|[0-9])+$");    

			Matcher m = p.matcher(password);    

			return m.matches();  
		}else{
			return false;
		}
	} 
	 /** 
     * 字符串编码转GBK
     */  
    public static String changeCharset(String str,String newCharset)  
            throws UnsupportedEncodingException {  
        if (str != null) {  
            // 用源字符编码解码字符串  
            byte[] bs = str.getBytes();  
            return new String(bs, "GBK");  
        }  
        return null;  
    }  
}
