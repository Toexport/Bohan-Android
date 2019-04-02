package cn.mioto.bohan.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import cn.mioto.bohan.entity.SingleDevice;

/** 
 * 类说明：计算工具类
 * 计算效验码，处理数据格式等
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年6月7日 下午9:22:59 
 */
public class ModbusCalUtil {

	/**
	 * @Title: isReqCodeEqual 
	 * @Description:判断请求码是否相等
	 * @return Boolean    
	 * @throws
	 */
	public static Boolean isReqCodeEqual(String message,String reqCode){
		//3A 691283749267 00 0002 0001 01 6F 0D
		Boolean isReqCodeOK = false;
		if(message.substring(16,20).equals(reqCode)){//请求码
			isReqCodeOK=true;
		}
		return isReqCodeOK;
	}

	/**
	 * 
	 * @Description:处理日用电量数据的方法
	 * 00000000
	 * 
	 * 
	 * 
	 */
	public String dealPowerDay(){
		return null;

	}

	/**
	 * 
	 * @Description:处理日用电功率数据的方法
	 * 
	 * 
	 */
	public String dealRateDay(){

		return null;
	}

	/**
	 * 
	 * @Description:处理月用电量数据的方法
	 * 00000000
	 * 
	 * 
	 * 
	 */
	public String dealPowerMonth(){
		return null;

	}

	/**
	 * 
	 * @Description:处理月用电功率数据的方法
	 * 
	 * 
	 */
	public String dealRateMonth(){

		return null;
	}

	/**
	 * 
	 * @Description:处理年用电量数据的方法
	 * 00000000
	 * 
	 * 
	 * 
	 */
	public String dealPowerYear(){
		return null;

	}

	/**
	 * 
	 * @Description:处理年用电功率数据的方法
	 * 000000
	 * 
	 */
	public String dealRateYear(){

		return null;
	}

	/**
	 * 批量查询继电器状态的辅助方法
	 * 传入设备集合，得到批量指令
	 * @Description:
	 */
	public static String combindMsgs(List<SingleDevice> list){
		StringBuffer b = new StringBuffer();
		for(int i = 0 ; i < list.size(); i ++){
			//遍历这个集合
			String sDeviceId = list.get(i).getDeviceID();
			String verCode = ModbusCalUtil.verNumber(sDeviceId+"00020000");
			String msg="00020000"+verCode +"0D";//查询继电器开关状态
			b.append(msg+",");
		}
		return b.toString();
	}

	/**
	 * @Description:把字符串转成16进制数，再转10进制，在转成byte数组
	 */
	public static byte[] getHexByteArray(String message) {
		int len = message.length() / 2;//
		char[] chars = message.toCharArray();//字符串转成Byte数组
		String[] hexStr = new String[len]; 
		byte[] bytes = new byte[len];
		for (int i = 0, j = 0; j < len; i += 2, j++) {
			hexStr[j] = "00" + chars[i] + chars[i + 1];
			bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
			//			LogUtilNIU.value("观察计算过程-两个数截取为-hexStr[j]--"+hexStr[j]);
			//			LogUtilNIU.value("观察计算过程--10进制结果为-bytes[j]--"+bytes[j]);
		}
		return bytes;
	}

