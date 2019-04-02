package cn.mioto.bohan.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;
import cn.mioto.bohan.view.timepicker.TimePicker;

import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

/** 
 * 类说明：9组定时页面设置时间和星期的activity
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年6月17日 下午2:00:03 
 */
public class NineSetSetTimeActivity extends BaseCheckDataMenuActivity{
	private TextView llClickSelectTime1;
	private TextView llClickSelectTime2;
	//清空设定按钮
	private Button tvClear;
	private Button tvCancle;
	private Button tvConfirm;
	private TextView tvStartTime;
	private TextView tvEndTime;
	protected String startHour;
	protected String endHour;
	protected String endMin;
	protected String startMin;
	private CheckBox check1;
	private CheckBox check2;
	private CheckBox check3;
	private CheckBox check4;
	private CheckBox check5;
	private CheckBox check6;
	private CheckBox check7;
	private List<CheckBox> checks = new ArrayList<>();
	private int settingPosition;
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title, R.string.time_setting, true, false, 0, 0);
		ViewUtil.setToolbarTitle("时间选择");
		/***************************************************/
		//把选择框到添加进去一个集合里面，方便以后使用
		checks.add(check6);
		checks.add(check5);
		checks.add(check4);
		checks.add(check3);
		checks.add(check2);
		checks.add(check1);
		checks.add(check7);
		/***************************************************/
		//设置时间和checkBox的初始化显示
		LogUtilNIU.value("settingPosition"+settingPosition);
		Intent intent = getIntent();
		Bundle b =intent.getExtras();
		settingPosition=b.getInt(Constant.POSITION_NINE_SET_SETTING_ITEM);
		String settedTime =b.getString(Constant.DATA_EXISTING_TIME);
		tvStartTime.setText(settedTime.substring(0, 2)+":"+settedTime.substring(2, 4));
		tvEndTime.setText(settedTime.substring(4, 6)+":"+settedTime.substring(6, 8));
		String d= settedTime.substring(8);
		//把这个2位16进制换成101010形式  例如 01010001
		String binerayWeekDays =ModbusCalUtil.hexWeekDayToBinerayString(d);
		binerayWeekDays=binerayWeekDays.substring(1);//去掉前面第一串口协议里没有用的字符
		for(int i = 0 ; i <checks.size(); i++){
			//设置checkBox的初始化
			if(binerayWeekDays.charAt(i)=='1'){
				checks.get(i).setChecked(true);
			}
		}
		//
		startHour = settedTime.substring(0, 2);
		endHour = settedTime.substring(4, 6);
		startMin = settedTime.substring(2, 4);
		endMin = settedTime.substring(6, 8);
	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_nine_set_set_time;
	}

	@Override
	public void onClick(View v) {
		if(v==llClickSelectTime1){
			//默认选中当前时间
			TimePicker picker = new TimePicker(this, TimePicker.HOUR_OF_DAY);
			picker.setTopLineVisible(false);
			picker.setOnTimePickListener(new TimePicker.OnTimePickListener() {
				@Override
				public void onTimePicked(String hour, String minute) {
					//	                ToastUtils.shortToast(NineSetSetTimeActivity.this, hour + ":" + minute);
					startHour = hour;
					startMin = minute;
					tvStartTime.setText(hour + ":" + minute);
				}
			});
			picker.show();
		}else if(v==llClickSelectTime2){
			//默认选中当前时间
			TimePicker picker = new TimePicker(this, TimePicker.HOUR_OF_DAY);
			picker.setTopLineVisible(false);
			picker.setOnTimePickListener(new TimePicker.OnTimePickListener() {
				@Override
				public void onTimePicked(String hour, String minute) {
					//	                ToastUtils.shortToast(NineSetSetTimeActivity.this, hour + ":" + minute);
					//先计算当前选的结束时间是否大于开始时间
					endHour = hour;
					endMin = minute;
					if(Integer.valueOf(startHour)>Integer.valueOf(endHour)){
						ToastUtils.shortToast(NineSetSetTimeActivity.this, "结束时间必须晚于开始时间");
					}else if(Integer.valueOf(startHour)==Integer.valueOf(endHour)&&Integer.valueOf(startMin)>=Integer.valueOf(endMin)){
						ToastUtils.shortToast(NineSetSetTimeActivity.this, "结束时间必须晚于开始时间");
					}else{
						tvEndTime.setText(hour + ":" + minute);
					}
				}
			});
			picker.show();
		}else if(v==tvCancle){
			finish();
		}else if(v==tvConfirm){
			if(Integer.valueOf(startHour)>Integer.valueOf(endHour)){
				ToastUtils.shortToast(NineSetSetTimeActivity.this, "结束时间必须晚于开始时间");
			}else if(Integer.valueOf(startHour)==Integer.valueOf(endHour)&&Integer.valueOf(startMin)>=Integer.valueOf(endMin)){
				ToastUtils.shortToast(NineSetSetTimeActivity.this, "结束时间必须晚于开始时间");
			}else{
				//判断输入框有没有输入数据，如果没有，提示输入
				//把符合协议格式的数据传递
				if(!(getSelectedResult().equals("请选择开始和结束时间")||getSelectedResult().equals("请选择星期"))){
					Intent data = new Intent();
					Bundle b = new Bundle();
					b.putString(Constant.DATA_NINE_SET_SET_RESULT_EXTRA, getSelectedResult());
					b.putInt(Constant.POSITION_NINE_SET_SETTING_ITEM, settingPosition);
					data.putExtras(b);
					setResult(RESULT_OK,data);
					finish();
				}else if(getSelectedResult().equals("请选择开始和结束时间")){
					ToastUtils.shortToast(NineSetSetTimeActivity.this, "请选择开始和结束时间");
				}else if(getSelectedResult().equals("请选择星期")){
					LogUtilNIU.value("*****请选择星期提示返回");
					ToastUtils.shortToast(NineSetSetTimeActivity.this, "请选择星期");
				};
				
			}
		}else if(v==tvClear){
			//清空设定
			for(int i = 0 ; i <checks.size(); i++){
				//设置checkBox全不选
				checks.get(i).setChecked(false);
			}
			tvStartTime.setText("00:00");//开始时间设置为0
			tvEndTime.setText("00:00");//结束时间设置为0
		}
		super.onClick(v);
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("9组时间设置时间的页面"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this);          //统计时长
	}
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("9组时间设置时间的页面"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
		MobclickAgent.onPause(this);
	}
	private String startTime;
	private String endTime;

	/**
	 * 得到设置的所有数据，组成协议的格式
	 */
	private String getSelectedResult(){
		
		//得到开始时间
		startTime =tvStartTime.getText().toString().replace(":", "");
		endTime =tvEndTime.getText().toString().replace(":", "");
		LogUtilNIU.value("startTime--"+startTime+",endTime--"+endTime);
		if(startTime.equals("0000")&&endTime.equals("0000")){
			return "请选择开始和结束时间";
		}
		//得到结束时间
		StringBuffer b = new StringBuffer();
		b.append("0");//首位为0，保留的指令位
		for(CheckBox c : checks){
			if(c.isChecked()){
				b.append("1");
			}else{
				b.append("0");
			}
		}
		LogUtilNIU.value("选择的星期b.toString()---"+b.toString());
		if(b.toString().equals("00000000")){
			LogUtilNIU.value("返回请选择星期");
			return "请选择星期";
		}
		LogUtilNIU.value("b.toString()"+b.toString());
		//10进制数字转16进制
		String hexTime =Integer.toHexString(Integer.valueOf(b.toString(),2)).toString();
		hexTime=hexTime.toUpperCase();
		if(hexTime.length()==1){//如果16进制只有1位，需要往前补上0
			hexTime = "0"+hexTime;
		}
		StringBuffer resultBuffer = new StringBuffer();
		resultBuffer.append(startTime);
		resultBuffer.append(endTime);
		resultBuffer.append(hexTime);
		LogUtilNIU.value("设置的指令为"+resultBuffer.toString());
		return resultBuffer.toString();
	}

	@Override
	protected String onServiceUDPBack(String content) {

		return null;
	}

	@Override
	protected String onCheckByServiceOnInterfaceFail(String json) {
		// TODO Auto-generated method stub
		return null;
	}
}

