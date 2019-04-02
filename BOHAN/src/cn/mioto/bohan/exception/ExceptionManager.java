package cn.mioto.bohan.exception;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;

import android.content.Context;
import cn.mioto.bohan.R;

public class ExceptionManager {
	
	/**
	 * 显示错误提示
	 * @param e
	 */
	public static String getErrorDesc(Context context, Exception e){
		if(e instanceof NetworkStatusLineException){
    	    return context.getString(R.string.exception_statusline) + e.getMessage();
		}else if(e instanceof JSONException){
    	    return context.getString(R.string.exception_json) + e.getMessage();
		}else if(e instanceof UnknownHostException){
		    return context.getString(R.string.exception_unknownhost);
		}else if(e instanceof SocketException){
		    return context.getString(R.string.exception_socket) + e.getMessage();
		}else if(e instanceof  SocketTimeoutException){
		    return context.getString(R.string.exception_sockettimeout);
		}else if(e instanceof ConnectTimeoutException){
		    return context.getString(R.string.exception_connecttimeout);
		}else if(e instanceof IOException){
			return context.getString(R.string.exception_io);
		}else if(e instanceof IllegalArgumentException){
			return context.getString(R.string.exception_illegalargument);
		}
        return context.getString(R.string.exception_unknownhost) + e.getMessage();
	}
	

}