	/** 
	 * Convert byte[] to hex string. 把字节数组转化为字符串 
	 * 这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。 
	 * @param src byte[] data 
	 * @return hex string 
	 * 可以把16进制的 3A 56 78 98 0D  转成 3A 56 78 98 0D字符~
	 */     
	public static String bytesToHexString(byte[] src){  
		StringBuilder stringBuilder = new StringBuilder("");  
		if (src == null || src.length <= 0) {  
			return null;  
		}  
		for (int i = 0; i < src.length; i++) {  
			int v = src[i] & 0xFF;  
			String hv = Integer.toHexString(v);  
			if (hv.length() < 2) {  
				stringBuilder.append(0);  
			}  
			stringBuilder.append(hv+" ");  
		}  
		return stringBuilder.toString();  
	}  
	// ------------------------------------------------------------------------------
	/** 
	 * 将十六进制字符数组转换为字节数组 
	 *  
	 * @param data 
	 *            十六进制char[] 
	 * @return byte[] 
	 * @throws RuntimeException 
	 *             如果源十六进制字符数组是一个奇怪的长度，将抛出运行时异常 
	 */  
	public static byte[] decodeHex(char[] data) {  

		int len = data.length;  

		if ((len & 0x01) != 0) {  
			throw new RuntimeException("Odd number of characters.");  
		}  

		byte[] out = new byte[len >> 1];  

		// two characters form the hex value.  
		for (int i = 0, j = 0; j < len; i++) {  
			int f = toDigit(data[j], j) << 4;  
			j++;  
			f = f | toDigit(data[j], j);  
			j++;  
			out[i] = (byte) (f & 0xFF);  
		}  

		return out;  
	} 

	/** 
	 * 将十六进制字符转换成一个整数 
	 *  
	 * @param ch 
	 *            十六进制char 
	 * @param index 
	 *            十六进制字符在字符数组中的位置 
	 * @return 一个整数 
	 * @throws RuntimeException 
	 *             当ch不是一个合法的十六进制字符时，抛出运行时异常 
	 */  
	protected static int toDigit(char ch, int index) {  
		int digit = Character.digit(ch, 16);  
		if (digit == -1) {  
			throw new RuntimeException("Illegal hexadecimal character " + ch  
					+ " at index " + index);  
		}  
		return digit;  
	}  
	// ------------------------------------------------------------------------------

	/**
	 * 
	 * @Description:转为16进制数组
	 */
	private byte[] getHexBytes(String message) {
		int len = message.length() / 2;
		char[] chars = message.toCharArray();
		String[] hexStr = new String[len];
		byte[] bytes = new byte[len];
		for (int i = 0, j = 0; j < len; i += 2, j++) {
			hexStr[j] = "" + chars[i] + chars[i + 1];
			bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
		}
		return bytes;
	}


	/**
	 * 柱状图的最大纵坐标的计算方法
	 * @Title: countBiggestShowItsUpper 
	 * @Description:计算传过来的数集合中最大的那个数的上一级数
	 * 
	 * 
	 * 例如 
	 * 传入来的集合中最大的书为 1499.00  则结果为 1300.00 
	 * 传入来的集合中最大的书为 140.00  则结果为 150.00
	 * 如果传过来的是小于1的数，则返回1
	 * 
	 * 观察规律 为这个数的有效数位的第二位 +1
	 * 
	 * 对于此算法，针对两位数时要特殊处理
	 * 原传入42，算法运算后为43，此规律有BUG,如果数为42.99， 就会出现显示不全
	 * 
	 * 所以规律应该改为 这个数的有效数位的第二位 +2
	 * 
	 */
	public static Double countBiggestShowItsUpper(List<Double> numbers){
		Double max =countBiggest(numbers); 
//		StringBuffer b = new StringBuffer();
//		if(max>=0&&max<=0.1){
//			return 0.12;
//		}else if(max > 0.1&&max <=0.25){
//			return 0.3;
//		}else if(max > 0.25&&max<=0.50){//如果最大的小于1，最大的值就为1
//			return 0.6;
//		}else if (max>0.50&&max<=1.00){
//			return 1.2;
//		}else if (max > 1 &&max<=2.5){
//			return 2.8;//   9   6.54  106  1204.76
//		} else if (max > 2.5 &&max<=5){
//			return 5.20;//   9   6.54  106  1204.76
//		}else if (max > 5 &&max<=10){
//			return 12.00;//   9   6.54  106  1204.76
//		}else if (max > 10){ //12.00 13.00  
//			//把数字化为字符
//			String m =String.valueOf(max);
//			//算出小数点在第几为
//			int dot = m.indexOf(".");//小数点在第几位
//			if(m.substring(1,2).equals("9")){
//				//如果第二个数是9的话
//				//第二个数变成1，第一个数加1
//				int one =Integer.valueOf(m.substring(0,1))+1;//第一个数加1
//				b =b.append(one+"");
//				b= b.append("1");//第二个9+2数为1
//				b = b.append(m.substring(2,dot));//第二位后面到小数点前都是一样不变
//				b = b.append(".00");//最后保留2个零
//			}else if(m.substring(1,2).equals("8")){
//				//如果第二位为8
//				int one =Integer.valueOf(m.substring(0,1))+1;//第一个数加1
//				b= b.append("0");//第二个8+2数为0
//				b = b.append(m.substring(2,dot));//第二位后面到小数点前都是一样不变
//				b = b.append(".00");//最后保留2个零
//			}else{
//				b =b.append(m.substring(0,1));
//				b = b.append((Integer.valueOf(m.substring(1, 2))+2)+"");
//				b = b.append(m.substring(2,dot));
//				b = b.append(".00");
//			}
//			//结果小数点后变成  .00
//			return Double.valueOf(b.toString());
//		}else{
//			return 0.0;
//		}
		if(max==0){
			return 1.0;
		}
		//向上取整
		return Math.ceil(max*1.2);
	}
	
