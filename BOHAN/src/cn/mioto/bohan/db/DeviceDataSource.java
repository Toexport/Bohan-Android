package cn.mioto.bohan.db;



import android.database.sqlite.SQLiteDatabase;

/** 
 * 类说明：设备列表数据库,此类先不用
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年6月13日 上午9:16:58 
 */
public class DeviceDataSource {

	private SQLiteDatabase database;

	private String[] allColumns = { 
			DatabaseHelper.DEVICE_COLUMN_ID, 
			DatabaseHelper.DEVICE_COLUMN_TYPE,
			DatabaseHelper.DEVICE_COLUMN_LOCATION,
			DatabaseHelper.DEVICE_COLUMN_BRAND ,
			DatabaseHelper.DEVICE_COLUMN_SERIALID };

	public DeviceDataSource(DatabaseHelper dbHelper) {  
		database = dbHelper.getWritableDatabase();
	}

}
