package cn.mioto.bohan.fragment;

import cn.mioto.bohan.BApplication;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

/** 
 * 类说明：Fragment基类
 * 定义了获取context的方法
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年5月26日 下午5:51:08 
 */
public class BaseFragment extends Fragment{

	protected Boolean isViewInitiated;//view是否已经初始化
	protected Boolean isViewsibleToUser;//是否对用户可视
	protected Boolean isDataInitiated;//数据是否已经初始化
	

	/**********让fragment取activity对象时不会出现空指针*****************************************/
	private Activity activity;

	public Context getContext(){
		if(activity == null){
			return BApplication.instance;
		}
		return activity;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activity = getActivity();
	}
	/***************************************************/

}
