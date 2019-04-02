package cn.mioto.bohan.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import cn.mioto.bohan.exception.NetworkStatusLineException;


public class HttpUtils {
    private static final String TAG = "HttpUtils";

    public static String TOKIN = "";


    public static String getData(String url, String encode) throws Exception {
        return getData(url, encode, 60000);
    }

    /**
     * 请求数据
     *
     * @param url    网址
     * @param encode 返回数据字符集编码
     * @return
     * @throws Exception
     */
    public static String getData(String url, String encode, int timeout) throws Exception {
        LogUtils.d(TAG, url);
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
        HttpGet get = new HttpGet(url);
//        get.setHeader("Accept","application/json");
//        get.setHeader("Content-type","application/json");
//        if (!TextUtils.isEmpty(TOKIN)) {
//            get.setHeader("Cookie", "JSESSIONID=" + TOKIN);
//            LogUtils.d(TAG, "TOKIN = " + TOKIN);
//        }
        HttpResponse response = client.execute(get);
        int statusCode = response.getStatusLine().getStatusCode();
        if (response.getStatusLine().getStatusCode() == 200) {
//            String result = readString(response.getEntity(), encode);
            //第五步：从相应对象当中取出数据，放到entity当中
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity,"gb2312");//将entity当中的数据转换为字符串

            LogUtils.d(TAG, result);
            return result;
        } else {
            throw new NetworkStatusLineException(String.valueOf(statusCode));
        }
    }




    /***
     * 以post方式提交请求
     *
     * @param url
     * @param params
     * @param encode 返回数据编码
     * @return
     * @throws Exception
     */
    public static String postData(String url, List<NameValuePair> params, String encode)
            throws Exception {
        LogUtils.d(TAG, url + " parsms:" + params.toString());
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 120000);
        HttpPost post = new HttpPost(url);
        if (!TextUtils.isEmpty(TOKIN)) {
            post.setHeader("Cookie", "JSESSIONID=" + TOKIN);
            LogUtils.d(TAG, "TOKIN = " + TOKIN);
        }
        post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        post.setEntity((HttpEntity) new UrlEncodedFormEntity(params, "UTF-8"));
        HttpResponse response = client.execute(post);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String result = readString(response.getEntity(), encode);
            LogUtils.d(TAG, result);
            return result;
        } else {
            throw new IllegalStateException("Status Line " + statusCode);
        }
    }

    /**
     * 读取实体数据
     *
     * @param entity
     * @param encode
     * @return
     * @throws Exception
     */
    private static String readString(HttpEntity entity, String encode) throws Exception {
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), encode));
        String temp = null;
        while ((temp = reader.readLine()) != null) {
            buffer.append(temp);
        }
        return buffer.toString();
    }


    /**
     * 上传文件
     *
     * @param requestUrl
     * @param filePath
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public static String doUploadFile(String requestUrl, String filePath)
            throws MalformedURLException, IOException {
        LogUtils.d(TAG, requestUrl + " file:" + filePath);
        String result = null;
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url
                    .openConnection();

            httpURLConnection.setReadTimeout(15 * 1000); // 缓存的最长时间
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(
                    httpURLConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""
                    + filePath.substring(filePath.lastIndexOf("/") + 1)
                    + "\""
                    + end);
            dos.writeBytes(end);

            FileInputStream fis = new FileInputStream(filePath);
            byte[] buffer = new byte[8192]; // 8k
            int count = 0;
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);
            }
            fis.close();

            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();

            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            result = br.readLine();
            dos.close();
            is.close();
        } catch (Exception e) {
            LogUtils.d(TAG, "Upload file error:" + e.getMessage());
        }
        LogUtils.d(TAG, result);
        return result;
    }


}
