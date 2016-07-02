package util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import model.City;
import model.CoolWeatherDB;
import model.County;
import model.Province;

/**
 * 工具类，用来解析服务器返回的 省市县 的数据， 返回格式为 代号|城市，代号|城市
 * Created by T on 2016/4/7.
 */
public class Utility {

    /**
     * 解析从服务器解析返回的JSON数据，并将解析的数据保存到本地
     *
     * @param context
     * @param response
     */
    public static void handlerWeatherResponse(Context context, String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");

            String cityName = weatherInfo.getString("city");
            Log.e("Utility--cityName", cityName);
            String weatherCode = weatherInfo.getString("cityid");
            Log.e("Utility--weatherCode", weatherCode);
            String temp1 = weatherInfo.getString("temp1");
            Log.e("Utility--temp1", temp1);
            String temp2 = weatherInfo.getString("temp2");
            Log.e("Utility--temp2", temp2);
            String weatherDesp = weatherInfo.getString("weather");
            String publishTime = weatherInfo.getString("ptime");
            String img = weatherInfo.getString("img2");

            saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime, img);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将服务器返回的所有天气信息存储到SharedPreferences文件中
     *  @param context
     * @param cityName
     * @param weatherCode
     * @param temp1
     * @param temp2
     * @param weatherDesp
     * @param publishTime
     * @param img
     */
    private static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime, String img) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CANADA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_time", sdf.format(new Date()));
        editor.putString("img", img);
        editor.commit();
    }


    /**
     * 解析和处理服务器返回的省级数据
     *
     * @param coolWeatherDB
     * @param response
     * @return
     */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB, String response) {

        if (!TextUtils.isEmpty(response)) {
            String[] allProvinces = response.split(",");//第一层解析，创建一个String数组，接收解析出来的数据，

            if (allProvinces != null && allProvinces.length > 0) {
                for (String p : allProvinces) {
                    String[] array = p.split("\\|");//第二层解析，再创建一个String数组,分别存放 代号 和 城市

                    //创建一个Province对象，并将解析出来的数据分别给 code name 赋值
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);

                    coolWeatherDB.saveProvince(province);//将从服务器解析出来的数据存储到本地数据库
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器传回来的市级数据
     *
     * @param coolWeatherDB
     * @param response
     * @param provinceId    province的外键
     * @return
     */
    public static boolean handlerCitiesResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId) {

        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");//第一层解析,创建一个String数组，接收解析出来的数据

            if (allCities != null && allCities.length > 0) {
                for (String c : allCities) {
                    String[] array = c.split("\\|");//第二层解析，创建一个String数组，接收解析出来的code 和 name

                    //创建一个City对象，并将解析出来的code name 赋值给实体类的变量
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);

                    coolWeatherDB.saveCity(city);//将从服务器解析出来的City数据保存到本地数据库
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 处理和解析服务器返回的县级的数据
     *
     * @param coolWeatherDB
     * @param response
     * @param cityId
     * @return
     */
    public static boolean handlerCounties(CoolWeatherDB coolWeatherDB, String response, int cityId) {

        if (!TextUtils.isEmpty(response)) {
            String[] counties = response.split(",");//第一次解析

            if (counties != null && counties.length > 0) {
                for (String c : counties) {
                    String[] array = c.split("\\|");//第二层解析

                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);

                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }
}
