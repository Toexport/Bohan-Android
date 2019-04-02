package cn.mioto.bohan.activity;

import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ToastUtils;

import android.os.Bundle;
import steed.framework.android.client.JsonClientHandler2;

/** 
 * 类说明：对6各查询页面的封装
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年8月25日 下午12:59:57 
 */
public abstract class BaseCheckDataMenuActivity2 extends BaseUDPActivity2{
	/**
	 * @Description:通过服务器查询设备数据
	 * content为指令内容
	 */
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

			}

			@Override
			public void onFailureConnected(Boolean canConnect) {
				LogUtilNIU.value("服务器查询的单个设备信息接口无效");
			}
		});
	}

	/**
	 * 多态方法
	 * @Description:通过服务器查询设备数据,查询两个数据
	 * content为指令内容
	 */
	protected void checkByService(String content1,final String content2) {
		LogUtilNIU.value("服务器单个查询必备参数参数---"+deviceId+"--"+content1);
		new Enterface("sendToDevice.act").addParam("deviceid", deviceId).addParam("content", content1).doRequest(new JsonClientHandler2() {

			@Override
			public void onInterfaceSuccess(String message, String contentJson) {
				LogUtilNIU.value("得到服务器查询的单个设备信息为---->"+contentJson);
				onServiceUDPBack(contentJson);
			}

			@Override
			public void onInterfaceFail(String json) {

			}

			@Override
			public void onFailureConnected(Boolean canConnect) {
				LogUtilNIU.value("服务器查询的单个设备信息接口无效1");
			}
		});
		
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				LogUtilNIU.value("服务器单个查询必备参数参数---"+deviceId+"--"+content2);
				checkSecond(content2);
			}

			private void checkSecond(String content2) {
				new Enterface("sendToDevice.act").addParam("deviceid", deviceId).addParam("content", content2).doRequest(new JsonClientHandler2() {

					@Override
					public void onInterfaceSuccess(String message, String contentJson) {
						LogUtilNIU.value("得到服务器查询的单个设备信息为---->"+contentJson);
						onServiceUDPBack(contentJson);
					}

					@Override
					public void onInterfaceFail(String json) {

					}

					@Override
					public void onFailureConnected(Boolean canConnect) {
						LogUtilNIU.value("服务器查询的单个设备信息接口无效2");
					}
				});
			}
		}, 2000);
	}

	protected abstract String onServiceUDPBack(String content);
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
}
