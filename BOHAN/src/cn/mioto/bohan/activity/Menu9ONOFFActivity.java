package cn.mioto.bohan.activity;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.R.string;
import cn.mioto.bohan.activity.MenuNowDataActivity.NowDataBrocastReceiver;
import cn.mioto.bohan.adapter.NineSetBaseAdapter;
import cn.mioto.bohan.socket.SocketLong;
import cn.mioto.bohan.utils.LoadDataThreadUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;
import cn.mioto.bohan.view.pulltorefresh.MaterialRefreshLayout;
import cn.mioto.bohan.view.pulltorefresh.MaterialRefreshListener;

import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 类说明：9组时段设置页面 UDP改写中 作者： jiemai liangminhua 创建时间：2016年5月24日 下午5:27:13
 */
@SuppressLint("ResourceAsColor")
public class Menu9ONOFFActivity extends BaseCheckDataMenuActivity {
	/********** 初始化布局控件 *************/
	// private ListView m9setList;
	private MaterialRefreshLayout refresh;
	private Boolean refreshing = false;
	// 9组定时那9组的设置信息
	private Map<Integer, String> mNineSetSetting = new HashMap<>();
	// 下面的确认当前修改按钮
	private TextView tvBottomBtnConRev;
	// 底下的修改设置区域
	private LinearLayout llBottomRev;

	/******* 广播接收者 ********************************************/
	private NineSetBaseAdapter adapter;
	private List<String> strings;
	// udp指令的类型
	private int udpKind = 0;

	/***************************************************/
	private List<LinearLayout> setlist = new ArrayList<>();
	private int revicePosition;
	/***************************************************/
	private ListView m9setList;

	public static Socket socket;
	public static BufferedReader in;
	public static PrintWriter out;
	public ReceiveThread receiveThread;

	private TextView menuTv;
	private TextView menuFront;
	private TextView toolbarTitle;

	private LinearLayout layoutSet;// 提示layout
	private TextView tvSetTip;// 提示文字
	private TextView tvSet;// 提示按钮
	private LinearLayout layoutListView;// listview的layout

	private TextView tvHeater;// 熱水器
	private TextView tvFishTank;// 魚缸
	private TextView tvNightLight;// 小夜灯
	private TextView tvMosquitoRepellentIncense;// 蚊香
	private TextView tvHeaterQ;// 取暖器
	private TextView tvParentModel;// 家长模式
	private LinearLayout layoutTimeSet;
	private boolean isSet = false;// 时段模式设置

	private String model = "10";
	public static final int TOKEN_TIME_OUT = 12, TOKEN_TIME_OUT_NO_DIALOG = 13,
			SOCKET_TOKEN_TIME_OUT = 14, SET_ON_OFFF = 15;

	protected BroadcastReceiver receiver;
	protected IntentFilter filter;

