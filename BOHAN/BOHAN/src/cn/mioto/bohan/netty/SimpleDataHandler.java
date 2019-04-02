package cn.mioto.bohan.netty;

import java.io.UnsupportedEncodingException;

public abstract class SimpleDataHandler extends MainThreadDataHandler{

	@Override
	public void onDataReturnInMainThread(byte[] bytes) {
		try {
			onDataReturn(new String(bytes,"GBK"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void onDataReturn(String data);


}
