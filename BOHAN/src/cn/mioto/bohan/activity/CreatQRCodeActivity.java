package cn.mioto.bohan.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.niuniu.qrcode.scanner.encode.MyQRCodeEncoder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.bingoogolapple.qrcode.core.DisplayUtils;
import cn.mioto.bohan.R;
import cn.mioto.bohan.utils.Enterface;
import cn.mioto.bohan.utils.LogUtilNIU;
import cn.mioto.bohan.utils.ToastUtils;
import cn.mioto.bohan.utils.ViewUtil;
import steed.framework.android.client.JsonClientHandler2;

public class CreatQRCodeActivity extends BaseActivity implements OnClickListener,MyQRCodeEncoder.MyEncoderDelegate {
	private TextView shareQRTextView;
	private TextView saveImage;
	private ImageView showEncodeResultImageView;
	private List<String> selectedIDs = new ArrayList<>();
	private String targetUserId;
	File appDir;
	String appDirPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_creat_qrcode);
		ViewUtil.initToolbar(this, getString(R.string.share_device), true, false, 0);
		Intent i = getIntent();
		selectedIDs=i.getStringArrayListExtra("infoList");
		StringBuffer idsBuffer = new StringBuffer();
		for(int j = 0 ; j < selectedIDs.size(); j ++){
			if(j==selectedIDs.size()-1){
				idsBuffer.append(selectedIDs.get(j));
			}else{
				idsBuffer.append(selectedIDs.get(j)+",");
			}
		}
		LogUtilNIU.value("得到后的id列表是"+idsBuffer.toString());
		targetUserId = i.getStringExtra("userId");
		bindViews();
		//		//生成一个全网唯一的序列号
		//		LogUtilNIU.value("勾选了的设备的ID是"+selectedIDs.toString()+"目标用户是"+targetUserId);
		//		UUID uuid = UUID.randomUUID();     
		//		String input = uuid.toString();
		//		LogUtilNIU.value("生成的UUID是"+input);
	}


	private void bindViews() {
		shareQRTextView = (TextView) findViewById(R.id.shareQRTextView);
		saveImage = (TextView) findViewById(R.id.saveImage);
		showEncodeResultImageView = (ImageView) findViewById(R.id.showEncodeResultImageView);
		shareQRTextView.setOnClickListener(this);
		saveImage.setOnClickListener(this);
	}

	//	@Override
	//	public boolean onCreateOptionsMenu(Menu menu) {
	//		// Inflate the menu; this adds items to the action bar if it is present.
	//		getMenuInflater().inflate(R.menu.creat_qrcode, menu);
	//		return true;
	//	}
	//
	//	@Override
	//	public boolean onOptionsItemSelected(MenuItem item) {
	//		// Handle action bar item clicks here. The action bar will
	//		// automatically handle clicks on the Home/Up button, so long
	//		// as you specify a parent activity in AndroidManifest.xml.
	//		int id = item.getItemId();
	//		if (id == R.id.action_settings) {
	//			return true;
	//		}
	//		return super.onOptionsItemSelected(item);
	//	}

	@Override
	public void onClick(View v) {
		if(v==shareQRTextView){
			share();
		}else if (v==saveImage){
			saveImage();
		}
	}

	private void share(){
		Intent intent=new Intent(Intent.ACTION_SEND);   
		intent.setType("text/plain");  
		//      intent.setPackage("com.sina.weibo");   
		intent.putExtra(Intent.EXTRA_SUBJECT, "分享");   
		intent.putExtra(Intent.EXTRA_TEXT, "你好 ");  
		intent.putExtra(Intent.EXTRA_TITLE, "我是标题");  
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
		startActivity(Intent.createChooser(intent, "请选择"));  
	}

	private void createQRCode(String chineseInput) {
		try {
			chineseInput = new String(chineseInput.getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		MyQRCodeEncoder.encodeQRCode(chineseInput, DisplayUtils.dp2px(CreatQRCodeActivity.this, 150), Color.parseColor("#000000"), this);
	}

	@Override
	public void onEncodeQRCodeSuccess(Bitmap bitmap) {//生成成功，返回bitmap
		showEncodeResultImageView.setImageBitmap(bitmap);
	}

	@Override
	public void onEncodeQRCodeFailure() {
		// TODO 生成二维码失败的回调
		ToastUtils.shortToast(this, "生成二维码失败");
	}

	/**********************************************************/
	/**
	 * 保存图片到目录
	 */
	private void saveImage() {
		showEncodeResultImageView.setDrawingCacheEnabled(true);
		showEncodeResultImageView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
				View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		Bitmap viewBitmap = Bitmap.createBitmap(showEncodeResultImageView.getDrawingCache());
		if (viewBitmap != null) {
			new SaveImageTask().execute(viewBitmap);
		}
	}

	public class SaveImageTask extends AsyncTask<Bitmap, Void, String> {

		@Override
		protected String doInBackground(Bitmap... params) {
			String result = getResources().getString(R.string.save_picture_success);
			try {
				saveImageToGallery(params[0]);
			} catch (Exception e) {
				result = getResources().getString(R.string.save_picture_failed);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String s) {
			if (s.equals(getResources().getString(R.string.save_picture_success))) {
				Toast.makeText(CreatQRCodeActivity.this, getResources().getString(R.string.picture_has_been_save_to) + appDirPath + getResources().getString(R.string.dir), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(CreatQRCodeActivity.this, s, Toast.LENGTH_LONG).show();
			}
		}
	}

	private void saveImageToGallery(Bitmap bitmap) throws Exception {
		appDir = new File(Environment.getExternalStorageDirectory(), "BOHAN");
		if (!appDir.exists()) {
			appDir.mkdir();
		}
		appDirPath = appDir.getAbsolutePath();
		String fileName = "" + System.currentTimeMillis() + ".jpg";
		File file = new File(appDir, fileName);
		String imagePath = file.getAbsolutePath();
		FileOutputStream fos = new FileOutputStream(file);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] bytes = baos.toByteArray();
		fos.write(bytes);
		fos.flush();
		fos.close();
		MediaStore.Images.Media.insertImage(this.getContentResolver(), imagePath, "", "BOHAN");
		this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + imagePath)));
	}

}
