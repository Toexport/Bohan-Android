package cn.mioto.bohan.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import cn.mioto.bohan.BApplication;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.ToastUtils;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import steed.framework.android.client.JsonClientHandler2;

/** 
 * 类说明：查询单个设备年月日数据的父类
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年8月29日 下午4:39:46 
 * 
 */
public abstract class BaseSingleDeviceDataFragment extends BaseUDPFragment{
	protected ProgressDialog p;	//指令的种类 用电量 0， 用电功率 1
	protected int udpKind = 0;
	//清除标题
	protected abstract void clearTitle();
	//添加标题
	protected abstract void addTitle();
	protected Handler h = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what==Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK){
				//加载到数据后正在加载消失
			}else if (msg.what==Constant.MSG_WHAT_SHOW_DIALOG_OF_RESEND_TASK){
				//显示正在加载
				if(udpKind==0){
					progressGettingDataShow(getString(R.string.recent_power_consumption_query));
				}else if(udpKind==1){
//					progressGettingDataShow("最近用电功率查询中");
				}
			}else if(msg.what==Constant.MSG_WHAT_DISMISS_DIALOG_OF_RESEND_TASK_FAIL){
				//发送几次后，加载失败
				if(udpKind==0){
					progressGettingDataDismiss(getString(R.string.power_consumption_query_failed_recently));
				}else if(udpKind==1){
					progressGettingDataDismiss(getString(R.string.electricity_power_query_failed_recently));
				}
			}else if(msg.what==Constant.MSG_WHAT_SEND_MSG_BY_SERVICE_RESEND_TASK_FAIL){
				//通过服务器去查询信息
				checkByService((String)msg.obj);
			}
		};
	};
	
	protected void checkByService(String content) {
		LogUtilNIU.value("开始用服务器查询，服务器单个查询传递的参数有---"+deviceId+"--"+content);
		new Enterface("sendToDevice.act").addParam("deviceid", deviceId).addParam("content", content).doRequest(new JsonClientHandler2() {

			@Override
			public void onInterfaceSuccess(String message, String contentJson) {
				LogUtilNIU.value("得到服务器查询的单个设备信息为---->"+contentJson);
				contentJson = contentJson.toUpperCase();
				onServiceUDPBack(contentJson);
			}

			@Override
			public void onInterfaceFail(String json) {
                onServiceInterfaceFail(json);
			}

			@Override
			public void onFailureConnected(Boolean canConnect) {
				LogUtilNIU.value("服务器查询的单个设备信息接口无效");
				onServiceFailureConnected();
			}
		});
	}
	
	//用电量和功率查询 服务器接口连接不上回调
	protected abstract void onServiceFailureConnected();
	//用电量和功率查询 接口返回错误信息回调
	protected abstract void onServiceInterfaceFail(String json); 
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		p = new ProgressDialog(getActivity());
		p.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				BApplication.instance.setResendTaskShowBreak(true);
				progressGettingDataDismissNoToast();
			}
		});
	}
	
	/***************************************************/
	//获取数据的dialog效果
	protected Boolean gettingDataOk = true;
	protected void progressGettingDataShow(String message) {
		if(getActivity()!=null){
			gettingDataOk = false;
			p.setMessage(message);
			p.setCancelable(false);
			p.setCanceledOnTouchOutside(false);
			p.show();
		}
	}

	protected void progressGettingDataDismiss(String message) {
		if(!gettingDataOk){
			gettingDataOk = true;
			p.dismiss();
			ToastUtils.shortToast(getActivity(),message);
		}
	}

	protected void progressGettingDataDismissNoToast() {
		if(!gettingDataOk){
			gettingDataOk = true;
			p.dismiss();
		}
	}
	
	//{"g":"{\"10\":\"000000\",\"09\":\"001200\",\"11\":\"000000\",\"12\":\"000000\"}","d":"{\"10\":\"00000000\",\"09\":\"00000000\",\"11\":\"00000000\",\"12\":\"00000000\"}"}

	//从服务器查询年月或日数据。历史数据的方法
	public void checkDataFromeService(String deviceID,final String timeType,String targetTime){
		LogUtilNIU.value("接口getDG.act"+"参数deviceid"+deviceID+"参数"+targetTime);

		new Enterface("getDG.act").addParam("deviceid", deviceID).addParam(timeType, targetTime).doRequest(new JsonClientHandler2() {

			@Override
			public void onInterfaceSuccess(String message, String contentJson) {
				LogUtilNIU.value("单个设备历史数据查询---->"+contentJson);
				if(contentJson.equals("{}")){
					ToastUtils.shortToast(getContext(), "没有该日期的历史数据");
					List<Double> powers = new ArrayList<>();
					List<Double> rates = new ArrayList<>();
					for(int i = 0 ; i < 32; i ++){
						powers.add(0.0);
						rates.add(0.0);
					}
					dealHistoryDataFromService(powers,rates);	
				}else{
					try {
						LogUtilNIU.value("解析历史数据JSON");
						JSONObject obj = new JSONObject(contentJson);
						String gResult = "";
						String dResult = "";
						if(obj.has("g")&&!obj.isNull("g")){
							gResult= (String) obj.get("g");
							LogUtilNIU.value("g获取后为---》"+gResult);
						}else{

						}
						if(obj.has("d")&&!obj.isNull("d")){
							dResult= (String) obj.get("d");
							LogUtilNIU.value("d获取后为---》"+dResult);
						}else{

						}
						LogUtilNIU.value("d******->"+dResult+"g*******->"+gResult);
						LogUtilNIU.value("timeType--->"+timeType);
						if(timeType.equals("year")){
							//处理年数据 12个数字
							dealDataAndPutIntList(12,dResult,gResult);
						}else if (timeType.equals("year_month")){
							//处理月数据  30个数字
							dealDataAndPutIntList(31,dResult,gResult);
						}else if (timeType.equals("year_month_day")){
							dealDataAndPutIntList(24,dResult,gResult);
						}
					} catch (JSONException e) {
						e.printStackTrace();
						LogUtilNIU.value("解析历史数据JSONException e");
					}
				}}

			@Override
			public void onInterfaceFail(String json) {
				LogUtilNIU.value("单个设备历史数据接口失败---->"+json);
				ToastUtils.shortToast(getContext(), "没有该日期的历史数据");
				List<Double> powers = new ArrayList<>();
				List<Double> rates = new ArrayList<>();
				for(int i = 0 ; i < 32; i ++){
					powers.add(0.0);
					rates.add(0.0);
				}
				dealHistoryDataFromService(powers,rates);	
			}

			@Override
			public void onFailureConnected(Boolean canConnect) {
				LogUtilNIU.value("单个设备历史数据网络错误");
                 ToastUtils.shortToast(getContext(), "网络不给力");
			}
		});
	}

	//处理历史数据的数据回调
	public abstract void dealHistoryDataFromService(List<Double> powers, List<Double> rates);
	//单个设备查询的 用电量 或 用电功率 查询数据回调
	protected String onServiceUDPBack(String content) {
		showContent(content);
		return null;
	}

	protected abstract void showContent(String content);

	//处理数据
	protected void dealDataAndPutIntList(int qua, String d, String g) {
		List<Double> powers = new ArrayList<>();
		List<Double> rates = new ArrayList<>();
		//月份
		try {
			JSONObject objd = new JSONObject(d);
			JSONObject objg = new JSONObject(g);
			int start= 1;
			//如果是日数据，就从0开始  
			if(qua==24){
				start = 0;
			}

			for(int i = start; i <=qua; i ++){
				String dKey=ModbusCalUtil.add0fillLength(String.valueOf(i), 2);
				if(objd.has(dKey)&&!objd.isNull(dKey)){
					String dValue= (String) objd.get(dKey);
					dValue=ModbusCalUtil.addDotDel0(dValue, 6);
					Double eachValue=Double.valueOf(dValue);
					powers.add(eachValue);
				}else{
					powers.add(0.0);
				}

				String gKey=ModbusCalUtil.add0fillLength(String.valueOf(i), 2);
				if(objg.has(gKey)&&!objg.isNull(gKey)){
					String dValue= (String) objg.get(gKey);
					dValue=ModbusCalUtil.addDotDel0(dValue, 2);
					Double eachValue=Double.valueOf(dValue);
					rates.add(eachValue);
				}else{
					rates.add(0.0);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			LogUtilNIU.value("异常"+e);
		}
		LogUtilNIU.value("数据被处理后"+"powers--大小"+powers.size()+powers+"rates--大小"+rates.size()+rates);
		dealHistoryDataFromService(powers,rates);	
	}


}
