package cn.mioto.bohan;

import cn.mioto.bohan.activity.SteedAppCompatActivity;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.view.timepicker.TimePicker;
import cn.mioto.bohan.view.timepicker.TimePickerForCountDown;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/** 
* 类说明：用于测试各种选择器
*/
public class TestPickerActivity extends SteedAppCompatActivity{
	private Button button1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_test_timepicker;
	}
	
	@Override
	public void onClick(View v) {
		if(v==button1){
			  //默认选中当前时间
			TimePickerForCountDown picker = new TimePickerForCountDown(this, TimePickerForCountDown.HOUR_CUSTOM);
	        picker.setSelectedItem(0, 0);
	        picker.setLabel("小时", "分钟");
	        picker.setTopLineVisible(false);
	        picker.setOnTimePickListener(new TimePickerForCountDown.OnTimePickListener() {
	            @Override
	            public void onTimePicked(String hour, String minute) {
	                ToastUtils.shortToast(TestPickerActivity.this, hour + ":" + minute);
	            }
	        });
	        picker.show();
		}
		super.onClick(v);
	}

}
