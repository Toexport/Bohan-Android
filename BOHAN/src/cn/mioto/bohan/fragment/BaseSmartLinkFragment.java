package cn.mioto.bohan.fragment;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.activity.SmartLinkActivity;
import cn.mioto.bohan.entity.Config;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.NetWorkStateUtil;
import cn.mioto.bohan.utils.ToastUtils;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.demo_activity.EspWifiAdminSimple;
import com.umeng.analytics.MobclickAgent;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import steed.framework.android.client.JsonClientHandler2;

/**
 * 类说明：手动连接和极速连接的基类 
 */
public class BaseSmartLinkFragment extends BaseUDPFragment implements OnClickListener {
	// WIFI密码
	protected String apPassword;
	// WIFI名称
	protected String apSsid;
	protected Boolean buttonIsClick = false;
	protected Boolean linkSuccess = false;
	protected View view;
	protected LinearLayout explain_linearLayout;
	protected TextView stepTextView;
	protected LinearLayout wifiLoginLinearLayout;
	protected TextView SsidTextView;
	protected TextView tvCircleProgress;
	protected EditText SsidEditText;
	protected LinearLayout llprogress;
	protected TextView passwordTextView;
	protected ImageButton ivRmPwd;
	protected TextView tvInfomation;
	protected EditText passwordEditText;
	protected LinearLayout showpwdLinerLayout;
	protected RadioButton showPasswordRadioButton;
	protected TextView addButton;
	protected EspWifiAdminSimple mWifiAdmin;
	protected RotateAnimation rotaAnimation;
	protected boolean mbDisplayFlg = true;
	protected List<IEsptouchResult> result = new ArrayList<>();
	protected ProgressDialog mProgressDialog;
	protected ProgressDialog ipProgressDialog;
	protected BroadcastReceiver receiver;
	protected Boolean checkThreadStop = false;
	// 全局的sp editor
	protected Editor editor;
	// sp
	SharedPreferences sp;

