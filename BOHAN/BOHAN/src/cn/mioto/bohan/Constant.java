package cn.mioto.bohan;

import android.os.Environment;

/** 
 * 类说明：常量类
 */
public class Constant {
	//文件的根目录
	public static final String SDCARD_ROOT=Environment.getExternalStorageDirectory().getAbsolutePath();
	//用于存放伯瀚apk的目录
	public static final String  APK_PATH=SDCARD_ROOT+"/bohan/apk";
	/***************************************************/
	//用电图表值 用电量
	public static double CHART_BART_HOUR_DEFAULT_MAX = 2.5;
	public static double CHART_BAR_DAY_DEFAULT_MAX = 60;
	public static double CHART_BAR_MONTH_DEFAULT_MAX = 1800;
	
	public static double CHART_AREA_HOUR_DEFAULT_MAX = 2.5;
	public static double CHART_AREA_DAY_DEFAULT_MAX = 2.5;
	public static double CHART_AREA_MONTH_DEFAULT_MAX = 2.5;
	/***************************************************/
	public static final String baseUrl = "http://211.149.227.179:8080/boHan/";
	public static String SERIAL_NUMBER_INTENT_KEY = "serialNumber";
	public static String BOHAN_WEB = "http://www.bohan-smartec.com/";
	/*
	 * 设备列表对象的对象下标，用于itent传递数据时指定key值
	 */
	//	public static String DEVICE_LIST_POSITION_INTENT_KEY="devliceListPosition";
	/*
	 * 三种广播
	 */
	public static String SOCKET_BROCAST_ONCONNECTED="socketConnected";
	public static String SOCKET_BROCAST_ONRECEIVED="socketReceive";
	public static String SOCKET_BROCAST_DISCONNECT="socketDisconnected";
	
	/***************************************************/
	//控制progressDialog出现的广播
	public static String BROCAST_ONLINE_LIST_SHOW_OPEN_DIALOG="onlineShowOpen";
	public static String BROCAST_ONLINE_LIST_SHOW_CLOSE_DIALOG="onlineShowClose";
	public static String BROCAST_ONLINE_LIST_DIMISS_DIALOG="onlineDialogDimiss";
	public static String BROCAST_REFRESHING0002_DIALOG="onlineShow0002";
	//
	public static String BROCAST_CHART_DIMISS_DIALOG="chartDialogDimiss";
	public static String BROCAST_CHART_SHOW_DIALOG="chartDialogShow";
	
	
	public static String NINE_SET_SETTING_SP="nineSetSp";
	/***************************************************/
	public static String BROCAST_ALL_DATE_POWER_DATA="allDeviceDatePowerData";
	public static String BROCAST_ALL_DATE_POWER_DATA_EXTRA="allDeviceDatePowerDataExtra";
	// 9组定时最下面的大按钮出现
	public static String BROCAST_NINE_SET_SHOW_BOTTOM_BTN="showBottomBtn";
	/***************************************************/
	//传递数据
	public static String DATA_ALL_DEVICE_DATE="dataAllDeviceDate";
	/*
	 * socket 消息 key
	 */
	public static String SOCKET_BROCAST_EXTRA_MESSAGE="socketMessageExtra";
	/*
	 * 查询用电参数的指令
	 */
	public static String CHECK_NOW_DATA_INSTRUCTION="3AA200010000A30D";
	/*
	 * 传递的bundle key
	 */
	public static String SINGLE_DEVICE_30_DAY_POWER_BUNDLE_KEY="singleDevice30dayPower";
	public static String SINGLE_DEVICE_24_HOUR_POWER_BUNDLE_KEY="singleDevice24hourPower";
	public static String SINGLE_DEVICE_12_MONTH_POWER_BUNDLE_KEY="singleDevice12monthPower";
	public static String SINGLE_DEVICE_30_DAY_RATE_BUNDLE_KEY="singleDevice30dayRate";
	public static String SINGLE_DEVICE_24_HOUR_RATE_BUNDLE_KEY="singleDevice24hourRate";
	public static String SINGLE_DEVICE_12_MONTH_RATE_BUNDLE_KEY="singleDevice12monthRate";
	/*
	 * 注册成功的resultCode
	 */
	public static String REGISTER_OK_RESULT_USER_NAME_BUNDLE_KEY="userName";
	public static String REGISTER_OK_RESULT_USER_PASSWORD_BUNDLE_KEY="password";
	public static int REGISTER_OK_RESULT=20;
	public static String RESULT_START_TIME_HOUR="startHour";
	public static String RESULT_START_TIME_MIN="startMin";
	public static String RESULT_END_TIME_HOUR="endHour";
	public static String RESULT_END_TIME_MIN="endMin";
	public static String DATA_NINE_SET_SET_RESULT_EXTRA="nineSetSetResult";
	public static String DEVICE_COUNT_DOWN_DURACTION="deviceCountDuraction";
	public static String POSITION_NINE_SET_SETTING_ITEM="positionNineSetSettingItem";//被设置的项目的位置
	/*
	 * 传递值，9组时段传到设置页的时段信息
	 */
	public static String DATA_EXISTING_TIME="exsitingTime";//现在已经设置了的时间
	public static String DEVICE_LIST_JSON="deviceListJson";
	/***************************************************/
	public static int DELAY = 1000;//UDP查询完毕后，从服务器查询的延迟秒数
	public static int REQUEST_TIME_OUT = 15000;//服务器超时时间，超过这个时间代表从服务器查询数据失败
	public static int UDP_LIST_UPDATE = 1000;// UDP在线列表的指令间隔
	public static int UDP_CHART_SEND_DIS = 1200;//柱状图数据的指令间隔
	public static int CHART_Y_TEXT_SIZE = 26;// 柱子图Y坐标标签字体大小
	public static int CHART_X_TEXT_SIZE = 26;// 柱子图Y坐标标签字体大小
	public static int CHART_DATA_TEXT_SIZE = 26;// 柱子图Y坐标标签字体大小
	/***************************************************/
	//时间选择器所规定的终止时间范围
	
