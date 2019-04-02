package cn.mioto.bohan.activity;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LoadDataThreadUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;
import cn.mioto.bohan.view.timepicker.DateTimePicker;

import com.umeng.analytics.MobclickAgent;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import steed.framework.android.client.JsonClientHandler2;

/** 
 * 类说明：定时计量页面 
 * (计算一定时间内用电量)
 * 用socket收发（需要加上服务器数据和测试设备已经解决定时问题）
 */
public class MenuSetTimeSetActivity extends BaseCheckDataMenuActivity2 implements OnClickListener {
	//	/***************************************************/
	//	private TextView tvResultLast;
	//	private TextView tvResultThis;

	/**********布局控件*************/
	private Space spNoLast;
	private LinearLayout llLastCount;
	private TextView tvLastBegingDate;
	private TextView tvLastBeginTime;
	/*
	 * 上次计量计量结束日期
	 */
	private TextView tvLastEndDate;
	/*
	 * 上次计量计量结束时间
	 */
	private TextView tvLastEndTime;
	/*
	 * 开始计量按钮
	 */
	private TextView tvBeginToCount;
	/*
	 * 本次计量开始日期
	 */
	private TextView tvSetStartDate;
	/*
	 * 本次计量开始时间
	 */
	private TextView tvSetStartTime;
	/*
	 * 本次计量结束日期
	 */
	private TextView tvSetEndDate;
	/*
	 * 本次计量结束时间
	 */
	private TextView tvSetEndTime;
	/*
	 * 本次计量标题
	 */
	private TextView tvThisTimeTitle;
	/*
	 * 设置成功后，提示正在计量
	 */
	private TextView tvBeginToCountDismiss;
	private TextView tvTotalUseTime;// 总共用时textView
	private TextView tvTotalUsePower;
	private LinearLayout llThisTimeBeginTime;
	private LinearLayout llThisTimeEndime;
	private LinearLayout llBeginToCount;
	private TextView tvThisTimeRemainTime;
	/********断开连接，参数*****************************************/
	private Boolean canshowDisConnectMsg;
	private ProgressDialog disconnectDialog;
	/***********MODBUS协议参数************************************/
	private String endNum="0D";//结束符
	private String reqStartTime;
	private String reqEndTime;
	/******定义时间选择器date的时间格式*********************************************/
	private SimpleDateFormat mFormatter = new SimpleDateFormat("MMMM dd yyyy hh:mm aa");
	/******判断popwindow上的时间赋值给开始时间还是结束时间的布尔值赋值*******************************************/
	private Boolean startTimeSeleted=false;//有没有选择开始时间
	private Boolean endTimeSelected=false;//有没有选择结束时间
	private Date dateBegin;
	private Date dateEnd;
	/*
	 * 开始计量时间，
	 */
	private String startTime="";
	/*
	 * 结束计量时间
	 */
	private String endTime="";//防止空指针
	/*********广播接收者******************************************/
	private Calendar calendar = Calendar.getInstance();
	//指令的种类 查询上次计量0 开始计量1
	private int udpKind = 0;
	
	

	public static Socket socket;
	public static BufferedReader in;
	public static PrintWriter out;
	public ReceiveThread receiveThread;
	

	private Handler h = new Handler(){//另一个hanlder
		public void handleMessage(Message msg) {
			if(msg.what==Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK){
				//加载到数据后正在加载消失
			}else if (msg.what==Constant.MSG_WHAT_SHOW_DIALOG_OF_RESEND_TASK){
				//显示正在加载
				if(udpKind==0){
					progressGettingDataShow("上次计量数据查询中");
				}else if(udpKind==1){
					progressGettingDataShow("设置计量中");
				}
			}else if(msg.what==Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK_FAIL){
				//发送几次后，加载失败
				if(udpKind==0){
					progressGettingDataDismiss("上次计量数据查询失败");
				}else if(udpKind==1){
					progressGettingDataDismiss("设置计量区间失败");
				}
			}else if(msg.what==Constant.MSG_WHAT_SEND_MSG_BY_SERVICE_RESEND_TASK_FAIL){
				//通过服务器去查询信息
				//通过服务器查询设备实时数据
				checkByService((String)msg.obj);
			}else if(msg.what == Constant.MSG_SUCCESS){
				progressDismiss("");
				String message = (String) msg.obj;
				showContent(message);
			}else if(msg.what == Constant.MSG_EXCPTION){
				progressGettingDataDismiss("连接超时");
			} else if(msg.what == Constant.MSG_FAILURE){
				progressGettingDataDismiss("设备已离线");
			}
		}

	};

