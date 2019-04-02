package cn.mioto.bohan.utils;

import steed.framework.android.client.ClientUtil;
import steed.framework.android.client.SteedHttpResponseHandler;

import com.loopj.android.http.RequestParams;

public class Enterface {
	private String url;
	private RequestParams requestParams = new RequestParams();
	//act地址
	public Enterface(String url) {
		super();
		this.url = url;
	}
	
	public Enterface() {
		
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public RequestParams getRequestParams() {
		return requestParams;
	}

	public void setRequestParams(RequestParams requestParams) {
		this.requestParams = requestParams;
	}
	
	public Enterface addParam(String key,Object value){
		requestParams.put(key, value);
		return this;
	}
	
	public void doRequest(SteedHttpResponseHandler handler){
		ClientUtil.post(url, requestParams, handler,false);
	}
	
	public void doRequest(SteedHttpResponseHandler handler,boolean showLoadingDialog){
		ClientUtil.post(url, requestParams, handler,showLoadingDialog);
	}
	
	
}
