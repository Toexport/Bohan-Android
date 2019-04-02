package cn.mioto.bohan;

import java.util.ArrayList;
import java.util.List;

import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ModbusCalUtil;
import cn.mioto.bohan.view.BarChart02View;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class TestChartActivity extends Activity {

	private cn.mioto.bohan.view.BarChart02View bar;
	private List<String> xLables = new ArrayList<>();
	private BarChart02View chartView;
	private List<String> colors = new ArrayList<>();
	private Boolean showOneDay = false;
	private String year;
	private String month;
	private String today;
	private String hour;
	private List<String> hoursPowerList = new ArrayList<>();
	private List<Double> powerDatas = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_chart);
		chartView = (BarChart02View) findViewById(R.id.bar);
		for (int i = 0; i < 25; i++) {
			hoursPowerList.add(i + "");
		}
		for (int i = 0; i < hoursPowerList.size(); i++) {
			String data = hoursPowerList.get(i);
			//data = ModbusCalUtil.addDotDel0(data, 24);
			Double d = Double.valueOf(data);
			powerDatas.add(d);
		}
		bar.setChartLabels(hoursPowerList);
	}
}
