package activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.t.coolwheather.R;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import service.AutoUpdateService;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

import static com.example.t.coolwheather.R.id.tv_temp1;

/**
 * Created by T on 2016/4/13.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {

    private LinearLayout weatherInfoLayout;
    private TextView cityTextName;//用于显示城市的名称
    private TextView publishText;//用于显示发布时间
    private TextView weatherDespText;//用于显示天气描述信息
    private TextView temp1Text;//用于显示气温1
    private TextView temp2Text;//用于显示气温2
    private TextView currentDatetext;//用于显示当前时间

    private Button switchCity;      //用于切换城市
    private Button refreshWeather;      //更新天气按钮

    private ImageView iconWeather;      //天气图标
    private Bitmap bm = null;// 生成了一张bmp图像

    private String imgAddress = "http://m.weather.com.cn/img/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);

        initView();//初始化

        switchCity.setOnClickListener(this);        //切换城市按钮监听事件
        refreshWeather.setOnClickListener(this);    //刷新监听事件

        String countyCode = getIntent().getStringExtra("county_code");//获取从ChooseActivity传过来的county_code
        //对countyCode进行判断
        if (!TextUtils.isEmpty(countyCode)) {
            //有县级代号就查询天气
            publishText.setText("同步中....");

            weatherInfoLayout.setVisibility(View.INVISIBLE);//将weatherInfoLayout布局先隐藏
            cityTextName.setVisibility(View.INVISIBLE);//将cityName控件隐藏

            queryWeatherCode(countyCode);
        } else {
            //没有县级代号就显示本地天气
            showWeather();
        }
    }

    /**
     * 查询县级代号所对应的天气代号
     *
     * @param countyCode
     */
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryForServer(address, "countyCode");
    }

    /**
     * 查询天气代号所对应的天气
     *
     * @param weatherCode
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryForServer(address, "weatherCode");//从服务器查询天气
    }

    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
     *
     * @param address
     * @param type
     */
    private void queryForServer(final String address, final String type) {
//        Log.d("WeatherActivity--->", address);
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                Log.d("WeatherActivity--->", response);
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        //从服务器返回的数据中解析出天气代号
                        //解析出来的数据实例为  县级名称|天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];//拿到天气代号
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    Log.v("返回的json是--->", response);
                    //处理服务器返回的天气信息
                    Utility.handlerWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                        showWeather();
                    }
                });
            }
        });
    }

    /**
     * 从SharedPreference文件中读取存储的天气信息，并显示到界面上
     */
    private void showWeather() {

//        queryForServer(address, "weatherCode");//从服务器查询天气
        SharedPreferences preds = PreferenceManager.getDefaultSharedPreferences(this);

        cityTextName.setText(preds.getString("city_name", ""));
        temp1Text.setText(preds.getString("temp1", ""));
        temp2Text.setText(preds.getString("temp2", ""));

        Log.d("test", "showWeather: " + preds.getString("weather_desp", ""));
        weatherDespText.setText(preds.getString("weather_desp", ""));
        publishText.setText("今天" + preds.getString("publish_time", "") + "发布");

        currentDatetext.setText(preds.getString("current_time", ""));

        weatherInfoLayout.setVisibility(View.VISIBLE);//将weatherInfoLayout设置为可见
        currentDatetext.setVisibility(View.VISIBLE);    //将时间显示可见
        cityTextName.setVisibility(View.VISIBLE);   //将citytextName设置为可见

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);

        String img = preds.getString("img", "");

//        switch (img) {
//            case "d0.jif":
//                iconWeather.setImageDrawable(getResources().getDrawable(R.drawable.d0));
//                break;
//            case "d1.jif":
//                iconWeather.setImageDrawable(getResources().getDrawable(R.drawable.d1));
//                break;
//            case "d2.jif":
//                iconWeather.setImageDrawable(getResources().getDrawable(R.drawable.d2));
//                break;
//            case "d3.jif":
//                iconWeather.setImageDrawable(getResources().getDrawable(R.drawable.d3));
//                break;
//            case "d4.jif":
//                iconWeather.setImageDrawable(getResources().getDrawable(R.drawable.d4));
//                break;
//            case "d5.jif":
//                iconWeather.setImageDrawable(getResources().getDrawable(R.drawable.d0));
//                break;
//            case "d6.jif":
//                iconWeather.setImageDrawable(getResources().getDrawable(R.drawable.d6));
//                break;
//            case "d7.jif":
//                iconWeather.setImageDrawable(getResources().getDrawable(R.drawable.d7));
//                break;
//            case "d8.jif":
//                iconWeather.setImageDrawable(getResources().getDrawable(R.drawable.d8));
//                break;
//            case "d9.jif":
//                iconWeather.setImageDrawable(getResources().getDrawable(R.drawable.d0));
//                break;
//            case "d10.jif":
//                iconWeather.setImageDrawable(getResources().getDrawable(R.drawable.d0));
//                break;
//
//
//        }
        setBitmap(preds);
    }

    private void setBitmap(SharedPreferences preds) {
        final SharedPreferences predss = preds;
        new Thread() {
            @Override
            public void run() {
                getBiamapFromServer(imgAddress + predss.getString("img", ""));
            }
        }.start();
    }

    private Bitmap getBiamapFromServer(String img) {
        try {
            URL iconurl = new URL(img);
            URLConnection conn = iconurl.openConnection();
            conn.connect();
            // 获得图像的字符流
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, 1024);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();// 关闭流
            handler.sendEmptyMessage(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bm;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    iconWeather.setImageBitmap(bm);
                    break;
            }
        }
    };

    private void initView() {
        weatherInfoLayout = (LinearLayout) findViewById(R.id.layout_weather_info);
        cityTextName = (TextView) findViewById(R.id.tv_city_name);
        publishText = (TextView) findViewById(R.id.tv_publish_time);
        weatherDespText = (TextView) findViewById(R.id.tv_weather_desp);
        temp1Text = (TextView) findViewById(tv_temp1);
        temp2Text = (TextView) findViewById(R.id.tv_temp2);
        currentDatetext = (TextView) findViewById(R.id.tv_current_date);
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        iconWeather = (ImageView) findViewById(R.id.icon_weather);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中....");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = prefs.getString("weather_code", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }
    }
}
