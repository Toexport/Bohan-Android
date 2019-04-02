package cn.mioto.bohan.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.activity.BindDeviceActivity;
import cn.mioto.bohan.activity.CreatQRCodeActivity;
import cn.mioto.bohan.activity.LoginActivity;
import cn.mioto.bohan.activity.OnlineStatusActivity;
import cn.mioto.bohan.activity.ScanActivity;
import cn.mioto.bohan.adapter.BangListAdapter;
import cn.mioto.bohan.adapter.BangListAdapter2;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.utils.HttpUtils;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.StringUtil;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.view.CustomShareDialog;
import com.mioto.bohan.webservice.WebServiceClient;
import com.umeng.analytics.MobclickAgent;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 类说明：已绑定设备列表界面（Fragment） 显示用户名下的所有的已经绑定设备。
 */

public class MainListBangFragment2 extends BaseFragment implements
		OnClickListener {
	View view;
	ListView lvListBang;
	LinearLayout llNoData;// 如果设备为空，则显示
	private TextView tvBang;
	List<SingleDevice> devices = new ArrayList<>();// 最初为空数据;
	// private LoadingView loadingView;
	private LinearLayout llLoading;
	BangListAdapter2 adapter;

	private TextView toolbarTitle;
	private TextView toolbarMenu;
	private TextView menu_front;
	private ImageView ivScan;
	private LinearLayout llShare;
	private ProgressDialog p;
	private static final int DEL_DEVICE_SUCCESS = 10, SHARE_SUCCESS = 11,
			SHARE_FAIL = 12;
	private SingleDevice currentDelSingleDevice;

	private static final int TOKEN_TIME_OUT = 15,
			TOKEN_TIME_OUT_NO_DIALOG = 13;

	private boolean isZH;

	// 从设备集合提出Id集合
	private List<String> getDevicesIds(List<SingleDevice> devices) {
		List<String> devicesIds = new ArrayList<>();
		for (int i = 0; i < devices.size(); i++) {
			String sDeviceId = devices.get(i).getDeviceID();
			devicesIds.add(sDeviceId);
		}
		return devicesIds;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_main_list_bang2, null);
		lvListBang = (ListView) view.findViewById(R.id.lvListBang);
		llNoData = (LinearLayout) view.findViewById(R.id.llNoData);
		tvBang = (TextView) view.findViewById(R.id.tvBang);
		llLoading = (LinearLayout) view.findViewById(R.id.llLoading);

		menu_front = (TextView) view.findViewById(R.id.menu_front);
		menu_front.setOnClickListener(this);
		toolbarMenu = (TextView) view.findViewById(R.id.menu_tv);
		toolbarMenu.setOnClickListener(this);
		toolbarTitle = (TextView) view.findViewById(R.id.toolbar_title);
		toolbarTitle.setText(getString(R.string.manage_device));
		ivScan = (ImageView) view.findViewById(R.id.ivScan);
		ivScan.setOnClickListener(this);
		llShare = (LinearLayout) view.findViewById(R.id.llShare);
		llShare.setOnClickListener(this);
		/***************************************************/
		adapter = new BangListAdapter2(getActivity(), devices, this);
		lvListBang.setAdapter(adapter);
		/***************************************************/
		tvBang.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),
						BindDeviceActivity.class);
				// 跳到扫码页面
				startActivity(intent);
			}
		});

		isZH = isZh();
		return view;
	}

	@Override
	public void onResume() {
		// adapter.notifyDataSetChanged();

		super.onResume();
		getDeviceCount();
		// getDevices();
		// 在扫描后数据发生变化时更新
		// if(adapter.getShowLeft()){
		// //从其他地方返回这个界面时，如果左边正在出现（分享状态），让左边消失
		// adapter.setShowLeft(false);
		// adapter.notifyDataSetChanged();
		// }
		MobclickAgent.onPageStart("绑定列表");
	}

	private void checkDeviceSize() {
		if (devices.size() < 1) {
			LogUtilNIU.e("list无设备");
			llNoData.setVisibility(View.VISIBLE);
			llLoading.setVisibility(View.GONE);// 加载中除去
			lvListBang.setVisibility(View.GONE);
			ivScan.setVisibility(View.GONE);
		} else {
			LogUtilNIU.e("list有数据");
			llNoData.setVisibility(View.GONE);
			llLoading.setVisibility(View.GONE);// 加载中除去
			lvListBang.setVisibility(View.VISIBLE);
			ivScan.setVisibility(View.VISIBLE);
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 删除(解绑)设备
	 */
	public void delDevices(final SingleDevice singleDevice) {
		progressDialogShow(getString(R.string.deling));
		if (NetworkUtils.isNetworkConnected(getActivity())) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						// HttpUtils.TOKIN = "";
						Map map = new HashMap<>();
						map.put("DeviceCode", singleDevice.getDeviceID());
						map.put("DeviceKey", singleDevice.getDeviceID());
						String result = WebServiceClient.CallWebService(
								"UnbindDevice", map, getActivity());
						Log.d("resultUnbindDevice", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int code = obj.getInt("statusCode");
							Log.i("delDevices", code + "");
							if (code == 0) {
								currentDelSingleDevice = singleDevice;
								msg.what = DEL_DEVICE_SUCCESS;
							} else if (code == 2) {
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
					// if (isRequesting) {
					defalutHandler.sendMessage(msg);
					// }
				}
			}.start();
		}
	}

	public void update() {
		// getDevices();//
		getDeviceCount();
		LogUtilNIU.circle("绑定列表更新");
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("绑定列表页");
	}

	/**
	 * 
	 * @Description:供外部调用的，出现左边选择布局的方法 Parameters: return:void
	 */
	public void showLeftLayout() {
		adapter.setShowLeft(true);
		adapter.notifyDataSetChanged();
	}

	/**
	 * 
	 * @Description:供外部调用的，隐藏左边选择布局的方法 Parameters: return:void
	 */
	public void hideLeftLayout() {
		adapter.setShowLeft(false);
		adapter.notifyDataSetChanged();
	}

	/**
	 * 
	 * @Description:执行全选方法 Parameters: return:void
	 */
	public void selectAll() {
		adapter.selectAll();
		// 数量设为list的长度
		// 刷新listview和TextView的显示
		adapter.notifyDataSetChanged();
	}

	/**
	 * 
	 * @Description:执行全不选的方法
	 */
	public void disSelectAll() {// 全不选
		adapter.disSelectAll();
		// 数量设为list的长度
		// 刷新listview和TextView的显示
		adapter.notifyDataSetChanged();
	}

	/**
	 * @Description:统计选中的有哪些id,并保存到全局变量
	 */
	public List<String> howManyIsSelected() {
		List<String> idSelectedList = new ArrayList<>();
		idSelectedList = adapter.howManySelected();
		return idSelectedList;
	}

	// 获取设备列表
	private void getDeviceCount() {
		if (NetworkUtils.isNetworkConnected(getActivity())) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					// msg.obj = getString(R.string.cannot_connection_server);
					try {
						// HttpUtils.TOKIN = "";
						String result = WebServiceClient.CallWebService(
								"GetUserDeviceList", null, getActivity());
						Log.d("resultgetDeviceCount", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int code = obj.getInt("statusCode");
							if (code == 0) {
								devices.removeAll(devices);
								JSONArray array = obj.getJSONArray("content");
								for (int i = 0; i < array.length(); i++) {
									JSONObject jsonObject = array
											.getJSONObject(i);
									SingleDevice singleDevice = new SingleDevice();
									singleDevice.setIsOnline(true);
									singleDevice.setDeviceLocation(jsonObject
											.getString("position"));
									singleDevice.setDeviceBrand(jsonObject
											.getString("brand"));
									singleDevice.setDeviceID(jsonObject
											.getString("id"));
									singleDevice.setDeviceName(jsonObject
											.getString("name"));
									// singleDevice.setDeviceWIFIBSSID(jsonObject
									// .getString("wifibssid"));
									singleDevice.setDeviceType(jsonObject
											.getString("sort"));
									singleDevice.setDeviceType(jsonObject
											.getString("name"));
									devices.add(singleDevice);
								}
								// 得到用户名下的所有设备后，把他们的id存在全局
								BApplication.instance.getCurrentDeviceIds()
										.clear();
								BApplication.instance
										.setCurrentDeviceIds(getDevicesIds(devices));
								msg.what = Constant.MSG_SUCCESS;
							} else if (code == 1) {
								msg.what = Constant.MSG_SUCCESS;
							} else if (code == 2) {
								msg.what = TOKEN_TIME_OUT;
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
		}

		else {
			defalutHandler.sendEmptyMessage(Constant.MSG_NETWORK_ERROR);
		}
	}

	private void toast(String text) {
		Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
	}

	private Handler defalutHandler = new Handler() {
		public void handleMessage(Message msg) {
			progressDialogDismiss();
			switch (msg.what) {
			case Constant.MSG_SUCCESS:
				checkDeviceSize();
				break;
			case Constant.MSG_EXCPTION:
				toast(ExceptionManager.getErrorDesc(getActivity(),
						(Exception) msg.obj));
				break;
			case Constant.MSG_FAILURE:
				if (msg.obj != null) {
					String res = (String) msg.obj;
					ToastUtils.shortToast(getActivity(), res);
				}
				break;
			case Constant.MSG_TIME_OUT:
				ToastUtils.shortToast(getActivity(),
						getString(R.string.connection_timeout));
				break;
			case Constant.MSG_NETWORK_ERROR:
				ToastUtils.shortToast(getActivity(),
						getString(R.string.no_connection));
				break;
			case DEL_DEVICE_SUCCESS:
				ToastUtils.shortToast(getActivity(),
						getString(R.string.del_device_success));
				if (null != currentDelSingleDevice) {
					for (int i = 0; i < devices.size(); i++) {
						SingleDevice data = devices.get(i);
						if (currentDelSingleDevice.getDeviceID().equals(
								data.getDeviceID())) {
							devices.remove(data);
							break;
						}
					}
					checkDeviceSize();
					adapter.notifyDataSetChanged();
				}
				// getDeviceCount();
				break;
			case SHARE_SUCCESS:
				if (msg.obj != null) {
					ToastUtils.shortToast(getActivity(), (String) msg.obj);
				}
				break;
			case SHARE_FAIL:
				if (msg.obj != null) {
					ToastUtils.shortToast(getActivity(), (String) msg.obj);
				}
				break;
			case TOKEN_TIME_OUT:
				ToastUtils.longToast(getActivity(),
						getString(R.string.login_expired));
				BApplication.instance.clearThisUserFlashDatasOfApplication();// 清空application此用户的共用数据
				getActivity().finish();
				Intent intent = new Intent(getActivity()
						.getApplicationContext(), LoginActivity.class);
				startActivity(intent);
				break;
			}
		}

		;
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivScan:
			// 点击分享
			// shareShowing = false;
			ivScan.setVisibility(View.GONE);
			toolbarMenu.setText(getString(R.string.cancle));
			toolbarMenu.setVisibility(View.VISIBLE);
			// 出现左边的全选按钮 TODO
			menu_front.setVisibility(View.VISIBLE);
			menu_front.setText(getString(R.string.all_select));
			llShare.setVisibility(View.VISIBLE);
			// 出现左边区域
			showLeftLayout();
			break;
		case R.id.menu_front:
			// 全选按钮的回调接口
			// 全选按钮被点击
			if (menu_front.getText().equals(getString(R.string.all_no_select))) {
				disSelectAll();
				menu_front.setText(getString(R.string.all_select));
			} else if (menu_front.getText().equals(
					getString(R.string.all_select))) {
				selectAll();
				menu_front.setText(getString(R.string.all_no_select));
			}
			break;
		case R.id.llShare:
			// 弹出自定义对话框
			if (howManyIsSelected().size() < 1) {// 没有选到设备
				ToastUtils.shortToast(getActivity(),
						getString(R.string.please_select_share_device));
			} else {
				// final EditText et = new EditText(getActivity());
				// et.setSingleLine();
				// et.setMaxEms(11);// 设置只能输入11位
				// et.setHint(getString(R.string.please_input_mobile));
				// final EditText et2 = new EditText(getActivity());
				// et2.setSingleLine();
				// et2.setTransformationMethod(PasswordTransformationMethod
				// .getInstance());
				// et2.setHint(getString(R.string.please_input_pwd));
				// AlertDialog.Builder b = new
				// AlertDialog.Builder(getActivity());
				// b.setTitle(getString(R.string.please_input_user_id))
				// .setView(et)
				// .setPositiveButton(getString(R.string.OK),
				// new DialogInterface.OnClickListener() {
				// // 如果分享区域被选中，跳转到生成了二维码的页面
				// // 这里还需要叫某个地方，执行一个统计有哪些ID被选中的命令。
				// @Override
				// public void onClick(DialogInterface dialog,
				// int which) {
				// String targetUserId = et.getText()
				// .toString();// 获取输入框内容
				// String tempPwd = et2.getText()
				// .toString().trim();
				// // 判断输入的内容是否为手机号
				// if (targetUserId.equals("")) {
				// ToastUtils
				// .shortToast(
				// getActivity(),
				// getString(R.string.please_input_user_id));
				// } else if (TextUtils.isEmpty(tempPwd)) {
				// ToastUtils
				// .shortToast(
				// getActivity(),
				// getString(R.string.please_input_pwd));
				// } else if (!StringUtil
				// .isMobileNO(targetUserId)) {
				// ToastUtils
				// .shortToast(
				// getActivity(),
				// getString(R.string.input_fomart_error));
				// } else {
				// Intent i = new Intent(
				// getActivity(),
				// CreatQRCodeActivity.class);
				// Bundle b = new Bundle();
				// b.putStringArrayList(
				// "infoList",
				// (ArrayList<String>) howManyIsSelected());
				// b.putString("userId", targetUserId);
				// i.putExtras(b);
				// startActivity(i);// 跳转
				// }
				// }
				// })
				// .setNegativeButton(getString(R.string.cancle), null)
				// .show();

				showAddPointDialog("", "");
			}
			break;

		case R.id.menu_tv:
			// 点击了取消以后 TODO
			// shareShowing = true;
			ivScan.setVisibility(View.VISIBLE);
			toolbarMenu.setVisibility(View.GONE);
			llShare.setVisibility(View.GONE);
			menu_front.setVisibility(View.GONE);
			disSelectAll();
			hideLeftLayout();
			break;
		}
	}

	private void showAddPointDialog(final String x1, final String y1) {
		CustomShareDialog.Builder builder = new CustomShareDialog.Builder(
				getActivity(), x1, y1);
		builder.setTitle(getString(R.string.share));
		builder.setNegativeButton(R.string.cancel, null);
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String no = ((CustomShareDialog) dialog)
								.getInput_pile_no();
						String noEn = ((CustomShareDialog) dialog)
								.getInput_no_en();
						String x = ((CustomShareDialog) dialog).getInput_x();
						EditText et = ((CustomShareDialog) dialog)
								.getEdit_pile_no();
						EditText etEn = ((CustomShareDialog) dialog)
								.getEdit_pile_no_en();
						if (et.getVisibility() == View.VISIBLE) {
							// 中文
							if (TextUtils.isEmpty(no)) {
								ToastUtils
										.longToast(
												getActivity(),
												getString(R.string.please_input_mobile));
							} else if (!StringUtil.isMobileNO(no)) {
								ToastUtils.shortToast(getActivity(),
										getString(R.string.input_fomart_error));
							}else if (TextUtils.isEmpty(x)) {
								ToastUtils.longToast(getActivity(),
										getString(R.string.please_input_pwd));
							}else {
								dialog.dismiss();
								List list = howManyIsSelected();
								String[] temp = new String[list.size()];
								String tempStr = "";
								for (int i = 0; i < temp.length; i++) {
									// if( i != temp.length -1){
									// temp[i] = (String) list.get(i) +",";
									// }else{
									// temp[i] = (String) list.get(i);
									// }
									if (tempStr.equals("")) {
										tempStr = (String) list.get(i);
									} else {
										tempStr = tempStr + ","
												+ (String) list.get(i);
									}
								}
								share(tempStr, no, x);
							}
						} else if (TextUtils.isEmpty(noEn)) {
							ToastUtils.longToast(getActivity(),
									getString(R.string.please_input_mail));
						} else if (!isValidEmail(noEn)) {
							ToastUtils.longToast(getActivity(),
									getString(R.string.email_format_error));
						} else if (TextUtils.isEmpty(x)) {
							ToastUtils.longToast(getActivity(),
									getString(R.string.please_input_pwd));
						} else {
							dialog.dismiss();
							List list = howManyIsSelected();
							String[] temp = new String[list.size()];
							String tempStr = "";
							for (int i = 0; i < temp.length; i++) {
								// if( i != temp.length -1){
								// temp[i] = (String) list.get(i) +",";
								// }else{
								// temp[i] = (String) list.get(i);
								// }
								if (tempStr.equals("")) {
									tempStr = (String) list.get(i);
								} else {
									tempStr = tempStr + ","
											+ (String) list.get(i);
								}
							}
							share(tempStr, noEn, x);
						}
					}
				});
		builder.show();
	}

	public void progressDialogShow(String msg) {
		p = new ProgressDialog(getActivity());// DIALOG
		p.setMessage(msg);
		p.setCancelable(false);
		p.setCanceledOnTouchOutside(false);
		p.show();
	}

	public void progressDialogDismiss() {
		if (p != null) {
			p.dismiss();
			p = null;
		}
	}

	private void share(final String deviceCode, final String mobileNum,
			final String password) {
		if (NetworkUtils.isNetworkConnected(getActivity())) {
			progressDialogShow(getString(R.string.loading));
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						Map map = new HashMap();
						map.put("deviceCodes", deviceCode);
						map.put("mobileNum", mobileNum);
						map.put("password", password);
						String result = WebServiceClient.CallWebService(
								"ShareDevice", map, getActivity());
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								msg.obj = obj.getString("message");
								msg.what = SHARE_SUCCESS;
							} else if (statusCode == 2) {
								msg.what = TOKEN_TIME_OUT;
							} else {
								msg.obj = obj.getString("message");
								msg.what = SHARE_FAIL;
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

	/**
	 * 获取系统语言
	 * 
	 * @return
	 */
	private boolean isZh() {
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if (language.endsWith("zh")) {
			isZH = true;
			return true;
		} else {
			isZH = false;
			return false;
		}

	}

	private boolean isValidEmail(String mail) {
		Pattern pattern = Pattern
				.compile("^[A-Za-z0-9][\\w\\._]*[a-zA-Z0-9]+@[A-Za-z0-9-_]+\\.([A-Za-z]{2,4})");
		Matcher mc = pattern.matcher(mail);
		return mc.matches();
	}
}
