package cn.mioto.bohan.utils;
/** 
 * 类说明：用于加密的类
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年8月9日 上午9:49:39 
 */
public class EncrypUtil {
	/**
	 * 
	 * @Description:用于二维码文加密的方法
	 * Parameters: 
	 * return:String
	 */
	public static String EncryptionQR(String a){
		char[]array =a.toCharArray();
		for(int i=0;i<array.length;i++)//遍历字符数组  
		{  
			array[i]=(char)(array[i] ^ 9487308);//对每个数组元素进行异或运算，异或的值可以自己选择  
		}  
		return new String(array);
	}
}
