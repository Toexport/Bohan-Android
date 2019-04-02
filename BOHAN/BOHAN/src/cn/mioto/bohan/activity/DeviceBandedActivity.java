package cn.mioto.bohan.activity;

import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.ViewUtil;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class DeviceBandedActivity extends SteedAppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*******初始化toolbar********************************************/
		ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title, R.string.title_activity_device_banded, true, false, 0, 0);
	}

	@Override
	protected Integer getLayoutId() {
		return R.layout.activity_device_banded;
	}
}
