package util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by T on 2016/4/7.
 */
public class HttpUtil {
    public static void sendHttpRequest(final String address, final HttpCallbackListener listener){
        new Thread(){
            @Override
            public void run() {
                HttpURLConnection connection = null;//定义一个HttpURLConnection对象

                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();//获取HttpURLConnection实例
                    connection.setRequestMethod("GET");//这是GET方法，从服务器获取数据
                    connection.setConnectTimeout(8000);//设置连接的时间
                    connection.setReadTimeout(8000);//设置读取时间
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null){
                        response.append(line);
                    }

                    if (listener != null){
                        //回调onFinish()方法
                        Log.e("sendHttpResponse--->",response.toString() + "");
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    if (listener != null){
                        listener.onError(e);
                    }
                }finally {
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }.start();
    }
}
