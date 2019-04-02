package cn.mioto.bohan;

import java.util.Calendar;

import cn.mioto.bohan.activity.SteedAppCompatActivity;
import cn.mioto.bohan.utils.ModbusCalUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
/**
 * 
 * @ClassName: CalActivity 
 * @Description: 用于测试计算的类 
 * @author jiemai liangminhua 
 * @date 2016年6月7日 下午9:50:38 
 *
 */
public class CalActivity extends SteedAppCompatActivity {
	/**********DECLARES*************/
	private TextView textView2;
	private EditText editText1;
	private Button button1;
	private EditText etDataNum;
	private EditText editText3;
	private TextView tvResult;
	private TextView tvResunt3;
	private TextView tvTime;
	private Button button2;
	private Button button3;
	private EditText etDel0FillLength;
	private EditText etLength;
	private EditText etDotLo;
	private Button button4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_cal;
	}

	@Override
	public void onClick(View v) {
		if(v==button1){
			String order =editText1.getText().toString().trim();
			String hexResult=ModbusCalUtil.verNumber(order);
			textView2.setText(hexResult);
		}else if (v==button2){
			String data =etDataNum.getText().toString().trim();
			int dot=Integer.valueOf(editText3.getText().toString().trim());
			String result=ModbusCalUtil.addDotDel0(data, dot);
			tvResult.setText(result);
		}else if (v==button3){
			String str = etDel0FillLength.getText().toString().trim();
			String dolo = etDotLo.getText().toString().trim();
			int doloint = Integer.valueOf(dolo);
		    String length = etLength.getText().toString().trim();
		    int lengthint = Integer.valueOf(length);
		    String result3 = ModbusCalUtil.delDotFillLength(str, lengthint, doloint);
		    tvResunt3.setText(result3);
		}else if(v==button4){
			String result3=ModbusCalUtil.getSystemTimeFormat();
			tvTime.setText(result3);
		}
		super.onClick(v);
	}


}
