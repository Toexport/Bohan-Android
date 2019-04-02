package cn.mioto.bohan.activity;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.activity.OnlineStatusActivity.UDPBrocastReceiver;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.socket.SocketLong;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LoadDataThreadUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;

import com.umeng.analytics.MobclickAgent;
import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.helper.SocketPacket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import steed.framework.android.client.JsonClientHandler2;

/**
 * 类说明：实时参数界面 用UDP协议收发(完成)
 * 
 * 查看此Activity的运作原理前，需要先查看父类的OnCreat代码
 * 
 */
public class MenuNowDataActivity extends BaseCheckDataMenuActivity {
	/********** 布局文件控件 *************/
	private TextView tvDate;
	private TextView tvWeekDay;
	private TextView tvTime;
	private TextView tvVoltageNumber;
	private TextView tvflowNumber;
	private TextView tvPowerRateNumber;
	private TextView tvRateFactorNumber;
	private TextView tvSummerPowerNumber;
	private TextView tvCarbonNumber;
	private TextView tvTotalPrice;
	private TextView menu_tv;
	private TextView tvDeviceTmep;
	private TextView tvID;

	public static Socket socket;
	public static BufferedReader in;
	public static PrintWriter out;
	public ReceiveThread receiveThread;
	
	private static final int TOKEN_TIME_OUT = 12,
			TOKEN_TIME_OUT_NO_DIALOG = 13,SOCKET_TOKEN_TIME_OUT = 14;
	
	protected BroadcastReceiver receiver;
	protected IntentFilter filter;
	
	boolean clickRefresh = false;//刷新
	
	/********* 广播接收者 ******************************************/
	private Handler h = new Handler() {// 另一个hanlder
		public void handleMessage(Message msg) {

			if (msg.what == Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK) {
				// 加载到数据后正在加载消失
			} else if (msg.what == Constant.MSG_WHAT_SHOW_DIALOG_OF_RESEND_TASK) {
				// 显示正在加载
//				progressGettingDataShow(getString(R.string.realtime_data_loading));
			} else if (msg.what == Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK_FAIL) {
				// 发送几次后，加载失败
				progressGettingDataDismiss(getString(R.string.real_time_data_loading_failure));
			} else if (msg.what == Constant.MSG_WHAT_SEND_MSG_BY_SERVICE_RESEND_TASK_FAIL) {
				// 通过服务器去查询信息
				// 通过服务器查询设备实时数据
				checkByService((String) msg.obj);
			} else if (msg.what == Constant.MSG_SUCCESS) {
				progressDismiss("");
				String message = (String) msg.obj;
				showContent(message);
			} else if (msg.what == Constant.MSG_EXCPTION) {
				progressGettingDataDismiss(getString(R.string.real_time_data_loading_failure));
			} else if (msg.what == Constant.MSG_FAILURE) {
				progressGettingDataDismiss(getString(R.string.device_offline));
			} else if(msg.what == Constant.MSG_TIME_OUT){
				progressGettingDataDismiss(getString(R.string.connection_timeout));
			}else if(msg.what == SOCKET_TOKEN_TIME_OUT){
				progressGettingDataDismissNoToast();
				ToastUtils.longToast(MenuNowDataActivity.this,getString(R.string.login_expired));
				BApplication.instance.clearThisUserFlashDatasOfApplication();// 清空application此用户的共用数据
				finish();
				Intent intent = new Intent(getApplicationContext(),
						LoginActivity.class);
				startActivity(intent);
			}
		}
	};

