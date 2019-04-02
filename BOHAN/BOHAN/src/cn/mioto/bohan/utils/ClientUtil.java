package cn.mioto.bohan.utils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import cz.msebera.android.httpclient.entity.StringEntity;
import steed.framework.android.client.SteedHttpResponseHandler;
import steed.framework.android.client.Token;
import steed.framework.android.client.TokenUtil;
import steed.framework.android.core.ActivityStack;
import steed.framework.android.core.ContextUtil;
import steed.framework.android.util.LogUtil;
import steed.framework.android.widget.SteedProgressDialog;
import steed.util.base.StringUtil;

/** 
* 类说明：
* 作者：  jiemai liangminhua 
* 创建时间：2016年6月1日 下午3:45:31 
*/
public class ClientUtil {
	private static AsyncHttpClient client = new AsyncHttpClient();
	private static SyncHttpClient syncClient = new SyncHttpClient();
	static{
		client.setMaxRetriesAndTimeout(0, 1000);
		client.setConnectTimeout(5000);
		client.setTimeout(5000);
		client.setResponseTimeout(5000);
		
		syncClient.setMaxRetriesAndTimeout(0, 1000);
		syncClient.setConnectTimeout(5000);
		syncClient.setTimeout(5000);
		syncClient.setResponseTimeout(5000);
	}
	private static String baseUrl = "";

	public static AsyncHttpClient getClient() {
		return client;
	}

	public static String getBaseUrl() {
		return baseUrl;
	}

	public static void setBaseUrl(String baseUrl) {
		ClientUtil.baseUrl = baseUrl;
	}

	public static void setClient(AsyncHttpClient client) {
		ClientUtil.client = client;
	}
	
	private static void showDialog(SteedHttpResponseHandler handler){
		SteedProgressDialog progressDialog = new SteedProgressDialog(ActivityStack.currentActivity());
		progressDialog.setCancelable(false);
		progressDialog.show();
		handler.setDialog(progressDialog);
	}

	public static void get(final String url, 
			final RequestParams params,
			final SteedHttpResponseHandler responseHandler,boolean showLoaddingDialog) {
		if (showLoaddingDialog) {
			showDialog(responseHandler);
		}
		delHandler(responseHandler, url, params);
		client.get(getAbsoluteUrl(url,params), params, responseHandler);
	}
	
	private static void delHandler(SteedHttpResponseHandler responseHandler,String url,RequestParams params){
		responseHandler.setParams(params);
		/*if (responseHandler.isUseCache()) {
			String key = url + params;
			responseHandler.setCacheKey(key);
		}*/
	}
	
	public static void post(final String url, final RequestParams params, final SteedHttpResponseHandler responseHandler,boolean showLoaddingDialog) {
		if (showLoaddingDialog) {
			showDialog(responseHandler);
		}
		delHandler(responseHandler, url, params);
		client.post(getAbsoluteUrl(url,params), params, responseHandler);
	}
	
	public static void post(final String url, final RequestParams params, final SteedHttpResponseHandler responseHandler,final String outPutString,boolean showLoaddingDialog){
		if (showLoaddingDialog) {
			showDialog(responseHandler);
		}
		final StringEntity entity;
		entity = new StringEntity(outPutString,StringUtil.getCharacterSet());
		entity.setContentEncoding(StringUtil.getCharacterSet());    
		delHandler(responseHandler, url, params);
		client.post(ContextUtil.getApplicationContext(),getAbsoluteUrl(url,params), entity, "json", responseHandler);
	}
	
	public static void get(final String url, final RequestParams params, final SteedHttpResponseHandler responseHandler) {
		get(url, params, responseHandler,false);
	}
	public static void post(final String url, final RequestParams params, final SteedHttpResponseHandler responseHandler) {
		post(url, params, responseHandler,false);
	}
	public static void post(final String url, final RequestParams params, final SteedHttpResponseHandler responseHandler,final String outPutString){
		post(url, params, responseHandler, outPutString, false);
	}
	
	private static String getToken(){
		/*int sleepTime = 0;
		while (!TokenUtil.isAccessTokenValid()&&sleepTime < 1500) {
			TokenUtil.refreshToken();
			sleepTime += 200;
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/
		Token token = TokenUtil.getToken();
		if (token == null) {
			LogUtil.w("token为空!");
			return null;
		}
		return token.getAccess_token();
	}
	
	public static String getAbsoluteUrl(String relativeUrl,RequestParams params) {
		String url = getAbsoluteUrl(relativeUrl);
		String token = getToken();
		if (token == null) {
			//return "http://tttdfadsafdsfadsafdsfa.com";
			token = "333";
		}
		if (params == null) {
			if (!relativeUrl.contains("?")) {
				url +="?";
			}
			url += "token="+token+"&ajax=1";
		}else {
			params.put("token", token);
			params.put("ajax", "1");
		}
		LogUtil.d("请求url-->"+url);
		return url;
	}
	public static String getAbsoluteUrl(String relativeUrl) {
		String url;
		if (relativeUrl.startsWith("http://")||relativeUrl.startsWith("https://")) {
			return relativeUrl ;
		}else if (relativeUrl.startsWith("/")&&baseUrl.endsWith("/")) {
			url = baseUrl + relativeUrl.substring(1);
		}else {
			url = baseUrl + relativeUrl;
		}
		return url;
	}
}
