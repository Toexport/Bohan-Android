package cn.mioto.bohan;

import cn.mioto.bohan.activity.BaseBrocastReceiverActivity;
import cn.mioto.bohan.utils.ToastUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
/**
 * @ClassName: TestBrocastActivity 
 * @Description: 测试广播 
 */
public class TestBrocastActivity extends BaseBrocastReceiverActivity {
	private SocketBrocastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_brocast);
		/*********初始化广播******************************************/
		receiver = new SocketBrocastReceiver();
		registerReceiver(receiver,filter);
	}
	
	public class SocketBrocastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			//区分接收到的是哪种广播
			String action = intent.getAction();
			if(action.equals(Constant.SOCKET_BROCAST_ONCONNECTED)){
				ToastUtils.shortToast(TestBrocastActivity.this, "连接成功");
				socketClient.sendString("Test START");
			}
			
			else if(action.equals(Constant.SOCKET_BROCAST_ONRECEIVED)){
				String message=intent.getStringExtra(Constant.SOCKET_BROCAST_EXTRA_MESSAGE);
				ToastUtils.shortToast(TestBrocastActivity.this, "收到消息----"+message);
			}
			else if(action.equals(Constant.SOCKET_BROCAST_DISCONNECT)){
				ToastUtils.shortToast(TestBrocastActivity.this, "连接断开");
			}
		}

	}
	/**********************************************************/
	/**
	 * 取消注册广播
	 */
	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

}
