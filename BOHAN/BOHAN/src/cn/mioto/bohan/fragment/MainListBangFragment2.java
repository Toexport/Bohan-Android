package cn.mioto.bohan.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.activity.CreatQRCodeActivity;
import cn.mioto.bohan.activity.LoginActivity;
import cn.mioto.bohan.activity.MainActivity;
import cn.mioto.bohan.activity.ScanActivity;
import cn.mioto.bohan.adapter.BangListAdapter;
import cn.mioto.bohan.adapter.BangListAdapter2;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.StringUtil;
import cn.mioto.bohan.utils.ToastUtils;

import com.google.gson.Gson;
import com.mioto.bohan.webservice.WebServiceClient;
import com.umeng.analytics.MobclickAgent;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
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
import steed.framework.android.client.JsonClientHandler2;

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
	private static final int DEL_DEVICE_SUCCESS = 10;

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
				Intent intent = new Intent(getActivity(), ScanActivity.class);
				// 跳到扫码页面
				startActivity(intent);
			}
		});
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
		} else {
			LogUtilNIU.e("list有数据");
			llNoData.setVisibility(View.GONE);
			llLoading.setVisibility(View.GONE);// 加载中除去
			lvListBang.setVisibility(View.VISIBLE);
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 删除(解绑)设备
	 */
	public void delDevices(final SingleDevice singleDevice) {
		progressDialogShow();
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
								"UnbindDevice", map);
						Log.d("resultUnbindDevice", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int code = obj.getInt("statusCode");
							Log.i("delDevices", code+"");
							if (code == 0) {
								msg.what = DEL_DEVICE_SUCCESS;
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
		// if (NetworkUtils.isNetworkConnected(getActivity())) {
		// getDefaultProgressDialog(getString(R.string.loging),false);
		// isRequesting = true;
		new Thread() {
			public void run() {
				Message msg = new Message();
				msg.what = Constant.MSG_FAILURE;
				// msg.obj = getString(R.string.cannot_connection_server);
				try {
					// HttpUtils.TOKIN = "";
					String result = WebServiceClient.CallWebService(
							"GetUserDeviceList", null);
					Log.d("resultgetDeviceCount", result);
					if (result != null) {
						JSONObject obj = new JSONObject(result);
						int code = obj.getInt("statusCode");
						if (code == 0) {
							devices.removeAll(devices);
							JSONArray array = obj.getJSONArray("content");
							for (int i = 0; i < array.length(); i++) {
								JSONObject jsonObject = array.getJSONObject(i);
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
//								singleDevice.setDeviceWIFIBSSID(jsonObject
//										.getString("wifibssid"));
								singleDevice.setDeviceType(jsonObject
										.getString("sort"));
								devices.add(singleDevice);
							}
							// 得到用户名下的所有设备后，把他们的id存在全局
							BApplication.instance
									.setCurrentDeviceIds(getDevicesIds(devices));
							msg.what = Constant.MSG_SUCCESS;
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

	// else {
	// defalutHandler.sendEmptyMessage(Constant.MSG_NETWORK_ERROR);
	// }
	// }
	private Handler defalutHandler = new Handler() {
		public void handleMessage(Message msg) {
			progressDialogDismiss();
			switch (msg.what) {
			case Constant.MSG_SUCCESS:
				checkDeviceSize();
				break;
			case Constant.MSG_EXCPTION:
			case Constant.MSG_FAILURE:
				ToastUtils.shortToast(getActivity(),
						getString(R.string.del_device_fail));
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
				getDeviceCount();
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
				final EditText et = new EditText(getActivity());
				et.setSingleLine();
				et.setMaxEms(11);// 设置只能输入11位
				et.setHint(getString(R.string.please_input_mobile));
				AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
				b.setTitle(getString(R.string.please_input_user_id))
						.setView(et)
						.setPositiveButton(getString(R.string.OK),
								new DialogInterface.OnClickListener() {
									// 如果分享区域被选中，跳转到生成了二维码的页面
									// 这里还需要叫某个地方，执行一个统计有哪些ID被选中的命令。
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										String targetUserId = et.getText()
												.toString();// 获取输入框内容
										// 判断输入的内容是否为手机号
										if (targetUserId.equals("")) {
											ToastUtils
													.shortToast(
															getActivity(),
															getString(R.string.please_input_user_id));
										} else if (!StringUtil
												.isMobileNO(targetUserId)) {
											ToastUtils
													.shortToast(
															getActivity(),
															getString(R.string.input_fomart_error));
										} else {
											Intent i = new Intent(
													getActivity(),
													CreatQRCodeActivity.class);
											Bundle b = new Bundle();
											b.putStringArrayList(
													"infoList",
													(ArrayList<String>) howManyIsSelected());
											b.putString("userId", targetUserId);
											i.putExtras(b);
											startActivity(i);// 跳转
										}
									}
								})
						.setNegativeButton(getString(R.string.cancle), null)
						.show();
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

	public void progressDialogShow() {
		p = new ProgressDialog(getActivity());// DIALOG
		p.setMessage(getString(R.string.deling));
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
}
