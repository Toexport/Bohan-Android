package cn.mioto.bohan.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import steed.netty.client.NettyClientBootstrap;

public class BoHanNettyClientBootstrap extends NettyClientBootstrap{
//	private boolean sendding = false;
//	private long sleepTime = 0;
	private BoHanBytesHandler boHanBytesHandler;

	public BoHanNettyClientBootstrap(int port, String host)throws InterruptedException {
		super(port, host);
	}

	public BoHanBytesHandler getBoHanBytesHandler() {
		return boHanBytesHandler;
	}

	@Override
	protected ChannelHandler[] getHandlers() {
		ChannelHandler[] handlers = new ChannelHandler[1];
		boHanBytesHandler = new BoHanBytesHandler(this);
		handlers[0] = boHanBytesHandler;
		return handlers;
	}

	@Override
	public ChannelFuture send(Object data) {
		/*while (sendding && sleepTime < 3000) {
			try {
				Thread.sleep(500);
				sleepTime += 500;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}*/
		return super.send(data);
	}
	
	

}