	/**
	 * 
	 * @Description:区域图的最大纵坐标的计算方法
	 */
	public static Double countBiggestShowItsUpperArea(List<Double> numbers){
		Double max =countBiggest(numbers); 
		//直接是传入的数的1.2倍
		return max*1.2;
	}

	/**
	 * 
	 * @Description:得到集合中最大的数
	 */
	public static Double countBiggest(List<Double> numbers){
		Double max = 0.0;
		max = numbers.get(0);
		for(int i = 0 ; i < numbers.size()-1;i++){//前面哪个数是否大于后面那个数
			max = max>=numbers.get(i+1)?max:numbers.get(i+1);
		}
		return max;
	}

	/**
	 * @Title: countTotalSeconds 
	 * @Description:计算型如  304520（30：45：20） 这样的字符一共是多少秒
	 * @return int 
	 * @throws
	 */
	public static int countTotalSeconds(String duraction){
		String hh =duraction.substring(0, 2);
		String mm =duraction.substring(2,4);
		String ss = duraction.substring(4);
		return Integer.valueOf(hh)*60*60+Integer.valueOf(mm)*60+Integer.valueOf(ss);
	}

	/**
	 * @Title: twoTimepercent 
	 * @Description:总时间和剩余时间之间的百分比
	 * @return int    
	 * @throws
	 */
	public static int twoTimePercent(String leftTime, String totalTime){
		//两个时间全部化为秒   003200 084443 
		int left = stringToSecond(leftTime);
		int total =stringToSecond(totalTime);
		return left/total*100;
	}

	private static int stringToSecond(String time){
		String hh =time.substring(0,2);
		String mm = time.substring(2, 4);
		String ss = time.substring(4);
		int h = 0;
		int min = 0;
		int sec = 0;
		if(hh.equals("00")){//如果有2位是0
			h=0;
		}
		if(hh.substring(0,1).equals("0")){//如果有1位是0
			h = Integer.valueOf(hh.substring(1, 2));
		}
		if(mm.equals("00")){
			min=0;
		}
		if(mm.substring(0,1).equals("0")){
			min = Integer.valueOf(hh.substring(1, 2));
		}
		if(ss.equals("00")){
			sec=0;
		}
		if(ss.substring(0,1).equals("0")){
			sec = Integer.valueOf(hh.substring(1, 2));
		}
		return h*60*60+min*60+sec;
	}

	/**
	 * 把16进制字符转为2进制字符
	 * @Description:  
	 * param:
	 */
	public static String hexStringToBinaryString(String hex){
		//16进制字符转为10进制数值
		int hexInt = Integer.valueOf(hex,16);
		//10进制数值转为2进制字符
		String binaryString = Integer.toBinaryString(hexInt);
		
		return binaryString;
	}
	
