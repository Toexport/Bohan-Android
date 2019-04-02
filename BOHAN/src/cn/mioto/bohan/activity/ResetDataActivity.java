package cn.mioto.bohan.activity;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.ExceptionUtil;
import cn.mioto.bohan.utils.HttpUtils;
import cn.mioto.bohan.utils.LoadDataThreadUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;
import cn.mioto.bohan.view.pop.DropDownMenu;
import cn.mioto.bohan.view.pop.DropDownMenuTextView;
import cn.mioto.bohan.view.pop.OnMenuSelectedListener;

import com.mioto.bohan.webservice.WebServiceClient;
import com.umeng.analytics.MobclickAgent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import steed.framework.android.client.JsonClientHandler2;

/**
 * 类说明：重新设置设备信息
 */
public class ResetDataActivity extends BaseCheckDataMenuActivity {
	/********** DECLARES *************/
	private EditText etDeviceName;
	private TextView tvDeviceId;
	private LinearLayout llLocationSet;
	private LinearLayout llBrandSet;
	private LinearLayout llRemindSmarkLinkSpace;
	private TextView tvCheck;
	private TextView tvBrandName;
	private TextView tvSave;
	private TextView tvSmartLink;
	private TextView tvNoLink;
	private LinearLayout refresh1;
	private LinearLayout refresh2;
	private LinearLayout refresh3;
	private LinearLayout llRemindSmarkLink;
	private SingleDevice singledevice;
	private TextView tvLocation;
	private DropDownMenu locationDropMenu;
	private DropDownMenu brandDropMenu;
	private DropDownMenuTextView typeDropMenu;
	private EditText mEtLocationInput;
	private EditText mEtBrandInput;
	private int lo_index;
	/************* 位置菜单属性 **************************************/
	private String oldLocation;
	private String oldBrand;
	private String oldAppType;
	String[] locations;
	/*********** 品牌菜单属性 ****************************************/
	String[] brands;
	String[] apptypes;
	/***************************************************/
	private String deviceLocation;// 初始化，在没有选择任何下拉的时候自动选择
	private String deviceBrand;
	/***************************************************/
	private BroadcastReceiver receiver;

	public static Socket socket;
	public static BufferedReader in;
	public static PrintWriter out;
	public ReceiveThread receiveThread;
	
	private static final int TOKEN_TIME_OUT = 12,
			TOKEN_TIME_OUT_NO_DIALOG = 13,SOCKET_TOKEN_TIME_OUT = 14;
	
	
	private static final int TYPE_SUCCESS = 10,LOCATION_SUCCESS = 11,TYPE_FAIL = 12,LOCATION_FAIL = 13,BRAND_SUCCESS = 14,BRAND_FAIL = 15,MODIFY_SUCCESS = 16 ,MODIFY_FAILE = 17;