	/**
	 * 程序入口
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/******* 初始化toolbar ********************************************/
		ViewUtil.initToolbar(this, R.string.now_data, true, true,
				R.string.update);
		ViewUtil.setToolbarTitle(getResources().getString(R.string.now_data));
		/********* 初始化广播 ******************************************/
		receiver = new NowDataBrocastReceiver();// 注册广播
		filter = new IntentFilter();
		filter.addAction(Constant.SOCKET_BROCAST_ONRECEIVED);
		filter.addAction(Constant.BROCAST_NOW_DATA_MSG);
		filter.addAction(Constant.BROCAST_FAILE_MSG);
		filter.addAction(Constant.SOCKET_SEND_FAIL);
//		filter.addAction(Constant.BROCAST_TIME_OUT);
//		filter.addAction(Constant.BROCAST_SOCKET_FAIL);
		registerReceiver(receiver, filter);
		/***************************************************/
		// 第一次发送数据
		checkNowData();// 进入页面就发socket请求
	}

	/**********************************************************/
	/**
	 * 发送查询实时数据指令，新指令
	 */
	@SuppressWarnings("static-access")
	public void checkNowData() {
		// 显示正在加载
		progressGettingDataShow(getString(R.string.realtime_data_loading));
		String verCode = ModbusCalUtil.verNumber(deviceId + "00010000");
		final String message = "E7" + deviceId + "00010000" + verCode + "0D";// 0001
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					SocketLong.sendMsg(message);
				} catch (Exception e) {
					e.printStackTrace();
//					h.sendEmptyMessage(Constant.MSG_EXCPTION);
				};;
			}
		}).start();
	}

	/**********************************************************/
	/**
	 * 点击事件，刷新即时用电参数
	 */

	@Override
	public void onClick(View v) {
		if (v == menu_tv) {
//			clickRefresh = true;
//			if(!clickRefresh){
//				h.postDelayed(new Runnable() {  
//	                public void run() {  
//	                	checkNowData();
//	                	clickRefresh = false;
//	                }  
//	            }, 2000);  
//			}else{
//				
//			}
			checkNowData();
			// //这里可以考虑显示正在刷新的动画，知道数据显示成功
			// progressShow("正在刷新");
			// checkNowData();
			// handler.postDelayed(new Runnable() {
			//
			// @Override
			// public void run() {
			// progressDismiss("刷新失败");
			// }
			// }, Constant.REQUEST_TIME_OUT);
		}
		super.onClick(v);
	}

	/**********************************************************/
	/**
	 * 初始化布局
	 */
	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_menu_now_data;
	}

	/**********************************************************/
	/**
	 * 收到数据后显示内容
	 */
	private void showContent(String content) {
		LogUtilNIU.value("返回的数据为" + content);
		String statCode = content.substring(14, 16);// ID后的状态码
		String id = content.substring(2, 14);
		if (id.equals(deviceId)) {// 手机将会收到所有设备的数据。所以要判断是不是该设备的，判断一下为好
			if (statCode.equals("00")) {// 接收数据成功
				/****** 赋值各变量 *********************************************/
				if (content.substring(16, 20).equals("0001")) {// 查询用电数据
					BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
					progressGettingDataDismissNoToast();
					// 所有数据在原来基础上移动10位
					LogUtilNIU.value("用电参数返回的数据为" + content);
					
					tvID.setText(BApplication.instance.getCurrentDevice().getDeviceID());
					String year = content.substring(72, 74);
					String month = content.substring(74, 76);
					String date = content.substring(76, 78);
					String weekday = content.substring(78, 80);// 星期
					String hour = content.substring(80, 82);
					String minute = content.substring(82, 84);
					String second = content.substring(84, 86);
					switch (weekday) {
					case "00":
						weekday = "一";
						break;
					case "01":
						weekday = "二";
						break;
					case "02":
						weekday = "三";
						break;
					case "03":
						weekday = "四";
						break;
					case "04":
						weekday = "五";
						break;
					case "05":
						weekday = "六";
						break;
					case "06":
						weekday = "日";
						break;
					}
					String voltage = content.substring(24, 30);
					String flow = content.substring(30, 36);
					String rate = content.substring(36, 42);
					String rateFactor = content.substring(42, 46);
					String summerPower = content.substring(46, 54);
					String carbon = content.substring(54, 62);
					String totalPrice = content.substring(62, 72);
					String deviceTmepStr = content.substring(86,90);
					/***** 设置控件显示各值 **********************************************/
					// tvDate.setText(year + "年" + month + "月" + date + "日");//
					// 年月日
					if(weekday.equals("一")){
						tvWeekDay.setText(getString(R.string.mon));// 设置星期
					}else if(weekday.equals("二")){
						tvWeekDay.setText(getString(R.string.tue));// 设置星期
					}else if(weekday.equals("三")){
						tvWeekDay.setText(getString(R.string.wed));// 设置星期
					}else if(weekday.equals("四")){
						tvWeekDay.setText(getString(R.string.thu));// 设置星期
					}else if(weekday.equals("五")){
						tvWeekDay.setText(getString(R.string.fri));// 设置星期
					}else if(weekday.equals("六")){
						tvWeekDay.setText(getString(R.string.sat));// 设置星期
					}else if(weekday.equals("日")){
						tvWeekDay.setText(getString(R.string.sun));// 设置星期
					}
					tvTime.setText(year + getString(R.string.year1) + month + getString(R.string.month1) + date + getString(R.string.day1)
							+ hour + ":" + minute + ":" + second);// 设置时间
					try {
						voltage = ModbusCalUtil.addDotDel0(voltage, 3);
						tvVoltageNumber.setText(voltage);
						flow = ModbusCalUtil.addDotDel0(flow, 3);
						tvflowNumber.setText(flow);
						rate = ModbusCalUtil.addDotDel0(rate, 4);
						tvPowerRateNumber.setText(rate);
						rateFactor = ModbusCalUtil.addDotDel0(rateFactor, 1);
						tvRateFactorNumber.setText(rateFactor);// 功率因素
						summerPower = ModbusCalUtil.addDotDel0(summerPower, 6);
						tvSummerPowerNumber.setTypeface(Typeface
								.defaultFromStyle(Typeface.BOLD));// 加粗
						tvSummerPowerNumber.setText(summerPower);
						carbon = ModbusCalUtil.addDotDel0(carbon, 6);
						tvCarbonNumber.setText(carbon);
						totalPrice = ModbusCalUtil.addDotDel0(totalPrice, 8);
						tvTotalPrice.setText(totalPrice);
						deviceTmepStr = ModbusCalUtil.addDotDel0(deviceTmepStr, 2);
						tvDeviceTmep.setText(deviceTmepStr);
					} catch (Exception e) {
						e.printStackTrace();
						LogUtilNIU.e("解析数据失败");
						progressGettingDataDismiss(getString(R.string.data_parsing_failed));
					}

				}
			} else if (statCode.equals("03")) {
				LogUtilNIU.e("发送指令有错，返回03");
			}
		} else {
			// 在这个设备的查询界面接收到其他Id的数据
			LogUtilNIU.e("在" + deviceId + "的界面接收到其他设备" + id + "的数据");
		}
	}

	/**********************************************************/
	/**
	 * 取消注册
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("9组时段设置"); // 统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this); // 统计时长
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("9组时段设置"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证
											// onPageEnd 在onPause 之前调用,因为
											// onPause
											// 中会保存信息。"SplashScreen"为页面名称，可自定义
		MobclickAgent.onPause(this);
	}

	@Override
	protected String onServiceUDPBack(String content) {
		showContent(content);
		return null;
	}

	@Override
	protected String onCheckByServiceOnInterfaceFail(String json) {
		// TODO Auto-generated method stub
		return null;
	}

	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
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
					} else if(code == 2){
						msg.what = SOCKET_TOKEN_TIME_OUT;
					}else {
						msg.what = Constant.MSG_FAILURE;
					}
				}
			} catch(SocketTimeoutException e){
				e.printStackTrace();
				msg.what = Constant.MSG_TIME_OUT;
			} catch (Exception e) {
				e.printStackTrace();
				msg.obj = e;
				msg.what = Constant.MSG_EXCPTION;
			}
			h.sendMessage(msg);
		}
	}
	
	
	public class NowDataBrocastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			 if(action.equals(Constant.BROCAST_NOW_DATA_MSG)){
				 progressDismiss("");
				//开关操作
				String message = intent
						.getStringExtra("message");
				showContent(message);
				gettedDataOK = true;
			} else if(action.equals(Constant.BROCAST_FAILE_MSG)){
				gettedDataOK = false;
				progressGettingDataDismissNoToast();
				String message = intent
						.getStringExtra("message");
				ToastUtils.longToast(MenuNowDataActivity.this, message);
			}else if(action.equals(Constant.BROCAST_SOCKET_FAIL)){
				gettedDataOK = false;
//				ToastUtils.longToast(MenuNowDataActivity.this, getString(R.string.socket_disconnected));
			} else if(action.equals(Constant.BROCAST_TIME_OUT)){
				gettedDataOK = false;
//				ToastUtils.longToast(MenuNowDataActivity.this, getString(R.string.exception_sockettimeout));
			} else if(action.equals(Constant.SOCKET_SEND_FAIL)){
				gettedDataOK = false;
				progressGettingDataDismissNoToast();
//				ToastUtils.longToast(MenuNowDataActivity.this, getString(R.string.real_time_data_loading_failure));
			}
		}
	}
	
}