	/**
	 * @Title: hexweekDaysToWeekShow 
	 * @Description:让星期16进制数据显示为星期文字
	 * 
	 * @return String 星期的文字显示    
	 * @throws
	 */
	public static String hexweekDaysToWeekShow(String hexweekDays){
		// 16进制转10进制在转字符串
		int weekDays=Integer.valueOf(hexweekDays,16);
		//			LogUtilNIU.value("16进制字符转10进制weekDays--"+weekDays);
		//转化为二进制形式  如  101110011
		String binaryWeekDays=Integer.toBinaryString(weekDays);
		//			LogUtilNIU.value("10进制转2进制"+binaryWeekDays);
		//反转这个字符串  011001101
		//			LogUtilNIU.value("显示日期8位数为binaryWeekDays"+binaryWeekDays);
		//向前补0满8位
		binaryWeekDays=ModbusCalUtil.add0fillLength(binaryWeekDays, 8);
		binaryWeekDays=ModbusCalUtil.reverseString(binaryWeekDays);
		//			LogUtilNIU.value("翻转2进制数"+binaryWeekDays);
		String weekDaysShow="一二三四五六日";
		for(int i =0; i <binaryWeekDays.length()-1;i++){//遍历前7个字符
			char c =binaryWeekDays.charAt(i);
			//				LogUtilNIU.value(i+"位置"+c);
			if(c=='1'){//如果该字符等于1
				// weekDaysShow 的该为char不用变化
				//					LogUtilNIU.value("c==1---"+c);
			}else if (c=='0'){
				//					LogUtilNIU.value("weekDaysShow.charAt(i)--"+weekDaysShow.charAt(i));
				weekDaysShow=weekDaysShow.replace(weekDaysShow.charAt(i),' ');
				//					LogUtilNIU.value("c==0---"+c);
				//					LogUtilNIU.value("weekDaysShow---"+weekDaysShow);
			}
		}
		//			LogUtilNIU.e("weekDaysShow---->"+weekDaysShow);
		weekDaysShow=weekDaysShow.replace(" ", "");
		LogUtilNIU.value("计算出来的星期weekDaysShow-->"+weekDaysShow);
		return weekDaysShow;
	}

	/**
	 * @Title: countOrder 
	 * @Description:用于计算传进来的正整数是多少位的方法
	 * @return Integer    
	 * @throws
	 */
	private int countOrder(int number){
		int order = -1;//默认1位
		for(int i = 1 ; i <10 ; i ++){
			if(number/(count10Squ(i))<1){
				order=i;
				break;
			}
		}
		return order;
	}

	/**
	 * @Title: count10Squ 
	 * @Description: 计算10r的j次方
	 * @return Integer    
	 * @throws
	 */
	private int count10Squ(int j){
		int result = 1;
		for(int i = 0 ; i < j ;i++){
			result = result*10;
		}
		return result;
	}

	/**
	 * @Title: hexWeekDayToBineray 
	 * @Description:把16进制化为101010的二进制形式
	 * @return void    
	 * @throws
	 */
	public static String hexWeekDayToBinerayString(String hexweekDays){
		// 16进制字符转10进制
		int weekDays=Integer.valueOf(hexweekDays,16);
		//转化为二进制形式  如  101110011
		String binaryWeekDays=Integer.toBinaryString(weekDays);
		//当这个二进制前面是0时，不会显示。所以要向前补0满8位
		binaryWeekDays=add0fillLength(binaryWeekDays, 8);
		return binaryWeekDays;
	}

