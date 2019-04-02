package cn.mioto.bohan.fragment;

import java.util.ArrayList;
import java.util.List;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.utils.LoadDataThreadUtil;
import cn.mioto.bohan.utils.ModbusCalUtil;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

/** 
 * 类说明：智能连接wifi,快速添加Fragment
 * 
 * 很重要的一步
 */
public class LinkFastAddFragment extends BaseSmartLinkFragment implements OnClickListener{

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// 显示已经连接的wifi名称
		String apSsid = mWifiAdmin.getWifiConnectedSsid();
		if (apSsid != null) {
			SsidEditText.setText(apSsid);
		} else {
			//重sp里尝试获取路由器密码
			String password = sp.getString(apSsid, "no");
			if(password.equals("no")){
				SsidEditText.setText("");
			}else{
				SsidEditText.setText(password);
			}
				
		}
		// check whether the wifi is connected 
		boolean isApSsidEmpty = TextUtils.isEmpty(apSsid);
		//addButton.setEnabled(!isApSsidEmpty);
	}
}
