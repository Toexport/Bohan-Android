package cn.mioto.bohan.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.exception.ExceptionManager;
import cn.mioto.bohan.fragment.SingleDeviceDayDataFragment;
import cn.mioto.bohan.fragment.SingleDeviceMonthDataFragment;
import cn.mioto.bohan.fragment.SingleDeviceYearDataFragment;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.ExceptionUtil;
import cn.mioto.bohan.utils.HttpUtils;
import cn.mioto.bohan.utils.LoadDataThreadUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.NetworkUtils;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;

import com.mioto.bohan.webservice.WebServiceClient;
import com.umeng.analytics.MobclickAgent;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import steed.framework.android.client.JsonClientHandler2;

/**
 * 类说明：单个设备的用电统计页面 ，已经改好UDP，差显示图表 此页面包括3个不可Fragment
 */
public class MenuSingleDeviceSumActivity extends BaseCheckDataMenuActivity {
	/*********** MODBUS协议参数 ************************************/
	private String endNum = "0D";// 结束符
	/********** DECLARES *************/
	private List<TextView> tvList = new ArrayList<TextView>();
	private LinearLayout tag_ll;
	private TextView tvDayData;
	private TextView tvMonthData;
	private TextView tvYearData;
	private TextView tvName;
	private TextView tvLocation;
	private TextView tvID;
	private ImageView ivCursor;
	private TextView menu_tv;
	private LinearLayout llContainer;
	private Fragment[] fragments = new Fragment[3];
	Fragment singleDeviceDayDataFragment, singleDeviceMonthDataFragment,
			singleDeviceYearDataFragment;
	int currentFragmentIndex = 0;
	int clickIndex = 0;
	int offset; // 下标的偏移量
	int currIndex = 0;// 当前页卡编号
	int ivWide;// 下标宽度
	int linerLayoutW;
	View view;
	/******** 查询到的6组数据 ******************************************/
	public String m12MonthPower;
	public String m30DayPower;
	public String m24HourPower;
	public String m12MonthRate;
	public String m30DayRate;
	public String m24HourRate;
	private ProgressDialog p2;
	private Handler handler = new Handler();

	/********* 广播接收者 ******************************************/
	private SocketBrocastReceiver receiver;
	private int udpKind = 0;

	public static Socket socket;
	public static BufferedReader in;
	public static PrintWriter out;
	public ReceiveThread receiveThread;

	public Handler getHandler() {
		return h;
	}

