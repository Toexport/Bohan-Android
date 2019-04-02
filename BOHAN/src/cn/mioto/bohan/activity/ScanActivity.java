package cn.mioto.bohan.activity;
/** 
 * 类说明：扫描二维码的类
 * 作者：  jiemai liangminhua 
 * 创建时间：2016年5月27日 下午6:28:08 
 */

import com.niuniu.qrcode.scanner.decode.MyQRCodeDecoder;
import com.niuniu.qrcode.scanner.utils.BitMapUtils;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import cn.mioto.bohan.Constant;
import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;
import steed.framework.android.client.JsonClientHandler2;

public class ScanActivity extends AppCompatActivity implements QRCodeView.Delegate, MyQRCodeDecoder.MyDecodeDelegate,OnClickListener {
	private static final String TAG = ScanActivity.class.getSimpleName();
	private QRCodeView mQRCodeView;
	private TextView tvRightMenu;
	private static final int GET_PICTURE_FROM_ALBUM = 100;//打开相册

	private Boolean isSerialNumber;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		setViews();
		ViewUtil.initToolbar(this, R.id.toolbar, R.id.toolbar_title, R.string.qrcode, true, true, R.id.menu_tv, R.string.album);
		mQRCodeView.setResultHandler(this);
		tvRightMenu.setOnClickListener(this);
	}

	private void setViews() {
		mQRCodeView = (ZXingView) findViewById(R.id.zxingview);
		tvRightMenu = (TextView) findViewById(R.id.menu_tv);
	}

	@Override
	public void onScanQRCodeOpenCameraError() {
		Log.e(TAG, "打开相机出错");
		ToastUtils.shortToast(ScanActivity.this, getString(R.string.error_opening_camera));
	}

	private void decodeQRCodeBitmap(Bitmap bitmap) {
		MyQRCodeDecoder.decodeQRCodeBitmap(bitmap, this);
	}

	private void initView() {
		mQRCodeView.startSpotAndShowRect();//显示扫描框
	}

	@Override
	protected void onStart() {
		super.onStart();
		mQRCodeView.startCamera();
		//		mQRCodeView.startCamera();
		initView();
	}

	@Override
	protected void onStop() {
		mQRCodeView.stopCamera();
		super.onStop();
	}

	private void vibrate() {
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(200);
	}

	@Override
	public void onScanQRCodeSuccess(String result) {
		vibrate();//震动0.2秒
		showScanResultForID(result);
		if(result.length()==12){//如果结果为12位，则为设备ID
			showScanResultForID(result);
		}else if(result.length()==14){
			LogUtilNIU.value("扫描结果是"+result);
            Intent intent = new Intent(this,ShareResultActivity.class);
            intent.putExtra(Constant.INTENT_KEY_SHARE_CODE, result);
			//扫描到的为分享码
			startActivity(intent);
			finish();
		}
	}
	
	/**********************************************************/
	/**
	 * 条码图片解析成功
	 */
	@Override
	public void myOnDecodeQRCodeSuccess(String result) {
		if(result.length()==12){//如果结果为12位，则为设备ID
			showScanResultForID(result);
		}else if(result.length()==14){
			LogUtilNIU.value("扫描结果是"+result);
            Intent intent = new Intent(this,ShareResultActivity.class);
            intent.putExtra(Constant.INTENT_KEY_SHARE_CODE, result);
			//扫描到的为分享码
			startActivity(intent);
			finish();
		}
	}


	/**********************************************************/
	/**
	 * 条码图片解析失败
	 */
	@Override
	public void myOnEncodeQRCodeFailure() {
		Toast.makeText(this, getString(R.string.not_qrcode), Toast.LENGTH_LONG).show();
		mQRCodeView.startSpotDelay(1500);// 1.5秒后可重新识别
	}

	private void showScanResultForID(String result) {
		LogUtilNIU.value("扫描到的二维码为"+result+"该结果长度为"+result.length());
		Intent intent = new Intent();
		intent.putExtra("result", result);
		setResult(RESULT_OK,intent);
		finish();
		if(result.length()!=12){
			ToastUtils.shortToast(ScanActivity.this, getString(R.string.qrcode_error));
			//2秒后再启动扫描
			mQRCodeView.startSpotDelay(2000);// 1.5秒后可重新识别
		}else{
			LogUtilNIU.value("result是"+result+"长度为"+result.length());
			result = result.substring(0,12);
			LogUtilNIU.value("result.substring(0,12)是"+result);
//			Intent intent = new Intent();
//			intent.putExtra("result", result);
//			setResult(RESULT_OK,intent);
//			finish();
			//先判断该id有没有被该用户绑定
			/*  
			 * 正则表达式判断扫描结果是不是厂家出的设备id的格式，是的话，再发送到服务器进行验证
			 * 不是的话，提示用户重新扫描。重启启动扫描功能。
			 */
			//ModbusCalUtil.isBiaoAddress(result)
//			if(true){//如果二维码符合格式。
//				String deviceId = result.substring(0,12);//处理扫描结果，得到设备id
//				String deviceType = result.substring(0,2);
//				if(deviceType.equals("68")){
//					deviceType="C";//插座
//				}else if(deviceType.equals("69")){
//					deviceType="K";//开关
//				}
//				ToastUtils.shortToast(this, "扫描成功");
//				/***************************************************/
//				//			//以下是离线模式，用于没有服务器时测试
//				//			if(BApplication.offLineMode){//离线模式，用于测试
//				//				LogUtilNIU.e("是否离线模式"+BApplication.offLineMode);
//				//				if(BApplication.getItemByItemID(deviceId)!=null){
//				//					//证明设备已经被绑定过
//				//					ToastUtils.shortToast(ScanActivity.this, "亲，该设备已经被你绑定过了");
//				//					mQRCodeView.startSpotDelay(1500);// 1.5秒后可重新识别
//				//					//重新激活扫描
//				//				}else {
//				////					ToastUtils.shortToast(ScanActivity.this, "设备绑定成功");
//				//					/*******为用户新建一个设备对象，把设备对象的id信息记录，并把设备放到全局List*****/
//				//					SingleDevice singledevice= new SingleDevice();
//				//					singledevice.setDeviceID(deviceId);
//				//					singledevice.setDeviceName(getResources().getString(R.string.device)+deviceId);//给设备一个初始化名字
//				//					BApplication.devices.add(singledevice);//把设备对象添加到全局的设备列表
//				//					/*************************跳转至保存设备信息页面**************************/
//				//					Intent intent = new Intent(ScanActivity.this,SaveDeviceSettingActivity.class);
//				//					intent.putExtra(Constant.SERIAL_NUMBER_INTENT_KEY, deviceId);
//				//					startActivity(intent);
//				//					ScanActivity.this.finish();}//扫描页面关闭
//				//			}else{
//				// ------------------------------------------------------------------------------
//				//非离线模式
//				bindDevice(deviceId,deviceType);
//				//				Boolean webFailScanAgain=!bindDevice(deviceId,deviceType ); 
//				//				//这里是有可能无论成不成都重新启动扫描的，所以延迟时间为好
//				//				if(webFailScanAgain){//绑定操作网络没有生效
//				//					mQRCodeView.startSpotDelay(50000);// 5秒后可重新识别
//				//				}
//				//			}
//			}else{
//				//如果二维码不符合表地址格式
//				ToastUtils.shortToast(ScanActivity.this, "该二维码不符合格式");
//				mQRCodeView.startSpotDelay(3000);// 1.5秒后可重新识别
//			}
		}

	}

	/**********************************************************/
	/** 
	 * 绑定设备,发送符合设备id格式的设备id到服务器验证。
	 */ 
	Boolean isAct=false;//判断联网有没有生效 TODO
	private Boolean bindDevice(final String id,final String sort){

		new Enterface("band.act").addParam("device.id",id).addParam("device.sort", sort).addParam("device.name", getResources().getString(R.string.device)+id).
		addParam("device.position", "").addParam("device.brand", "").doRequest(new JsonClientHandler2() {
			@Override
			public void onInterfaceSuccess(String message, String contentJson) {
				//服务器会把改singledevice 添加到user里，并记录设备id,类型信息。
				LogUtilNIU.e("返回 的content--->"+contentJson);
				ToastUtils.shortToast(ScanActivity.this, getResources().getString(R.string.band_success));
				//跳转至信息保存页面
				Intent intent = new Intent(ScanActivity.this,SaveDeviceSettingActivity.class);
				intent.putExtra(Constant.SERIAL_NUMBER_INTENT_KEY, id);
				startActivity(intent);
				ScanActivity.this.finish();//扫描页面关闭
				isAct =true;
			}

			@Override
			public void onInterfaceFail(String json) {
				super.onInterfaceFail(json);
				Intent intent = new Intent(ScanActivity.this, DeviceBandedActivity.class);
				startActivity(intent);
				finish();
				//设备如果已经被同一个用户绑定了 TODO
				//				LogUtilNIU.e("绑定ID失败的json"+json);
				//				mQRCodeView.startSpotDelay(1500);// 1.5秒后可重新识别
				//				isAct = true;
			}

			@Override
			public void onFailureConnected(Boolean canConnect) {

			}
		},true);
		return isAct;
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("扫描二维码"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
		MobclickAgent.onResume(this);          //统计时长
	}
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("扫描二维码"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
		MobclickAgent.onPause(this);
	}

	@Override
	public void onClick(View v) {
		if(v==tvRightMenu){//相册选项
			//打开系统相册
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_PICK);
			intent.setType("image/*");
			startActivityForResult(intent, GET_PICTURE_FROM_ALBUM);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK&&requestCode==GET_PICTURE_FROM_ALBUM) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = {MediaStore.Images.Media.DATA};
			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			Bitmap bitmap = BitMapUtils.getSmallBitmap(picturePath, 720, 960);
			decodeQRCodeBitmap(bitmap);
		} else {

		}
	}


}