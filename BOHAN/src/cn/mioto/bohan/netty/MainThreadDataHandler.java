package cn.mioto.bohan.netty;

import steed.framework.android.core.thread.ThreadUtil;

public abstract class MainThreadDataHandler implements DataHandler{

	@Override
	public final void onDataReturn(final byte[] bytes) {
		ThreadUtil.runOnMainThreadAsync(new Runnable() {
			@Override
			public void run() {
				onDataReturnInMainThread(bytes);
			}
		});
	}
	
	public abstract void onDataReturnInMainThread(byte[] bytes);


}
