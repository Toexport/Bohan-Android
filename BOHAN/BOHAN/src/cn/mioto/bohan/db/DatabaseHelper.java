package cn.mioto.bohan.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/** 
 * 类说明：数据库帮助类
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年6月12日 下午11:58:36 
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	//db
	public static final String DB_NAME = "bohan_user.db";
	public static final int DB_VERSION = 7;

	//volatile，保证对这个变量的操作只在主存中进行。
	//因为这个mDBHelper是单例的，是一起公用的。
	private volatile static DatabaseHelper mDBHelper;
	
	    //1.device_list 
		//store device list and device detail
		public static final String DEVICE_TABLE_NAME = "devices_list";//表名
		public static final String DEVICE_COLUMN_ID = "_id";//排序id
		public static final String DEVICE_COLUMN_TYPE = "type";//类型
		public static final String DEVICE_COLUMN_LOCATION = "location";//位置
		public static final String DEVICE_COLUMN_BRAND = "brand";//品牌
		public static final String DEVICE_COLUMN_SERIALID = "serId";//序列号
	/*
	 * 建立设备表
	 */
	private static final String DEVICE_TABLE_CREATE = "CREATE TABLE " + DEVICE_TABLE_NAME
			+ "(" + DEVICE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ DEVICE_COLUMN_SERIALID + " CHAR(256) UNIQUE NOT NULL, "
			+ DEVICE_COLUMN_TYPE + " CHAR(256) UNIQUE NOT NULL, "
			+ DEVICE_COLUMN_LOCATION + " TEXT NOT NULL, " + DEVICE_COLUMN_BRAND
			+ " TEXT NOT NULL);";
	
	
	/**********************************************************/
	/**
	 * 单例获取该helper
	 */
	public static synchronized DatabaseHelper getInstance(Context context) {
		if (mDBHelper == null) {
			//上锁，因为是单例的，只有一个返回值。
			synchronized (DatabaseHelper.class) {
				if (mDBHelper == null) {
					mDBHelper = new DatabaseHelper(context);
				}
			}
		}
		//使得实例唯一
		return mDBHelper;
	}

    /**********************************************************/
	/**
	 * 构造方法，定义了名称和版本
	 */
	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DEVICE_TABLE_CREATE);//建造设备表
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
