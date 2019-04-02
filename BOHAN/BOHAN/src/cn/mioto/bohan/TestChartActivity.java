package cn.mioto.bohan;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class TestChartActivity extends Activity {
	
	private cn.mioto.bohan.view.BarChart02View bar ;
	private List<String> xLables = new ArrayList<>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_chart);
	}
}