	/**********************************************************/
	/**
	 * 程序入口
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settime_set);
		bindViews();
		setListeners();
		ViewUtil.initToolbar(this, R.id.toolbar,R.id.toolbar_title, R.string.device_name, true, false, 0,0);
		ViewUtil.setToolbarTitle(getResources().getString(R.string.timing_setting));
		/*
		 * 查询设备资料，看有没有设置过定时计量，如果有，
		 * 返回结束时间，判断当前时间有没有到达结束时间.
		 * 如果没有到结束时间，显示进行中数据
		 * 如果到了结束时间。查询，显示
		 * 	checkIfHasCount();
		 * 
		 * 如果没有设置，显示没有设置的本次计量页面
		 */
		progressGettingDataShow("正在查询计量信息");
		// 初始化Socket
		socket = BApplication.instance.getSocket();
		checkNowData();
	}

	private void setListeners() {
		// TODO Auto-generated method stub
		tvBeginToCount.setOnClickListener(this);
		llThisTimeBeginTime.setOnClickListener(this);
		llThisTimeEndime.setOnClickListener(this);
	}

	private void bindViews() {
		llLastCount = (LinearLayout) findViewById(R.id.llLastCount);
		tvLastBegingDate = (TextView) findViewById(R.id.tvLastBegingDate);
		tvLastBeginTime = (TextView) findViewById(R.id.tvLastBeginTime);
		tvLastEndDate = (TextView) findViewById(R.id.tvLastEndDate);
		tvLastEndTime = (TextView) findViewById(R.id.tvLastEndTime);
		tvTotalUseTime = (TextView) findViewById(R.id.tvTotalUseTime);
		tvTotalUsePower = (TextView) findViewById(R.id.tvTotalUsePower);
		tvThisTimeTitle = (TextView) findViewById(R.id.tvThisTimeTitle);
		llThisTimeBeginTime = (LinearLayout) findViewById(R.id.llThisTimeBeginTime);
		tvSetStartDate = (TextView) findViewById(R.id.tvSetStartDate);
		tvSetStartTime = (TextView) findViewById(R.id.tvSetStartTime);
		llThisTimeEndime = (LinearLayout) findViewById(R.id.llThisTimeEndime);
		tvSetEndDate = (TextView) findViewById(R.id.tvSetEndDate);
		tvSetEndTime = (TextView) findViewById(R.id.tvSetEndTime);
		tvThisTimeRemainTime = (TextView) findViewById(R.id.tvThisTimeRemainTime);
		llBeginToCount = (LinearLayout) findViewById(R.id.llBeginToCount);
		tvBeginToCountDismiss = (TextView) findViewById(R.id.tvBeginToCountDismiss);
		tvBeginToCount = (TextView) findViewById(R.id.tvBeginToCount);
		spNoLast = (Space) findViewById(R.id.spNoLast);
	}



	/**********************************************************/
	/**
	 * 判断收到的信息，做相应显示
	 */
	protected void showContent(String content) {//位数已经加10改好，但是实际需要设备测
		if(checkUDPMessage(content)){
			if(isReqCodeEqual(content,"0007")){//查询定时计量的返回信息
				BApplication.instance.setResendTaskShowBreak(true);//停止重发机制
				progressGettingDataDismissNoToast();
				/***************************************************/
				llLastCount.setVisibility(View.VISIBLE);//定时计量结果显示
				spNoLast.setVisibility(View.GONE);//显示定时计量的结果，隐藏空白5倍的空白space控件
				String startTime = content.substring(24,36);//开始时间 YYMMDDHHMMSS 年月日时分秒
				String yy = startTime.substring(0,2);
				String mo = startTime.substring(2,4);
				String dd = startTime.substring(4, 6);
				String hh = startTime.substring(6, 8);
				String mm = startTime.substring(8, 10);
				String ss = startTime.substring(10);
				/********上次开始时间****显示***************************************/
				tvLastBegingDate.setText("20"+yy+"-"+mo+"-"+dd);
				tvLastBeginTime.setText(hh+":"+mm+":"+ss);
				String endTime = content.substring(36,48);//结束时间 YYMMDDHHMMSS 年月日时分秒
				String eyy = endTime.substring(0,2);
				String emo = endTime.substring(2,4);
				String edd = endTime.substring(4, 6);
				String ehh = endTime.substring(6, 8);
				String emm = endTime.substring(8, 10);
				String ess = endTime.substring(10);
				/***************************************************/
				//计算两个时间总用时
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				String begin = "20"+startTime;
				String end  = "20"+endTime;
				Date d1 = null;
				Date d2 = null;
				try {
					d1 = sdf.parse(begin);
					d2 = sdf.parse(end);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				LogUtilNIU.e("总共用时"+ModbusCalUtil.calDisOfDate(d1, d2, 0));
				if(ModbusCalUtil.calDisOfDate(d1, d2, 0).equals("small")){
					tvTotalUseTime.setText("00 : 00 : 00");
				}else{
					tvTotalUseTime.setText(ModbusCalUtil.calDisOfDate(d1, d2, 0));
				}
				/********上次结束时间****显示***************************************/
				tvLastEndDate.setText("20"+eyy+"-"+emo+"-"+edd);
				tvLastEndTime.setText(ehh+":"+emm+":"+ess);
				String durQuantity=content.substring(48,56);//期间用电量 000000.00 kWh
				durQuantity=ModbusCalUtil.addDotDel0(durQuantity, 6);//加点去零数据处理
				/**********显示期间用电量*****************************************/
				tvTotalUsePower.setText(durQuantity);
				/********上次计量显示成功，说明上次计量已经结束，定时计量按钮设置为可点击*******************************************/
				tvBeginToCount.setVisibility(View.VISIBLE);//上次计量结束，按钮设置为可点击
				tvBeginToCountDismiss.setVisibility(View.GONE);//
				llThisTimeBeginTime.setClickable(true);//时间选择器变为可选择
				llThisTimeEndime.setClickable(true);
				//				tvResultLast.setText(content);//
			}else if(isReqCodeEqual(content,"0006")){//定时计量设置成功
				LogUtilNIU.value("定时计量设置反馈--->"+content);
				//3A681607220002 00 0006 0001 00 B0 0D
				if((content.substring(24, 26).equals("00"))){//设置OK
					BApplication.instance.setResendTaskShowBreak(true);//停止重发机制
					progressGettingDataDismissNoToast();
					tvBeginToCount.setVisibility(View.GONE);//按钮不可见//时间选择器不可点击出现
					tvBeginToCountDismiss.setVisibility(View.VISIBLE);
					llThisTimeBeginTime.setClickable(false);
					llThisTimeEndime.setClickable(false);
					tvThisTimeTitle.setText("本次正在计量");
					//					tvResultThis.setText(content);// 测试
					// TODO 
					/* 把设置的开始时间和结束时间发送到服务器保存，下次进来是显示正在计量的状态
					 * 
					 * 改设备是否设置了定时计量
					 * 定时计量结束时间
					 * 把结束时间记录起来。（下次进入该页面，判断是否到这个时间，如果到了这个时间，就查询，显示）
					 */
					// TODO 开启线程，如果页面一直没有退出的话，到了定时计量结束时间，直接查询数据，显示结果
				}else if(content.substring(24, 26).equals("01")){//数值超出
					ToastUtils.shortToast(MenuSetTimeSetActivity.this, "数值超出");
				}else if(content.substring(24, 26).equals("02")){//FLASH出错
					ToastUtils.shortToast(MenuSetTimeSetActivity.this, "FLASH出错");
				}else{
					LogUtilNIU.e("设置结果码为其他数字---->"+content.substring(24, 26));
				}
			}
		}
		//		String statCode = content.substring(4, 6);//获得状态码
		//
		//		if(content.contains("3AA2000007001055555555555555555555555500000000B5")){
		//			LogUtilNIU.e("截住数据"+content);
		//			return;
		//		}
		//		if(statCode.equals("00")){//成功收到数据
		//			if(content.substring(6, 10).equals("0007")){//查询定时计量的返回信息
		//				/********处理查询定时计量收到的数据*******************************************/
		//				/**********显示显示定时计量的控件，隐藏空白5倍的空白space控件*****************************************/
		//				llLastCount.setVisibility(View.VISIBLE);
		//				spNoLast.setVisibility(View.GONE);
		//				String startTime = content.substring(14,26);//开始时间 YYMMDDHHMMSS 年月日时分秒
		//				String yy = startTime.substring(0,2);
		//				String mo = startTime.substring(2,4);
		//				String dd = startTime.substring(4, 6);
		//				String hh = startTime.substring(6, 8);
		//				String mm = startTime.substring(8, 10);
		//				String ss = startTime.substring(10);
		//				/********上次开始时间****显示***************************************/
		//				tvLastBegingDate.setText("20"+yy+"-"+mo+"-"+dd);
		//				tvLastBeginTime.setText(hh+":"+mm+":"+ss);
		//				String endTime = content.substring(26,38);//结束时间 YYMMDDHHMMSS 年月日时分秒
		//				String eyy = endTime.substring(0,2);
		//				String emo = endTime.substring(2,4);
		//				String edd = endTime.substring(4, 6);
		//				String ehh = endTime.substring(6, 8);
		//				String emm = endTime.substring(8, 10);
		//				String ess = endTime.substring(10);
		//				/***************************************************/
		//				//显示总共用时 对传入的两个电表时间数据计算，得时间差
		//				int disYY = Integer.valueOf(yy)-Integer.valueOf(eyy);
		//				int disMM = Integer.valueOf(mo)-Integer.valueOf(emo);
		//				int disDD = Integer.valueOf(dd)-Integer.valueOf(edd);
		//				int disHH = Integer.valueOf(hh)-Integer.valueOf(ehh);
		//				int disMIN = Integer.valueOf(mm)-Integer.valueOf(emm);
		//				int disSS = Integer.valueOf(ss)-Integer.valueOf(ess);
		//				String disResTime = disYY+"年"+disMM+"月"+disDD+"日"+disHH+"时"+disMIN+"分"+disSS+"秒";
		//				LogUtilNIU.e("总共用时"+disResTime);
		//				tvTotalUseTime.setText(disResTime);
		//				/********上次结束时间****显示***************************************/
		//				tvLastEndDate.setText("20"+eyy+"-"+emo+"-"+edd);
		//				tvLastEndTime.setText(ehh+":"+emm+":"+ess);
		//				String durQuantity=content.substring(38,46);//期间用电量 000000.00 kWh
		//				durQuantity=ModbusCalUtil.addDotDel0(durQuantity, 6);//加点去零数据处理
		//				/**********显示期间用电量*****************************************/
		//				tvTotalUsePower.setText(durQuantity);
		//				/********上次计量显示成功，说明上次计量已经结束，定时计量按钮设置为可点击*******************************************/
		//				tvBeginToCount.setVisibility(View.VISIBLE);//上次计量结束，按钮设置为可点击
		//				tvBeginToCountDismiss.setVisibility(View.GONE);//
		//				llThisTimeBeginTime.setClickable(true);//时间选择器变为可选择
		//				llThisTimeEndime.setClickable(true);
		//				tvResultLast.setText(content);// TODO 测试
		//
		//			}
		//			if(content.substring(6, 10).equals("0006")){//定时计量设置成功
		//				if((!content.substring(14, 16).equals("01")&&!content.substring(14,16).equals("02"))||content.substring(14, 16).equals("01")){//设置OK
		//					// 因为设备质量有BUG，暂时判断设置为不等于02，03 或者 等于01 就是设置成功
		//					ToastUtils.shortToast(MenuSetTimeSetActivity.this, "定时计量设置成功");
		//					tvBeginToCount.setVisibility(View.GONE);//按钮不可见//时间选择器不可点击出现
		//					tvBeginToCountDismiss.setVisibility(View.VISIBLE);
		//					llThisTimeBeginTime.setClickable(false);
		//					llThisTimeEndime.setClickable(false);
		//					tvThisTimeTitle.setText("本次正在计量");
		//					tvResultThis.setText(content);// TODO 测试
		//					// TODO 
		//					/* 把设置的开始时间和结束时间发送到服务器保存，下次进来是显示正在计量的状态
		//					 * 现在先用sp在本地保存。到时查本地信息，找不到才到服务器上去找
		//					 * 
		//					 * 改设备是否设置了定时计量
		//					 * 定时计量结束时间
		//					 * 把结束时间记录起来。（下次进入该页面，判断是否到这个时间，如果到了这个时间，就查询，显示）
		//					 */
		//
		//					// TODO 开启线程，如果页面一直没有退出的话，到了定时计量结束时间，直接查询数据，显示结果
		//				}else if(content.substring(14, 16).equals("01")){//数值超出
		//					ToastUtils.shortToast(MenuSetTimeSetActivity.this, "数值超出");
		//				}else if(content.substring(14, 16).equals("02")){//FLASH出错
		//					ToastUtils.shortToast(MenuSetTimeSetActivity.this, "FLASH出错");
		//				}
		//			}
		//
		//		}else if (statCode.equals("03")){
		//			LogUtilNIU.e("接收数据失败");//接收数据失败
		//		}
	}

	/**********************************************************/
	/**
	 * 查询定时计量的数据
	 */
	protected void checkNowData() {
		udpok=false;
		String verCode = ModbusCalUtil.verNumber(deviceId+"00070000");
		final String message="E7"+deviceId+"00070000"+verCode +"0D";//0001 查询用电参数 
		//new LoadDataThreadUtil(message, h,deviceBSSID,this).start();
		BApplication.instance.socketSend(message);
		try {
			BApplication.instance.socket.setSoTimeout(10 * 1000);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		receiveThread = new ReceiveThread();
		receiveThread.start();
	}


	@Override
	public void onClick(View v) {
		/********开始计量********设置定时计量时间***********************************/
		if(v==tvBeginToCount){//设置好开始和结束时间后，在这里发送定时计量指令
			if(!(startTime.equals("")||endTime.equals(""))){//开始时间不为空
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				//对比当前时间和系统时间大小
				Date now = new Date(System.currentTimeMillis());
				Date start = null;
				try {
					start = sdf.parse("20"+startTime);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				String disNowAndStart = ModbusCalUtil.calDisOfDate(now, start, 0);
				if(disNowAndStart.equals("small")){
					ToastUtils.shortToast(MenuSetTimeSetActivity.this, "输入的开始时间必须大于当前时间");
				}else{
					try {
						String dis=ModbusCalUtil.calDisOfDate(start, sdf.parse("20"+endTime), 0);//对比前后两个输入的时间的大小
						if(dis.equals("small")){
							ToastUtils.shortToast(MenuSetTimeSetActivity.this, "设置的结束时间必须大于开始时间");
						}else{
							tvThisTimeRemainTime.setText(dis);
							setCountTime();//设置定时计量
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}else{
				ToastUtils.shortToast(MenuSetTimeSetActivity.this, "请选择开始时间和结束时间");
			}
		}else if(v==llThisTimeBeginTime){//这次开始时间
			DateTimePicker picker = new DateTimePicker(this, DateTimePicker.HOUR_OF_DAY);
			picker.setRange(2000, 2030);
			int minute = calendar.get(Calendar.MINUTE);
			picker.setSelectedItem(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH),
					calendar.get(Calendar.HOUR_OF_DAY), minute+1);//对于单数初始化失效
			picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
				@Override
				public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
					/***************************************************/
					tvSetStartDate.setText(year+"-"+month+"-"+day);//显示数据
					tvSetStartTime.setText(hour+":"+minute);
					startTime = year.substring(2)+month+day+hour+minute+"30";
					LogUtilNIU.value("startTime"+startTime);
				}
			});
			picker.show();
			startTimeSeleted=true;
		}else if (v==llThisTimeEndime){//这次结束时间
			/********弹出时间选择框****选择结束时间**************************************/
			DateTimePicker picker2 = new DateTimePicker(this, DateTimePicker.HOUR_OF_DAY);
			picker2.setRange(2000, 2030);
			//选择默认时间
			picker2.setSelectedItem(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH),
					calendar.get(Calendar.HOUR_OF_DAY),(calendar.get(Calendar.MINUTE))+1);
			picker2.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
				@Override
				public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
					tvSetEndDate.setText(year+"-"+month+"-"+day);//显示数据
					tvSetEndTime.setText(hour+":"+minute);
					endTime = year.substring(2)+month+day+hour+minute+"30";
					LogUtilNIU.value("endTime"+endTime);
				}
			});
			picker2.show();
			endTimeSelected=true;
		}
	}

	private void setCountTime() {//设置定时计量
		udpKind = 1;
		String verCode = ModbusCalUtil.verNumber(deviceId+"0006000C"+startTime+endTime);
		final String message="E7"+deviceId+"0006000C"+startTime+endTime+verCode +"0D";//0001 查询用电参数 
		//new LoadDataThreadUtil(message, h,deviceBSSID,this).start();
		BApplication.instance.socketSend(message);
		receiveThread = new ReceiveThread();
		receiveThread.start();
	}

	/**********************************************************/
	/**
	 * 时间日期选择器
	 * 时间选择器控件，日期被选择后监听回调
	 */
	//	private SlideDateTimeListener listener = new SlideDateTimeListener() {
	//
	//		@Override
	//		public void onDateTimeSet(Date date)
	//		{
	//
	//			if(startTimeSeleted){//设置开始时间
	//				LogUtilNIU.e("选择器的date是------>"+date);
	//				//选择器的date是------>Wed Jun 15 21:25:37 GMT+08:00 2016
	//				dateBegin=date;
	//				/***************************************************/
	//				//判断选择的开始时间是否符合规则
	//				//要求所选时间大于系统当前时间
	//				long currentTimeMill=System.currentTimeMillis();//当前系统时间的毫秒知
	//				Calendar cal = Calendar.getInstance();
	//				cal.setTime(dateBegin);
	//				long dateBeginMill=cal.getTimeInMillis();//开始时间的毫秒值
	//				if(!(dateBeginMill>currentTimeMill)){
	//					//提示
	//					ToastUtils.longToast(MenuSetTimeSetActivity.this, "亲，所选择的时间必须大于当前时间");
	//					return;
	//				}
	//				/***************************************************/
	//				startTime=ModbusCalUtil.millFormat(date);//处理数据
	//				LogUtilNIU.e("startTime---->"+startTime);
	//				String yy = startTime.substring(0,2);
	//				String mo = startTime.substring(2,4);
	//				String dd = startTime.substring(4, 6);
	//				String ww = startTime.substring(6, 8);
	//				String hh = startTime.substring(8, 10);
	//				String mm = startTime.substring(10, 12);
	//				String ss = startTime.substring(12);
	//				startTime=yy+mo+dd+hh+mm+ss;//把星期这个不需要的数据去除
	//				/***************************************************/
	//				tvSetStartDate.setText("20"+yy+"-"+mo+"-"+dd);//显示数据
	//				tvSetStartTime.setText(hh+":"+mm);
	//				startTimeSeleted=false;
	//			}
	//			if(endTimeSelected){//设置结束时间
	//				LogUtilNIU.e("选择器的date是------>"+date);
	//				//选择器的date是------>Wed Jun 15 21:25:37 GMT+08:00 2016
	//				dateEnd=date;
	//
	//				/***************************************************/
	//				endTime=ModbusCalUtil.millFormat(date);
	//				LogUtilNIU.e("startTime---->"+endTime);
	//				String eyy = endTime.substring(0,2);
	//				String emo = endTime.substring(2,4);
	//				String edd = endTime.substring(4, 6);
	//				String eww = endTime.substring(6, 8);
	//				String ehh = endTime.substring(8, 10);
	//				String emm = endTime.substring(10, 12);
	//				String ess = endTime.substring(12);
	//				endTime=eyy+emo+edd+ehh+emm+ess;//把星期这个不需要的数据去除
	//				/*********date显示在本次计量控件上******************************************/
	//				tvSetEndDate.setText("20"+eyy+"-"+emo+"-"+edd);
	//				tvSetEndTime.setText(ehh+":"+emm);
	//				endTimeSelected=false;
	//			}
	//			if(dateBegin!=null&&dateEnd!=null){//如果开始时间和结束时间都选择好了
	//				/***************************************************/
	//				//判断选择的结束时间是否符合规则
	//				//要求结束时间必须大于所选的开始时间
	//				Calendar cal = Calendar.getInstance();
	//				cal.setTime(dateBegin);
	//				long dateBeginMill=cal.getTimeInMillis();//开始时间的毫秒值
	//				cal.setTime(dateEnd);
	//				long dateEndMill = cal.getTimeInMillis();//结束时间的毫秒值
	//				if(!(dateEndMill>dateBeginMill)){//当所选时间不大于系统当前时间
	//					//提示
	//					ToastUtils.longToast(MenuSetTimeSetActivity.this, "亲，结束时间必须大于开始时间");
	//					tvThisTimeRemainTime.setText("");
	//					return;
	//				}
	//
	//				String distance=ModbusCalUtil.calDisOfDate(dateBegin,dateEnd);//计算两个date的时间差
	//				LogUtilNIU.e("时间差是--->"+distance);
	//				tvThisTimeRemainTime.setText(distance);
	//			}
	//		}
	//
	//
	//		@Override
	//		public void onDateTimeCancel()
	//		{
	//			if(endTimeSelected){
	//				endTimeSelected=false;
	//			}
	//			if(startTimeSeleted){
	//				startTimeSeleted=false;
	//			}
	//			Toast.makeText(MenuSetTimeSetActivity.this,
	//					"Canceled", Toast.LENGTH_SHORT).show();
	//		}
	//	};


	/**********************************************************/
	/**
	 * 处理date格式，变成插座能识别的数据
	 */
	public String formatDate(Date date){
		int year=date.getYear();
		String syear = year+"";//转为字符
		syear = syear.substring(1);//取后两位
		int month=date.getMonth();
		month= month+1;//实际月份是month+1
		String smon = month+"";//转化为字符
		smon=ModbusCalUtil.add0fillLength(smon, 2);//向前补0
		int week = date.getDay();//得到手机星期
		week = week-1;//系统日期4，等于真实日期3。转化为插座日期
		String sweek ="0"+week+"";//转化2位的星期字符
		int dat = date.getDate();
		String sdate = dat+"";//转化为字符显示
		sdate=ModbusCalUtil.add0fillLength(sdate, 2);//日期向前补0，以00形式显示
		int hh = date.getHours();
		String shh = hh+"";
		shh=ModbusCalUtil.add0fillLength(shh, 2);
		int min = date.getMinutes();
		String smin = min+"";
		smin=ModbusCalUtil.add0fillLength(smin, 2);
		String result = syear+smon+sdate+sweek+shh+smin+59;
		return result;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("定时计量"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this);          //统计时长
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("定时计量"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
		MobclickAgent.onPause(this);
	}

	@Override
	protected String onServiceUDPBack(String content) {
		showContent(content);
		return null;
	}

	
	/**
	 * 接收数据线程
	 */
	private class ReceiveThread extends Thread {
		@Override
		public void run() {
			Message msg = new Message();
			msg.what = Constant.MSG_FAILURE;
			msg.obj = getString(R.string.cannot_connection_server);
			Log.d("Receive", "ReceiveThread() start");
			String result = "";
				try {
					result = BApplication.instance.readString();
					Log.d("Receive", "socket receive:" + result);
					if (result != null) {
						JSONObject obj = new JSONObject(result);
						int code = obj.getInt("statusCode");
						if (code == 0) {
							String content = obj.getString("content");
							msg.obj = content;
							msg.what = Constant.MSG_SUCCESS;
						} else{
							msg.what = Constant.MSG_FAILURE;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					msg.obj = e;
					msg.what = Constant.MSG_EXCPTION;
				}
			h.sendMessage(msg);
		}
	}
}