	protected Handler h = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 200) {
				buttonIsClick = false;
				// 尝试验证表地址后没有收到数据
				mProgressDialog.setMessage("智能连接成功，验证表地址超时，没有接收到表地址信息,但是不影响使用\n5秒后跳转");
				h.postDelayed(new Runnable() {
					@Override
					public void run() {
						mProgressDialog.dismiss();// 3秒后消失
						// 要求activity退出
						((SmartLinkActivity) getActivity()).finish();
					}
				}, 8000);
			}else if(msg.what == 300){
				ToastUtils.longToast(getActivity(), getString(R.string.equipment2)+deviceId+getString(R.string.equ_connection_successful));
				mProgressDialog.dismiss();// 3秒后消失
				// 要求activity退出
				((SmartLinkActivity) getActivity()).finish();
			}
		};
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_add_byhand, null);
		editor = ((BApplication) getContext()).getEditor();
		bindViews();
		mWifiAdmin = new EspWifiAdminSimple(getContext());
		// 设置添加的监听
		addButton.setOnClickListener(this);
		ivRmPwd.setOnClickListener(this);
		/***************************************************/
		receiver = new UDPBrocastReceiver();// 注册广播
		getContext().registerReceiver(receiver, filter);
		mProgressDialog = new ProgressDialog(getActivity());
		// 得到全局的sp
		sp = ((BApplication) getContext()).getSp();
		return view;
	}

	public class UDPBrocastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 说明:由于发送指令的时候带本ID号
			// 所以所以设备虽然都会收到该指令，但是不会对没有带自身ID号的指令作回应
			String action = intent.getAction();
			if (action.equals(Constant.SOCKET_BROCAST_ONRECEIVED)) {
				String message = intent.getStringExtra(Constant.SOCKET_BROCAST_EXTRA_MESSAGE);
				//
				LogUtilNIU.value("！！！！！！！！接收到了指令了！！！！！！！！！！！！！");
				if (buttonIsClick) {
					showContent(message);
				}
			} else {

			}
		}
	}

	public void showContent(String message) {
		LogUtilNIU.value("收到表地址数据---》" + message);
		// 681609050012010000
		buttonIsClick = false;
		BApplication.instance.setResendTaskShowBreak(true);
		if (message.substring(16, 20).equals("0000")) {
			checkThreadStop = true;// 查询线程停止
			// 对比两个ID是否一致
			if (message.substring(2, 14).equals(deviceId)) {
				// 结果可能可以加到服务器里面
				mProgressDialog.setMessage("表地址验证成功，正在保存设备的局域网信息");
				addMacAddress();
				// h.postDelayed(new Runnable() {
				//
				// @Override
				// public void run() {
				// mProgressDialog.dismiss();// 3秒后消失
				// ((SmartLinkActivity)getActivity()).finish();
				// }
				// }, 7000);
				// TODO 重新要求用户记录局域网信息
			} else {
				checkThreadStop = true;// 查询线程停止
				mProgressDialog.setMessage("智能连接成功，表地址验证失败\n,新接入的设备ID为" + message.substring(2, 14) + "而你刚才所选择的列表设备为"
						+ deviceId + "\n8秒后跳转至已绑设备列表");
				// mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setText("连接成功");
				// mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setEnabled(true);
				h.postDelayed(new Runnable() {

					@Override
					public void run() {
						mProgressDialog.dismiss();
						((SmartLinkActivity) getActivity()).finish();
					}
				}, 10000);
				// TODO 重新要求用户记录局域网信息
			}
		}
	}

	protected void bindViews() {
		explain_linearLayout = (LinearLayout) view.findViewById(R.id.explain_linearLayout);
		wifiLoginLinearLayout = (LinearLayout) view.findViewById(R.id.wifi_login_linearLayout);
		SsidEditText = (EditText) view.findViewById(R.id.etWifiName);
		passwordEditText = (EditText) view.findViewById(R.id.etPwd);
		addButton = (TextView) view.findViewById(R.id.tvAdd);
		llprogress = (LinearLayout) view.findViewById(R.id.llprogress);
		ivRmPwd = (ImageButton) view.findViewById(R.id.ivRmPwd);
	}

	@Override
	public void onClick(View v) {
		if (v == addButton) {
			//判断wifi是否打开
			if(mWifiAdmin.getWifiNetworkInfo().isConnected()){
			
			buttonIsClick = true;
			// 设置为手动连接中
			// WIFI名称
			apSsid = SsidEditText.getText().toString();
			apPassword = passwordEditText.getText().toString();
			// apBssid无线接入点的mac地址。得到路由器MAC地址
			String apBssid = mWifiAdmin.getWifiConnectedBssid();
			// 需求文档定义了这个参数是不隐藏的
			String isSsidHiddenStr = "NO";// 局域网是否隐藏
			// 我们定义每次适配的连接数默认为1
			String taskResultCountStr = Integer.toString(1);
			// 路由器wifi名，路由器mac地址，wifi密码，是否隐藏wifi名，建立连接的个数
			//wifi名称不能为空
			if(!TextUtils.isEmpty(apSsid)){
				new EsptouchAsyncTask3().execute(apSsid, apBssid, apPassword, isSsidHiddenStr, taskResultCountStr);
			}else{
				Toast.makeText(getContext(), getString(R.string.please_input_wifi_username), Toast.LENGTH_LONG).show();
			}
			
			}else {
				//wifi没有打开
				Toast.makeText(getContext(), getString(R.string.please_open_wifi), Toast.LENGTH_LONG).show();
			}
		}
		if (v == ivRmPwd) {
			// 控制密码为可见或不可见
			if (!mbDisplayFlg) { // 不可见状态
				passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				ivRmPwd.setImageDrawable(getResources().getDrawable(R.drawable.addfast_circle_filled));
			} else { // 可见状态
				passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
				ivRmPwd.setImageDrawable(getResources().getDrawable(R.drawable.addfast_circle));
			}
			mbDisplayFlg = !mbDisplayFlg;
			passwordEditText.postInvalidate();
		}
	}

	// 连接成功的回调
	protected void onEsptoucResultAddedPerform(final IEsptouchResult result) {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				String text = result.getBssid() + getResources().getString(R.string.connected);
//				Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
			}

		});
	}

	protected IEsptouchListener myListener = new IEsptouchListener() {

		@Override
		public void onEsptouchResultAdded(final IEsptouchResult result) {
			onEsptoucResultAddedPerform(result);
		}
	};

	/**
	 * 智能连接的主要逻辑
	 */
	protected class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

		protected IEsptouchTask mEsptouchTask;
		// without the lock, if the user tap confirm and cancel quickly enough,
		// the bug will arise. the reason is follows:
		// 0. task is starting created, but not finished
		// 1. the task is cancel for the task hasn't been created, it do nothing
		// 2. task is created
		// 3. Oops, the task should be cancelled, but it is running
		protected final Object mLock = new Object();

		// 连接前的显示
		@Override
		protected void onPreExecute() {
			// llprogress.setVisibility(View.VISIBLE);
			// rotaAnimation = (RotateAnimation)
			// AnimationUtils.loadAnimation(getContext(),
			// R.anim.animation_rotate);
			// tvCircleProgress.startAnimation(rotaAnimation);
			mProgressDialog.setMessage(getResources().getString(R.string.it_is_smarking_wait));
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					synchronized (mLock) {
						if (mEsptouchTask != null) {
							mEsptouchTask.interrupt();
						}
					}
				}
			});

			mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.connecting),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
			mProgressDialog.show();
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
		}

		@Override
		protected List<IEsptouchResult> doInBackground(String... params) {
			int taskResultCount = -1;
			synchronized (mLock) {
				String apSsid = params[0];
				String apBssid = params[1];
				String apPassword = params[2];
				String isSsidHiddenStr = params[3];
				String taskResultCountStr = params[4];
				boolean isSsidHidden = false;
				if (isSsidHiddenStr.equals("YES")) {
					isSsidHidden = true;
				}
				taskResultCount = Integer.parseInt(taskResultCountStr);
				mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, isSsidHidden, getActivity());
				mEsptouchTask.setEsptouchListener(myListener);
			}
			List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
			return resultList;
		}

		@Override
		protected void onPostExecute(List<IEsptouchResult> result) {

			IEsptouchResult firstResult = result.get(0);
			// check whether the task is cancelled and no results received
			if (!firstResult.isCancelled()) {
				int count = 0;
				// max results to be displayed, if it is more than
				// maxDisplayCount,
				// just show the count of redundant ones
				final int maxDisplayCount = 5;
				// the task received some results including cancelled while
				// executing before receiving enough results
				if (firstResult.isSuc()) {
					LogUtilNIU.value("智能连接成功*********");
					// 把WIFI的密码记录一下
//					editor.putString(apSsid, apPassword);
//					editor.commit();
					SharedPreferences sharedPref = getActivity().getSharedPreferences(Config.FILE,
							Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString(Config.WIFIPWD, apPassword);
					editor.putString(Config.WIFINAME, apSsid);
					editor.commit();
					// 智能连接成功，把现在局域网的路由器地址和设备捆绑起来
					linkSuccess = true;
					BApplication.instance.setDeviceDataHasChanged(true);
					StringBuilder sb = new StringBuilder();
					for (IEsptouchResult resultInList : result) {
						sb.append("智能连接成功" + "3秒后开始校对表地址");
						count++;
						if (count >= maxDisplayCount) {
							break;
						}
					}

					if (count < result.size()) {
						sb.append("\nthere's " + (result.size() - count) + " more result(s) without showing\n");
					}

					// 智能连接成功后，progress提示改变
//					mProgressDialog.setMessage(sb.toString());
					// 保存局域网信息
//					h.postDelayed(new Runnable() {
//						public void run() {
//							checkID();// 读表地址
//							LogUtilNIU.value("3秒过了，开始读表地址");
//						}
//					}, 3000);
					Message msg = h.obtainMessage();
					msg.what = 300;// DIALOG
					h.sendMessage(msg);
				} else {
					buttonIsClick = false;
					mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
					mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
							.setText(getResources().getString(R.string.sumit));
					linkSuccess = false;// 连接不成功
					mProgressDialog.setMessage(getResources().getString(R.string.connect_fail));
				}
			}
		}
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("极速添加"); // 统计页面，"MainScreen"为页面名称，可自定义
	}

	public void checkID() {
		if (buttonIsClick) {
			// 校队表地址
			new CheckVerAddress().start();
		}
	}

	// 通过服务器，设备信息记录当前设备局域网的BSSID
	Boolean retryAddMac = false;

	public void addMacAddress() {
		// 获得当前局域网的BSSID
		final String routerSSID = NetWorkStateUtil.getRouterBSSID(getContext());
		// 传入设备id和设备所在局域网路由器的mac地址
		LogUtilNIU.value("setDeviceInfo.act-device.id->" + deviceId + "-device.wifibssid-" + routerSSID);
		new Enterface("setDeviceInfo.act").addParam("device.id", deviceId).addParam("device.wifibssid", routerSSID)
				.doRequest(new JsonClientHandler2() {

					@Override
					public void onInterfaceSuccess(String message, String contentJson) {
						mProgressDialog.setMessage("保存局域网信息成功，5秒后跳转到设备列表页面");
						LogUtilNIU.value("设备" + deviceId + "新保存的局域网地址为" + routerSSID);
						// 智能连接成功以后校对表地址的线程1.5秒后执行
						h.postDelayed(new Runnable() {

							@Override
							public void run() {
								mProgressDialog.dismiss();// 3秒后消失
								((SmartLinkActivity) getActivity()).finish();
							}
						}, 7000);
					}

					@Override
					public void onInterfaceFail(String json) {
						LogUtilNIU.value("设置bssid接口错误-->" + json);
						if (retryAddMac == false) {// 连接不成功重试
							mProgressDialog.setMessage("系统繁忙!无法记录局域网信息，正在重试");
							addMacAddress();
						} else {// 重试不成功后执行以下
							mProgressDialog.setMessage("系统繁忙!无法记录局域网信息,请稍候手动记录，5秒后跳转到在线列表");
							h.postDelayed(new Runnable() {

								@Override
								public void run() {
									mProgressDialog.dismiss();// 3秒后消失
									((SmartLinkActivity) getActivity()).finish();
								}
							}, 7000);
						}
						retryAddMac = true;
						// h.postDelayed(new Runnable() {
						//
						// @Override
						// public void run() {
						// mProgressDialog.dismiss();// 3秒后消失
						// ((SmartLinkActivity)getActivity()).finish();
						// }
						// }, 7000);
					}

					@Override
					public void onFailureConnected(Boolean canConnect) {
						LogUtilNIU.value("设置bssid接口无法连接" + canConnect);
						mProgressDialog.setMessage("网络超时，保存局域网信息失败,请稍候手动记录，5秒后跳转到在线列表");
						h.postDelayed(new Runnable() {

							@Override
							public void run() {
								mProgressDialog.dismiss();// 3秒后消失
								((SmartLinkActivity) getActivity()).finish();
							}
						}, 7000);
					}
				});
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("极速添加");
	}

	@Override
	public void onDestroy() {
		getContext().unregisterReceiver(receiver);
		super.onDestroy();
	}

	/**
	 * 查表地址 这里修改为不用查表地址命令去做 改为用查实时数据去做
	 */
	protected void checkVerAddress() {
		String verCode = ModbusCalUtil.verNumber(deviceId + "00000000");
//		String msg = "E7" + deviceId + "00000000" + verCode + "0D";
		String msg ="E7AAAAAAAAAAAA00000000FC0D";
		// 3A693228572211000000004D0D
		LogUtilNIU.value("*****发送查询实时参数的命令******" + msg);
		// 点击了确定以后所执行的操作
		try {
			BApplication.instance.sendUDPMsg(msg);
			new UDPReceiveThread().start(); // 初始化接收UDP消息的线程
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 此线程在application内运行，一收到有UDP消息就会向外发广播。
		private class UDPReceiveThread extends Thread {
			@Override
			public void run() {
				super.run();
				while (true) {
					try {
						byte data[] = new byte[256];
						InetAddress serverAddress = InetAddress.getByName("122.10.97.35"); // 设置地址
						DatagramPacket dpReceive = new DatagramPacket(data, data.length,
								InetAddress.getByName("22.10.97.35"), 34500);
						BApplication.multicastSocket.receive(dpReceive);
						InetAddress address = BApplication.multicastSocket.getInetAddress();
						byte[] r = dpReceive.getData();
						String resultMsg = new String(dpReceive.getData(),
								dpReceive.getOffset(), dpReceive.getLength());
						resultMsg = ModbusCalUtil.bytesToHexString(r);
						resultMsg = resultMsg.toUpperCase();
						// LogUtilNIU.value("接收到的16进制数据数据转为String--->"+resultMsg);
						// resultMsg为收到的消息
						// 收到消息后，发广播，指定Constant.SOCKET_BROCAST_ONRECEIVED为广播频道
						// 在任何地方注册了这个频道的广播接收者都可以收到这条消息
						// if(resultMsg.contains("0D")){
						// int a = resultMsg.indexOf("0D");
						// resultMsg =resultMsg.substring(0,a+2);
						// }
						BApplication.instance.setIsAtHome(true);
						resultMsg = resultMsg.replace(" ", "");// 去掉字符中间的空格
						LogUtilNIU.value("数据去掉空格处理后为" + resultMsg);
						Intent intent = new Intent();
						intent.setAction(Constant.SOCKET_BROCAST_ONRECEIVED);
						intent.putExtra(Constant.SOCKET_BROCAST_EXTRA_MESSAGE,
								resultMsg);
//						sendBroadcast(intent);// 接收到数据后发广播
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}


	public class CheckVerAddress extends Thread {
		@Override
		public void run() {
			checkThreadStop = false;
			for (int i = 0; i <= 8; i++) {
				if (checkThreadStop) {
					break;
				}
				if (i == 0) {
					checkVerAddress();
				} else if (i == 1) {
					if (!checkThreadStop) {// 没有查到继续查
						checkVerAddress();
					}
				} else if (i == 2) {
					if (!checkThreadStop) {// 没有查到继续查
						checkVerAddress();
					}

				} else if (i == 3) {
					if (!checkThreadStop) {// 没有查到继续查
						checkVerAddress();
					}
				}

				else if (i == 4) {
					if (!checkThreadStop) {// 没有查到继续查
						checkVerAddress();
					}
				}

				else if (i == 5) {
					if (!checkThreadStop) {// 没有查到继续查
						checkVerAddress();
					}
				}

				else if (i == 8) {
					if (!checkThreadStop) {
						Message msg = h.obtainMessage();
						msg.what = 200;// DIALOG
						h.sendMessage(msg);
					}
				}
				try {
					Thread.sleep(2500);// 每2秒查一次
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			super.run();
		}

	}

}
