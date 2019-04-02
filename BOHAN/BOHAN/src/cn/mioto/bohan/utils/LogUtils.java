package cn.mioto.bohan.utils;

import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日记类
 * @author 
 *
 */
public class LogUtils {		

	
	private static final String TAG = "bohan";
	private static boolean ENABLE_LOG = true; 
	private static boolean ENABLE_FILE = false;
	
	
	public static void d(String tag, String msg){
		if(ENABLE_LOG){
			Log.d(tag, "" + msg);
			if(ENABLE_FILE){
				sd(tag + "--d--" + msg);
			}
		}
	}	
	
	public static void i(String tag, String msg){
		if(ENABLE_LOG){
			Log.i(tag, "" + msg);
			if(ENABLE_FILE){
				sd(tag + "--i--" + msg);
			}
		}
	}	
	
	public static void e(String tag, String msg){
		if(ENABLE_LOG){
			Log.e(tag, "" + msg);
			if(ENABLE_FILE){
				sd(tag + "--e--" + msg);
			}
		}
	}	
	
	public static void e(String tag, String msg, Throwable throwable){
		if(ENABLE_LOG){
			Log.e(tag, "" + msg, throwable);
			if(ENABLE_FILE){
				sd(tag + "--e--" + msg + " " + Log.getStackTraceString(throwable));
			}
		}
	}	
	
	public static void sd(String msg) {
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ", Locale.CHINESE).format(new Date(System.currentTimeMillis()));
				File file = new File(Environment.getExternalStorageDirectory(),	TAG + "_log.txt");
				RandomAccessFile randomFile = new RandomAccessFile(file, "rwd");
				randomFile.seek(randomFile.length());
				randomFile.write((time + msg + "\r\n").getBytes());
				randomFile.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void sd(String fileName, String msg) {
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ", Locale.CHINESE).format(new Date(System.currentTimeMillis()));
				File file = new File(Environment.getExternalStorageDirectory(),	fileName);
				RandomAccessFile randomFile = new RandomAccessFile(file, "rwd");
				randomFile.seek(randomFile.length());
				randomFile.write((time + msg + "\r\n").getBytes());
				randomFile.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