	private Handler h = new Handler() {// 另一个hanlder
		public void handleMessage(Message msg) {
			// if(msg.what==Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK){
			// int kind = msg.arg1;
			// // kind 分别代表 年 ，月 ，日 查询
			// if(kind ==0){
			// udpKind =0;
			// }else if (kind ==1){
			// udpKind =1;
			// }else if (kind ==2){
			// udpKind =2;
			// }
			// //加载到数据后正在加载消失
			// }else if
			// (msg.what==Constant.MSG_WHAT_SHOW_DIALOG_OF_RESEND_TASK){
			//
			// }else
			// if(msg.what==Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK_FAIL){
			// LogUtilNIU.value("*****************收到Handler消息");;
			// progressGettingDataDismiss("数更新失败");
			// }else
			// if(msg.what==Constant.MSG_WHAT_SEND_MSG_BY_SERVICE_RESEND_TASK_FAIL){
			// //通过服务器去查询信息
			// //通过服务器查询设备实时数据
			// checkByService();
			// }

			if (msg.what == Constant.MSG_SUCCESS) {
				progressDismiss("");
				String message = (String) msg.obj;
				onServiceUDPBack(message);
			} else if (msg.what == Constant.MSG_EXCPTION) {
				progressGettingDataDismiss("数更新失败");
			}  else if(msg.what == Constant.MSG_FAILURE){
				progressGettingDataDismiss("设备已离线");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/******** 初始化toolbar *******************************************/
		ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title,
				R.string.device_name, true, true, R.id.menu_tv, R.string.update);
		ViewUtil.setToolbarTitle(getResources().getString(R.string.power_info));
		/***************************************************/
		// 头显示
		tvName.setText(deviceName);
		tvLocation.setText(currentDevice.getDeviceLocation());
		tvID.setText(deviceId);
		/***************************************************/
		tvList.add(tvDayData);
		tvList.add(tvMonthData);
		tvList.add(tvYearData);
		singleDeviceDayDataFragment = new SingleDeviceDayDataFragment();
		singleDeviceMonthDataFragment = new SingleDeviceMonthDataFragment();
		singleDeviceYearDataFragment = new SingleDeviceYearDataFragment();
		fragments[0] = singleDeviceDayDataFragment;
		fragments[1] = singleDeviceMonthDataFragment;
		fragments[2] = singleDeviceYearDataFragment;
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.add(R.id.llContainer, singleDeviceDayDataFragment);// 设置显示第一个fragment
		transaction.commit();
		textViewChangeColor(0);// 设置第一个textView变成蓝色，实现被默认选择的效果。
		initImageView();// 初始化游标的位置
		// 让主线程代码延迟
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				setListener();// 由于事件监听的参数有view控件大小等需要延迟获得的数据，所以监听需要延迟执行
			}
		}, 100);
		/********* 初始化广播 ******************************************/
		// receiver = new SocketBrocastReceiver();
		// registerReceiver(receiver,filter);
		/***************************************************/
		// 查询该设备的日数据，其余数据分布在其他fragment查询

		// 初始化Socket
		socket = BApplication.instance.getSocket();
		// checkNowData();//进入页面就发UDP请求
	}

	/**********************************************************/
	/**
	 * 发送查询实时数据指令，新指令
	 */
	public void checkNowData() {
		// 显示正在加载
		progressGettingDataShow("实时数据加载中");
		String verCode = ModbusCalUtil.verNumber(deviceId + "00100000");
		final String message = "E7" + deviceId + "00100000" + verCode + "0D";// 0001
																				// 查询用电参数
		// new LoadDataThreadUtil(message,h,deviceDBssid,this).start();
		BApplication.instance.socketSend(message);
		try {
			BApplication.instance.socket.setSoTimeout(10 * 1000);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		receiveThread = new ReceiveThread();
		receiveThread.start();
	}

	// protected void checkNowDataFromService(String smessage) {
	//
	// new Enterface("sendToDevice.act").addParam("deviceid",
	// deviceId).addParam("String content", smessage).doRequest(new
	// JsonClientHandler2() {
	// @Override
	// public void onInterfaceSuccess(String message, String contentJson) {
	// LogUtilNIU.e("查询设备实时数据，服务器返回content--->"+contentJson);
	// Intent intent = new Intent();
	// intent.setAction(Constant.SOCKET_BROCAST_ONRECEIVED);
	// intent.putExtra(Constant.SOCKET_BROCAST_EXTRA_MESSAGE, contentJson);
	// //发送服务器传的数据，暂时也当作为UPD数据来传给fragment
	// //收到服务器数据后发送广播给指定fragment
	// sendBroadcast(intent);
	// }
	//
	// @Override
	// public void onInterfaceFail(String json) {
	//
	// }
	//
	// @Override
	// public void onFailureConnected(Boolean canConnect) {
	// // TODO Auto-generated method stub
	//
	// }
	// });
	// }

	/**
	 * 
	 * @ClassName: SocketBrocastReceiver
	 * @Description: 当收到设备返回的UDP数据时，作处理
	 * @author jiemai liangminhua
	 * @date 2016年6月29日 下午2:39:13
	 * 
	 */
	public class SocketBrocastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 区分接收到的是哪种广播
			String action = intent.getAction();
			if (action.equals(Constant.SOCKET_BROCAST_ONRECEIVED)) {
				String message = intent
						.getStringExtra(Constant.SOCKET_BROCAST_EXTRA_MESSAGE);
				LogUtilNIU.e("返回的数据总位数为" + message.length());
				// if(content.length()<XX){
				// LogUtilNIU.e("返回的数据位数出错，总位数为");
				// return;//不向下执行
				// }
				// 这里只接收，不处理，要在fragment里真正接收和处理
				if (message.substring(2, 14).equals(deviceId)) {
					if (isReqCodeEqual(message, "0010")) {// 判断请求码
						udpok = true;
					} else if (isReqCodeEqual(message, "0011")) {
						udpok2 = true;
					}
				} else {
					// 收到其他
					// 在这个设备的查询界面接收到其他Id的数据
					LogUtilNIU.e("在" + deviceId + "的界面接收到其他设备"
							+ message.substring(2, 14) + "的数据");
				}
			}
			// else if (action.equals(Constant.BROCAST_CHART_SHOW_DIALOG)){
			// //收到出现progressDialog的广播
			// // ToastUtils.testToast(MenuSingleDeviceSumActivity.this,
			// "收到广播");
			// if(clickIndex==0){
			// progressGettingDataShow("正在查询日数据");
			// }else if (clickIndex==1){
			// progressGettingDataShow("正在查询月数据");
			// }else if (clickIndex==2){
			// progressGettingDataShow("正在查询年数据");
			// }
			// }
			else if (action.equals(Constant.BROCAST_CHART_DIMISS_DIALOG)) {
				BApplication.instance.setResendTaskShowBreak(true);
				progressGettingDataDismissNoToast();
			} else if (action.equals(Constant.BROCAST_BEGIN_LOAD_SHOW_DIALOG)) {

			} else if (action.equals(Constant.BROCAST_BEGIN_LOAD_SUS)) {

			} else if (action.equals(Constant.BROCAST_BEGIN_LOAD_FAIL)) {
				progressGettingDataDismiss("数更新失败");
			} else if (action
					.equals(Constant.BROCAST_BEGIN_LOAD_LOAD_BY_SERVICE)) {
				// 通过服务器去查询信息
				// 通过服务器查询设备实时数据
				// 根据回调消息去服务器查询数据
				List<String> msgs = new ArrayList<>();
				msgs = (List<String>) intent.getExtras().get(
						Constant.BROCAST_BEGIN_LOAD_LOAD_BY_SERVICE_MSG_KEY);
				checkByService(msgs.get(0), msgs.get(1));
			}
		}
	}

	/**
	 * 初始化游标位置 初始游标的长度，最初位置
	 */
	private void initImageView() {
		ViewTreeObserver vto = ivCursor.getViewTreeObserver();

		/**
		 * 这个回调接口使view的计算推迟到view被加载完毕 在view改变后回调
		 */
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				ivCursor.getViewTreeObserver().removeGlobalOnLayoutListener(
						this);
				ivWide = ivCursor.getWidth();// 游标长度
				linerLayoutW = tag_ll.getWidth();// 外围ll宽度
				offset = (linerLayoutW / 3 - ivWide) / 2;// 获取图片偏移量
				/*
				 * cursor的初始位置设定： 思路 屏幕总宽减去LinerLayout宽度再除以2在加上offset就得到初始位置
				 */
				DisplayMetrics dm = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(dm);
				int screenW = dm.widthPixels; // 获取手机屏幕宽度分辨率
				int initX = (screenW - linerLayoutW) / 2 + offset;
				// 计算出初始位置后，设置控件的位置
				LinearLayout.MarginLayoutParams margin = new LinearLayout.MarginLayoutParams(
						ivCursor.getLayoutParams());
				margin.setMargins(initX, margin.topMargin, margin.rightMargin,
						margin.bottomMargin);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						margin);
				ivCursor.setLayoutParams(params);
			}
		});
	}

	/**
	 * 设置tag被选中的蓝色字体效果
	 */
	private void textViewChangeColor(int i) {
		switch (i) {
		case 0:
			tvDayData.setTextColor(getResources().getColor(R.color.darkBlue));
			tvMonthData.setTextColor(getResources()
					.getColor(R.color.middleGray));
			tvYearData
					.setTextColor(getResources().getColor(R.color.middleGray));
			break;
		case 1:
			tvDayData.setTextColor(getResources().getColor(R.color.middleGray));
			tvMonthData.setTextColor(getResources().getColor(R.color.darkBlue));
			tvYearData
					.setTextColor(getResources().getColor(R.color.middleGray));
			break;
		case 2:
			tvDayData.setTextColor(getResources().getColor(R.color.middleGray));
			tvMonthData.setTextColor(getResources()
					.getColor(R.color.middleGray));
			tvYearData.setTextColor(getResources().getColor(R.color.darkBlue));
			break;
		}

	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_menu_power_total;
	}

	@Override
	public void onClick(View v) {
		if (v == menu_tv) {
			refresh();
		}

		switch (v.getId()) {
		case R.id.tvDayData:
			clickIndex = 0;
			break;
		case R.id.tvMonthData:
			clickIndex = 1;
			break;
		case R.id.tvYearData:
			clickIndex = 2;
			break;
		}

		if (v == tvDayData || v == tvMonthData || v == tvYearData) {
			if (clickIndex != currentFragmentIndex) {
				Fragment showFragment = fragments[clickIndex];
				FragmentTransaction transaction = getSupportFragmentManager()
						.beginTransaction();
				if (!showFragment.isAdded()) {
					transaction.add(R.id.llContainer, showFragment);

				}
				transaction.hide(fragments[currentFragmentIndex]);
				transaction.show(showFragment);
				transaction.commit();
				textViewChangeColor(clickIndex);
				currentFragmentIndex = clickIndex;
			}
			/*
			 * 计算出标签的偏移量 标签偏移量大小为offset * 2 + ivWide
			 */
			int one = offset * 2 + ivWide;
			// 初始化移动的动画（从当前位置，x平移到即将要到的位置）
			// 这个动画设定了平移效果，从哪个位置移动到哪个位置，记录了上次的位置，计算出下次的位置
			Animation animation = new TranslateAnimation(currIndex * one,
					clickIndex * one, 0, 0);
			currIndex = clickIndex;
			animation.setFillAfter(true); // 动画终止时停留在最后一帧，不然会回到没有执行前的状态
			animation.setDuration(200); // 动画持续时间，0.2秒
			ivCursor.startAnimation(animation); // 是用imageview来显示动画
		}

		super.onClick(v);
	}

	private void refresh() {// 刷新数据
		// TODO Auto-generated method stub
		// 判断当前是哪个数据页被显示
		if (clickIndex == 0) {// 发送日数据查询项
			refreshDayData();//
		} else if (clickIndex == 1) {// 发送月数据查询项
			refreshMonthData();
		} else if (clickIndex == 2) {// 发送年数据查询项
			refreshYearData();
		}
	}

	private void refreshDayData() {
		((SingleDeviceDayDataFragment) singleDeviceDayDataFragment)
				.checkDataAndUpdateCharts();
	}

	private void refreshYearData() {
		((SingleDeviceYearDataFragment) singleDeviceYearDataFragment)
				.checkDataAndUpdateCharts();
	}

	private void refreshMonthData() {
		((SingleDeviceMonthDataFragment) singleDeviceMonthDataFragment)
				.checkDataAndUpdateCharts();// 获得数据
	}

	@Override
	protected void onDestroy() {
		if(receiver!=null){
			unregisterReceiver(receiver);
		}
		super.onDestroy();
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
	protected String onServiceUDPBack(String content) {
		if (checkUDPMessage(content)) {
			if (isReqCodeEqual(content, "0010")
					|| isReqCodeEqual(content, "0011")) {
				// 日数据
				((SingleDeviceDayDataFragment) singleDeviceDayDataFragment)
						.showContent(content);
			} else if (isReqCodeEqual(content, "000E")
					|| isReqCodeEqual(content, "000F")) {
				// 月数据
				((SingleDeviceMonthDataFragment) singleDeviceMonthDataFragment)
						.showContent(content);
			} else if (isReqCodeEqual(content, "000C")
					|| isReqCodeEqual(content, "000D")) {
				// 年数据
				((SingleDeviceYearDataFragment) singleDeviceYearDataFragment)
						.showContent(content);
			}
		}
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
						}else {
							msg.what = Constant.MSG_FAILURE;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					msg.obj = e;
					msg.what = Constant.MSG_EXCPTION;
				}
			h.sendMessage(msg);
		}
	}

}
