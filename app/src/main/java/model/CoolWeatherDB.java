package model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import db.CoolWeatherOpenHelper;

/**
 * 该类主要封装了一些常用的数据库操作
 * Created by T on 2016/4/7.
 */
public class CoolWeatherDB {

    /**
     * 数据库名称
     */
    public static final String DB_NAME = "cool_weather";

    /**
     * 数据库版本
     */
    public static final int VERSION = 1;

    /**
     * 创建CoolWeatherDB SQLiteDatabase 对象
     */
    public static CoolWeatherDB coolWeatherDB;
    private SQLiteDatabase db;

    /**
     * 创建一个构造方法，并私有化
     *
     * @param context
     */
    private CoolWeatherDB(Context context) {
        //创建一个CoolWeatherOpenHelper对象
        CoolWeatherOpenHelper coolWeatherOpenHelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);

        //执行getWritableDatabase()方法,获得一个SQLiteDatabase对象
        db = coolWeatherOpenHelper.getWritableDatabase();
    }

    /**
     * 获取CoolWeatherDB的实例
     *
     * @param context
     * @return
     */
    public synchronized static CoolWeatherDB getInstance(Context context) {

        if (coolWeatherDB == null) {
            coolWeatherDB = new CoolWeatherDB(context);
        }

        return coolWeatherDB;
    }

    /**
     * 将Province实例存储到数据库
     *
     * @param province
     */
    public void saveProvince(Province province) {

        if (province != null) {
            ContentValues values = new ContentValues();
            values.put("province_name", province.getProvinceName());
            values.put("province_code", province.getProvinceCode());
            db.insert("Province", null, values);
        }
    }

    /**
     * 从数据库中读取全国所有的省份信息
     *
     * @return
     */
    public List<Province> loadProvinces() {

        List<Province> list = new ArrayList<Province>();

        //执行数据库查询语句，并接收返回的结果
        Cursor cursor = db.query("Province", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                //创建一个Province对象
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));//获取id列的值
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));//获取province_name列的值
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));//获取province_code列的值

                //将读取出来的每个Province对象添加到List
                list.add(province);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    /**
     * 将City实例存储到数据库
     *
     * @param city
     */
    public void saveCity(City city) {

        if (city != null) {
            ContentValues values = new ContentValues();
            values.put("city_name", city.getCityName());
            values.put("city_code", city.getCityCode());
            values.put("province_id", city.getProvinceId());

            //执行数据库插入操作
            db.insert("City", null, values);
        }
    }

    /**
     * 从数据库 读取某省下所有的城市的信息
     *
     * @param provinceId 外键,所要查询省份的ID
     * @return
     */
    public List<City> loadCity(int provinceId) {
        //创建一个集合对象
        List<City> list = new ArrayList<City>();

        Log.v("provinceId--->", provinceId + "");

        //执行数据库查询操作，并接收返回的数据
        Cursor cursor = db.query("City", null, "province_id = ?", new String[]{String.valueOf(provinceId)}, null, null, null);

//        Cursor cursor = db.query("City", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                //创建一个City对象接收数据
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);

                //将city对象添加到List集合
                list.add(city);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    /**
     * 将County实例存储到数据库中
     *
     * @param county
     */
    public void saveCounty(County county) {

        if (county != null) {
            ContentValues values = new ContentValues();
            values.put("county_name", county.getCountyName());
            values.put("county_code", county.getCountyCode());
            values.put("city_id", county.getCityId());

            //执行数据库插入操作
            db.insert("County", null, values);
        }
    }

    /**
     * 从数据库中读取某城市下所有县的信息
     *
     * @param cityId
     * @return
     */
    public List<County> loadCounty(int cityId) {
        //创建一个List集合，用于存放County实例
        List<County> list = new ArrayList<County>();

        //执行数据库查询操作，并用Cursor接收返回的数据
        Cursor cursor = db.query("County", null, "city_id   = ?", new String[]{String.valueOf(cityId)}, null, null, null);

        if (cursor.moveToFirst()){
            do {
                //创建County实例，用于存储从数据库读取出来的数据
                County county = new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cityId);

                //将读取出来的county实例对象添加到List
                list.add(county);
            }while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return list;
    }
}
