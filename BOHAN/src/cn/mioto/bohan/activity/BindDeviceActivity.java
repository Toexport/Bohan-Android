package cn.mioto.bohan.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.mioto.bohan.webservice.WebServiceClient;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.utils.HttpUtils;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;
import cn.mioto.bohan.view.pop.DropDownMenu;
import cn.mioto.bohan.view.pop.DropDownMenuTextView;
import cn.mioto.bohan.view.pop.OnMenuSelectedListener;

/**
 * 绑定设备
 * 
 * @author liangl
 * 
 */

public class BindDeviceActivity extends BaseActivity implements OnClickListener {

	public static final int REQUEST_SCAN_ID = 10;
	public static final int REQUEST_SCAN_KEY = 11;
	private TextView menuFront;
	private EditText etKey;
	private ImageView ivScanKey;
	private TextView tvConfirm;
	private TextView menuTv;
	private boolean isRequesting = true;
	private ProgressDialog dialog;
	private LinearLayout refresh1;
	private LinearLayout refresh2;
	private LinearLayout refresh3;
	private DropDownMenu locationDropMenu;
	private DropDownMenu brandDropMenu;
	private DropDownMenu typeDropMenu;
	String[] locations = new String[] {};
	String[] brands = new String[] {};;
	String[] apptypes = new String[] {};;
	private static final int TYPE_SUCCESS = 10, LOCATION_SUCCESS = 11,
			TYPE_FAIL = 12, LOCATION_FAIL = 13, BRAND_SUCCESS = 14,
			BRAND_FAIL = 15, MODIFY_SUCCESS = 16;
	private static final int TOKEN_TIME_OUT = 20,
			TOKEN_TIME_OUT_NO_DIALOG = 21;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bind_device);
		ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title,
				R.string.band_device, true, true, R.id.menu_tv, R.string.album);
		initViews();
		setListener();
		// 从服务器获得下来列表的信息
		getLocations();
		getBrands();
		getDeviceTypes();
	}

	private void initViews() {
		menuFront = (TextView) findViewById(R.id.menu_front);
		etKey = (EditText) findViewById(R.id.etKey);
		ivScanKey = (ImageView) findViewById(R.id.ivScanKey);
		tvConfirm = (TextView) findViewById(R.id.tvConfirm);
		menuTv = (TextView) findViewById(R.id.menu_tv);
		menuTv.setVisibility(View.GONE);

		refresh1 = (LinearLayout) findViewById(R.id.refresh1);
		refresh2 = (LinearLayout) findViewById(R.id.refresh2);
		refresh3 = (LinearLayout) findViewById(R.id.refresh3);
		typeDropMenu = (DropDownMenu) findViewById(R.id.typeDropMenu);
		locationDropMenu = (DropDownMenu) findViewById(R.id.locationDropMenu);
		brandDropMenu = (DropDownMenu) findViewById(R.id.brandDropMenu);
	}

	private void setListener() {
		tvConfirm.setOnClickListener(this);
		ivScanKey.setOnClickListener(this);
		ivScanKey.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.menu_front:
			finish();
			break;
		case R.id.tvConfirm:
			dataView();
			break;
		case R.id.ivScanKey:
			intent = new Intent(this, ScanActivity.class);
			startActivityForResult(intent, REQUEST_SCAN_KEY);
			break;
		}
	}

	// 数据验证
	private void dataView() {
		String etKeyStr = etKey.getText().toString().trim();
		String etLocationStr = locationDropMenu.getEditTextContent().toString()
				.trim();
		String etTypeStr = typeDropMenu.getEditTextContent().toString().trim();
		String etBrandStr = brandDropMenu.getEditTextContent().toString()
				.trim();
		if (TextUtils.isEmpty(etKeyStr)) {
			toast(getString(R.string.key_not_null));
		} else if (etKeyStr.length() < 12) {
			toast(getString(R.string.please_input_error_code));
		} else if (TextUtils.isEmpty(etLocationStr)
				|| getString(R.string.please_select_device_location).equals(
						etLocationStr)) {
			toast(getString(R.string.location_not_null));
		} else if (TextUtils.isEmpty(etTypeStr)
				|| getString(R.string.please_dian_type).equals(etTypeStr)) {
			toast(getString(R.string.type_not_null));
		} else if (TextUtils.isEmpty(etBrandStr)
				|| getString(R.string.please_select_brand).equals(etBrandStr)) {
			toast(getString(R.string.brand_not_null));
		} else {
			String tempCode;
			String tempKey;
			if (etKeyStr.length() == 12) {
				tempCode = etKeyStr.substring(0, 12);
				tempKey = tempCode;
			} else {
				tempCode = etKeyStr.substring(0, 12);
				tempKey = etKeyStr.substring(12, etKeyStr.length());
			}
			bindDevice(tempCode, tempKey, etLocationStr, etTypeStr, etBrandStr);
		}
	}

	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_SCAN_KEY:
			if (resultCode == RESULT_OK) {
				if (data != null) {
					etKey.setText(data.getExtras().getString("result"));
					etKey.setSelection(etKey.getText().length());
				}
			}
			break;
		}
	}

	private void bindDevice(final String DeviceCode, final String DeviceKey,
			final String PosName, final String LoadName, final String LoadBrand) {
		if (NetworkUtils.isNetworkConnected(this)) {
			getDefaultProgressDialog(getString(R.string.binding), false);
			isRequesting = true;
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						Map map = new HashMap<>();
						map.put("DeviceCode", DeviceCode);
						map.put("DeviceKey", DeviceKey);
						map.put("PosName", PosName);
						map.put("LoadName", LoadName);
						map.put("LoadBrand", LoadBrand);
						String result = WebServiceClient.CallWebService(
								"BindingDevice", map, BindDeviceActivity.this);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								msg.obj = DeviceCode;
								msg.what = Constant.MSG_SUCCESS;
							} else if (statusCode == 2) {
								msg.what = TOKEN_TIME_OUT;
							} else {
								msg.obj = obj.getString("message");
								msg.what = Constant.MSG_FAILURE;
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						msg.obj = e;
						msg.what = Constant.MSG_EXCPTION;
					}
					if (isRequesting) {
						defalutHandler.sendMessage(msg);
					}
				}
			}.start();
		} else {
			defalutHandler.sendEmptyMessage(Constant.MSG_NETWORK_ERROR);
		}
	}

	private Handler defalutHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constant.MSG_SUCCESS:
				String etIDStr = (String) msg.obj;
				bindSuccess(etIDStr);
				break;
			case Constant.MSG_FAILURE:
				bindFailure();
				toast((String) msg.obj);
				break;
			case Constant.MSG_EXCPTION:
				bindFailure();
				toast(getString(R.string.bang_fail));
				break;
			case Constant.MSG_TIME_OUT:
				bindFailure();
				toast(getString(R.string.connection_timeout));
				break;
			case Constant.MSG_NETWORK_ERROR:
				bindFailure();
				toast(getString(R.string.no_connection));
				break;
			case LOCATION_SUCCESS:
				refresh1.setVisibility(View.GONE);
				if (null != locations) {
					initLocationDropMenu();
				}
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
				if (null != brands) {
					initBrandDropMenu();
				}
				break;
			case BRAND_FAIL:
				initBrandDropMenu();
				break;
			case TOKEN_TIME_OUT:
				ToastUtils.longToast(BindDeviceActivity.this,
						getString(R.string.login_expired));
				BApplication.instance.clearThisUserFlashDatasOfApplication();// 清空application此用户的共用数据
				finish();
				Intent intent = new Intent(getApplicationContext(),
						LoginActivity.class);
				startActivity(intent);
				break;
			case TOKEN_TIME_OUT_NO_DIALOG:
				// 不做处理
				break;
			}
		}

		;
	};

	private void bindSuccess(String etIDStr) {
		defalutHandler.removeCallbacks(defalutTimeout);
		dismissProgressDialog();
		// TODO绑定设备,绑定成功后直接跳转到智能连接页面连接wifi
		Intent intent = new Intent(this, SmartLinkActivity.class);
		SingleDevice device = new SingleDevice();
		device.setDeviceID(etIDStr);
		((BApplication) getApplication()).setCurrentDevice(device);
		startActivity(intent);
	}

	private void bindFailure() {
		defalutHandler.removeCallbacks(defalutTimeout);
		dismissProgressDialog();
		isRequesting = false;
	}

	private boolean dismissProgressDialog() {
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
			return true;
		}
		return false;
	}

	private void getDefaultProgressDialog(String msg, boolean cancelable) {
		dialog = new ProgressDialog(this);
		dialog.setMessage(msg);
		dialog.setCancelable(cancelable);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				defalutHandler.removeCallbacks(defalutTimeout);
				isRequesting = false;
			}

		});
		dialog.show();
	}

	private Runnable defalutTimeout = new Runnable() {
		@Override
		public void run() {
			defalutHandler.obtainMessage(Constant.MSG_TIME_OUT).sendToTarget();
		}
	};

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
		String[] brandstrings = null;
		if(apptypes.length>0){
			brandstrings = new String[] { apptypes[0] };
		}else{
			brandstrings = new String[] { };
		}
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
		String[] brandstrings = null;
		if(brands.length>0){
			brandstrings = new String[] { brands[0] };// 输入框初始化显示的内容
		}else{
			brandstrings = new String[] { };// 输入框初始化显示的内容
		}
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
		String[] lostrings = null;
		if(locations.length >0){
			lostrings = new String[] { locations[0] };
		}else{
			lostrings = new String[] { };
		}
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
		}
	}

	// 从服务器获取位置
	private void getLocationsService() {
		if (NetworkUtils.isNetworkConnected(this)) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = LOCATION_FAIL;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						HttpUtils.TOKIN = "";
						String result = WebServiceClient
								.CallWebService("GetPosNameList", null,
										BindDeviceActivity.this);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								locations = new String[1000];
								String rest = obj.getString("content");
								if (!TextUtils.isEmpty(rest)) {
									locations = rest.split(",");
								}
//								if (locations.length < 1) {
//									locations = new String[] { getString(R.string.not_data) };
//								}
								msg.what = LOCATION_SUCCESS;
							} else if (statusCode == 2) {
								msg.what = TOKEN_TIME_OUT;
							} else if (statusCode == 1) {
//								if (locations.length < 1) {
//									locations = new String[] { getString(R.string.not_data) };
//								}
								msg.what = LOCATION_SUCCESS;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						msg.obj = e;
						msg.what = LOCATION_FAIL;
//						if (locations.length < 1) {
//							locations = new String[] { getString(R.string.not_data) };
//						}
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
						String result = WebServiceClient.CallWebService(
								"GetLoadNameList", null,
								BindDeviceActivity.this);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								apptypes = new String[1000];
								String res = obj.getString("content");
								if (!TextUtils.isEmpty(res)) {
									apptypes = res.split(",");
								}
//								if (apptypes.length < 1) {
//									apptypes = new String[] {getString(R.string.not_data)};
//								}
								msg.what = TYPE_SUCCESS;
							} else if (statusCode == 1) {
//								if (apptypes.length < 1) {
//									apptypes = new String[] { getString(R.string.not_data) };
//								}
								msg.what = TYPE_SUCCESS;
							} else if (statusCode == 2) {
								msg.what = TOKEN_TIME_OUT_NO_DIALOG;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						msg.obj = e;
						msg.what = TYPE_FAIL;
//						if (apptypes.length < 1) {
//							apptypes = new String[] { getString(R.string.not_data) };
//						}
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
						String result = WebServiceClient.CallWebService(
								"GetLoadBrandList", null,
								BindDeviceActivity.this);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								brands = new String[1000];
								String res = obj.getString("content");
								if (!TextUtils.isEmpty(res)) {
									String[] temp = res.split(",");
									brands = new String[temp.length];
									brands = temp;
								}
//								if (brands.length < 1) {
//									brands = new String[] { getString(R.string.not_data) };
//								}
								msg.what = BRAND_SUCCESS;
							} else if (statusCode == 1) {
//								if (brands.length < 1) {
//									brands = new String[] { getString(R.string.not_data) };
//								}
								msg.what = BRAND_SUCCESS;
							} else if (statusCode == 2) {
								msg.what = TOKEN_TIME_OUT_NO_DIALOG;
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
		}
	}
}
