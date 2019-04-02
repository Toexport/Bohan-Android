package cn.mioto.bohan.fragment;

import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.activity.FAQActivity;
import cn.mioto.bohan.activity.InstructionActivity;
import cn.mioto.bohan.activity.MainActivity;
import cn.mioto.bohan.activity.ScanActivity;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.ExceptionUtil;
import cn.mioto.bohan.utils.LogUtilNIU;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import steed.framework.android.client.JsonClientHandler2;
import steed.framework.android.core.ContextUtil;
import steed.framework.android.util.ToastUtil;
import steed.framework.android.util.base.PathUtil;
/** 
 * 类说明：个人中心Fragment
 */
public class MainMeFragment extends BaseFragment implements OnClickListener{
	View view;
	/**********DECLARES*************/
	private TextView tvUserName;
	private TextView tvVersion;
	private LinearLayout llOut;
	private LinearLayout addDevice;
	private LinearLayout llInstruction;
	private LinearLayout llFaq;
	private LinearLayout llVersionUpdate;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view =inflater.inflate(R.layout.fragment_main_me, null);
		bindViews();
		tvUserName.setText(((BApplication)getContext()).getCurrentUser().getUserName());
		tvVersion.setText(getVersionName());
		llOut.setOnClickListener(this);
		llInstruction.setOnClickListener(this);
		llFaq.setOnClickListener(this);
		llVersionUpdate.setOnClickListener(this);
		return view;
	}

	private void bindViews() {
		tvUserName = (TextView) view.findViewById(R.id.tvUserName);
		tvVersion = (TextView) view.findViewById(R.id.tvVersion);
		llOut = (LinearLayout) view.findViewById(R.id.llOut);
		llInstruction = (LinearLayout) view.findViewById(R.id.llInstruction);
		llFaq = (LinearLayout) view.findViewById(R.id.llFaq);
		llVersionUpdate = (LinearLayout) view.findViewById(R.id.llVersionUpdate);
	}

	@Override
	public void onClick(View v) {
		if(v==llOut){
			//弹出一个对话框
			AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(getActivity());
			alertDialogBuilder.setMessage(getResources().getString(R.string.exit_account));//设置显示文本
			alertDialogBuilder.setPositiveButton(R.string.cancle, null);
			alertDialogBuilder.setNegativeButton(R.string.sumit, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					Intent intent = new Intent(getActivity(),MainActivity.class);
					intent.putExtra(MainActivity.TAG_EXIT, true);//把意图设置为退出意图
					startActivity(intent);
				}
			});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}else if(v==addDevice){
			LogUtilNIU.e("点击了添加设备");
			Intent intent = new Intent(getContext(),ScanActivity.class);
			startActivity(intent );
		}else if(v==llInstruction){
			Intent intent = new Intent(getContext(),InstructionActivity.class);
			startActivity(intent);
		}else if (v==llFaq){
			Intent intent = new Intent(getContext(),FAQActivity.class);
			startActivity(intent);
		}else if (v==llVersionUpdate){
			//checkUpdate();
		}
	}
	
		//查询有没有新版本
		private void checkUpdate() {
			int versionCode = ContextUtil.getVersionCode();
			new Enterface("getLatestVersion.act").addParam("versioncode", versionCode).doRequest(new JsonClientHandler2() {
				@Override
				public void onInterfaceSuccess(String message, final String contentJson) {
					LogUtilNIU.value("版本控制打印的信息为--->"+contentJson);
					//{"versionname":"1.0","url":"\/upload\/0279ad5cc9f19c55a76084c6cb1d333176\/file\/8588e02a7cbe959f7fbb485435c2cdd7.apk"}
					//自定义版本更新提醒dialog
					final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();  
					alertDialog.setCanceledOnTouchOutside(false);
					alertDialog.show();  
					Window window = alertDialog.getWindow();  
					window.setContentView(R.layout.dialog_version_control_update);  
					TextView tvDownLoad = (TextView) window.findViewById(R.id.tvDownLoad);
					TextView tvNotDownLoad = (TextView) window.findViewById(R.id.tvNotDownLoad);
					tvDownLoad.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO 下载更新
							LogUtilNIU.value("点击了下载更新");
							new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										JSONObject jsonObject = new JSONObject(contentJson);
										DownloadManager dm = (DownloadManager)getActivity().getSystemService(Activity.DOWNLOAD_SERVICE);
										Request request = new Request(Uri.parse(PathUtil.mergePath(Constant.baseUrl, jsonObject.getString("url"))));
										request.setNotificationVisibility(Request.VISIBILITY_VISIBLE);
										PackageManager pm =getActivity().getPackageManager();
										String appName = getActivity().getApplicationInfo().loadLabel(pm).toString(); 
										request.setTitle(appName);
										request.setDescription("下载最新版本"+appName);
										request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
										request.setAllowedOverRoaming(false);
										dm.enqueue(request);
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
							alertDialog.dismiss();
						}
					});
				}

				@Override
				public void onInterfaceFail(String json) {
					LogUtilNIU.value("版本控制接口错误--->"+json);
					ToastUtil.shortToast("当前版本是最新版本");
				}

				@Override
				public void onFailureConnected(Boolean canConnect) {
					LogUtilNIU.value("连接失效--->"+canConnect);
					ToastUtil.shortToast("网络异常");
				}
			});
		}
		

	/**********************************************************/
	/**
	 * 获取当前版本号
	 */
	private String getVersionName() 
	{
		// 获取packagemanager的实例
		PackageManager packageManager = getContext().getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(getContext().getPackageName(),0);
		} catch (NameNotFoundException e) {
			ExceptionUtil.handleException(e);
		}
		String version = packInfo.versionName;
		return version;
	}
	
	public void onResume() {
	    super.onResume();
	    MobclickAgent.onPageStart("用户信息页"); //统计页面，"MainScreen"为页面名称，可自定义
	}
	public void onPause() {
	    super.onPause();
	    MobclickAgent.onPageEnd("用户信息页"); 
	}

}
