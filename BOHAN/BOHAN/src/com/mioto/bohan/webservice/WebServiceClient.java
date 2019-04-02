package com.mioto.bohan.webservice;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
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
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

/**
 * webservice
 * 
 * @author STAR
 * 
 */
public class WebServiceClient {
	static// 命名空间
	String nameSpace = "http://bohansever.top/";
	// 调用的方法名称
	static String methodName = "getDeviceList2";

	// EndPoint将WSDL地址末尾的"?wsdl"去除后剩余的部分
	static String endPoint = "http://bohansever.top/WebService.asmx";
	// SOAP Action命名空间 + 调用的方法名称
	static String soapAction = "http://bohansever.top/getDeviceList2";

	// 服务器链接
	final static String WEB_SERVICE_URL = "http://bohanserver.top/WebService.asmx?wsdl";

	public static String getData(String data) {
		// 指定WebService的命名空间和调用的方法名
		SoapObject rpc = new SoapObject(nameSpace, methodName);
		// 设置需调用WebService接口需要传入的两个参数mobileCode、userId
		rpc.addProperty("mobileCode", "");
		rpc.addProperty("userId", "");

		// 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER10);
		envelope.bodyOut = rpc;
		// 设置是否调用的是dotNet开发的WebService
		envelope.dotNet = true;
		// 等价于envelope.bodyOut = rpc;
		envelope.setOutputSoapObject(rpc);
		// 超时时间
		HttpTransportSE transport = new HttpTransportSE(endPoint, 20);
		HeaderProperty headerPropertyObj = new HeaderProperty("cookie",
				"username");
		try {
			// List headerList = transport.call(endPoint, envelope, null);
			// 调用WebService
			// transport.call(soapAction, envelope, headerList);
			transport.call(soapAction, envelope);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 获取返回的数据
		SoapObject object = (SoapObject) envelope.bodyIn;
		// 获取返回的结果
		String result = object.getProperty(0).toString();
		return result;
	}

	/**
	 * 调用WebService
	 * 
	 * @return WebService的返回值
	 * 
	 */
	// public static String CallWebService(String MethodName,
	// Map<String, String> Params) {
	// // 1、指定webservice的命名空间和调用的方法名
	//
	// SoapObject request = new SoapObject(nameSpace, MethodName);
	// // 2、设置调用方法的参数值，如果没有参数，可以省略，
	// if (Params != null) {
	// Iterator iter = Params.entrySet().iterator();
	// while (iter.hasNext()) {
	// Map.Entry entry = (Map.Entry) iter.next();
	// request.addProperty((String) entry.getKey(),
	// (String) entry.getValue());
	// }
	// }
	// // 3、生成调用Webservice方法的SOAP请求信息。该信息由SoapSerializationEnvelope对象描述
	// SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
	// SoapEnvelope.VER12);
	// envelope.bodyOut = request;
	// // c#写的应用程序必须加上这句
	// envelope.dotNet = false;
	//
	// HttpTransportSE ht = new HttpTransportSE(WEB_SERVICE_URL);
	// // 使用call方法调用WebService方法
	// try {
	// ht.call(null, envelope);
	// } catch (HttpResponseException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (XmlPullParserException e) {
	// e.printStackTrace();
	// }
	// try {
	// final SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
	// if (result != null) {
	// Log.d("----收到的回复----", result.toString());
	// return result.toString();
	// }
	//
	// } catch (SoapFault e) {
	// Log.e("----发生错误---", e.getMessage());
	// e.printStackTrace();
	// }
	// return null;
	// }

	public static String CallWebService(String methodName2,
			Map<String, String> Params) throws Exception {
		String WSDL_URI = "http://bohanserver.top/webservice.asmx?wsdl";// wsdl
																		// 的uri
		String namespace = "http://bohansever.top/";// namespace

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
		// 创建SoapSerializationEnvelope 对象，同时指定soap版本号(之前在wsdl中看到的)
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapSerializationEnvelope.VER12);
		envelope.bodyOut = request;// 由于是发送请求，所以是设置bodyOut
		envelope.dotNet = true;// 由于是.net开发的webservice，所以这里要设置为true

		Element[] header = new Element[1];
		header[0] = new Element().createElement(nameSpace, methodName2);
		header[0].setAttribute(nameSpace, "token", "login");
		envelope.headerOut = header;

		HttpTransportSE httpTransportSE = new HttpTransportSE(WSDL_URI, 20000);
		httpTransportSE.call(null, envelope);// 调用

		// 获取返回的数据
		SoapObject object = (SoapObject) envelope.bodyIn;
		// 获取返回的结果
		String result = object.getProperty(0).toString();
		Log.d("debug", result);
		return result;

	}
}