	/**
	 * 正则表达式判断扫描的二维码是否符合表地址格式
	 * 68或69开头，加11为数字
	 * 表地址格式：12位英文和数字混合的字符串
	 */
	public static Boolean isBiaoAddress(String biaoString){
		Pattern pattern = Pattern.compile("^[6]{1}+[89]{1}+[0-9]{10}$");
		return pattern.matcher(biaoString).matches(); 
	}
	/**
	 * @Title: countTimeDistance 
	 * @Description:计算两个时间差
	 * 两个时间的格式为yyyyMMddHHmmss形式
	 * @return String    
	 * @throws
	 */
	public static List<String> countTimeDistance(String time1, String time2){
		int yearDis=Integer.valueOf(time2.substring(0, 4))-Integer.valueOf(time1.substring(0, 4));
		int monDis = Integer.valueOf(time2.substring(4, 6))-Integer.valueOf(time1.substring(4, 6));
		int dayDis = Integer.valueOf(time2.substring(6, 8))-Integer.valueOf(time1.substring(6, 8));
		int hourDis = Integer.valueOf(time2.substring(8, 10))-Integer.valueOf(time1.substring(8, 10));
		int MinDis = Integer.valueOf(time2.substring(10, 12))-Integer.valueOf(time1.substring(10, 12));
		int SecDis = Integer.valueOf(time2.substring(12))-Integer.valueOf(time1.substring(12));
		List<String> dis = new ArrayList<>();
		dis.add(yearDis+"");
		dis.add(monDis+"");
		dis.add(dayDis+"");
		dis.add(hourDis+"");
		dis.add(MinDis +"");
		dis.add(SecDis+"");
		return dis;
	}

	/**
	 * 反转字符串
	 */
	public static String reverseString(String str) {  
		if (str == null || str.length() <= 1) {  
			return str;  
		}  
		StringBuffer sb = new StringBuffer(str);  
		sb = sb.reverse();  
		return sb.toString();  
	}  

	/**********************************************************/
	/** 
	 *  计算两个date之间的时间差 精确到分钟
	 */
	public static String calDisOfDate(Date date1, Date date2) {
		Calendar c1 = Calendar.getInstance();
		c1.clear();
		Calendar c2 = Calendar.getInstance();
		c2.clear();
		c1.setTime(date1);
		c2.setTime(date2);
		long time1 = c1.getTimeInMillis();
		long time2 = c2.getTimeInMillis();
		long diff = time2 - time1;//得到两个时间的毫秒差
		long ss=diff/1000;//毫秒转秒值
		long mm=0;
		if(ss>60l){
			mm =ss/60;//秒转分
			ss=ss%60;//转为分后剩下的余数直接显示-----ss
		}
		long hh=0;
		if(mm>60){
			hh = mm/60;//分转小时
			mm= mm%60;//转为小时后剩下的余数直接显示-----mm
		}

		long dd=0;
		if(hh>=24l){//如果算得的小时大于24小时,则要显示天
			dd=hh/24;//则给天赋值
			hh = hh%24;//小时转天后的余数---将显示为小时
		}
		String hhs=add0fillLength(hh+"", 2);
		String mms = add0fillLength(mm+"", 2);
		String formated = dd+"天  "+hhs+":"+mms;
		LogUtilNIU.value("旧方法执行"+ss);
		return formated;  
	}
	
	
	public static String binaryStringToHexString(String binaryString){
		String intValue=Integer.valueOf(binaryString,2).toString();
		return String.valueOf(Integer.toHexString(Integer.valueOf(intValue)));  	
	}

	/**********************************************************/
	/**
	 * @Title: calDisOfDate 
	 * @Description:计算两个date之间的时间差 精确到秒 
	 * 
	 * 注意：此方法带3参数，表示此模式返回数字带秒显示
	 * 
	 * 
	 * date2要大于date1
	 * 
	 * @return String    
	 * @throws
	 */
	public static String calDisOfDate(Date date1, Date date2,int mode) {
		Calendar c1 = Calendar.getInstance();
		c1.clear();
		Calendar c2 = Calendar.getInstance();
		c2.clear();
		c1.setTime(date1);
		c2.setTime(date2);
		long time1 = c1.getTimeInMillis();
		long time2 = c2.getTimeInMillis();
		long diff = time2 - time1;//得到两个时间的毫秒差
		if(diff<=0){//如果后者比前者小
			return "small";
		}
		long ss=diff/1000;//毫秒转秒值
		long mm=0;
		if(ss>60l){
			mm =ss/60;//秒转分
			ss=ss%60;//转为分后剩下的余数直接显示-----ss
		}
		long hh=0;
		if(mm>60){
			hh = mm/60;//分转小时
			mm= mm%60;//转为小时后剩下的余数直接显示-----mm
		}
		String hhs=add0fillLength(hh+"", 2);
		String mms =add0fillLength(mm+"", 2);
		String sss = add0fillLength(ss+"", 2);
		String formated = hhs+" : "+mms+" : "+sss;
		return formated;  
	}

