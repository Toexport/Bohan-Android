package cn.mioto.bohan.activity;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ToastUtils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * 类说明：UDP Activity 基类 进行操作UDP 数据的Activity的初始化
 * 
 */
public class BaseUDPActivityNoCurrent extends BaseActivity {
	protected IntentFilter filter;
	protected BApplication app;
	/**********************************************************/
	protected Boolean udpok;// 判断发出的UDP有没有收到返回数据
	protected Boolean udpok2;// 判断发出的UDP有没有收到返回数据,并列发送数据时使用

	/***************************************************/
	protected long loadOnceDur;// 每次一轮加载相隔的时间
	protected long udpSendDis;// UDP之间间隔的时间
	protected long udpNoneRecieveTime;// UDP之间间隔的时间
	protected ProgressDialog p;

	private static final int BROCAST_TIME_OUT_UDP_CURRENT = 51;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		p = new ProgressDialog(this);
		app = BApplication.instance;
		/****** 得到app用于调用app里面发UDP的方法 *********************************************/
		/******* 初始化接收频道 ********************************************/
		filter = new IntentFilter();
		filter.addAction(Constant.SOCKET_BROCAST_ONRECEIVED);

	}

	/***************************************************/
	// 父类的progressDialog方法，可在子类调用
	protected Boolean refreshingOk = true;

	protected void progressShow(String message) {
		refreshingOk = false;
		p.setMessage(message);
		p.setCancelable(false);
		p.setCanceledOnTouchOutside(false);
		p.show();
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(Constant.REQUEST_TIME_OUT);
					p.dismiss();
					if (!gettingDataOk) {
						//handler.sendEmptyMessage(BROCAST_TIME_OUT_UDP_CURRENT);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	protected void progressDismiss(String message) {
		if (!refreshingOk) {
			refreshingOk = true;
			p.dismiss();
			ToastUtils.shortToast(this, message);
		}
	}

	/***************************************************/
	// 获取数据的dialog效果
	protected Boolean gettingDataOk = true;

	protected void progressGettingDataShow(String message) {
		gettingDataOk = false;
		p.setMessage(message);
		p.setCancelable(true);
		p.setCanceledOnTouchOutside(false);
		p.show();
		// new CountingThread().start();
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(Constant.REQUEST_TIME_OUT);
					p.dismiss();
					if (!gettingDataOk) {
						 Intent intent = new Intent(Constant.BROCAST_ONLINE_LIST_DIMISS_DIALOG);
						 sendBroadcast(intent);
//						 handler.sendEmptyMessage(BROCAST_TIME_OUT_UDP_CURRENT);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	protected boolean countingToDismiss = false;

	// 用来计时的线程
	public class CountingThread extends Thread {

		@Override
		public void run() {
			super.run();
			while (!countingToDismiss) {
				try {
					Thread.sleep(Constant.REQUEST_TIME_OUT);// 15秒后转圈自动消失
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!countingToDismiss) {
					if (!gettingDataOk) {
						p.dismiss();
						gettingDataOk = true;
						// ToastUtils.shortToast(BaseUDPActivityNoCurrent.this,
						// getResources().getString(R.string.loading_data_fail));
						LogUtilNIU
								.value("因为线程关系，这里用handler的timeout判断转圈消失了！！！！！！！！");
						countingToDismiss = true;
					}
				}
			}
		}
	}

	protected void progressGettingDataDismiss(String message) {
		if (!gettingDataOk) {
			gettingDataOk = true;
			p.dismiss();
			ToastUtils.shortToast(this, message);
			LogUtilNIU.value("控制线程不要运行的要素停止");
			countingToDismiss = true;
		}
	}

	protected void progressGettingDataDismissNoToast() {
		if (!gettingDataOk) {
			gettingDataOk = true;
			p.dismiss();
			countingToDismiss = true;
		}
	}

	/**
	 * 需要传入的参数:UDP返回的数据，（带请求码判断）
	 * 
	 * @Title: checkUDPMessage
	 * @Description: 判断 长度 id 状态码 请求码 是否符合
	 * 
	 * @return Boolean
	 * @throws
	 */
	protected Boolean checkUDPMessage(String content, String reqCode, String mId) {
		Boolean checkUDPMessageOK = false;
		LogUtilNIU.e("返回的数据总位数为" + content.length());
		// if(content.length()<87){
		// LogUtilNIU.e("返回的数据位数出错，总位数为");
		// return;//不向下执行
		// }
		String statCode = content.substring(14, 16);// 状态码
		String id = content.substring(2, 14);// ID
		if (id.equals(mId)) {// 判断接收到的信息的ID
			if (statCode.equals("00")) {// 状态是否成功
				if (content.substring(16, 20).equals(reqCode)) {// 请求码
					checkUDPMessageOK = true;
				}
			} else if (statCode.equals("03")) {
				LogUtilNIU.e("发送指令有错，返回03");
			}
		} else {
			// 在这个设备的查询界面接收到其他Id的数据
			LogUtilNIU.e("在" + mId + "的界面接收到其他设备" + id + "的数据");
		}
		return checkUDPMessageOK;// 如果没有在if内赋值，会return false
	}

	/**
	 * 需要传入的参数:UDP返回的数据(不带请求码判断)
	 * 
	 * @Title: checkUDPMessage
	 * @Description: 判断 长度 id 状态码是否符合
	 * 
	 * @return Boolean
	 * @throws
	 */
	protected Boolean checkUDPMessage(String content, String mId) {
		Boolean checkUDPMessageOK = false;
		LogUtilNIU.e("返回的数据总位数为" + content.length());
		// if(content.length()<87){
		// LogUtilNIU.e("返回的数据位数出错，总位数为");
		// return;//不向下执行
		// }
		String statCode = content.substring(14, 16);// 状态码
		String id = content.substring(2, 14);// ID
		if (id.equals(mId)) {// 判断接收到的信息的ID
			if (statCode.equals("00")) {// 状态是否成功
				checkUDPMessageOK = true;
			} else if (statCode.equals("03")) {
				LogUtilNIU.e("发送指令有错，返回03");
			}
		} else {
			// 在这个设备的查询界面接收到其他Id的数据
			LogUtilNIU.e("在" + mId + "的界面接收到其他设备" + id + "的数据");
		}
		return checkUDPMessageOK;// 如果没有在if内赋值，会return false
	}

	/**
	 * @Title: isReqCodeEqual
	 * @Description:判断请求码是否相等
	 * @return Boolean
	 * @throws
	 */
	protected Boolean isReqCodeEqual(String message, String reqCode) {
		// 3A 691283749267 00 0002 0001 01 6F 0D
		Boolean isReqCodeOK = false;
		if (message.substring(16, 20).equals(reqCode)) {// 请求码
			isReqCodeOK = true;
		}
		return isReqCodeOK;
	}

	private Handler handler = new Handler() {// 另一个hanlder
		public void handleMessage(Message msg) {
			if (msg.what == BROCAST_TIME_OUT_UDP_CURRENT) {
				Log.d("exception_sockettimeout_onLIne--------->", "exception_sockettimeout--------->Online");
				if(p != null){
					ToastUtils.shortToast(
							BaseUDPActivityNoCurrent.this,
							getResources().getString(
									R.string.exception_sockettimeout));
				}
			}
		}
	};
}
