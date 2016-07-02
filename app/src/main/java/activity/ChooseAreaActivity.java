package activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.t.coolwheather.R;

import java.util.ArrayList;
import java.util.List;

import db.CoolWeatherOpenHelper;
import model.City;
import model.CoolWeatherDB;
import model.County;
import model.Province;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

/**
 * Created by T on 2016/4/7.
 */
public class ChooseAreaActivity extends Activity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView areaListView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList = new ArrayList<String>();

    private List<Province> provinceList;//省列表
    private List<City> cityList;//市列表
    private List<County> countyList;//县列表

    private Province selectedProvince;//选中的省份
    private City selectedCity;//选中的城市
    private County selectedCounty;//选中的县

    private int currentLevel;//当前选中的级别

    private boolean isFromWeatherActivity;      //是否从WeatherActivity跳转过来

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);

        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity){
            Log.d("Choose", "onCreate: " + "测试");
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        initView();//初始化各种控件

        areaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){//如果当前选择是省级
                    selectedProvince = provinceList.get(position);//实例化Province对象，并获取到点击到的对象

                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);//实例化City对象，并获取到点击到的对象
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY){
                    String countyCode = countyList.get(position).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("county_code", countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvince();
    }

    /**
     * 捕获back按键，根据当前的级别来判断，此时应该返回市列表，省列表，还是直接退出
     */
    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if (currentLevel == LEVEL_CITY){
            queryProvince();
        }else {
            if (isFromWeatherActivity){
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }

    private void queryProvince() {
        provinceList = coolWeatherDB.loadProvinces();

        if (provinceList.size() > 0){
            dataList.clear();
            for (Province p : provinceList){
                dataList.add(p.getProvinceName());//从数据库中读取全国所有的省份
            }
            adapter.notifyDataSetChanged();
            areaListView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }else {//否则从服务器中查询
            queryFromServer(null, "province");
        }
    }

    /**
     * 查询选中市所有的县，有限从数据库查询，如果没有再到服务器查询
     */
    private void queryCounties() {
        countyList = coolWeatherDB.loadCounty(selectedCity.getId());//从数据库中获取选中市所有的县的集合

        if (countyList.size() > 0){
            dataList.clear();//先清空之前存储的市级的数据
            for (County c : countyList){
                dataList.add(c.getCountyName());//将市所有的县名称添加到dataList中
            }
            adapter.notifyDataSetChanged();
            areaListView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else {
            //否则从服务器中查询
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }

    /**
     * 查询选中省的所有市，优先从数据库查询，如果没有再到服务器上查询
     */
    private void queryCities() {
        cityList = coolWeatherDB.loadCity(selectedProvince.getId());

        if (cityList.size() > 0){
            dataList.clear();//先清空之前存储的省份的数据
            for (City c : cityList){
                dataList.add(c.getCityName());
            }

            adapter.notifyDataSetChanged();
            areaListView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else {
            //否则从服务器中查询
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    /**
     * 根据传入的代号和类型从服务器上查询省市县数据
     * @param code
     * @param type
     */
    private void queryFromServer(final String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)){
            //如果code不为空，则返回的是省内所有的市
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        }else {
            //如果code为空，则返回的是全国各省的数据
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();

        /**
         * 调用sendHttpRequest() 从服务器获取数据
         */
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;

                if ("province".equals(type)){
                    result = Utility.handleProvincesResponse(coolWeatherDB, response);//将数据存储到本地数据库
                }else if ("city".equals(type)){
                    result = Utility.handlerCitiesResponse(coolWeatherDB, response, selectedProvince.getId());//将数据存储到本地数据库
                }else if ("county".equals(type)){
                    result = Utility.handlerCounties(coolWeatherDB, response, selectedCity.getId());//将数据存储到本地数据库
                }

                if (result){
                    //通过runOnUiThread()方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvince();//从本地数据库读取数据
                            }else if ("city".equals(type)){
                                queryCities();//从本地数据库读取数据
                            }else if ("county".equals(type)){
                                queryCounties();//从本地数据库读取数据
                        }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载....");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void initView() {
        areaListView = (ListView) findViewById(R.id.lv_view);
        titleText = (TextView) findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        areaListView.setAdapter(adapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);//获取CoolWeatherDB对象实例
    }
}
