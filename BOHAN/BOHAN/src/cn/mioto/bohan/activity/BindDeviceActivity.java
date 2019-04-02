package cn.mioto.bohan.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.mioto.bohan.webservice.WebServiceClient;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.utils.HttpUtils;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.ViewUtil;

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
	private EditText etID;
	private ImageView ivScanId;
	private EditText etKey;
	private ImageView ivScanKey;
	private EditText etLocation;
	private EditText etType;
	private EditText etBrand;
	private TextView tvConfirm;
	private TextView menuTv;
	private boolean isRequesting = true;
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bind_device);
		ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title,
				R.string.band_device, true, true, R.id.menu_tv, R.string.album);
		initViews();
		setListener();
	}

	private void initViews() {
		menuFront = (TextView) findViewById(R.id.menu_front);
		etID = (EditText) findViewById(R.id.etID);
		etKey = (EditText) findViewById(R.id.etKey);
		etLocation = (EditText) findViewById(R.id.etLocation);
		etType = (EditText) findViewById(R.id.etType);
		etBrand = (EditText) findViewById(R.id.etBrand);
		ivScanId = (ImageView) findViewById(R.id.ivScanId);
		ivScanKey = (ImageView) findViewById(R.id.ivScanKey);
		tvConfirm = (TextView) findViewById(R.id.tvConfirm);
		menuTv = (TextView) findViewById(R.id.menu_tv);
		menuTv.setVisibility(View.GONE);
	}

	private void setListener() {
		tvConfirm.setOnClickListener(this);
		ivScanId.setOnClickListener(this);
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
		case R.id.ivScanId:
			intent = new Intent(this, ScanActivity.class);
			startActivityForResult(intent, REQUEST_SCAN_ID);
			break;
		case R.id.ivScanKey:
			intent = new Intent(this, ScanActivity.class);
			startActivityForResult(intent, REQUEST_SCAN_KEY);
			break;
		}
	}

	// 数据验证
	private void dataView() {
		String etIDStr = etID.getText().toString().trim();
		String etKeyStr = etKey.getText().toString().trim();
		String etLocationStr = etLocation.getText().toString().trim();
		String etTypeStr = etType.getText().toString().trim();
		String etBrandStr = etBrand.getText().toString().trim();
		if (TextUtils.isEmpty(etIDStr)) {
			toast(getString(R.string.id_not_null));
		} else if (TextUtils.isEmpty(etKeyStr)) {
			toast(getString(R.string.key_not_null));
		} else if (TextUtils.isEmpty(etLocationStr)) {
			toast(getString(R.string.location_not_null));
		} else if (TextUtils.isEmpty(etTypeStr)) {
			toast(getString(R.string.type_not_null));
		} else if (TextUtils.isEmpty(etBrandStr)) {
			toast(getString(R.string.brand_not_null));
		} else {
			bindDevice(etIDStr, etKeyStr, etLocationStr, etTypeStr, etBrandStr);
		}
	}

	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_SCAN_ID:
			if (resultCode == RESULT_OK) {
				if (data != null) {
					etID.setText(data.getExtras().getString("result"));
					etID.setSelection(etID.getText().length());
				}
			}
			break;
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
								"BindingDevice", map);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								msg.obj = DeviceCode;
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
				toast(ExceptionManager.getErrorDesc(getApplicationContext(),
						(Exception) msg.obj));
				break;
			case Constant.MSG_TIME_OUT:
				bindFailure();
				toast(getString(R.string.connection_timeout));
				break;
			case Constant.MSG_NETWORK_ERROR:
				bindFailure();
				toast(getString(R.string.no_connection));
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
}