	//封装的数据重发机制所需参数
	public static int RESENDCOUNT = 4;//加载单个数据的超时提示时间
	
	//*******重发机制间隔**********
	public static long RESENDTIME = 2000;//重发机制的间隔
	public static long REFRESH_ONLINE = 500;//控制继电器后刷新的间隔
	
	public static int MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK = 5;//停止重发机制，dialog消失
	public static int MSG_WHAT_SHOW_DIALOG_OF_RESEND_TASK = 6;//停止重发机制，dialog消失
	public static int MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK_FAIL = 7;//发送几次后，都失败，停止重发机制，dialog消失
	public static int MSG_WHAT_SEND_MSG_BY_SERVICE_RESEND_TASK_FAIL = 8;//发送几次后，都失败，停止重发机制，dialog消失
	private static int LOADDATATIMEOUTSINGLEALL = 1500;//加载单个数据的超时提示时间
	//保存的列表信息字符
	public static String SP_KEY_LOCATIONS="locationsListSp";//位置信息
	public static String SP_KEY_TYPES="locationsTypesSp";//用电类别信息
	public static String SP_KEY_BRANDS="locationsBrandsSp";//品牌信息
	//
	public static String INTENT_KEY_THIS_SORT="thisType";//本类型
	public static String INTENT_KEY_SHARE_CODE="shareCode";//本类型
	// 重发机制多信息发送中使用到的广播
	public static String BROCAST_BEGIN_LOAD_SHOW_DIALOG="beginToLoad";//开始下载
	public static String BROCAST_BEGIN_LOAD_SUS="beginToLoadSus";//下载成功
	public static String BROCAST_BEGIN_LOAD_FAIL="beginToLoadFail";//下载失败
	public static String BROCAST_BEGIN_LOAD_LOAD_BY_SERVICE="beginToLoadService";//服务器下载数据
	public static String BROCAST_BEGIN_LOAD_LOAD_BY_SERVICE_MSG_KEY="beginToLoadbyService";//服务器下载数据
	//要求服务器发送数据的广播中传递的内容Key值
	public static String BROCAST_ACTION_NINESETADAPTER_OPENINESET="openNineSet";
	// 9组定时adapter 命令 9组定时activity发送开指令的广播
	public static String BROCAST_ACTION_NINESETADAPTER_CLOSENINESET="closeNineSet";
	// 9组定时adapter 命令 9组定时activity发送关指令的广播所携带的指令数据
	public static String BROCAST_DATA_NINESETADAPTER_MSG="msgNineSet";
	
	
	/**
	 * 通用消息字段
	 */
	public static final int MSG_NETWORK_ERROR = 1000;
	public static final int MSG_TIME_OUT = 1001;
	public static final int MSG_EXCPTION = 1002;
	public static final int MSG_SUCCESS = 1003;
	public static final int MSG_FAILURE = 1004;
	
	public static final int MSG_SEND_CONTENT = 1005;
}
