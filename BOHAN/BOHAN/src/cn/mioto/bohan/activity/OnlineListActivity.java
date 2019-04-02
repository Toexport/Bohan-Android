package cn.mioto.bohan.activity;

import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LogUtilNIU;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import steed.framework.android.client.JsonClientHandler2;

/**
 * 基类
 * 名称：在线列表类 位置，类型
 * 描述：类别下的在线列表类
 * 所有在线状态不通过UDP查，直接通过服务器去查询
 */
public class OnlineListActivity extends BaseActivity {
	private ListView listView;
    private String thisType;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_online_list);
		thisType = getIntent().getStringExtra(Constant.INTENT_KEY_THIS_SORT);
		bindViews();
		checkThisTypeList();
	}
	
    /**
     * @Description:从服务器查询本数据
     * Parameters: 
     * return:void
     */
	private void checkThisTypeList() {
		new Enterface("getAllDevices.act").addParam("", thisType).doRequest(new JsonClientHandler2() {
		@Override
		public void onInterfaceSuccess(String message, String contentJson) {
			LogUtilNIU.value(thisType+"得到的位置分类信息的Json为---->"+contentJson);
			
		}

		@Override
		public void onInterfaceFail(String json) {

		}

		@Override
		public void onFailureConnected(Boolean canConnect) {
			// TODO Auto-generated method stub
			
		}
	});
		
	}

	private void bindViews() {
		listView = (ListView) findViewById(R.id.listview);
	}
	
}