	/**********************************************************/
	/**
	 * 毫秒时间化为插座时间
	 */
	public static String millFormat(Date mdate){
		Calendar c = Calendar.getInstance();
		c.setTime(mdate);
		int year = c.get(Calendar.YEAR);
		int month=c.get(Calendar.MONTH);
		month=month+1;//系统月加1等于真实月份
		int date = c.get(Calendar.DATE);
		int week = c.get(Calendar.DAY_OF_WEEK);
		week = week-1;//系统日期4，等于真实日期3。转化为插座日期
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		int se= c.get(Calendar.SECOND);
		String yy = year+"";
		yy=yy.substring(2);
		String mm = month+"";
		mm=add0fillLength(mm, 2);
		String dd = date+"";
		dd =add0fillLength(dd, 2);
		String ww = week+"";
		ww = add0fillLength(ww, 2);
		String hh = hour+"";
		hh=add0fillLength(hh, 2);
		String mi = min+"";
		mi = add0fillLength(mi, 2);
		String ss = se+"";
		ss =add0fillLength(ss, 2);
		return yy+mm+dd+ww+hh+mi+ss;
	}
	/**********************************************************/
	/**
	 * 获取系统当前时间并转化为电表需要的格式
	 */
	public static String getSystemTimeFormat(){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		int year = c.get(Calendar.YEAR);
		int month=c.get(Calendar.MONTH);
		month=month+1;//系统月加1等于真实月份
		int date = c.get(Calendar.DATE);
		int week = c.get(Calendar.DAY_OF_WEEK);
		week = week-1;//系统日期4，等于真实日期3。转化为插座日期
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		int se= c.get(Calendar.SECOND);
		String yy = year+"";
		yy=yy.substring(2);
		String mm = month+"";
		mm=add0fillLength(mm, 2);
		String dd = date+"";
		dd =add0fillLength(dd, 2);
		String ww = week+"";
		ww = add0fillLength(ww, 2);
		String hh = hour+"";
		hh=add0fillLength(hh, 2);
		String mi = min+"";
		mi = add0fillLength(mi, 2);
		String ss = se+"";
		ss =add0fillLength(ss, 2);
		return yy+mm+dd+ww+hh+mi+ss;
	}

	/**********************************************************/
	/**
	 * 把字符串拆解为2个2个的形式
	 */
	public static String[] sepTo2(String order){
		if(order.length()%2 != 0){
			order = order + "0";
		}
		String[] group = new String[order.length()/2];
		for(int i = 0 ; i<order.length(); i+=2){
			String hex = order.substring(i,i+2);
			group[i/2]=hex;
		}
		return group;
	}
	/**********************************************************/
	/**
	 * 16进制字符转10进制数
	 */
	private static int HexToInt(String hex){
		return Integer.valueOf(hex,16);
	}

