package cn.mioto.bohan.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import steed.exception.runtime.system.FrameworkException;
import steed.framework.android.util.LogUtil;
import steed.framework.android.util.ToastUtil;
import steed.netty.client.NettyClientBootstrap;

/**
 * 设备控制器,不要乱动代码,以后会兼容服务器和局域网两种模式
 * @author 战马
 * @email battle_steed@163.com
 */
public class DeviceController {
	private static Map<String, BoHanNettyClientBootstrap> deviceMap = new HashMap<String, BoHanNettyClientBootstrap>();

	public static Map<String, BoHanNettyClientBootstrap> getDeviceMap() {
		return deviceMap;
	}
	
	/**
	 * 添加设备
	 * @param deviceID 设备id(mac地址或者其他唯一标识就可以了)
	 * @param host (设备ip地址)
	 * @return 是否添加成功,(host出错,或网络故障会添加失败)
	 */
	public static boolean addDevice(String deviceID,String host){
		try {
			deviceMap.put(deviceID, new BoHanNettyClientBootstrap(3333, host));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 移除设备
	 * @param deviceID
	 * @return
	 */
	public static boolean removeDevice(String deviceID){
		NettyClientBootstrap nettyClientBootstrap = getDevice(deviceID);
		boolean disconnect = nettyClientBootstrap.disconnect();
		if (disconnect) {
			deviceMap.remove(deviceID);
		}
		return disconnect;
	}
	
	/**
	 * 移除所有设备,程序退出时请调用改方法
	 */
	public static void removeAllDevice(){
		for (String temp:deviceMap.keySet()) {
			removeDevice(temp);
		}
	}
	
	/**
	 * 发送数据
	 * @param deviceID 设备id
	 * @param command 指令集
	 */
	public static void sendData(String deviceID,String command,DataHandler handler){
		try {
			ToastUtil.longToast("看到这个提示证明代码更新了");
			LogUtil.d("发送数据---->"+command);
			sendData(deviceID, command.getBytes("GBK"),handler);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送数据
	 * @param deviceID 设备id
	 * @param command 指令集
	 */
	public static void sendData(String deviceID,byte[] command,DataHandler handler){
		BoHanNettyClientBootstrap nettyClientBootstrap = getDevice(deviceID);
		try {
			nettyClientBootstrap.getBoHanBytesHandler().registHandler(new String(command,"GBK"), handler);
		} catch (UnsupportedEncodingException e) {
			//e.printStackTrace();
		}
		nettyClientBootstrap.send(command);
	}
	

	private static BoHanNettyClientBootstrap getDevice(String deviceID) {
		BoHanNettyClientBootstrap nettyClientBootstrap = deviceMap.get(deviceID);
		if (nettyClientBootstrap == null) {
			throw new FrameworkException("无法找到设备"+deviceID+",操作设备前要先添加设备!");
		}
		return nettyClientBootstrap;
	}
	
	public void test(){
		addDevice("dddd", "192.168.0.1");
		sendData("dddd", "指令集", new SimpleDataHandler() {
			@Override
			public void onDataReturn(String data) {
				ToastUtil.longToast(data);
			}
		});
	}
	
	
}
