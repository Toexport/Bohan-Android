package cn.mioto.bohan.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.entity.SingleDevice;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.fragment.MainInfoFragment;
import cn.mioto.bohan.fragment.MainInfoFragment2;
import cn.mioto.bohan.fragment.MainListBangFragment;
import cn.mioto.bohan.fragment.MainListBangFragment2;
import cn.mioto.bohan.fragment.MainMeFragment;
import cn.mioto.bohan.fragment.MainMeFragment2;
import cn.mioto.bohan.fragment.MainOnlineFragment;
import cn.mioto.bohan.fragment.MainOnlineFragment2;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.ExceptionUtil;
import cn.mioto.bohan.utils.HttpUtils;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.NetWorkStateUtil;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.StringUtil;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;

import com.mioto.bohan.webservice.WebServiceClient;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import steed.framework.android.client.JsonClientHandler2;
import steed.framework.android.core.ContextUtil;
import steed.framework.android.util.base.PathUtil;

/**
 * 类说明：主界面，包含4个Fragment
 */
public class MainActivity2 extends SteedAppCompatActivity implements
		OnClickListener {
	// private Toolbar toolbar;
	private List<RadioButton> btnList = new ArrayList<RadioButton>();
	private LinearLayout llShare;
	private LinearLayout llcontainer;
	private Fragment[] fragments = new Fragment[4];
	private MainOnlineFragment2 listFragment;
	private MainListBangFragment2 listDeviceBangFragment;
	private MainInfoFragment infoFragment;
	private MainMeFragment meFragment;
	private long currentBackTime = 0;
	private long lastBackTime = 0;
	private ImageView ivScan;
	public static final String TAG_EXIT = "exit";
	public static final String TAG_BANG_LIST = "banglist";
	int currentFragmentIndex = 0;
	int clickIndex = 0;
	int intTitle;
	private TextView toolbarTitle;
	private TextView toolbarMenu;
	private TextView menu_front;
	/***************************************************/
	private BroadcastReceiver receiver;
	private ProgressDialog p;
	private Handler h = new Handler();
	private String versionName;
	// 加入下载队列后会给该任务返回一个long型的id，
	// 通过该id可以取消任务，重启任务等等
	long mTaskId;

	private FragmentTabHost tabHost;

	private static final int BRAND_SUCCESS = 10;

	protected List<SingleDevice> deviceList = new ArrayList<>();

	public void progressDialogOpenShow() {
		p.setMessage(getString(R.string.opening));
		p.setCancelable(false);
		p.setCanceledOnTouchOutside(false);
		p.show();
	}

	public void progressDialogCloseShow() {
		p.setMessage(getString(R.string.closing));
		p.setCancelable(false);
		p.setCanceledOnTouchOutside(false);
		p.show();
	}

	public void progressDialogDismiss() {
		p.dismiss();
	}

	public class DialogBrocastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 区分接收到的是哪种广播
			String action = intent.getAction();
			if (action.equals(Constant.BROCAST_ONLINE_LIST_SHOW_CLOSE_DIALOG)) {
				progressDialogCloseShow();
			} else if (action
					.equals(Constant.BROCAST_ONLINE_LIST_SHOW_OPEN_DIALOG)) {
				progressDialogOpenShow();
			} else if (action
					.equals(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG)) {
				progressDialogDismiss();
			} else if (action.equals(Constant.BROCAST_CHART_SHOW_DIALOG)) {

			} else if (action.equals(Constant.BROCAST_CHART_DIMISS_DIALOG)) {
				//
				loadingDialogDismiss();
			} else if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
				checkDownloadStatus();// 检查下载状态
			}
		}

	}

	/***************************************************/
	private Runnable loadFail;

	private void loadingDialogShow(String message) {
		p.setMessage(message);
		p.setCancelable(false);
		p.setCanceledOnTouchOutside(false);
		p.show();
	}

	// 检查下载状态
	private void checkDownloadStatus() {
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(mTaskId);// 筛选下载任务，传入任务ID，可变参数
		Cursor c = downloadManager.query(query);
		if (c.moveToFirst()) {
			int status = c.getInt(c
					.getColumnIndex(DownloadManager.COLUMN_STATUS));
			switch (status) {
			case DownloadManager.STATUS_PAUSED:
				LogUtilNIU.value(">>>下载暂停");
			case DownloadManager.STATUS_PENDING:
				LogUtilNIU.value(">>>下载延迟");
			case DownloadManager.STATUS_RUNNING:
				LogUtilNIU.value(">>>正在下载");
				break;
			case DownloadManager.STATUS_SUCCESSFUL:
				LogUtilNIU.value(">>>下载完成");
				// 下载完成安装APK
				String downloadPath = Environment
						.getExternalStoragePublicDirectory(
								Environment.DIRECTORY_DOWNLOADS)
						.getAbsolutePath()
						+ File.separator + versionName;
				installAPK(new File(downloadPath));
				break;
			case DownloadManager.STATUS_FAILED:
				LogUtilNIU.value(">>>下载失败");
				break;
			}
		}
	}

	// 下载到本地后执行安装
	protected void installAPK(File file) {
		if (!file.exists())
			return;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.parse("file://" + file.toString());
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		// 在服务中开启activity必须设置flag,后面解释
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivity(intent);
	}

	private void loadingDialogDismiss(String message) {
		h.removeCallbacks(loadFail);
		ToastUtils.shortToast(this, message);
		p.dismiss();
	}

	private void loadingDialogDismiss() {
		h.removeCallbacks(loadFail);
		p.dismiss();
	}

	/***************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null
				&& savedInstanceState.getBoolean("isAccountRemoved", false)) {
			// 防止被移除后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
			// 三个fragment里加的判断同理
			finish();
			startActivity(new Intent(this, LoginActivity.class));
			return;
		} else if (savedInstanceState != null
				&& savedInstanceState.getBoolean("isConflict", false)) {
			// 防止被T后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
			// 三个fragment里加的判断同理
			finish();
			startActivity(new Intent(this, LoginActivity.class));
			return;
		}
		// 判断用户网络状态并设定app的BSSID
		NetWorkStateUtil.checkNetWorkTypeAndAction(this);
		receiver = new DialogBrocastReceiver();
		IntentFilter filter = new IntentFilter();
		// 得到各下拉菜单项的内容。包括品牌，位置，设备类型的信息。详情看界面
		checkDatasAndSave();
		filter.addAction(Constant.BROCAST_ONLINE_LIST_SHOW_CLOSE_DIALOG);
		filter.addAction(Constant.BROCAST_ONLINE_LIST_SHOW_OPEN_DIALOG);
		filter.addAction(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
		// 图表数据加载效果广播
		filter.addAction(Constant.BROCAST_CHART_DIMISS_DIALOG);
		filter.addAction(Constant.BROCAST_CHART_SHOW_DIALOG);
		// 下载进度监听
		filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		registerReceiver(receiver, filter);
		p = new ProgressDialog(this);// DIALOG
		int titleString;
		// 设置toolbar标题和toolbar;
		// toolbar上的刷新按钮，这里是要另外写监听的
		listFragment = new MainOnlineFragment2();
		listDeviceBangFragment = new MainListBangFragment2();
		infoFragment = new MainInfoFragment();
		meFragment = new MainMeFragment();
		fragments[0] = listFragment;
		fragments[1] = listDeviceBangFragment;
		fragments[2] = infoFragment;
		fragments[3] = meFragment;
		tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		tabHost.addTab(
				tabHost.newTabSpec("0").setIndicator(
						getIndicator2(getString(R.string.device_list),
								R.drawable.selector_tab_home)),
				MainOnlineFragment2.class, null);
		tabHost.addTab(
				tabHost.newTabSpec("1").setIndicator(
						getIndicator2(getString(R.string.manage_device),
								R.drawable.selector_tab_bang)),
				MainListBangFragment2.class, null);
		tabHost.addTab(
				tabHost.newTabSpec("2").setIndicator(
						getIndicator2(getString(R.string.all_info),
								R.drawable.selector_tab_data)),
				MainInfoFragment2.class, null);
		tabHost.addTab(
				tabHost.newTabSpec("3").setIndicator(
						getIndicator2(getString(R.string.user_center),
								R.drawable.selector_tab_me)),
				MainMeFragment2.class, null);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			tabHost.getTabWidget().setDividerDrawable(R.color.transparent);
		}
		tabHost.setCurrentTab(0);
		/***** 向服务器查询用户下的所有设备及设备信息，返回的json解析后保存在内存并保存在数据库 **********************************************/
		LogUtilNIU.value("Main的onCreat执行");
		// 查询更新
		//checkUpdate();
		// 查询用户设备总个数并保存在全局
		// checkDeivceQuaAndSave();
		getDeviceCount();
	}

	private View getIndicator2(String name, int icon) {
		View v = getLayoutInflater().inflate(R.layout.tab_indicator2, null);
		TextView tv = (TextView) v.findViewById(R.id.textView);
		TextView tv2 = (TextView) v.findViewById(R.id.textView2);
		ImageView iv = (ImageView) v.findViewById(R.id.imageView);
		tv2.setVisibility(View.GONE);
		tv.setText(name);
		iv.setImageResource(icon);
		return v;
	}

	private void checkDatasAndSave() {
		getBrandInfos();
		getTypes();
		getLocationsService();
	}

	// 查询有没有新版本
	private void checkUpdate() {
		int versionCode = ContextUtil.getVersionCode();
		new Enterface("getLatestVersion.act").addParam("versioncode",
				versionCode).doRequest(new JsonClientHandler2() {
			@Override
			public void onInterfaceSuccess(String message,
					final String contentJson) {
				LogUtilNIU.value("版本控制打印的信息为--->" + contentJson);
				// {"versionname":"1.0","url":"\/upload\/0279ad5cc9f19c55a76084c6cb1d333176\/file\/8588e02a7cbe959f7fbb485435c2cdd7.apk"}
				// 自定义版本更新提醒dialog
				final AlertDialog alertDialog = new AlertDialog.Builder(
						MainActivity2.this).create();
				alertDialog.setCanceledOnTouchOutside(false);
				alertDialog.show();
				Window window = alertDialog.getWindow();
				window.setContentView(R.layout.dialog_version_control_update);
				TextView tvDownLoad = (TextView) window
						.findViewById(R.id.tvDownLoad);
				TextView tvNotDownLoad = (TextView) window
						.findViewById(R.id.tvNotDownLoad);
				tvDownLoad.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						LogUtilNIU.value("点击了下载更新");
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									JSONObject jsonObject = new JSONObject(
											contentJson);
									// 得到版本名称
									versionName = jsonObject
											.getString("versionname");
									// 目标下载路径
									String versionUrl = PathUtil.mergePath(
											Constant.baseUrl,
											jsonObject.getString("url"));
									// 开始下载
									downloadAPK(versionUrl, versionName);
									// downloadManager =
									// (DownloadManager)MainActivity.this.getSystemService(Activity.DOWNLOAD_SERVICE);
									// Request request = new
									// Request(Uri.parse(PathUtil.mergePath(Constant.baseUrl,
									// jsonObject.getString("url"))));
									// request.setNotificationVisibility(Request.VISIBILITY_VISIBLE);
									// PackageManager pm
									// =MainActivity.this.getPackageManager();
									// String appName =
									// MainActivity.this.getApplicationInfo().loadLabel(pm).toString();
									// request.setTitle(appName);
									// request.setDescription("下载最新版本"+appName);
									// request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
									// request.setAllowedOverRoaming(false);
									// downloadManager.enqueue(request);
								} catch (Exception e) {
									ExceptionUtil.handleException(e);
								}
							}
						}).start();
						alertDialog.dismiss();
					}
				});
				tvNotDownLoad.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						alertDialog.dismiss();// 取消
					}
				});
			}

			@Override
			public void onInterfaceFail(String json) {
				LogUtilNIU.value("版本控制接口错误--->" + json);
			}

			@Override
			public void onFailureConnected(Boolean canConnect) {
				LogUtilNIU.value("连接失效--->" + canConnect);
			}
		});
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		// ViewUtil.setToolbarTitle(clickIndex == 0 ?
		// getResources().getString(R.string.device_list) : (clickIndex == 1 ?
		// getResources().getString(R.string.bound_list) :
		// clickIndex==2?getResources().getString(R.string.all_info):getResources().getString(R.string.user_center)));
	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_main2;
	}

	private Boolean shareShowing = true;

	@Override
	public void onClick(View v) {
		if (v == ivScan) {
			if (clickIndex == 0) {
				Intent intent = new Intent(MainActivity2.this,
						BindDeviceActivity.class);
				startActivity(intent);
			} else if (clickIndex == 1) {
				// 点击分享
				shareShowing = false;
				ivScan.setVisibility(View.GONE);
				toolbarMenu.setText("取消");
				toolbarMenu.setVisibility(View.VISIBLE);
				// 出现左边的全选按钮 TODO
				menu_front.setVisibility(View.VISIBLE);
				menu_front.setText("全选");
				llShare.setVisibility(View.VISIBLE);
				// 出现左边区域
				listDeviceBangFragment.showLeftLayout();
			}
		} else if (v == toolbarMenu) {
			// 刷新所有用电数据
			// 调用fragment的方法来刷新
			if (clickIndex == 2) {
				infoFragment.refreshBar();
			} else if (clickIndex == 1) {
				// 点击了取消以后 TODO
				shareShowing = true;
				ivScan.setVisibility(View.VISIBLE);
				toolbarMenu.setVisibility(View.GONE);
				llShare.setVisibility(View.GONE);
				menu_front.setVisibility(View.GONE);
				listDeviceBangFragment.disSelectAll();
				listDeviceBangFragment.hideLeftLayout();
			}
		} else if (v == menu_front) {
			// 全选按钮的回调接口
			// 全选按钮被点击
			if (menu_front.getText().equals("全不选")) {
				listDeviceBangFragment.disSelectAll();
				menu_front.setText("全选");
			} else if (menu_front.getText().equals("全选")) {
				listDeviceBangFragment.selectAll();
				menu_front.setText("全不选");
			}
		} else if (v == llShare) {// 下面的分享按钮
			// 弹出自定义对话框
			if (listDeviceBangFragment.howManyIsSelected().size() < 1) {// 没有选到设备
				ToastUtils.shortToast(MainActivity2.this, "请选中需要分享的设备");
			} else {
				final EditText et = new EditText(this);
				et.setSingleLine();
				et.setMaxEms(11);// 设置只能输入11位
				et.setHint("请输入手机号");
				AlertDialog.Builder b = new AlertDialog.Builder(this);
				b.setTitle("请输入目标用户的用户名")
						.setView(et)
						.setPositiveButton("确定",
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
											ToastUtils.shortToast(
													MainActivity2.this,
													"请输入共享用户用户名");
										} else if (!StringUtil
												.isMobileNO(targetUserId)) {
											ToastUtils.shortToast(
													MainActivity2.this,
													"输入的格式有误");
										} else {
											Intent i = new Intent(
													MainActivity2.this,
													CreatQRCodeActivity.class);
											Bundle b = new Bundle();
											b.putStringArrayList(
													"infoList",
													(ArrayList<String>) listDeviceBangFragment
															.howManyIsSelected());
											b.putString("userId", targetUserId);
											i.putExtras(b);
											startActivity(i);// 跳转
										}
									}
								}).setNegativeButton("取消", null).show();
			}

			// .setIcon(
			// android.R.drawable.ic_dialog_info)

		}

	}

	// 实现优雅退出,接收到intent判断传过来的消息。
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent != null) {
			boolean isExit = intent.getBooleanExtra(TAG_EXIT, false);
			if (isExit) {
				BApplication.instance.clearThisUserFlashDatasOfApplication();// 清空application此用户的共用数据
				this.finish();
			}
		}
	}

	// 栈底。补获返回键，提示是否要退出程序
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 捕获返回键按下的事件
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 获取当前系统时间的毫秒数
			currentBackTime = System.currentTimeMillis();
			// 比较上次按下返回键和当前按下返回键的时间差，如果大于2秒，则提示再按一次退出
			if (currentBackTime - lastBackTime > 2 * 1000) {
				Toast.makeText(
						this,
						getResources()
								.getString(R.string.click_back_again_exit),
						Toast.LENGTH_SHORT).show();
				lastBackTime = currentBackTime;
			} else {
				BApplication.instance.clearThisUserFlashDatasOfApplication();// 清空application此用户的共用数据
				this.finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this); // 统计时长
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	public void bangdListFragmentshowList() {// 非分享状态
		menu_front.setVisibility(View.GONE);
		toolbarMenu.setVisibility(View.GONE);
		llShare.setVisibility(View.GONE);
		ivScan.setVisibility(View.VISIBLE);
	}

	private DownloadManager downloadManager;

	// 使用系统下载器下载,需要下载的url目标路径和版本名称
	private void downloadAPK(String versionUrl, String versionName) {
		// 创建下载任务
		DownloadManager.Request request = new DownloadManager.Request(
				Uri.parse(versionUrl));
		request.setAllowedOverRoaming(true);// 漫游网络是否可以下载

		// 设置文件类型，可以在下载结束后自动打开该文件
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap
				.getFileExtensionFromUrl(versionUrl));
		request.setMimeType(mimeString);

		// 在通知栏中显示，默认就是显示的
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
		request.setVisibleInDownloadsUi(true);

		// sdcard的目录下的download文件夹，必须设置
		request.setDestinationInExternalPublicDir("/download/", versionName);
		// request.setDestinationInExternalFilesDir(),也可以自己制定下载路径

		// 将下载请求加入下载队列
		downloadManager = (DownloadManager) this
				.getSystemService(Context.DOWNLOAD_SERVICE);
		// 加入下载队列后会给该任务返回一个long型的id，
		// 通过该id可以取消任务，重启任务等等，看上面源码中框起来的方法
		mTaskId = downloadManager.enqueue(request);
		ToastUtils.shortToast(this, "已经在手机后台启动下载,下载完毕后会提示安装");
	}

	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	private Runnable defalutTimeout = new Runnable() {
		@Override
		public void run() {
			defalutHandler.obtainMessage(Constant.MSG_TIME_OUT).sendToTarget();
		}
	};

	private Handler defalutHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constant.MSG_SUCCESS:
				break;
			case Constant.MSG_FAILURE:
				toast((String) msg.obj);
				break;
			case Constant.MSG_EXCPTION:
				toast(ExceptionManager.getErrorDesc(getApplicationContext(),
						(Exception) msg.obj));
				break;
			case Constant.MSG_TIME_OUT:
				toast(getString(R.string.connection_timeout));
				break;
			case Constant.MSG_NETWORK_ERROR:
				toast(getString(R.string.no_connection));
				break;
			}
		}

		;
	};

	// 获取品牌信息
	private void getBrandInfos() {
		if (NetworkUtils.isNetworkConnected(this)) {
			// getDefaultProgressDialog(getString(R.string.loging),false);
			// isRequesting = true;
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						String result = WebServiceClient.CallWebService(
								"GetLoadBrandList", null);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							String[] brands;
							if (statusCode == 0) {
								brands = new String[] {};
								String res = obj.getString("content");
								if (!TextUtils.isEmpty(res)) {
									brands = res.split(",");
								}
								msg.what = BRAND_SUCCESS;
								StringBuffer sb = new StringBuffer();
								for (int i = 0; i < brands.length; i++) {
									sb.append(brands[i] + ",");
								}
								String locations = sb.toString();
								String key = Constant.SP_KEY_LOCATIONS;
								BApplication.instance.getEditor().putString(
										key, locations);
								BApplication.instance.getEditor().commit();
								LogUtilNIU
										.value("保存了到sp的品牌信息是"
												+ BApplication.instance
														.getSp()
														.getString(
																Constant.SP_KEY_LOCATIONS,
																"no"));

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

	// 获取设备个数
	private void getDeviceCount() {
		if (NetworkUtils.isNetworkConnected(this)) {
			// getDefaultProgressDialog(getString(R.string.loging),false);
			// isRequesting = true;
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						// HttpUtils.TOKIN = "";
						String result = WebServiceClient.CallWebService(
								"GetUserDeviceList", null);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int code = obj.getInt("statusCode");
							if (code == 0) {
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
//									singleDevice.setDeviceWIFIBSSID(jsonObject
//											.getString("wifibssid"));
									singleDevice.setDeviceType(jsonObject
											.getString("sort"));
									deviceList.add(singleDevice);
								}
								msg.what = Constant.MSG_SUCCESS;
							} else {
								msg.obj = obj.getString("message");
								msg.what = Constant.MSG_FAILURE;
							}
						}
						int deviceQua = 0;
						BApplication.instance.setDeviceQua(deviceQua);
						msg.what = Constant.MSG_SUCCESS;
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
	 * 从服务器获取名称
	 */
	private void getTypes() {
		if (NetworkUtils.isNetworkConnected(this)) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						HttpUtils.TOKIN = "";
						String result = WebServiceClient.CallWebService(
								"GetLoadNameList", null);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								String[] apptypes = new String[] {};
								String res = obj.getString("content");
								if (!TextUtils.isEmpty(res)) {
									apptypes = res.split(",");
								}

								StringBuffer sb = new StringBuffer();
								for (int i = 0; i < apptypes.length; i++) {
									sb.append(apptypes[i] + ",");
								}
								String appTypes = sb.toString();
								String key = Constant.SP_KEY_TYPES;
								BApplication.instance.getEditor().putString(
										key, appTypes);
								BApplication.instance.getEditor().commit();
								LogUtilNIU.value("保存了到sp的用电类型信息是"
										+ BApplication.instance.getSp()
												.getString(
														Constant.SP_KEY_TYPES,
														"no"));
								msg.what = Constant.MSG_SUCCESS;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						msg.obj = e;
						msg.what = Constant.MSG_EXCPTION;
						;
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

	// 从服务器获取位置
	private void getLocationsService() {
		if (NetworkUtils.isNetworkConnected(this)) {
			new Thread() {
				public void run() {
					Message msg = new Message();
					msg.what = Constant.MSG_FAILURE;
					msg.obj = getString(R.string.cannot_connection_server);
					try {
						HttpUtils.TOKIN = "";
						String result = WebServiceClient.CallWebService(
								"GetPosNameList", null);
						Log.d("resultLogin", result);
						if (result != null) {
							JSONObject obj = new JSONObject(result);
							int statusCode = obj.getInt("statusCode");
							if (statusCode == 0) {
								String[] locations = new String[] {};
								String rest = obj.getString("content");
								if (!TextUtils.isEmpty(rest)) {
									locations = rest.split(",");
								}
								StringBuffer sb = new StringBuffer();
								for (int i = 0; i < locations.length; i++) {
									sb.append(locations[i] + ",");
								}
								String location = sb.toString();
								String key = Constant.SP_KEY_LOCATIONS;
								BApplication.instance.getEditor().putString(
										key, location);
								BApplication.instance.getEditor().commit();
								LogUtilNIU
										.value("保存了到sp的位置信息是"
												+ BApplication.instance
														.getSp()
														.getString(
																Constant.SP_KEY_LOCATIONS,
																"no"));
								msg.what = Constant.MSG_SUCCESS;
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

}