	private Handler h = new Handler() {// 另一个hanlder
		public void handleMessage(Message msg) {
			if (msg.what == Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK) {
				// 加载到数据后正在加载消失
			} else if (msg.what == Constant.MSG_WHAT_SHOW_DIALOG_OF_RESEND_TASK) {
				// 显示正在加载
				progressGettingDataShow(getString(R.string.device_status_query));
			} else if (msg.what == Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK_FAIL) {
				// 发送几次后，加载失败
				progressGettingDataDismiss(getString(R.string.device_status_query_failed));
			} else if (msg.what == Constant.MSG_WHAT_SEND_MSG_BY_SERVICE_RESEND_TASK_FAIL) {
				// 通过服务器去查询信息
				// 通过服务器查询设备实时数据
				checkByService((String) msg.obj);
			} else if(msg.what == Constant.MSG_SUCCESS){
				String res = (String) msg.obj;
				showContent(res);
			}else if(msg.what == SOCKET_TOKEN_TIME_OUT){
				progressGettingDataDismissNoToast();
				ToastUtils.longToast(ResetDataActivity.this,getString(R.string.login_expired));
				BApplication.instance.clearThisUserFlashDatasOfApplication();// 清空application此用户的共用数据
				finish();
				Intent intent = new Intent(getApplicationContext(),
						LoginActivity.class);
				startActivity(intent);
			} else if(msg.what == Constant.MSG_EXCPTION){
				progressGettingDataDismissNoToast();
				ToastUtils.longToast(ResetDataActivity.this,getString(R.string.connection_timeout));
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title,
				R.string.resetDeviceInfo, true, false, 0, 0);
		// getEditText();//获得自定义下拉菜单的两个输入框
		/***************************************************/
		// 初始化表单信息
		// 这些信息都是从application保存的
		tvDeviceId.setText(deviceId);
		etDeviceName.setText(((BApplication) getApplication())
				.getCurrentDevice().getDeviceName());
		oldLocation = ((BApplication) getApplication()).getCurrentDevice()
				.getDeviceLocation();
		oldBrand = ((BApplication) getApplication()).getCurrentDevice()
				.getDeviceBrand();
		oldAppType = ((BApplication) getApplication()).getCurrentDevice()
				.getDeviceType();
		// 初始化Socket
		socket = BApplication.instance.getSocket();
		/***************************************************/
		// 从服务器获得下来列表的信息
		getLocations();
		getBrands();
		getDeviceTypes();
		/***************************************************/
		// 注册广播接收者
		receiver = new UDPBrocastReceiver();// 注册广播
		registerReceiver(receiver, filter);
		/***************************************************/
		// 设置按钮状态
		// 发送UDP指令查看该设备是否在线
		// 查询设备是否在线，不在线为不可点击 以下代码要改UDP查询
		checkDeviceOnline();
		if (null != ((BApplication) getApplication()).getCurrentDevice()
				.getDeviceIp()) {
			tvCheck.setEnabled(true);
			tvCheck.setBackgroundColor(R.drawable.bg_round_tv_button_blue);
		}
	}

	public class UDPBrocastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 在线列表查询设备继电器状态，如果在局域网内有查询到数据，代表在家
			BApplication.instance.setIsAtHome(true);
			// 区分接收到的是哪种广播
			String action = intent.getAction();
			if (action.equals(Constant.SOCKET_BROCAST_ONRECEIVED)) {
				String message = intent
						.getStringExtra(Constant.SOCKET_BROCAST_EXTRA_MESSAGE);
//				showContent(message);
			}
		}
	}

	/**
	 * 
	 * @Description:查询设备是否在线，让按钮生效
	 */
	private void checkDeviceOnline() {
		// 发送查询设备继电器指令
//		String verCode = ModbusCalUtil.verNumber(deviceId + "00020000");
//		String msg = "3A" + deviceId + "00020000" + verCode + "0D";// 查询继电器开关状态
//		new LoadDataThreadUtil(msg, h, deviceDBssid, this).start();
		String verCode = ModbusCalUtil.verNumber(deviceId + "10030012");
		final String message = "3A" + "10030012" + deviceId
				+ "0D";// 0001
		try {
			BApplication.instance.socketSend(message);
		} catch (Exception e) {
			e.printStackTrace();
			h.sendEmptyMessage(Constant.MSG_EXCPTION);
		};
		receiveThread = new ReceiveThread();
		receiveThread.start();
	}

	public void showContent(String message) {
//		if (checkUDPMessage(message)) {
//			if (isReqCodeEqual(message, "0002")) {// 返回了继电器的结果
//				progressGettingDataDismissNoToast();
//				BApplication.instance.setResendTaskShowBreak(true);
//				tvCheck.setEnabled(true);
//				tvCheck.setBackground(getResources().getDrawable(
//						R.drawable.bg_round_tv_button_blue));
//			}
//		} else if (message.equals("")) {
//			BApplication.instance.setResendTaskShowBreak(true);
//			progressGettingDataDismiss("没有设备在线");
//			tvCheck.setEnabled(false);
//		}
		if(!TextUtils.isEmpty(message)){
			if(message.equals("在线")){
				progressGettingDataDismissNoToast();
				BApplication.instance.setResendTaskShowBreak(true);
				tvCheck.setEnabled(true);
				tvCheck.setBackground(getResources().getDrawable(
						R.drawable.bg_round_tv_button_blue));
			}else{
				BApplication.instance.setResendTaskShowBreak(true);
				progressGettingDataDismiss(getString(R.string.all_device_offline));
				tvCheck.setEnabled(false);
			}
		}else{
			BApplication.instance.setResendTaskShowBreak(true);
			progressGettingDataDismiss(getString(R.string.all_device_offline));
			tvCheck.setEnabled(false);
		}
		
	}

	private void getDeviceTypes() {
		String ty = BApplication.instance.getSp().getString(
				Constant.SP_KEY_TYPES, "no");
		if (!ty.equals("no")) {// 如果有数据
			String[] br = ty.split(",");
			apptypes = new String[br.length];
			apptypes = br;
			LogUtilNIU.value("赋值到的brands--->" + Arrays.toString(brands));
			LogUtilNIU.value("有数据，已经赋值");
			refresh3.setVisibility(View.GONE);
			initTypeDropMenu();
		} else {
			getTypes();
//			new Enterface("getApplicanceSorts.act")
//					.doRequest(new JsonClientHandler2() {
//						@Override
//						public void onInterfaceSuccess(String message,
//								String contentJson) {
//							LogUtilNIU.e("返回 types 的content--->" + contentJson);
//							try {
//								JSONArray arry = new JSONArray(contentJson);
//								apptypes = new String[arry.length()];
//								for (int i = 0; i < arry.length(); i++) {
//									apptypes[i] = arry.getJSONObject(i)
//											.getString("name");
//								}
//								LogUtilNIU.e("types--->"
//										+ Arrays.toString(apptypes));
//							} catch (JSONException e) {
//								ExceptionUtil.handleException(e);
//							}
//							if (apptypes.length < 1) {
//								apptypes = new String[] { "没有数据" };
//							}
//							refresh3.setVisibility(View.GONE);
//							initTypeDropMenu();
//						}
//
//						@Override
//						public void onInterfaceFail(String json) {
//							// 接口statusCode不为0时回调该方法,
//							// 这行代码toast提示接口返回的message,你可以在这里写其它逻辑.
//							LogUtilNIU.interfaceWrong("失败--->" + json);
//							super.onInterfaceFail(json);
//							brands = new String[] { "没有数据" };
//							initTypeDropMenu();
//						}
//
//						@Override
//						public void onFailureConnected(Boolean canConnect) {
//
//						}
//					});
		}
	}

	private void initTypeDropMenu() {
		typeDropMenu.setmMenuCount(1);
		typeDropMenu.setmShowCount(5);
		typeDropMenu.setShowCheck(true);
		typeDropMenu.setmMenuTitleTextSize(14);
		typeDropMenu.setmMenuTitleTextColor(Color.parseColor("#0e7ef1"));
		typeDropMenu.setmMenuListTextSize(14);
		typeDropMenu.setmMenuListTextColor(Color.BLACK);
		typeDropMenu.setmMenuPressedBackColor(Color.WHITE);
		typeDropMenu.setmMenuPressedTitleTextColor(Color.BLACK);
		typeDropMenu.setmCheckIcon(R.drawable.ico_make);
		typeDropMenu.setmUpArrow(R.drawable.arrow_up);
		typeDropMenu.setmDownArrow(R.drawable.arrow_down);
		String[] brandstrings = new String[] { oldAppType };// 设为查询到的第一个数据
		typeDropMenu.setDefaultMenuTitle(brandstrings);
		typeDropMenu.setmMenuListBackColor(getResources().getColor(
				R.color.white));
		typeDropMenu.setmMenuListSelectorRes(R.color.white);
		typeDropMenu.setmMenuBackColor(getResources().getColor(R.color.white));// 展开list的背景色
		typeDropMenu.setmMenuListSelectorRes(R.color.white);// 展开list的listselector
		typeDropMenu.setmArrowMarginTitle(20);// Menu上箭头图标距title的margin
		typeDropMenu.setmMenuPressedTitleTextColor(Color.BLACK);// Menu按下状态的title文字颜色

		typeDropMenu.setMenuSelectedListener(new OnMenuSelectedListener() {
			@Override
			public void onSelected(View listview, int RowIndex, int ColumnIndex) {

			}
		});
		List<String[]> items = new ArrayList<>();
		items.add(apptypes);
		typeDropMenu.setmMenuItems(items);
		typeDropMenu.setIsDebug(false);
	}

	private void initBrandDropMenu() {
		brandDropMenu.setmMenuCount(1);
		brandDropMenu.setmShowCount(5);
		brandDropMenu.setShowCheck(true);
		brandDropMenu.setmMenuTitleTextSize(14);
		brandDropMenu.setmMenuTitleTextColor(Color.parseColor("#0e7ef1"));
		brandDropMenu.setmMenuListTextSize(14);
		brandDropMenu.setmMenuListTextColor(Color.BLACK);
		brandDropMenu.setmMenuPressedBackColor(Color.WHITE);
		brandDropMenu.setmMenuPressedTitleTextColor(Color.BLACK);
		brandDropMenu.setmCheckIcon(R.drawable.ico_make);
		brandDropMenu.setmUpArrow(R.drawable.arrow_up);
		brandDropMenu.setmDownArrow(R.drawable.arrow_down);
		final String[] brandstrings = new String[] { oldBrand };// 输入框初始化显示的内容
		brandDropMenu.setDefaultMenuTitle(brandstrings);// 默认标题
		brandDropMenu.setmMenuListBackColor(getResources().getColor(
				R.color.white));
		brandDropMenu.setmMenuListSelectorRes(R.color.white);
		brandDropMenu.setmMenuBackColor(getResources().getColor(R.color.white));// 展开list的背景色
		brandDropMenu.setmMenuListSelectorRes(R.color.white);// 展开list的listselector
		brandDropMenu.setmArrowMarginTitle(20);// Menu上箭头图标距title的margin
		brandDropMenu.setmMenuPressedTitleTextColor(Color.BLACK);// Menu按下状态的title文字颜色
		brandDropMenu.setMenuSelectedListener(new OnMenuSelectedListener() {
			@Override
			public void onSelected(View listview, int RowIndex, int ColumnIndex) {
				// deviceBrand = brands[RowIndex];
			}
		});
		List<String[]> items = new ArrayList<>();
		items.add(brands);
		brandDropMenu.setmMenuItems(items);
		brandDropMenu.setIsDebug(false);
	}

	private void initLocationDropMenu() {
		locationDropMenu.setmMenuCount(1);
		locationDropMenu.setmShowCount(6);
		locationDropMenu.setShowCheck(true);
		locationDropMenu.setmMenuTitleTextSize(14);
		locationDropMenu.setmMenuTitleTextColor(Color.parseColor("#0e7ef1"));
		locationDropMenu.setmMenuListTextSize(14);
		locationDropMenu.setmMenuListTextColor(getResources().getColor(
				R.color.middleGray));
		locationDropMenu.setmMenuPressedBackColor(Color.WHITE);
		locationDropMenu.setmMenuPressedTitleTextColor(Color.BLACK);
		locationDropMenu.setmCheckIcon(R.drawable.ico_make);
		locationDropMenu.setmUpArrow(R.drawable.arrow_up);
		locationDropMenu.setmDownArrow(R.drawable.arrow_down);
		String[] lostrings = new String[] { oldLocation };
		locationDropMenu.setDefaultMenuTitle(lostrings);
		locationDropMenu.setmMenuListBackColor(getResources().getColor(
				R.color.white));
		locationDropMenu.setmMenuListSelectorRes(R.color.white);
		locationDropMenu.setmMenuBackColor(getResources().getColor(
				R.color.white));// 展开list的背景色
		locationDropMenu.setmMenuListSelectorRes(R.color.white);// 展开list的listselector
		locationDropMenu.setmArrowMarginTitle(20);// Menu上箭头图标距title的margin
		locationDropMenu.setmMenuPressedTitleTextColor(Color.BLACK);// Menu按下状态的title文字颜色
		locationDropMenu.setMenuSelectedListener(new OnMenuSelectedListener() {
			@Override
			public void onSelected(View listview, int RowIndex, int ColumnIndex) {
				// deviceLocation =
				// locations[RowIndex];//为所选的那个，这里需要做判断，现在需要加个功能，手动输入
			}
		});
		List<String[]> items = new ArrayList<>();
		items.add(locations);
		locationDropMenu.setmMenuItems(items);
		locationDropMenu.setIsDebug(false);
	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_reset_data;
	}

	@Override
	public void onClick(View v) {
		if (v == tvSave) {
			/***************************************************/
			// 保存设备信息
			String deviceName = etDeviceName.getText().toString().trim();
			// 向服务器保存数据
			saveDeviceInfo(deviceId, deviceName,
					locationDropMenu.getEditTextContent(),
					brandDropMenu.getEditTextContent(),
					typeDropMenu.getEditTextContent());
		} else if (v == tvCheck) {// 进入查询设备信息页面
			// 传递设备ID值
			Intent intent = new Intent(ResetDataActivity.this,
					MenuDeviceInfoMenuActivity.class);
			startActivity(intent);
		} else if (v == tvSmartLink) {// 进入智能连接的界面,把设备表地址传到智能连接界面
			Intent intent = new Intent(this, SmartLinkActivity.class);// 智能连接
			intent.putExtra(Constant.SERIAL_NUMBER_INTENT_KEY, deviceId);
			startActivity(intent);
			finish();
		}
	}

	/**********************************************************/
	/**
	 * 通过网络保存设备信息
	 */
	private void saveDeviceInfo(final String id, final String name,
			final String position, final String brand, final String apptype) {
//		new Enterface("setDeviceInfo.act")
//				.addParam("device.id", id)
//				.addParam("device.name", name)
//				.addParam("device.position", position)
//				// .addParam("device.sort", type)
//				.addParam("device.brand", brand)
//				.addParam("device.appliancesort", apptype)
//				.doRequest(new JsonClientHandler2() {
//					@Override
//					public void onInterfaceSuccess(String message,
//							String contentJson) {
//						steed.framework.android.util.LogUtil
//								.d("返回 的content--->" + contentJson);
//						/*
//						 * 服务器保存设备信息成功后，也暂时把设备信息也保存到本地
//						 */
//						LogUtilNIU.e("重新设置的设备位置为" + position + "重新设置的设备品牌为"
//								+ brand);
//						ToastUtils.shortToast(ResetDataActivity.this,
//								"设备信息重设成功");
//						BApplication.instance.setDeviceDataHasChanged(true);
//						// singledevice=BApplication.getItemByItemID(id);
//						// singledevice.setDeviceName(name);
//						// singledevice.setDeviceLocation(position);
//						// singledevice.setDeviceBrand(brand);
//						// singledevice.setDeviceType(deviceType);
//					}
//
//					@Override
//					public void onInterfaceFail(String json) {
//						super.onInterfaceFail(json);
//					}
//
//					@Override
//					public void onFailureConnected(Boolean canConnect) {
//
//					}
//				});
		
		if (NetworkUtils.isNetworkConnected(this)) {
			progressDialogSaveShow();
			new Thread() {
				@SuppressWarnings("unchecked")
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						HttpUtils.TOKIN = "";
						Map map = new HashMap();
						map.put("DeviceCode", id);
						map.put("PosName", position);
						map.put("LoadName", name);
						map.put("LoadBrand",brand );
						String result = WebServiceClient.CallWebService("ModifyDeviceInfo",map,ResetDataActivity.this);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								msg.what = MODIFY_SUCCESS;
							} else {
								msg.obj = obj.getString("message");
								msg.what = MODIFY_FAILE;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						msg.obj = e;
						msg.what = Constant.MSG_EXCPTION;
					}
					// if (isRequesting) {
					defalutHandler.sendMessage(msg);
					// }
				}
			}.start();
		} else {
			defalutHandler.sendEmptyMessage(Constant.MSG_NETWORK_ERROR);
		}
		
	}

	/**********************************************************/
	/**
	 * 获取位置列表 string数组形式
	 */
	public void getLocations() {
		String location = BApplication.instance.getSp().getString(
				Constant.SP_KEY_LOCATIONS, "no");
		 BApplication.instance.getSp().edit().clear().commit();
		if (!location.equals("no")) {// 如果有数据
			String[] lo = location.split(",");
			locations = new String[lo.length];
			locations = lo;
			LogUtilNIU.value("有数据，已经赋值");
			LogUtilNIU.value("赋值到的Location--->" + Arrays.toString(locations));
			refresh1.setVisibility(View.GONE);
			initLocationDropMenu();
		} else {
			LogUtilNIU.value("获取不到位置数据");
			getLocationsService();
//			new Enterface("getPositions.act")
//					.doRequest(new JsonClientHandler2() {
//						@Override
//						public void onInterfaceSuccess(String message,
//								String contentJson) {
//							LogUtilNIU.e("返回 locations的content--->"
//									+ contentJson);
//							try {
//								JSONArray arry = new JSONArray(contentJson);
//								locations = new String[arry.length()];
//								for (int i = 0; i < arry.length(); i++) {
//									locations[i] = arry.getJSONObject(i)
//											.getString("name");
//								}
//								LogUtilNIU.e("获取到的Location--->"
//										+ Arrays.toString(locations));
//							} catch (JSONException e) {
//								ExceptionUtil.handleException(e);
//							}
//							if (locations.length < 0) {
//								locations = new String[] { "客厅", "厨房", "卧室",
//										"洗手间", "其他" };
//							}
//							refresh1.setVisibility(View.GONE);
//							initLocationDropMenu();
//						}
//
//						@Override
//						public void onInterfaceFail(String json) {
//							// 接口statusCode不为0时回调该方法,
//							// 这行代码toast提示接口返回的message,你可以在这里写其它逻辑.
//							locations = new String[] { "客厅", "厨房", "卧室", "洗手间",
//									"其他" };
//							initLocationDropMenu();
//							super.onInterfaceFail(json);
//						}
//
//						@Override
//						public void onFailureConnected(Boolean canConnect) {
//
//						}
//					});
		}

	}

	/**********************************************************/
	/**
	 * 获取品牌列表 string数组形式
	 */
	public void getBrands() {
		String b = BApplication.instance.getSp().getString(
				Constant.SP_KEY_LOCATIONS, "no");
		if (!b.equals("no")) {// 如果有数据
			String[] br = b.split(",");
			brands = new String[br.length];
			brands = br;
			LogUtilNIU.value("赋值到的brands--->" + Arrays.toString(brands));
			LogUtilNIU.value("有数据，已经赋值");
			refresh2.setVisibility(View.GONE);
			initBrandDropMenu();
		} else {
			LogUtilNIU.value("没有有数据，重新获取");
			getBrand();
//			new Enterface("getBrands.act").doRequest(new JsonClientHandler2() {
//				@Override
//				public void onInterfaceSuccess(String message,
//						String contentJson) {
//					LogUtilNIU.e("返回 brands 的content--->" + contentJson);
//					// [{"id":1,"name":"345"},{"id":2,"name":"4356"}]
//					try {
//						JSONArray arry = new JSONArray(contentJson);
//						brands = new String[arry.length()];
//						for (int i = 0; i < arry.length(); i++) {
//							brands[i] = arry.getJSONObject(i).getString("name");
//						}
//						LogUtilNIU.e("brands--->" + Arrays.toString(brands));
//					} catch (JSONException e) {
//						ExceptionUtil.handleException(e);
//					}
//					if (brands.length < 1) {
//						brands = new String[] { "格力", "美的", "海尔", "松下", "西门子",
//								"索尼", "日立", "其他" };
//					}
//					refresh2.setVisibility(View.GONE);
//					initBrandDropMenu();
//				}
//
//				@Override
//				public void onInterfaceFail(String json) {
//					// 接口statusCode不为0时回调该方法,
//					// 这行代码toast提示接口返回的message,你可以在这里写其它逻辑.
//					LogUtilNIU.interfaceWrong("失败--->" + json);
//					super.onInterfaceFail(json);
//					brands = new String[] { "格力", "美的", "海尔", "松下", "西门子",
//							"索尼", "日立", "其他" };
//					initBrandDropMenu();
//				}
//
//				@Override
//				public void onFailureConnected(Boolean canConnect) {
//
//				}
//			});
		}
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("重新设置设备信息页"); // 统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this); // 统计时长
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("重新设置设备信息页"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证
												// onPageEnd 在onPause 之前调用,因为
												// onPause
												// 中会保存信息。"SplashScreen"为页面名称，可自定义
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
	protected String onServiceUDPBack(String content) {
		LogUtilNIU.value("服务器回调，子类也收到回调--->" + content);
//		showContent(content);
		return null;
	}

	@Override
	protected String onCheckByServiceOnInterfaceFail(String json) {
		LogUtilNIU.value("*****失败接口回调********" + json);
		try {
			JSONObject jsonObject = new JSONObject(json);
			if (jsonObject.getInt("statusCode") == 1) {
				LogUtilNIU.value("statusCode是1");
				BApplication.instance.setResendTaskShowBreak(true);// 停止重发机制
				progressGettingDataDismissNoToast();
				ToastUtils.shortToast(ResetDataActivity.this,
						jsonObject.getString("message"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
			if (result != null) {
				try {
					result = BApplication.instance.readString();
					Log.d("Receive", "socket receive:" + result);
					if (result != null) {
						JSONObject jsonObject = new JSONObject(result);
						int code = jsonObject.getInt("statusCode");
						if (code == 0) {
							msg.obj = jsonObject.getString("message");
							msg.what = Constant.MSG_SUCCESS;
						} else if(code == 2){
							msg.what = SOCKET_TOKEN_TIME_OUT;
						}else {
							msg.what = Constant.MSG_EXCPTION;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					msg.obj = e;
					msg.what = Constant.MSG_EXCPTION;
				}
			} else {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					msg.obj = e;
					msg.what = Constant.MSG_EXCPTION;
				}
			}
			h.sendMessage(msg);
		}
	}
	
	
	//从服务器获取位置
	private void getLocationsService() {
		if (NetworkUtils.isNetworkConnected(this)) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = LOCATION_FAIL;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						HttpUtils.TOKIN = "";
						String result = WebServiceClient.CallWebService("GetPosNameList",null,ResetDataActivity.this);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								locations = new String[]{};
								String rest = obj.getString("content");
								if (!TextUtils.isEmpty(rest)) {
									locations = rest.split(",");
								}
								msg.what = LOCATION_SUCCESS;
							} 
						}
					} catch (Exception e) {
						e.printStackTrace();
						msg.obj = e;
						msg.what = LOCATION_FAIL;
					}
					// if (isRequesting) {
					defalutHandler.sendMessage(msg);
					// }
				}
			}.start();
		} else {
			defalutHandler.sendEmptyMessage(Constant.MSG_NETWORK_ERROR);
		}
	}
	
	/**
	 * 从服务器获取名称
	 */
	private void getTypes() {
		if (NetworkUtils.isNetworkConnected(this)) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = TYPE_FAIL;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						HttpUtils.TOKIN = "";
						String result = WebServiceClient
								.CallWebService("GetLoadNameList",null,ResetDataActivity.this);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								apptypes = new String[]{};
								String res = obj.getString("content");
								if (!TextUtils.isEmpty(res)) {
									apptypes = res.split(",");
								}
								msg.what = TYPE_SUCCESS;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						msg.obj = e;
						msg.what = TYPE_FAIL;
					}
					// if (isRequesting) {
					defalutHandler.sendMessage(msg);
					// }
				}
			}.start();
		} else {
			defalutHandler.sendEmptyMessage(Constant.MSG_NETWORK_ERROR);
		}
	}
	
	/**
	 * 从服务器获取品牌
	 */
	private void getBrand() {
		if (NetworkUtils.isNetworkConnected(this)) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = BRAND_FAIL;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						HttpUtils.TOKIN = "";
						String result = WebServiceClient
								.CallWebService("GetLoadBrandList",null,ResetDataActivity.this);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								brands = new String[]{};
								String res = obj.getString("content");
								if (!TextUtils.isEmpty(res)) {
									brands = res.split(",");
								}
								msg.what = BRAND_SUCCESS;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						msg.obj = e;
						msg.what = BRAND_FAIL;
					}
					// if (isRequesting) {
					defalutHandler.sendMessage(msg);
					// }
				}
			}.start();
		} else {
			defalutHandler.sendEmptyMessage(Constant.MSG_NETWORK_ERROR);
		}
	}

		
		private Handler defalutHandler = new Handler() {
			public void handleMessage(Message msg) {
				progressGettingDataDismissNoToast();
				progressDialogDismiss();
				switch (msg.what) {
				case Constant.MSG_SUCCESS:
//					adapter.notifyDataSetChanged();
					break;
				case Constant.MSG_FAILURE:
					toast((String) msg.obj);
					break;
				case Constant.MSG_EXCPTION:
					toast(ExceptionManager.getErrorDesc(ResetDataActivity.this,
							(Exception) msg.obj));
					break;
				case Constant.MSG_TIME_OUT:
					toast(getString(R.string.connection_timeout));
					break;
				case Constant.MSG_NETWORK_ERROR:
					toast(getString(R.string.no_connection));
					break;
				case LOCATION_SUCCESS:
					refresh1.setVisibility(View.GONE);
					initLocationDropMenu();
					break;
				case TYPE_SUCCESS:
					refresh3.setVisibility(View.GONE);
					initTypeDropMenu();
					break;
				case TYPE_FAIL:
					initTypeDropMenu();
					break;
				case LOCATION_FAIL:
					initLocationDropMenu();
					break;
				case BRAND_SUCCESS:
					refresh2.setVisibility(View.GONE);
					initBrandDropMenu();
					break;
				case BRAND_FAIL:
					initBrandDropMenu();
					break;
				case MODIFY_SUCCESS:
					toast(getString(R.string.update_success));
					finish();
					break;
				case MODIFY_FAILE:
					toast((String) msg.obj);
					break;
				}
			}

			;
		};
		
		private void toast(String text) {
			Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		}
		
		
		public void progressDialogSaveShow() {
			p.setMessage(getString(R.string.saving));
			p.setCancelable(false);
			p.setCanceledOnTouchOutside(false);
			p.show();
		}

		public void progressDialogDismiss() {
			if(p != null){
				p.dismiss();
			}
		}
}