	/**********************************************************/
	/**
	 * 把传入来的字符串算出校验码(String 16进制表示)
	 */
	public static String verNumber(String order){
		String[] group =sepTo2(order);
		int sum = 0;
		for(int i = 0 ; i < group.length; i++){
			int ten=HexToInt(group[i]);
			sum+=ten;
		}
		String ver = Integer.toHexString(sum);
		int n =ver.length();   
		return ver.substring(n-2).toUpperCase();//截取字符串后两位
	}
	/**********************************************************/
	/**
	 * 字符串按位数信息插入小数点
	 * content 原字符    
	 * dotLocation 要插入“.”的位置
	 * 
	 * 000.000
	 * 002 039
	 */
	private static String addDot(String content,int dotLocation){
		return content.substring(0,dotLocation)+"."+content.substring(dotLocation,content.length());
	}
	/**********************************************************/
	/**
	 * 除去字符串前面的0 
	 */
	private static String deleFront0(String str){
		int len = str.length();//取得字符串的长度
		int index = 0;//预定义第一个非零字符串的位置

		char strs[] = str.toCharArray();// 将字符串转化成字符数组
		for(int i=0; i<len; i++){
			if('0'!=strs[i]){
				index=i;// 找到非零字符串并跳出
				break;
			}
		}
		String strLast = str.substring(index, len);// 截取字符串
		if(strLast.startsWith(".")){//但是，如果去零以后，已经到了小数点。就要加回一个0  
			strLast="0"+strLast;
		}
		return strLast;
	}
	/**********************************************************/
	/**
	 * 加点去零
	 * 处理数据
	 */
	public static String addDotDel0(String str,int lo){
		return deleFront0(addDot(str, lo));
	}

	/**********************************************************/
	/**
	 * 判断字符是否为纯数字
	 */
	private static boolean isNumeric(String str){
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();   
	}
	/**********************************************************/
	/**
	 * 验证用户输入的数据的格式是否正确
	 * 规定：非负浮点数
	 * ^\d+(\.\d+)?$
	 */
	public static boolean nonNegfloat(String str){
		Pattern pattern = Pattern.compile("^\\d+(\\.\\d+)?$");
		return pattern.matcher(str).matches();   
	}
	/**
	 * @Title: is2SmallNumber 
	 * @Description:判断输入的数是正数或两位小数以内。
	 * @return boolean    
	 * @throws
	 */
	public static boolean is2SmallNumber(String str){
		Pattern pattern = Pattern.compile("^(?!0+(?:\\.0+)?$)(?:[1-9]\\d*|0)(?:\\.\\d{1,2})?$");
		return pattern.matcher(str).matches(); 
	}

	/**********************************************************/
	/**
	 * 去点加零
	 * str 将要处理的字符
	 * ruleLength 规定的字符长度(除去点后)
	 * dotlo 原要求的小数点位置占长度的位置
	 * 
	 * 
	 * 000000 ruleLength =6
	 * 000.000 int dotlo =3
	 * 22.8 str
	 * 022800 result
	 * 
	 * 
	 * 如果传如来的是整数，没有小数点
	 * 000000
	 * 000.000
	 * 32
	 */
	public static String delDotFillLength(String str,int ruleLength,int dotlo){
		/*****纯数字**********************************************/
		if(isNumeric(str)){
			String front = add0fillLength(str,dotlo);
			StringBuffer b = new StringBuffer();
			while(b.length()<ruleLength-dotlo){
				b=b.append("0");
			}
			String result = front+b.toString();
			return result;
		}
		/****带有小数点***********************************************/	
		String [] gr=str.split("\\.");//按小数点拆开
		String front=add0fillLength((gr[0]),dotlo);//向前补0
		int backLength = ruleLength-dotlo;
		while(gr[1].length()<backLength){
			gr[1]=gr[1]+"0";
		}
		String result = front+gr[1];
		return result;
	}

	/**********************************************************/
	/**
	 * 补零满数位
	 * str 原字符
	 * ruleLength数位
	 * 
	 */
	public static String add0fillLength(String str,int ruleLength){
		str = String.format("%0"+ruleLength+"d", Integer.valueOf(str)); 
		return str;
	}
	/**********************************************************/
	/**
	 * 去除小数点
	 */
	private static String delDot(String str){
		String b = new String();
		for(int i = 0 ; i < str.length(); i ++){
			if(str.charAt(i)!='.'){
				b=b+str.charAt(i);
			}
		}
		return b;
	}





}