	protected Handler h = new Handler() {// 另一个hanlder
		public void handleMessage(Message msg) {
			if (msg.what == Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK) {
				// 加载到数据后正在加载消失
			} else if (msg.what == Constant.MSG_WHAT_SHOW_DIALOG_OF_RESEND_TASK) {
				LogUtilNIU.value("activity的hanlder收到线程开始的消息");
				if (udpKind == 1) {
					progressGettingDataShow(getString(R.string.setting));
				}
			} else if (msg.what == Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK_FAIL) {
				if (udpKind == 0) {
					progressGettingDataDismiss(getString(R.string.loading_data_fail));
				} else if (udpKind == 1) {
					progressGettingDataDismiss(getString(R.string.set_fail));
				}
			} else if (msg.what == Constant.MSG_WHAT_SEND_MSG_BY_SERVICE_RESEND_TASK_FAIL) {
				// 通过服务器去查询信息
				// 通过服务器查询设备实时数据
				checkByService((String) msg.obj);
			} else if (msg.what == Constant.MSG_SUCCESS) {
				progressDismiss("");
				String message = (String) msg.obj;
				showContent(message);
			} else if (msg.what == Constant.MSG_EXCPTION) {
				progressGettingDataDismiss(getString(R.string.connection_timeout));
				tvBottomBtnConRev.setVisibility(View.GONE);
			} else if (msg.what == Constant.MSG_FAILURE) {
				progressGettingDataDismiss(getString(R.string.device_offline));
				tvBottomBtnConRev.setVisibility(View.GONE);
			} else if (msg.what == Constant.MSG_TIME_OUT) {
				progressGettingDataDismiss(getString(R.string.connection_timeout));
				tvBottomBtnConRev.setVisibility(View.GONE);
			} else if (msg.what == SOCKET_TOKEN_TIME_OUT) {
				progressGettingDataDismissNoToast();
				ToastUtils.longToast(Menu9ONOFFActivity.this,
						getString(R.string.login_expired));
				BApplication.instance.clearThisUserFlashDatasOfApplication();// 清空application此用户的共用数据
				finish();
				Intent intent = new Intent(getApplicationContext(),
						LoginActivity.class);
				startActivity(intent);
			} else if (msg.what == SET_ON_OFFF) {
				llBottomRev.setVisibility(View.VISIBLE);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title,
		// R.string.time_setting, true, false, 0, 0);
		// ViewUtil.setToolbarTitle(getResources().getString(R.string.dur_setting));
		layoutSet = (LinearLayout) findView(R.id.layoutSet);
		tvSetTip = (TextView) findView(R.id.tvSetTip);
		tvSet = (TextView) findView(R.id.tvSet);
		layoutListView = (LinearLayout) findView(R.id.layoutListView);

		menuFront = (TextView) findViewById(R.id.menu_front_9);
		menuTv = (TextView) findViewById(R.id.menu_tv_9);
		menuTv.setOnClickListener(this);
		menuFront.setOnClickListener(this);
		strings = new ArrayList<>();// 初始化,数据开始为0，界面显示全部离线
		/***************************************************/
		// 初始化ListView数据
		adapter = new NineSetBaseAdapter(this, strings, deviceId, deviceDBssid,
				h);
		m9setList.setAdapter(adapter);

		receiver = new Data9BrocastReceiver();// 注册广播
		filter = new IntentFilter();
		filter.addAction(Constant.SOCKET_BROCAST_ONRECEIVED);
		filter.addAction(Constant.BROCAST_GET_9TIME);
		filter.addAction(Constant.BROCAST_FAILE_MSG);
		filter.addAction(Constant.BROCAST_SET_9TIME);
		filter.addAction(Constant.BROCAST_TIME_OUT);
		filter.addAction(Constant.BROCAST_SOCKET_FAIL);
		registerReceiver(receiver, filter);

		/******* 判断，发送信息 ********************************************/
		progressGettingDataShow(getString(R.string.select_9_data));
		checkNowData();// 查询9组定时信息
		refresh.setMaterialRefreshListener(new MaterialRefreshListener() {

			@Override
			public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
				checkNowData();// 刷新
				refreshing = true;
			}
		});

		layoutTimeSet = findView(R.id.layoutTimeSet);
		tvHeater = findView(R.id.tv_heater);
		tvFishTank = findView(R.id.tv_fish_tank);
		tvNightLight = findView(R.id.tv_night_light);
		tvMosquitoRepellentIncense = findView(R.id.tv_mosquito_repellent_incense);
		tvHeaterQ = findView(R.id.tv_heater_q);
		tvParentModel = findView(R.id.tv_parent_model);
		tvHeater.setOnClickListener(this);
		tvFishTank.setOnClickListener(this);
		tvNightLight.setOnClickListener(this);
		tvMosquitoRepellentIncense.setOnClickListener(this);
		tvHeaterQ.setOnClickListener(this);
		tvParentModel.setOnClickListener(this);
	}

	/**********************************************************/
	/**
	 * 查询上次设置的9组定时信息
	 */
	private void checkNowData() {
		udpKind = 0;
		try {
			String verCode = ModbusCalUtil.verNumber(deviceId + "00080000");
			final String message = "E7" + deviceId + "00080000" + verCode
					+ "0D";// 0008
							// 查询9组继电器通断时间
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
//						BApplication.instance.socketSend(message);
						SocketLong.sendMsg(message);
					} catch (Exception e) {
						e.printStackTrace();
						h.sendEmptyMessage(Constant.MSG_EXCPTION);
					}
					;
					// receiveThread = new ReceiveThread();
					// receiveThread.start();
				}
			}).start();
		} catch (Exception e) {
			Message msg = new Message();
			msg.what = Constant.MSG_FAILURE;
			h.sendMessage(msg);
		}

	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_menu_nine_setting;
	}

	/**
	 * 在设置页面选择了时间后，在这里更新数据
	 * 
	 * @Description:通过map里面的数据，设置新的stringList
	 */
	public void makeNewSetFromMap() {
		strings.clear();
		for (int i = 0; i < mNineSetSetting.size(); i++) {
			strings.add(mNineSetSetting.get(i));
		}
	}

	/**********************************************************/
	/**
	 * 收到数据后显示内容
	 */
	public void showContent(String content) {
		if (checkUDPMessage(content)) {
			LogUtilNIU.value("16到20命令号" + content.substring(16, 20)
					+ "**数据长度**" + content.length());
			if (isReqCodeEqual(content, "0008")) {// 收到9组定时数据
				BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
				progressGettingDataDismissNoToast();
				// ToastUtils.testToast(Menu9ONOFFActivity.this,
				// "收到9组定时数据"+content);
				strings.clear();

				llBottomRev.setVisibility(View.GONE);
				// 查询查询9组继电器通断时间返回的信息
				String set0 = content.substring(24, 34);
				mNineSetSetting.put(0, set0);
				String set1 = content.substring(34, 44);
				mNineSetSetting.put(1, set1);
				String set2 = content.substring(44, 54);
				mNineSetSetting.put(2, set2);
				String set3 = content.substring(54, 64);
				mNineSetSetting.put(3, set3);
				String set4 = content.substring(64, 74);
				mNineSetSetting.put(4, set4);
				String set5 = content.substring(74, 84);
				mNineSetSetting.put(5, set5);
				String set6 = content.substring(84, 94);
				mNineSetSetting.put(6, set6);
				String set7 = content.substring(94, 104);
				mNineSetSetting.put(7, set7);
				String set8 = content.substring(104, 114);
				mNineSetSetting.put(8, set8);
				strings.add(set0);
				strings.add(set1);
				strings.add(set2);
				strings.add(set3);
				strings.add(set4);
				strings.add(set5);
				strings.add(set6);
				strings.add(set7);
				strings.add(set8);
				// TODO 先改为不做排序
				// NineSetComparator com = new NineSetComparator();
				// Collections.sort(strings,com);
				// for (int i = 0; i < mNineSetSetting.size(); i++) {
				// if (!mNineSetSetting.get(i).equals("0000000000")) {
				// // menuTv.setVisibility(View.VISIBLE);
				// break;
				// }
				// }
				// 如果全部为0则用电模式为00
				String temp = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
				if (content.subSequence(24, 114).equals(temp)) {
					// 全部为0没有设置用电数据
					model = "09";
				} else {
					model = content.substring(114, 116);
				}
				if (refreshing) {
					refreshing = false;
					refresh.finishRefresh();
					if (isSet) {
						ToastUtils.shortToast(Menu9ONOFFActivity.this,
								getString(R.string.refresh_success));
					}
					layoutSet.setVisibility(View.GONE);
					layoutListView.setVisibility(View.VISIBLE);
					layoutTimeSet.setVisibility(View.GONE);
				} else {
					layoutTimeSet.setVisibility(View.VISIBLE);
					layoutSet.setVisibility(View.VISIBLE);
					layoutListView.setVisibility(View.GONE);
				}
				// 解析用电模式

				if (model.equals("09")) {
					tvSetTip.setText(getString(R.string.no_set_time_tip));
					tvSetTip.setTextColor(getResources().getColor(R.color.red));
					tvSet.setText(getString(R.string.set));
					// menuTv.setVisibility(View.GONE);
				} else if (model.equals("03")) {
					// 部分设备
					tvSetTip.setText(getString(R.string.set_time_tip));
					tvSetTip.setTextColor(getResources()
							.getColor(R.color.green));
					tvSet.setText(getString(R.string.check));
					// menuTv.setVisibility(View.GONE);
				} else {
					tvSetTip.setText(getString(R.string.data_set_time_tip));
					tvSetTip.setTextColor(getResources().getColor(R.color.red));
					tvSet.setText(getString(R.string.start));
					// menuTv.setVisibility(View.VISIBLE);
				}
				if (content.substring(24, 32).equals("18002300")
						&& content.substring(34, 42).equals("00000000")) {
					// 热水器模式
					tvHeater.setBackgroundResource(R.drawable.selector_btn_radius_gradia_2);
					tvFishTank.setBackgroundResource(R.drawable.tablerow);
					tvNightLight.setBackgroundResource(R.drawable.tablerow);
					tvMosquitoRepellentIncense
							.setBackgroundResource(R.drawable.tablerow);
					tvHeaterQ.setBackgroundResource(R.drawable.tablerow);
					tvParentModel.setBackgroundResource(R.drawable.tablerow);
					tvHeater.setTextColor((R.color.white));
					tvFishTank.setTextColor(R.color.black);
					tvNightLight.setTextColor(R.color.black);
					tvMosquitoRepellentIncense.setTextColor(R.color.black);
					tvHeaterQ.setTextColor(R.color.black);
					tvParentModel.setTextColor(R.color.black);
				} else if (content.substring(24, 32).equals("22000600")
						&& content.substring(34, 42).equals("00000000")) {
					// 小夜灯
					tvHeater.setBackgroundResource(R.drawable.tablerow);
					tvFishTank.setBackgroundResource(R.drawable.tablerow);
					tvNightLight
							.setBackgroundResource(R.drawable.selector_btn_radius_gradia_2);
					tvMosquitoRepellentIncense
							.setBackgroundResource(R.drawable.tablerow);
					tvHeaterQ.setBackgroundResource(R.drawable.tablerow);
					tvParentModel.setBackgroundResource(R.drawable.tablerow);
					tvNightLight.setTextColor(R.color.white);
					tvHeater.setTextColor((R.color.black));
					tvFishTank.setTextColor(R.color.black);
					tvMosquitoRepellentIncense.setTextColor(R.color.black);
					tvHeaterQ.setTextColor(R.color.black);
					tvParentModel.setTextColor(R.color.black);
				} else if (content.substring(24, 32).equals("20000600")
						&& content.substring(34, 42).equals("00000000")) {
					// 蚊香
					tvHeater.setBackgroundResource(R.drawable.tablerow);
					tvFishTank.setBackgroundResource(R.drawable.tablerow);
					tvNightLight.setBackgroundResource(R.drawable.tablerow);
					tvMosquitoRepellentIncense
							.setBackgroundResource(R.drawable.selector_btn_radius_gradia_2);
					tvHeaterQ.setBackgroundResource(R.drawable.tablerow);
					tvParentModel.setBackgroundResource(R.drawable.tablerow);
					tvMosquitoRepellentIncense.setTextColor(R.color.white);
					tvNightLight.setTextColor(R.color.black);
					tvHeater.setTextColor((R.color.black));
					tvFishTank.setTextColor(R.color.black);
					tvHeaterQ.setTextColor(R.color.black);
					tvParentModel.setTextColor(R.color.black);
				} else if (content.substring(24, 32).equals("18000600")
						&& content.substring(34, 42).equals("00000000")) {
					// 取暖器
					tvHeater.setBackgroundResource(R.drawable.tablerow);
					tvFishTank.setBackgroundResource(R.drawable.tablerow);
					tvNightLight.setBackgroundResource(R.drawable.tablerow);
					tvMosquitoRepellentIncense
							.setBackgroundResource(R.drawable.tablerow);
					tvHeaterQ
							.setBackgroundResource(R.drawable.selector_btn_radius_gradia_2);
					tvParentModel.setBackgroundResource(R.drawable.tablerow);
					tvHeaterQ.setTextColor(R.color.white);
					tvMosquitoRepellentIncense.setTextColor(R.color.black);
					tvNightLight.setTextColor(R.color.black);
					tvHeater.setTextColor((R.color.black));
					tvFishTank.setTextColor(R.color.black);
					tvParentModel.setTextColor(R.color.black);
				} else if (content.substring(24, 32).equals("17002000")
						&& content.substring(34, 42).equals("00000000")) {
					// 家长模式
					tvHeater.setBackgroundResource(R.drawable.tablerow);
					tvFishTank.setBackgroundResource(R.drawable.tablerow);
					tvNightLight.setBackgroundResource(R.drawable.tablerow);
					tvMosquitoRepellentIncense
							.setBackgroundResource(R.drawable.tablerow);
					tvHeaterQ.setBackgroundResource(R.drawable.tablerow);
					tvParentModel
							.setBackgroundResource(R.drawable.selector_btn_radius_gradia_2);
					tvParentModel.setTextColor(R.color.white);
					tvHeaterQ.setTextColor(R.color.black);
					tvMosquitoRepellentIncense.setTextColor(R.color.black);
					tvNightLight.setTextColor(R.color.black);
					tvHeater.setTextColor((R.color.black));
					tvFishTank.setTextColor(R.color.black);
				} else if (content.substring(24, 32).equals("00000100")
						&& content.substring(34, 42).equals("02000300")
						&& content.substring(44, 52).equals("04000500")
						&& content.substring(54, 62).equals("06000700")
						&& content.substring(64, 72).equals("08001500")
						&& content.substring(74, 82).equals("16001700")
						&& content.substring(84, 92).equals("18001900")
						&& content.substring(94, 102).equals("20002100")
						&& content.substring(104, 112).equals("22002300")) {
					// 鱼缸
					tvHeater.setBackgroundResource(R.drawable.tablerow);
					tvFishTank
							.setBackgroundResource(R.drawable.selector_btn_radius_gradia_2);
					tvNightLight.setBackgroundResource(R.drawable.tablerow);
					tvMosquitoRepellentIncense
							.setBackgroundResource(R.drawable.tablerow);
					tvHeaterQ.setBackgroundResource(R.drawable.tablerow);
					tvParentModel.setBackgroundResource(R.drawable.tablerow);
					tvFishTank.setTextColor(R.color.white);
					tvParentModel.setTextColor(R.color.black);
					tvHeaterQ.setTextColor(R.color.black);
					tvMosquitoRepellentIncense.setTextColor(R.color.black);
					tvNightLight.setTextColor(R.color.black);
					tvHeater.setTextColor((R.color.black));
				}
				adapter.notifyDataSetChanged();// 更新数据
			} else if (isReqCodeEqual(content, "0009")) {// 设置9组继电器通断时间成功
				adapter.clearSettingMap();
				p.dismiss();
				LogUtilNIU.value("设置9组继电器通断时间反馈-->" + content);
				if (content.substring(24, 26).equals("00")) {// 设置成功
					BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
					progressGettingDataDismiss(getString(R.string.set_success));
					// 下面的修改区域消失
					/***************************************************/
					// 设置菊花消失和按钮新状态
					String id = content.substring(2, 14);
					// 判断该数据是lv第几位，然后把那个位置的按钮和其他控件的状态做相应改变
					int position = BApplication.instance
							.getNineSetSettingPosition();
					ToastUtils.shortToast(Menu9ONOFFActivity.this,
							getString(R.string.set_success));
					if (!isSet) {
						refreshing = false;
					} else {
						refreshing = true;
						// 把旧数据修改后先放到这儿显示，防止滑动带来的bug
					}
					h.postDelayed(new Runnable() {
						public void run() {
							progressGettingDataShow(getString(R.string.refreshing));
						}
					}, 500);
					// 真正更新
					h.postDelayed(new Runnable() {
						@Override
						public void run() {
							checkNowData();// 查询9组定时信息，来更新数据
						}
					}, Constant.RESENDTIME);

				} else if (content.substring(24, 26).equals("01")) {// 数值超出
					BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
					progressDismissNoToast();
					adapter.clearSettingMap();
					ToastUtils.shortToast(Menu9ONOFFActivity.this,
							getString(R.string.number_error));
				} else if (content.substring(24, 26).equals("02")) {// FLASH出错
					BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
					progressDismissNoToast();
					adapter.clearSettingMap();
					ToastUtils.shortToast(Menu9ONOFFActivity.this,
							getString(R.string.flash_error));
				}
			}
		} else {// TODO 这是一个待修复的小BUG
				// 3A 681606250010 03 0009 0000 C5 0D
			BApplication.instance.setResendTaskShowBreak(true);
			LogUtilNIU.value("接收到BUG数据！！！！" + content);
			p.dismiss();
			ToastUtils.shortToast(Menu9ONOFFActivity.this,
					getString(R.string.operation_failure));
			adapter.clearSettingMap();
			llBottomRev.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		// 时段控制模式公用
		String week = "01111111";
		String hexTime = Integer.toHexString(
				Integer.valueOf(week.toString(), 2)).toString();

		// 点击下面的确认修改按钮
		if (v == tvBottomBtnConRev) {
			// BApplication.instance.setNineSetWaitConfirm(false);//
			// 待确认状态在按钮被点击后取消
			isSet = true;
			refreshing = true;
			adapter.sendOrderToConfirmSetting();
			// receiveThread = new ReceiveThread();
			// receiveThread.start();
			udpKind = 1;// 正在设置
			// 点击确定修改按钮以后，收集所有当前的界面设置信息。再发送指令
			// //以下是根据选择框的情况修改9组定时。为设置单个的逻辑
			// //设置9组定时
			// StringBuffer sendToSetTime = new StringBuffer();
			// for(int i = 0 ; i < strings.size(); i++){
			// sendToSetTime.append(strings.get(i));
			// }
			// String setTime =sendToSetTime.toString();//指令的内容
			// String verCode =
			// ModbusCalUtil.verNumber(deviceId+"0009"+"002D"+setTime);
			// String msg = "3A"+deviceId+"0009"+"002D"+setTime+verCode+"0D";
			// LogUtilNIU.value("设置9组定时发送的数据为"+msg);
			// progressGettingDataShow("正在设置");
			// udpKind = 1;
			// new LoadDataThreadUtil(msg, h,deviceDBssid,this).start();
		} else if (v == menuTv) {
			// 开启时段控制总按钮
			// BApplication.instance.setNineSetWaitConfirm(false);//
			// 待确认状态在按钮被点击后取消
			isSet = true;
			adapter.sendOrderToConfirmSetting();
			// receiveThread = new ReceiveThread();
			// receiveThread.start();
			udpKind = 1;// 正在设置
		} else if (v == menuFront) {
			finish();
		} else if (v == tvSet) {
			layoutTimeSet.setVisibility(View.GONE);
			layoutSet.setVisibility(View.GONE);
			layoutListView.setVisibility(View.VISIBLE);
			if (model.equals("09")) {
				// 全部为0
				layoutListView.setVisibility(View.VISIBLE);
				layoutSet.setVisibility(View.GONE);
				menuTv.setVisibility(View.GONE);
				layoutTimeSet.setVisibility(View.GONE);

			} else if (model.equals("03")) {
				// 设备正在执行用电模式
				layoutListView.setVisibility(View.VISIBLE);
				layoutTimeSet.setVisibility(View.GONE);
				layoutSet.setVisibility(View.GONE);
				menuTv.setVisibility(View.GONE);
			} else {
				// 部分为0
				layoutListView.setVisibility(View.VISIBLE);
				layoutSet.setVisibility(View.GONE);
				layoutTimeSet.setVisibility(View.GONE);
				menuTv.setVisibility(View.VISIBLE);

			}
		} else if (v == tvHeater) {
			h.postDelayed(new Runnable() {
				public void run() {
					progressGettingDataShow(getString(R.string.setting));
				}
			}, 500);
			isSet = false;
			refreshing = false;
			mNineSetSetting.clear();
			adapter.clearSettingMap();// 清空数据
			String time = "18002300";
			hexTime = hexTime.toUpperCase();
			if (hexTime.length() == 1) {// 如果16进制只有1位，需要往前补上0
				hexTime = "0" + hexTime;
			}
			// 把设置时间放在adapter的mNineSetSetting里存起来，用于发送指令时使用
			adapter.setmNineSetSetting(revicePosition, time + hexTime);
			for (int i = 0; i < 9; i++) {
				mNineSetSetting.put(i, "none");
			}
			strings.clear();
			strings.add((time + hexTime) + "");
			for (int i = 0; i < mNineSetSetting.size(); i++) {
				strings.add("0000000000");
			}
			mNineSetSetting.put(revicePosition, time + hexTime);
			// 把这个item对应地设置成新的数据，然后更新一下显示
			// makeNewSetFromMap();

			adapter.sendOrderToConfirmSetting();
			// receiveThread = new ReceiveThread();
			// receiveThread.start();
			udpKind = 1;// 正在设置

			tvHeater.setBackgroundResource(R.drawable.selector_btn_radius_gradia_2);
			tvFishTank.setBackgroundResource(R.drawable.tablerow);
			tvNightLight.setBackgroundResource(R.drawable.tablerow);
			tvMosquitoRepellentIncense
					.setBackgroundResource(R.drawable.tablerow);
			tvHeaterQ.setBackgroundResource(R.drawable.tablerow);
			tvParentModel.setBackgroundResource(R.drawable.tablerow);
			tvHeater.setTextColor((R.color.white));
			tvFishTank.setTextColor(R.color.black);
			tvNightLight.setTextColor(R.color.black);
			tvMosquitoRepellentIncense.setTextColor(R.color.black);
			tvHeaterQ.setTextColor(R.color.black);
			tvParentModel.setTextColor(R.color.black);
		} else if (v == tvFishTank) {
			h.postDelayed(new Runnable() {
				public void run() {
					progressGettingDataShow(getString(R.string.setting));
				}
			}, 500);
			isSet = false;
			refreshing = false;
			// mNineSetSetting.clear();
			adapter.clearSettingMap();// 清空数据
			hexTime = hexTime.toUpperCase();
			if (hexTime.length() == 1) {// 如果16进制只有1位，需要往前补上0
				hexTime = "0" + hexTime;
			}
			String time1 = "00000100";
			mNineSetSetting.put(0, time1 + hexTime);
			adapter.setmNineSetSetting(0, time1 + hexTime);
			String time2 = "02000300";
			mNineSetSetting.put(1, time2 + hexTime);
			adapter.setmNineSetSetting(1, time2 + hexTime);
			String time3 = "04000500";
			mNineSetSetting.put(2, time3 + hexTime);
			adapter.setmNineSetSetting(2, time3 + hexTime);
			String time4 = "06000700";
			mNineSetSetting.put(3, time4 + hexTime);
			adapter.setmNineSetSetting(3, time4 + hexTime);
			String time5 = "08001500";
			mNineSetSetting.put(4, time5 + hexTime);
			adapter.setmNineSetSetting(4, time5 + hexTime);
			String time6 = "16001700";
			mNineSetSetting.put(5, time6 + hexTime);
			adapter.setmNineSetSetting(5, time6 + hexTime);
			String time7 = "18001900";
			mNineSetSetting.put(6, time7 + hexTime);
			adapter.setmNineSetSetting(6, time7 + hexTime);
			String time8 = "20002100";
			mNineSetSetting.put(7, time8 + hexTime);
			adapter.setmNineSetSetting(7, time8 + hexTime);
			String time9 = "22002300";
			adapter.setmNineSetSetting(8, time9 + hexTime);
			mNineSetSetting.put(8, time9 + hexTime);
			// 把这个item对应地设置成新的数据，然后更新一下显示
			makeNewSetFromMap();
			adapter.sendOrderToConfirmSetting();
			// receiveThread = new ReceiveThread();
			// receiveThread.start();
			udpKind = 1;// 正在设置

			tvHeater.setBackgroundResource(R.drawable.tablerow);
			tvFishTank
					.setBackgroundResource(R.drawable.selector_btn_radius_gradia_2);
			tvNightLight.setBackgroundResource(R.drawable.tablerow);
			tvMosquitoRepellentIncense
					.setBackgroundResource(R.drawable.tablerow);
			tvHeaterQ.setBackgroundResource(R.drawable.tablerow);
			tvParentModel.setBackgroundResource(R.drawable.tablerow);
			tvFishTank.setTextColor(R.color.white);
			tvParentModel.setTextColor(R.color.black);
			tvHeaterQ.setTextColor(R.color.black);
			tvMosquitoRepellentIncense.setTextColor(R.color.black);
			tvNightLight.setTextColor(R.color.black);
			tvHeater.setTextColor((R.color.black));
		} else if (v == tvNightLight) {
			h.postDelayed(new Runnable() {
				public void run() {
					progressGettingDataShow(getString(R.string.setting));
				}
			}, 500);
			isSet = false;
			refreshing = false;
			mNineSetSetting.clear();
			adapter.clearSettingMap();// 清空数据
			String time = "22000600";
			hexTime = hexTime.toUpperCase();
			if (hexTime.length() == 1) {// 如果16进制只有1位，需要往前补上0
				hexTime = "0" + hexTime;
			}
			// 把设置时间放在adapter的mNineSetSetting里存起来，用于发送指令时使用
			adapter.setmNineSetSetting(revicePosition, time + hexTime);
			for (int i = 0; i < 9; i++) {
				mNineSetSetting.put(i, "none");
			}
			strings.clear();
			strings.add((time + hexTime) + "");
			for (int i = 0; i < mNineSetSetting.size(); i++) {
				strings.add("0000000000");
			}
			mNineSetSetting.put(revicePosition, time + hexTime);
			// 把这个item对应地设置成新的数据，然后更新一下显示
			// makeNewSetFromMap();
			adapter.sendOrderToConfirmSetting();
			// receiveThread = new ReceiveThread();
			// receiveThread.start();
			udpKind = 1;// 正在设置

			tvHeater.setBackgroundResource(R.drawable.tablerow);
			tvFishTank.setBackgroundResource(R.drawable.tablerow);
			tvNightLight
					.setBackgroundResource(R.drawable.selector_btn_radius_gradia_2);
			tvMosquitoRepellentIncense
					.setBackgroundResource(R.drawable.tablerow);
			tvHeaterQ.setBackgroundResource(R.drawable.tablerow);
			tvParentModel.setBackgroundResource(R.drawable.tablerow);
			tvNightLight.setTextColor(R.color.white);
			tvHeater.setTextColor((R.color.black));
			tvFishTank.setTextColor(R.color.black);
			tvMosquitoRepellentIncense.setTextColor(R.color.black);
			tvHeaterQ.setTextColor(R.color.black);
			tvParentModel.setTextColor(R.color.black);
		} else if (v == tvMosquitoRepellentIncense) {
			h.postDelayed(new Runnable() {
				public void run() {
					progressGettingDataShow(getString(R.string.setting));
				}
			}, 500);
			isSet = false;
			refreshing = false;
			mNineSetSetting.clear();
			adapter.clearSettingMap();// 清空数据
			String time = "20000060";
			hexTime = hexTime.toUpperCase();
			if (hexTime.length() == 1) {// 如果16进制只有1位，需要往前补上0
				hexTime = "0" + hexTime;
			}
			// 把设置时间放在adapter的mNineSetSetting里存起来，用于发送指令时使用
			adapter.setmNineSetSetting(revicePosition, time + hexTime);
			for (int i = 0; i < 9; i++) {
				mNineSetSetting.put(i, "none");
			}
			strings.clear();
			strings.add((time + hexTime) + "");
			for (int i = 0; i < mNineSetSetting.size(); i++) {
				strings.add("0000000000");
			}
			mNineSetSetting.put(revicePosition, time + hexTime);
			// 把这个item对应地设置成新的数据，然后更新一下显示
			// makeNewSetFromMap();
			adapter.sendOrderToConfirmSetting();
			// receiveThread = new ReceiveThread();
			// receiveThread.start();
			udpKind = 1;// 正在设置

			tvHeater.setBackgroundResource(R.drawable.tablerow);
			tvFishTank.setBackgroundResource(R.drawable.tablerow);
			tvNightLight.setBackgroundResource(R.drawable.tablerow);
			tvMosquitoRepellentIncense
					.setBackgroundResource(R.drawable.selector_btn_radius_gradia_2);
			tvHeaterQ.setBackgroundResource(R.drawable.tablerow);
			tvParentModel.setBackgroundResource(R.drawable.tablerow);
			tvMosquitoRepellentIncense.setTextColor(R.color.white);
			tvNightLight.setTextColor(R.color.black);
			tvHeater.setTextColor((R.color.black));
			tvFishTank.setTextColor(R.color.black);
			tvHeaterQ.setTextColor(R.color.black);
			tvParentModel.setTextColor(R.color.black);
		} else if (v == tvHeaterQ) {
			h.postDelayed(new Runnable() {
				public void run() {
					progressGettingDataShow(getString(R.string.setting));
				}
			}, 500);
			isSet = false;
			refreshing = false;
			mNineSetSetting.clear();
			adapter.clearSettingMap();// 清空数据
			String time = "18000600";
			hexTime = hexTime.toUpperCase();
			if (hexTime.length() == 1) {// 如果16进制只有1位，需要往前补上0
				hexTime = "0" + hexTime;
			}
			// 把设置时间放在adapter的mNineSetSetting里存起来，用于发送指令时使用
			adapter.setmNineSetSetting(revicePosition, time + hexTime);
			for (int i = 0; i < 9; i++) {
				mNineSetSetting.put(i, "none");
			}
			strings.clear();
			strings.add((time + hexTime) + "");
			for (int i = 0; i < mNineSetSetting.size(); i++) {
				strings.add("0000000000");
			}
			mNineSetSetting.put(revicePosition, time + hexTime);
			// 把这个item对应地设置成新的数据，然后更新一下显示
			// makeNewSetFromMap();
			adapter.sendOrderToConfirmSetting();
			// receiveThread = new ReceiveThread();
			// receiveThread.start();
			udpKind = 1;// 正在设置

			tvHeater.setBackgroundResource(R.drawable.tablerow);
			tvFishTank.setBackgroundResource(R.drawable.tablerow);
			tvNightLight.setBackgroundResource(R.drawable.tablerow);
			tvMosquitoRepellentIncense
					.setBackgroundResource(R.drawable.tablerow);
			tvHeaterQ
					.setBackgroundResource(R.drawable.selector_btn_radius_gradia_2);
			tvParentModel.setBackgroundResource(R.drawable.tablerow);
			tvHeaterQ.setTextColor(R.color.white);
			tvMosquitoRepellentIncense.setTextColor(R.color.black);
			tvNightLight.setTextColor(R.color.black);
			tvHeater.setTextColor((R.color.black));
			tvFishTank.setTextColor(R.color.black);
			tvParentModel.setTextColor(R.color.black);
		} else if (v == tvParentModel) {
			h.postDelayed(new Runnable() {
				public void run() {
					progressGettingDataShow(getString(R.string.setting));
				}
			}, 500);
			isSet = false;
			refreshing = false;
			mNineSetSetting.clear();
			adapter.clearSettingMap();// 清空数据
			String time = "17002000";
			hexTime = hexTime.toUpperCase();
			if (hexTime.length() == 1) {// 如果16进制只有1位，需要往前补上0
				hexTime = "0" + hexTime;
			}
			// 把设置时间放在adapter的mNineSetSetting里存起来，用于发送指令时使用
			adapter.setmNineSetSetting(revicePosition, time + hexTime);
			for (int i = 0; i < 9; i++) {
				mNineSetSetting.put(i, "none");
			}
			strings.clear();
			strings.add((time + hexTime) + "");
			for (int i = 0; i < mNineSetSetting.size(); i++) {
				strings.add("0000000000");
			}
			mNineSetSetting.put(revicePosition, time + hexTime);
			// 把这个item对应地设置成新的数据，然后更新一下显示
			// makeNewSetFromMap();

			adapter.sendOrderToConfirmSetting();
			// receiveThread = new ReceiveThread();
			// receiveThread.start();
			udpKind = 1;// 正在设置

			tvHeater.setBackgroundResource(R.drawable.tablerow);
			tvFishTank.setBackgroundResource(R.drawable.tablerow);
			tvNightLight.setBackgroundResource(R.drawable.tablerow);
			tvMosquitoRepellentIncense
					.setBackgroundResource(R.drawable.tablerow);
			tvHeaterQ.setBackgroundResource(R.drawable.tablerow);
			tvParentModel
					.setBackgroundResource(R.drawable.selector_btn_radius_gradia_2);
			tvParentModel.setTextColor(R.color.white);
			tvHeaterQ.setTextColor(R.color.black);
			tvMosquitoRepellentIncense.setTextColor(R.color.black);
			tvNightLight.setTextColor(R.color.black);
			tvHeater.setTextColor((R.color.black));
			tvFishTank.setTextColor(R.color.black);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/***************************************************/
		// 选择完时间，点击确定按钮后，返回到这里
		// 发送指令设置新的时间
		if (resultCode == RESULT_OK) {
			// 收到设置数据
			String time = data.getExtras().getString(
					Constant.DATA_NINE_SET_SET_RESULT_EXTRA);// 返回的时间
			revicePosition = data.getExtras().getInt(
					Constant.POSITION_NINE_SET_SETTING_ITEM);// 这个Item的位置
			LogUtilNIU.value("revicePosition--->" + revicePosition);
			// 判断该item 发送设置9组定时的指令,其他数据不变，只修改该item
			LogUtilNIU.value("9组界面activityResult收到数据了time----" + time);
			// 设置该position的Item为待确认字体

			// 把设置时间放在adapter的mNineSetSetting里存起来，用于发送指令时使用
			adapter.setmNineSetSetting(revicePosition, time);
			// 把新设置的时间放到集合里备用，用于发指令设置。
			mNineSetSetting.put(revicePosition, time);
			// 把这个item对应地设置成新的数据，然后更新一下显示
			makeNewSetFromMap();
			adapter.notifyDataSetChanged();
			// 页面的修改区域和按钮出现
			llBottomRev.setVisibility(View.VISIBLE);
			menuTv.setVisibility(View.GONE);
		}
	}

	/**********************************************************/
	/**
	 * 取消注册广播
	 */
	@Override
	protected void onDestroy() {
		BApplication.instance.setNineSetWaitConfirm(false);
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("9组时段设置"); // 统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this); // 统计时长
	}

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
					} else if (code == 2) {
						msg.what = SOCKET_TOKEN_TIME_OUT;
					} else {
						msg.what = Constant.MSG_FAILURE;
					}
				}
			} catch (SocketTimeoutException e) {
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

	public class Data9BrocastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Constant.BROCAST_GET_9TIME)) {
				progressDismiss("");
				// 开关操作
				String message = intent.getStringExtra("message");
				showContent(message);
			} else if (action.equals(Constant.BROCAST_FAILE_MSG)) {
				progressGettingDataDismissNoToast();
				String message = intent.getStringExtra("message");
				ToastUtils.longToast(Menu9ONOFFActivity.this, message);
			} else if (action.equals(Constant.BROCAST_SET_9TIME)) {
				progressDismiss("");
				String message = intent.getStringExtra("message");
				showContent(message);
			}else if(action.equals(Constant.BROCAST_SOCKET_FAIL)){
				ToastUtils.longToast(Menu9ONOFFActivity.this, getString(R.string.socket_disconnected));
			} else if(action.equals(Constant.BROCAST_TIME_OUT)){
				ToastUtils.longToast(Menu9ONOFFActivity.this, getString(R.string.exception_sockettimeout));
			}
		}
	}
}
