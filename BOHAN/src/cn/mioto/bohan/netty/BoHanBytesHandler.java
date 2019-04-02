package cn.mioto.bohan.netty;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

import steed.framework.android.util.LogUtil;
import steed.netty.client.NettyClientBootstrap;
import steed.netty.client.NettyClientBytesHandler;

public class BoHanBytesHandler extends NettyClientBytesHandler{
	private Map<String, DataHandler> handlerMap = new HashMap<String, DataHandler>();
	
	public void registHandler(String commond,DataHandler handler){
		handlerMap.put(commond, handler);
	}
	public void unRegistHandler(String commond){
		handlerMap.remove(commond);
	}
	
	
	public BoHanBytesHandler(NettyClientBootstrap bootstrap) {
		super(bootstrap);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext,
			byte[] bytes) throws Exception {
		String data = new String(bytes,"GBK");
		LogUtil.d("收到数据---->"+data);
		DataHandler handler = getHandler(data);
		if (handler != null) {
			handler.onDataReturn(bytes);
		}
		//super.channelRead0(channelHandlerContext, bytes);
	}
	
	private DataHandler getHandler(String data){
		if (data.length() > 5) {
			DataHandler dataHandler = handlerMap.get(data.substring(0,6));
			if (dataHandler != null) {
				return dataHandler;
			}
		}
		if (data.length() > 4) {
			DataHandler dataHandler = handlerMap.get(data.substring(0,5));
			if (dataHandler != null) {
				return dataHandler;
			}
		}
		return null;
	}

}
