package com.mioto.bohan.webservice;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.client.HttpResponseException;
import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParserException;

import cn.mioto.bohan.entity.Config;

import android.content.Context;
import android.util.Log;

/**
 * webservice
 * 
 * @author STAR
 * 
 */
public class WebServiceClient {
	
	private static boolean isZH;
	static// 命名空间
	String nameSpace = "http://www.bohansever.top:8088/";
	// 调用的方法名称
	static String methodName = "getDeviceList2";

	// EndPoint将WSDL地址末尾的"?wsdl"去除后剩余的部分
	static String endPoint = "http://www.bohansever.top:8088/WebService.asmx";
	// SOAP Action命名空间 + 调用的方法名称
	static String soapAction = "http://www.bohansever.top:8088/getDeviceList2";

	// 服务器链接
	final static String WEB_SERVICE_URL = "http://www.bohanserver.top:8088/WebService.asmx?wsdl";

	public static String getData(String methodName2,
			Map<String, String> Params, Context context) {
		String WSDL_URI = "http://www.bohanserver.top:8088/webservice.asmx?wsdl";// wsdl
		String token = Config.getToken(context);
		Element[] header = new Element[1];
		header[0] = new Element().createElement(nameSpace, "SoapHeader");

		Element userName = new Element().createElement(nameSpace, "token");
		userName.addChild(Node.TEXT, token);
		header[0].addChild(Node.ELEMENT, userName);

		// 指定WebService的命名空间和调用的方法名
		SoapObject soapObject = new SoapObject(nameSpace, methodName);
		if (Params != null) {
			Iterator iter = Params.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				soapObject.addProperty((String) entry.getKey(),
						(String) entry.getValue());
			}
		}
		// 处理soap12:Body数据部分
		soapObject.addProperty("token", userName);
		soapObject.addProperty("token", userName);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		// SoapEnvelope.VER11 表示使用的soap协议的版本号 1.1 或者是1.2
		envelope.headerOut = header;
		envelope.bodyOut = soapObject;
		envelope.dotNet = true; // 指定webservice的类型的（java，PHP，dotNet）
		envelope.setOutputSoapObject(soapObject);

		HttpTransportSE ht = new HttpTransportSE(WSDL_URI);
		try {
			ht.call(soapAction, envelope);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SoapObject object = (SoapObject) envelope.bodyIn;
		// 获取返回的结果
		String result = object.getProperty(0).toString();
		Log.d("debug", result);
		return result;
	}

	public static String CallWebService(String methodName2,
			Map<String, String> Params, Context context) throws Exception {
		isZH = isZh(context);
		String WSDL_URI = "http://www.bohanserver.top:8088/webservice.asmx?wsdl";// wsdl
							// 的uri
		String namespace = "http://bohansever.top/";// namespace
		String soapAction2 = namespace + methodName2;

		SoapObject request = new SoapObject(namespace, methodName2);
		// 设置需调用WebService接口需要传入的两个参数mobileCode、userId
		// 2、设置调用方法的参数值，如果没有参数，可以省略，
		if (Params != null) {
			Iterator iter = Params.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				request.addProperty((String) entry.getKey(),
						(String) entry.getValue());
			}
		}
		
		Element[] header = new Element[2];
		header[0] = new Element().createElement(namespace, "Certificate");
//		header[0].setAttribute(nameSpace, "token", Config.getToken(context));
		Element userName = new Element().createElement(namespace, "token");
		userName.addChild(Node.TEXT, Config.getToken(context));
		header[0].addChild(Node.ELEMENT, userName);
		
		String languageStr = null;
		if(isZH){
			languageStr = "simple-chinese";
		}else{
			languageStr = "english";
		}
		header[1] = new Element().createElement(namespace, "Certificate");
		Element language = new Element().createElement(namespace, "language");
		language.addChild(Node.TEXT, languageStr);
		header[1].addChild(Node.ELEMENT, language);
		
		// 创建SoapSerializationEnvelope 对象，同时指定soap版本号(之前在wsdl中看到的)
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapSerializationEnvelope.VER12);
		envelope.bodyOut = request;// 由于是发送请求，所以是设置bodyOut
		envelope.dotNet = true;// 由于是.net开发的webservice，所以这里要设置为true
		envelope.headerOut = header;
		envelope.setOutputSoapObject(request);
		HttpTransportSE httpTransportSE = new HttpTransportSE(WSDL_URI, 10000);
		httpTransportSE.call(soapAction2, envelope);// 调用
		
		
		// 获取返回的数据
		SoapObject object = (SoapObject) envelope.bodyIn;
		// 获取返回的结果
		String result = object.getProperty(0).toString();
		Log.d("debug", result);
		return result;

	}
	
	/**
	 * 获取系统语言
	 * 
	 * @return
	 */
	private static boolean isZh(Context context) {
		Locale locale = context.getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if (language.endsWith("zh")) {
			isZH = true;
			return true;
		} else {
			isZH = false;
			return false;
		}

	}
}
