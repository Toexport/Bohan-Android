package cn.mioto.bohan.activity;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.ExceptionUtil;
import cn.mioto.bohan.utils.LoadDataThreadUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;
import cn.mioto.bohan.view.timepicker.TimePickerForCountDown;

import com.alipay.a.a.h;
import com.alipay.android.phone.mrpc.core.r;
import com.umeng.analytics.MobclickAgent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import steed.framework.android.client.JsonClientHandler2;

/**
 * 类说明：倒计时页面 作者： jiemai liangminhua 创建时间：2016年5月27日 下午2:04:19
 */
public class MenuCountDownActivity extends BaseCheckDataMenuActivity2 implements
		OnClickListener {
	/********** 布局文件控件 *************/
	private LinearLayout llNotSet;
	// private LinearLayout llBar;
	private LinearLayout llchooseTimeOut;
	private LinearLayout llStartGray;// 启动按钮灰色包裹区域
	private LinearLayout llStartBlue;// 蓝色
	private TextView tvOn;
	private TextView tvOff;
	private TextView tvStatus;
	private TextView tvCustom;
	private TextView tvWarning;
	private LinearLayout llChooseTime1;
	private TextView tv5m;
	private ImageView ivChoose1;
	private ImageView ivChoose3;
	private LinearLayout llChooseTime2;
	private TextView tv10m;
	private TextView tvLeftTime;
	private ImageView ivChoose2;
	private LinearLayout llCustomTime;
	private Space llBottomSpace;
	private Space spaceOpenClose;
	private TextView tvStart;
	private int UPDATE_COUNTTIME = 100;
	private cn.mioto.bohan.view.CircleProgressBar circleProgressBar;
	private LinearLayout relativeLayout1;
	private LinearLayout llOpenColse;
	private int duractionSeconds = 0;// 总秒数
	private int leftSeconds = 0;// 剩余秒数
	private float progressAngle;// 以360为基准的角度
	private Boolean setCountTimeSus = false;
	private String timeInfo = "";// 倒计时终点时间

	/******* 倒计时间设置 ********************************************/
	private String countDownTime = "0005";// 用户设置的倒计时时间
	/******** 倒计时显示 *******************************************/
	private String distanceTime;// 距离开或关还有多少时间
	private String targetStatus;// 目标状态是开还是关
	/*********** MODBUS协议参数 ************************************/
	private String verNum;// 效验码
	private String endNum = "0D";// 结束符
	/**********************************************************/
	private Boolean tvOnSelected = false;// 判断“开”按钮有没有被选择的变量
	private Boolean countDownTimeRun = true;
	// 指令的种类 0代表查询倒计时 1代表设定倒计时开 2代表设定倒计时关
	private int udpKind = 0;
	// 设置了倒计时开 settingStatus = 0，倒计时关 settingStatus = 1
	private int settingStatus = -1;

	public static Socket socket;
	public static BufferedReader in;
	public static PrintWriter out;
	public ReceiveThread receiveThread;

	/**
	 * 程序入口
	 */
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == UPDATE_COUNTTIME) {
				if (!((String) msg.obj).trim().equals("00 : 00 : 00")) {// 未结束时
					// 后来在测试过程中，由于做到秒精确显示很难。所以厂家建议修改为只显示时分，不显示秒倒计时。
					// 如果要显示秒倒计时，直接把text设置为(String)msg.obj)

					// 不显示具体时间的方式
					// int
					// lastSymble=((String)msg.obj).lastIndexOf(":");//最后的“:”的位置
					// if(((String)msg.obj).substring(0,lastSymble).trim().equals("00 : 00")){
					// tvLeftTime.setText("不到1分钟");
					// }else{
					// LogUtilNIU.value("(String)msg.obj--->"+(String)msg.obj);
					// tvLeftTime.setText(((String)msg.obj).substring(0,lastSymble));
					// }
					tvLeftTime.setText(((String) msg.obj));
					LogUtilNIU.value("倒计时" + ((String) msg.obj).trim());
					// 环形进度条改变状态
					if (progressAngle == 0 && duractionSeconds == 0) {

					} else {
						progressAngle = (float) (progressAngle - ((double) 1
								/ duractionSeconds * 360));
						// LogUtilNIU.value("progressAngle--"+progressAngle);
						circleProgressBar.setProgress(progressAngle);// 剩余秒数减去1
					}
				} else if (((String) msg.obj).trim().equals("00 : 00 : 00")) {
					// 倒计时所有到0点的时候，显示为开始的待设置状态
					// tvLeftTime.setText("00 : 00");
					tvLeftTime.setText("00 : 00 : 00");
					// tvLeftTime.setTextSize(R.dimen.large_font_size);
					progressAngle = 0;
					circleProgressBar.setProgress(progressAngle);
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							tvStatus.setText(getResources().getString(
									R.string.device_on_off));
							spaceOpenClose.setVisibility(View.GONE);// 这个空间一出现，开和关按钮就会消失，倒计时结束，开关按钮要出现
							llNotSet.setVisibility(View.VISIBLE);// 出现没有倒计时任务的提示
							tvWarning.setText(R.string.coutDownWarningEnd);// 倒计时已经结束
																			// 倒计时已结束。
							llBottomSpace.setVisibility(View.GONE);// 空白处消失（时间设置区域出现）
						}
					}, 1000);
				}
			} else if (msg.what == Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK) {
				// 加载到数据后正在加载消失
			} else if (msg.what == Constant.MSG_WHAT_SHOW_DIALOG_OF_RESEND_TASK) {
				// 显示正在加载
				if (udpKind == 0) {
					progressGettingDataShow("更新倒计时信息中");
				} else if (udpKind == 1) {
					progressGettingDataShow("正在设置倒计时开");
				} else if (udpKind == 2) {
					progressGettingDataShow("正在设置倒计时关");
				}
			} else if (msg.what == Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK_FAIL) {
				// 发送几次后，加载失败
				if (udpKind == 0) {
					progressGettingDataDismiss("倒计时信息更新失败");
				} else if (udpKind == 1) {
					progressGettingDataDismiss("倒计时开设置失败");
				} else if (udpKind == 2) {
					progressGettingDataDismiss("倒计时关设置失败");
				} else if (udpKind == 3) {
					progressGettingDataDismiss("信息更新失败");
				}
			} else if (msg.what == Constant.MSG_WHAT_SEND_MSG_BY_SERVICE_RESEND_TASK_FAIL) {
				// 通过服务器去查询信息
				// 通过服务器查询设备实时数据
				checkByService((String) msg.obj);
			} else if (msg.what == Constant.MSG_EXCPTION) {
				progressGettingDataDismiss("连接超时");
			} else if (msg.what == Constant.MSG_SUCCESS) {
				progressDismiss("");
				String message = (String) msg.obj;
				showContent(message);
			} else if (msg.what == Constant.MSG_FAILURE) {
				progressGettingDataDismiss("设备已离线");
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/******** 在父类初始化设备id,ip,设备名称 *******************************************/
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_count_down);
		bindViews();
		setListeners();
		/******* 初始化toolbar ********************************************/
		ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title,
				R.string.device_name, true, false, 0, 0);
		ViewUtil.setToolbarTitle(getResources().getString(R.string.count_down));
		/***************************************************/
		checkLastCountDown();// 查询上次设置的倒计时信息
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date d = new Date(System.currentTimeMillis());
		sdf.format(d);
		LogUtilNIU.value("当前时间为---" + sdf.format(d));
		// 初始化Socket
		socket = BApplication.instance.getSocket();
	}

	private void bindViews() {
		llNotSet = (LinearLayout) findViewById(R.id.llNotSet);
		circleProgressBar = (cn.mioto.bohan.view.CircleProgressBar) findViewById(R.id.circleProgressBar);
		relativeLayout1 = (LinearLayout) findViewById(R.id.relativeLayout1);
		tvLeftTime = (TextView) findViewById(R.id.tvLeftTime);
		tvStatus = (TextView) findViewById(R.id.tvStatus);
		tvWarning = (TextView) findViewById(R.id.tvWarning);
		spaceOpenClose = (Space) findViewById(R.id.spaceOpenClose);
		llOpenColse = (LinearLayout) findViewById(R.id.llOpenColse);
		tvOn = (TextView) findViewById(R.id.tvOn);
		tvOff = (TextView) findViewById(R.id.tvOff);
		llBottomSpace = (Space) findViewById(R.id.llBottomSpace);
		llchooseTimeOut = (LinearLayout) findViewById(R.id.llchooseTimeOut);
		llChooseTime1 = (LinearLayout) findViewById(R.id.llChooseTime1);
		tv5m = (TextView) findViewById(R.id.tv5m);
		ivChoose1 = (ImageView) findViewById(R.id.ivChoose1);
		llChooseTime2 = (LinearLayout) findViewById(R.id.llChooseTime2);
		tv10m = (TextView) findViewById(R.id.tv10m);
		ivChoose2 = (ImageView) findViewById(R.id.ivChoose2);
		llCustomTime = (LinearLayout) findViewById(R.id.llCustomTime);
		tvCustom = (TextView) findViewById(R.id.tvCustom);
		ivChoose3 = (ImageView) findViewById(R.id.ivChoose3);
		llStartGray = (LinearLayout) findViewById(R.id.llStartGray);
		llStartBlue = (LinearLayout) findViewById(R.id.llStartBlue);
		tvStart = (TextView) findViewById(R.id.tvStart);
	}

	/**
	 * @Title: checkLastCountDown
	 * @Description:查询倒计时信息
	 * @return void
	 * @throws
	 */
	private void checkLastCountDown() {
		udpKind = 0;
		String verCode = ModbusCalUtil.verNumber(deviceId + "00120000");
		String msg = "E7" + deviceId + "00120000" + verCode + "0D";// 0001
																	// 查询用电参数
		BApplication.instance.socketSend(msg);
		try {
			BApplication.socket.setSoTimeout(10 * 1000);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		receiveThread = new ReceiveThread();
		receiveThread.start();
	}

	private void setListeners() {
		llChooseTime1.setOnClickListener(this);
		llChooseTime2.setOnClickListener(this);
		llCustomTime.setOnClickListener(this);
		tvOn.setOnClickListener(this);
		tvOff.setOnClickListener(this);
		tvStart.setOnClickListener(this);
	}

	/**********************************************************/
	/**
	 * 点击事件
	 */
	@Override
	public void onClick(View v) {
		if (v == llChooseTime1) {
			/**** 5分钟被选中 ***********************************************/
			ivChoose3.setVisibility(View.GONE);
			ivChoose2.setVisibility(View.GONE);
			ivChoose1.setVisibility(View.VISIBLE);
			// tvLeftTime.setText("00 : 05");
			tvLeftTime.setText("00 : 05 : 00");
			tv10m.setTextColor(getResources().getColor(
					R.color.textcolor_darkGray));
			tvCustom.setTextColor(getResources().getColor(
					R.color.textcolor_darkGray));
			tv5m.setTextColor(getResources().getColor(R.color.red));
			countDownTime = "0005";
		} else if (v == llChooseTime2) {
			/**** 10分钟被选中 ***********************************************/
			ivChoose2.setVisibility(View.VISIBLE);
			ivChoose1.setVisibility(View.GONE);
			ivChoose3.setVisibility(View.GONE);
			// tvLeftTime.setText("00 : 10");
			tvLeftTime.setText("00 : 10 : 00");
			tv5m.setTextColor(getResources().getColor(
					R.color.textcolor_darkGray));
			tvCustom.setTextColor(getResources().getColor(
					R.color.textcolor_darkGray));
			tv10m.setTextColor(getResources().getColor(R.color.red));
			countDownTime = "0010";
		} else if (v == llCustomTime) {
			ivChoose3.setVisibility(View.VISIBLE);
			ivChoose1.setVisibility(View.GONE);
			ivChoose2.setVisibility(View.GONE);
			tv5m.setTextColor(getResources().getColor(
					R.color.textcolor_darkGray));
			tv10m.setTextColor(getResources().getColor(
					R.color.textcolor_darkGray));
			tvCustom.setTextColor(getResources().getColor(R.color.red));
			TimePickerForCountDown picker = new TimePickerForCountDown(this,
					TimePickerForCountDown.HOUR_CUSTOM);
			String a = tvLeftTime.getText().toString().split(" : ")[0];// 原来textView所显示的小时
			String b = tvLeftTime.getText().toString().split(" : ")[1];// 原来的分钟
			if (a.startsWith("0")) {// 如果是0开头，则为后面的数字，也就是去除开头的0
				a = a.substring(1);
			}
			if (b.startsWith("0")) {
				b = b.substring(1);
			}
			LogUtilNIU.value("原来的时间设置，a小时是" + a + "     b分钟是" + b);
			picker.setSelectedItem(Integer.valueOf(a), Integer.valueOf(b));// 如果不选，设置为原来的数
			picker.setLabel("小时", "分钟");
			picker.setTopLineVisible(false);
			picker.setOnTimePickListener(new TimePickerForCountDown.OnTimePickListener() {

				@Override
				public void onTimePicked(String hour, String minute) {
					LogUtilNIU.value("选择了的hour是" + hour + "，选择了的分钟是" + minute);
					// tvLeftTime.setText(ModbusCalUtil.add0fillLength(hour,
					// 2)+" : "+ModbusCalUtil.add0fillLength(minute, 2));
					tvLeftTime.setText(ModbusCalUtil.add0fillLength(hour, 2)
							+ " : " + ModbusCalUtil.add0fillLength(minute, 2)
							+ " : 00");
					countDownTime = ModbusCalUtil.add0fillLength(hour, 2)
							+ ModbusCalUtil.add0fillLength(minute, 2);
				}
			});
			picker.show();
		} else if (v == tvOn) {
			/****** 开按钮 *********************************************/
			llNotSet.setVisibility(View.GONE);
			llStartGray.setVisibility(View.GONE);// 灰色按钮消失
			llStartBlue.setVisibility(View.VISIBLE);// 蓝色按钮出现
			tvStart.setVisibility(View.VISIBLE);// 点击开以后，开始按钮出现
			// 选择按钮后，按钮改变字体颜色为红色
			tvStatus.setText("设备打开");
			tvOn.setTextColor(getResources().getColor(R.color.red));
			tvOff.setTextColor(getResources().getColor(R.color.blueDark));
			tvOnSelected = true;
		} else if (v == tvOff) {
			llNotSet.setVisibility(View.GONE);
			/******* 关按钮 ********************************************/
			tvOnSelected = false;
			tvStatus.setText("设备关闭");
			llStartGray.setVisibility(View.GONE);// 灰色按钮消失
			llStartBlue.setVisibility(View.VISIBLE);// 蓝色按钮出现
			tvStart.setVisibility(View.VISIBLE);// 点击关以后，开始按钮出现
			// 选择按钮后，按钮改变字体颜色为红色
			tvOff.setTextColor(getResources().getColor(R.color.red));
			tvOn.setTextColor(getResources().getColor(R.color.blueDark));
		} else if (v == tvStart) {
			/****** 启动，出现倒计时Bar *********************************************/
			// 这里需要加个设置倒计时的缓冲状态，如果设置成功，区域才消失
			// llBar.setVisibility(View.VISIBLE);
			// llStartGray.setVisibility(View.VISIBLE);//灰色按钮出现
			// 发送命令.
			// 先做判断是开指令还是关指令
			// 把设置的倒计时总时长记录下来
			// 判断所选择的分钟数是否符合条件，不得小于1分钟
			if (countDownTime.equals("0000")) {
				ToastUtils.shortToast(MenuCountDownActivity.this,
						"所设置的倒计时必须大于1分钟");
			} else {
				String duraction = tvLeftTime.getText().toString()
						.replace(" : ", "");
				BApplication.instance.getEditor().putString(
						deviceId + Constant.DEVICE_COUNT_DOWN_DURACTION,
						duraction);// 用时分秒形式显示
				LogUtilNIU
						.value("sp记录的duraction为----->"
								+ BApplication.instance
										.getSp()
										.getString(
												deviceId
														+ Constant.DEVICE_COUNT_DOWN_DURACTION,
												"nosp"));
				setCountTimeSus = false;
				if (tvOnSelected) {
					setOn();// 设置倒计时开
				} else {
					setOff();// 设置倒计时关
				}
			}
		}
	}

	/**********************************************************/
	/**
	 * 设置倒计时开
	 */
	private void setOn() {
		udpKind = 1;
		LogUtilNIU.e("countDownTime" + countDownTime);
		LogUtilNIU.e(deviceId + "000B0002" + countDownTime);
		verNum = ModbusCalUtil.verNumber(deviceId + "000B0002" + countDownTime);
		LogUtilNIU.e("verNum--->" + verNum);
		final String orderOn = "E7" + deviceId + "000B0002" + countDownTime
				+ verNum + endNum;
		// new LoadDataThreadUtil(orderOn, handler,deviceBSSID,this).start();
		BApplication.instance.socketSend(orderOn);
		receiveThread = new ReceiveThread();
		receiveThread.start();
	}

	/**********************************************************/
	/**
	 * 设置倒计时关
	 */
	private void setOff() {
		udpKind = 2;
		LogUtilNIU.value("设置倒计时关" + deviceId + "000A0002" + countDownTime);
		verNum = ModbusCalUtil.verNumber(deviceId + "000A0002" + countDownTime);
		LogUtilNIU.e("verNum--->" + verNum);
		final String orderOff = "E7" + deviceId + "000A0002" + countDownTime
				+ verNum + endNum;
		// new LoadDataThreadUtil(orderOff, handler,deviceBSSID,this).start();
		BApplication.instance.socketSend(orderOff);
		receiveThread = new ReceiveThread();
		receiveThread.start();
	}

	/**********************************************************/
	/**
	 * 设置倒计时开或倒计时关后，收到返回信息。 判断返回信息 判断定时设置有没有成功
	 * 
	 */
	private void showContent(String content) {
		if (checkUDPMessage(content)) {
			if (isReqCodeEqual(content, "000B")) {// 倒计时开设置成功
				// 把总时间保存在sp
				BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
				progressGettingDataDismiss("设置倒计时开成功");
				Editor e = BApplication.instance.getSp().edit();
				e.putString(deviceId + Constant.DEVICE_COUNT_DOWN_DURACTION,
						countDownTime + "00");// 手动加上秒
				e.commit();
				if (content.substring(24, 26).equals("00")) {// 设置OK
					setCountTimeSus = true;
					llBottomSpace.setVisibility(View.VISIBLE);// 下面设置项消失
					// 记录总用时
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							checkLastCountDown();// 查询并显示倒计时
						}
					}, Constant.RESENDTIME);// 发UDP的延迟时间
					/*
					 * 发送数据到服务器保存 开 时段
					 */
				} else if (content.substring(24, 26).equals("01")) {// 数值超出
					ToastUtils.shortToast(MenuCountDownActivity.this, "数值超出");
				} else if (content.substring(24, 26).equals("02")) {// FLASH出错
					ToastUtils
							.shortToast(MenuCountDownActivity.this, "FLASH出错");
				}
			} else if (isReqCodeEqual(content, "000A")) {// 倒计时关设置成功
				// 把总时间保存在sp
				BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
				progressGettingDataDismiss("设置倒计时关成功");
				Editor e = BApplication.instance.getSp().edit();
				e.putString(deviceId + Constant.DEVICE_COUNT_DOWN_DURACTION,
						countDownTime + "00");// 手动加上秒
				e.commit();
				if (content.substring(24, 26).equals("00")) {// 设置OK
					setCountTimeSus = true;
					llBottomSpace.setVisibility(View.VISIBLE);// 下面设置项消失
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							checkLastCountDown();// 查询并显示倒计时
						}
					}, Constant.RESENDTIME);
					/*
					 * 发送数据到服务器保存 关 时段
					 */
				} else if (content.substring(24, 26).equals("01")) {// 数值超出
					ToastUtils.shortToast(MenuCountDownActivity.this, "数值超出");
				} else if (content.substring(24, 26).equals("02")) {// FLASH出错
					ToastUtils
							.shortToast(MenuCountDownActivity.this, "FLASH出错");
				}
			} else if (isReqCodeEqual(content, "0012")) {// 查询倒计时终点时间
				/*
				 * //3A 681608050094 00 0012 0007 00 00 00 00 00 00 00 380D 显示逻辑
				 * 查询到生词倒计时信息，显示在界面上
				 */
				if (content.substring(24, 26).equals("00")) {// 已设置倒计时关
					LogUtilNIU.value("*****已经设置倒计时关");
					settingStatus = 1;
					// 停止查询终点时间的线程
					BApplication.instance.setResendTaskShowBreak(true);
					if (content.substring(26, 38).equals("000000000000")) {// 没有设置任何倒计时的情况
						// 如果完全没有设置过任何倒计时
						// 显示没有设置倒计时的布局
						LogUtilNIU.value("倒计时关00中，没有设置任何倒计时");
						progressGettingDataDismissNoToast();
						llBottomSpace.setVisibility(View.GONE);// 底下空白消失
						llNotSet.setVisibility(View.VISIBLE);
					} else {
						// 如有设置倒计时，空白页面出现来，隐藏时间设置栏
						llBottomSpace.setVisibility(View.VISIBLE);
						timeInfo = content.substring(26, 38);
						timeInfo = "20" + timeInfo;// 补全位数
						LogUtilNIU.value("返回时间为timeInfo--" + timeInfo);
						// 解析时间显示
						// YYMMDDWWhhmmss
						handler.postDelayed(new Runnable() {

							@Override
							public void run() {
								checkNowDeviceTime();
							}
						}, Constant.RESENDTIME);
					}
				} else if (content.substring(24, 26).equals("01")) {// 已设置倒计时开
					LogUtilNIU.value("*****已经设置倒计时开");
					settingStatus = 0;
					// 停止查询终点时间的线程
					BApplication.instance.setResendTaskShowBreak(true);
					// 如有设置倒计时，隐藏时间设置栏
					llBottomSpace.setVisibility(View.GONE);// 底下空白消失
					llBottomSpace.setVisibility(View.VISIBLE);
					timeInfo = content.substring(26, 38);// 终点时间
					timeInfo = "20" + timeInfo;// 补全位数
					LogUtilNIU.value("********返回时间为timeInfo--" + timeInfo);
					// 解析时间显示
					// YYMMDDWWhhmmss
					// 把查得的重点时间，和现在的时间做减法，计算出时间差
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							checkNowDeviceTime();
						}
					}, Constant.RESENDTIME);
				} else {
					BApplication.instance.setResendTaskShowBreak(true);
					progressGettingDataDismissNoToast();
					// 如果等于其他数据的话，也是提示用户没有设置倒计时 设备厂房说有可能返回其他状态码，但是不影响 TODO
					ToastUtils.testToast(MenuCountDownActivity.this,
							"（测试提示）状态码返回非00，01暂时显示此界面，表示没有设置任何倒计时");
					llNotSet.setVisibility(View.VISIBLE);
					llBottomSpace.setVisibility(View.GONE);
				}
				// 如果设置过倒计时关
			} else if (isReqCodeEqual(content, "0001")) {// 查询实时数据返回信息
				LogUtilNIU.value("****收到时间信息");
				// 停止更新状态的重发机制
				// 3A681608050094000001001F22924000000000000010000000003300000031000000041516090905093902310D00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
				BApplication.instance.setResendTaskShowBreak(true);
				progressGettingDataDismissNoToast();
				LogUtilNIU.value("0001返回了信息");
				// 年
				String year = content.substring(72, 74);
				// 月
				String month = content.substring(74, 76);
				// 日
				String day = content.substring(76, 78);
				// 时
				String hour = content.substring(80, 82);
				// 分
				String minute = content.substring(82, 84);
				// 秒
				String second = content.substring(84, 86);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				String mcuTime = "20" + year + month + day + hour + minute
						+ second;
				Date d1 = null;
				LogUtilNIU.value("mcuTime-->" + mcuTime);
				try {
					d1 = sdf.parse(mcuTime);
				} catch (ParseException e) {
					e.printStackTrace();
				}// MCU查得的终点时间
				Date d2 = null;
				// 判断现在的设置状态是设置了倒计时开还是倒计时关
				if (settingStatus == 0) {
					showCountingOpen(timeInfo, d1, d2);
				} else if (settingStatus == 1) {
					showCountingClose(timeInfo, d1, d2);
				}
			}
		}
	}

	private void showCountingOpen(String timeInfo, Date d1, Date d2) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			d2 = sdf.parse(timeInfo);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// 计算两个时间差
		String dis = ModbusCalUtil.calDisOfDate(d1, d2, 0);
		LogUtilNIU.value("时差结果为---》" + dis);
		if (dis.equals("small")) {
			LogUtilNIU.value("时差有问题" + "d1为" + d1 + ",d2为" + d2);
			llNotSet.setVisibility(View.VISIBLE);
			llBottomSpace.setVisibility(View.GONE);
		} else {
			LogUtilNIU.value("时间差计算结果为" + dis);
			tvLeftTime.setText(dis);
			LogUtilNIU.value("剩余时间" + dis);
			String leftTime = dis.replace(" : ", "");// 把 05 : 06 : 00 化为
														// 050600的形式
			tvStatus.setText("设备打开");
			// 隐藏下面两个开和关按钮
			spaceOpenClose.setVisibility(View.VISIBLE);
			/***************************************************/
			String duraction = BApplication.instance.getSp().getString(
					deviceId + Constant.DEVICE_COUNT_DOWN_DURACTION, "none");
			if (duraction.equals("none")) {

			} else {
				// 计算剩余时间一共时候多少秒
				// 计算duraction一共是多少秒
				leftSeconds = ModbusCalUtil.countTotalSeconds(leftTime);
				duractionSeconds = ModbusCalUtil.countTotalSeconds(duraction);// duraction上次所记录的设置的总时长
				LogUtilNIU.value("leftSeconds--" + leftSeconds
						+ "duractionSeconds--" + duractionSeconds + "除法结果--"
						+ leftSeconds / duractionSeconds);
				Double percent = (double) leftSeconds
						/ (double) duractionSeconds;
				LogUtilNIU.value("percent----" + percent);
				progressAngle = (int) (percent * 360);
				LogUtilNIU.value("progressAngle----" + progressAngle);
			}
			// 显示倒计时最初的倒计时百分比
			LogUtilNIU.value("duractionSeconds--->" + duractionSeconds
					+ "--leftSeconds--->" + leftSeconds);
			if (duractionSeconds >= leftSeconds - 60) {// 总时长必须大于剩余时长
				LogUtilNIU.value("duractionSeconds---大于--leftSeconds--->");
				circleProgressBar.setMax(duractionSeconds);// 总描述为环形max
				circleProgressBar.setProgress(progressAngle);// 剩余秒数显示为环形process
			} else {
				duractionSeconds = 0;
				progressAngle = 0;
				circleProgressBar.setMax(duractionSeconds);
				circleProgressBar.setProgress(progressAngle);
			}
			showCountDownCounting();
			/***************************************************/
		}
	}

	private void showCountingClose(String timeInfo, Date d1, Date d2) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			d2 = sdf.parse(timeInfo);// MCU查得的终点时间
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String dis = ModbusCalUtil.calDisOfDate(d1, d2, 0);
		LogUtilNIU.value("时差结果为----》" + dis);
		String leftTime = dis.replace(" : ", "");// 把 05 : 06 : 00 化为 050600的形式
		if (dis.equals("small")) {// 如果上个设置时间比现在时间小,倒计时过期了
			//
			// ModbusCalUtil.calDisOfDat返回此值
			llNotSet.setVisibility(View.VISIBLE);// 出现没有倒计时任务的提示
			llBottomSpace.setVisibility(View.GONE);// 空白处消失（时间设置区域出现）
		} else {
			tvLeftTime.setText(dis);
			tvStatus.setText("设备关闭");
			// 隐藏下面两个开和关按钮
			spaceOpenClose.setVisibility(View.VISIBLE);
			// 环形进度条初始化显示
			/***************************************************/
			String duraction = BApplication.instance.getSp().getString(
					deviceId + Constant.DEVICE_COUNT_DOWN_DURACTION, "none");
			if (duraction.equals("none")) {
			} else {
				// 计算duraction一共是多少秒
				// 计算剩余时间一共时候多少秒
				leftSeconds = ModbusCalUtil.countTotalSeconds(leftTime);
				duractionSeconds = ModbusCalUtil.countTotalSeconds(duraction);
				LogUtilNIU.value("leftSeconds--" + leftSeconds
						+ "duractionSeconds--" + duractionSeconds + "除法结果--"
						+ leftSeconds / duractionSeconds);
				Double percent = (double) leftSeconds
						/ (double) duractionSeconds;
				LogUtilNIU.value("percent----" + percent);
				progressAngle = (int) (percent * 360);
				LogUtilNIU.value("progressAngle----" + progressAngle);
			}
			// 显示倒计时最初的倒计时百分比
			LogUtilNIU.value("duractionSeconds--->" + duractionSeconds
					+ "--leftSeconds--->" + leftSeconds);
			if (duractionSeconds >= leftSeconds - 60) {// 总时长必须大于剩余时长，允许60秒误差，这个误差有可能是电表和手机时间不一样造成的
				LogUtilNIU.value("duractionSeconds---大于--leftSeconds--->");
				circleProgressBar.setMax(duractionSeconds);// 总描述为环形max
				circleProgressBar.setProgress(progressAngle);// 剩余秒数显示为环形process
			} else {
				duractionSeconds = 0;
				progressAngle = 0;
				circleProgressBar.setMax(duractionSeconds);
				circleProgressBar.setProgress(progressAngle);
			}
			showCountDownCounting();
			/***************************************************/
		}
	}

	private void checkNowDeviceTime() {
		// TODO 通过查询实时数据来查询设备当前时间
		LogUtilNIU.value("查询时间开始");
		progressGettingDataShow("正在更新状态");
		udpKind = 3;
		String verCode2 = ModbusCalUtil.verNumber(deviceId + "00010000");
		final String msg2 = "E7" + deviceId + "00010000" + verCode2 + "0D";// 0001
																			// 查询用电参数
		// new LoadDataThreadUtil(msg2, handler, deviceBSSID, this).start();
		BApplication.instance.socketSend(msg2);
		receiveThread = new ReceiveThread();
		receiveThread.start();
	}

	String hh;
	String mm;
	String ss;

	private void showCountDownCounting() {
		// circleProgressBar.setMax(duractionSeconds);//总描述为环形max
		String leftTime = tvLeftTime.getText().toString();
		String[] times = leftTime.split(" : ");
		hh = times[0];
		mm = times[1];
		ss = times[2];
		LogUtilNIU.circle("showCountDownCounting方法执行");
		new CountDownThread().start();
	}

	public class CountDownThread extends Thread {
		@Override
		public void run() {
			countDownTimeRun = true;
			LogUtilNIU.circle("ss--" + ss + "Integer.valueOf(ss)---"
					+ Integer.valueOf(ss));

			for (int i = Integer.valueOf(ss); i >= -1; i--) {
				if (countDownTimeRun) {
					if (i == -1) {// 秒已经是-1,分钟需要-1
						mm = (Integer.valueOf(mm) - 1) + "";// 分钟就减去1
						// 判断减出来的数是不是-1
						if (mm.length() == 1) {// 判断减去来的数是不是只有1位，往前补0显示
							mm = "0" + mm + "";
						}
						i = 59;
					}
					if (Integer.valueOf(mm) == -1) {// 如果分钟减去1后是-1，小时就需要-1
						// //判断减出来的数是不是-1
						hh = (Integer.valueOf(hh) - 1) + "";
						if (hh.equals("-1")) {// 如果小时减去1后是-1,则没得减了，倒计时结束，线程停止
							hh = "00";
							mm = "00";
							i = 0;
							countDownTimeRun = false;
						} else {
							if (hh.length() == 1) {// 判断减去来的数是不是只有1位，往前补0显示
								hh = "0" + hh + "";
							}
							mm = 59 + "";
						}
					}
					Message msg = new Message();
					msg.what = UPDATE_COUNTTIME;// 更新
					msg.obj = hh + " : " + mm + " : "
							+ ModbusCalUtil.add0fillLength(i + "", 2);// 发送时分秒显示结果
					handler.sendMessage(msg);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			super.run();
		}
	}

	@Override
	protected void onDestroy() {
		countDownTimeRun = false;
		super.onDestroy();
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("倒计时"); // 统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this); // 统计时长
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("倒计时"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证
										// onPageEnd 在onPause 之前调用,因为 onPause
										// 中会保存信息。"SplashScreen"为页面名称，可自定义
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
					} else {
						msg.what = Constant.MSG_FAILURE;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				msg.obj = e;
				msg.what = Constant.MSG_EXCPTION;
			}
			handler.sendMessage(msg);
		}
	}
}
