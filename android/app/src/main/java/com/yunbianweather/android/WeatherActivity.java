package com.yunbianweather.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.yunbianweather.android.gson.Forecast;
import com.yunbianweather.android.gson.Weather;
import com.yunbianweather.android.service.AutoUpdateService;
import com.yunbianweather.android.util.HttpUtil;
import com.yunbianweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefresh;

    public DrawerLayout drawerLayout;

    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //初始化各种控件
        weatherLayout=(ScrollView) findViewById(R.id.weather_layout);//显示天气信息的整个大布局
        titleCity=(TextView) findViewById(R.id.title_city);//显示城市名
        titleUpdateTime=(TextView) findViewById(R.id.title_update_time);//显示更新时间
        degreeText=(TextView) findViewById(R.id.degree_text);//显示当前气温
        weatherInfoText=(TextView) findViewById(R.id.weather_info_text);//显示天气概况
        forecastLayout=(LinearLayout) findViewById(R.id.forecast_layout);//用于显示未来几天天气信息的布局
        aqiText=(TextView) findViewById(R.id.aqi_text);//显示空气质量
        pm25Text=(TextView) findViewById(R.id.pm25_text);//显示pm2.5指数
        comfortText=(TextView) findViewById(R.id.comfort_text);//显示当前天气的舒适程度
        carWashText=(TextView) findViewById(R.id.car_wash_text);//对洗车的建议
        sportText=(TextView) findViewById(R.id.sport_text);//对运动的建议
        bingPicImg=(ImageView) findViewById(R.id.bing_pic_img);//背景图
        swipeRefresh=(SwipeRefreshLayout) findViewById(R.id.swipe_refresh);//下拉刷新
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);//为刷新时的进度条设置颜色
        drawerLayout=(DrawerLayout) findViewById(R.id.drawer_layout);//滑动菜单的布局
        navButton=(Button) findViewById(R.id.nav_button);//点击按钮出现滑动菜单

        //先从本地读取数据
        //getDefaultSharedPreferences（）方法它接收一个Context参数,并自动使用当前应用程序的包名作为前缀来命名SharedPreferences文件。
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        if(weatherString!=null){
            //有缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            if(weather!=null) {
                showWeatherInfo(weather);
            }
        }else {
            //没有缓存时去服务器查询天气
            String weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        String bingPic=prefs.getString("bing_pic",null);//读取背景图
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);//load方法中传入图片的url，然后加载出图片并设置到bingPicImg这个控件中
        }else {
            loadBingPic();
        }

        //点击按钮出现滑动菜单
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //SwipeRefreshLayout的监听器，当触发下拉刷新操作的时候，监听器中的方法会执行
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新的时候会重新请求服务器接口拿到最新的数据，所以需要先读缓存拿到当前城市的id
                SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                String weatherString=prefs.getString("weather",null);
                Weather weather= Utility.handleWeatherResponse(weatherString);
                if(weather!=null) {
                    String weatherId = weather.basic.weatherId;//拿到当前展示城市的weatherid
                    requestWeather(weatherId);//重新请求，进行天气刷新
                }
            }
        });
    }

    /**
     * 根据天气id请求城市的天气信息
     */
    public void requestWeather(final String weatherId){
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=59453da4fec2404caafa0666f29b99eb";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            //发送HTTP请求成功时的回调函数
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);//向SharedPreferences.Editor对象中添加数据
                            editor.apply();//调用apply方法将数据提交，保存到SharedPreferences中
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);//刷新事件结束时，需要隐藏进度条
                    }
                });
            }
            //发送HTTP请求失败时的回调函数
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);//刷新事件结束时，需要隐藏进度条
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";//接口地址
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            //发送HTTP请求成功时的回调函数
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);//向SharedPreferences.Editor对象中添加数据
                editor.apply();//将获取到的图片地址存入向SharedPreferences中
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            //发送HTTP请求失败时的回调函数
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
        });
    }


    /**
     * 处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather){
        String cityName=weather.basic.cityName;//城市名
        String updateTime=weather.basic.update.updateTime.split(" ")[1];//天气更新时间，格式为“2020-11-06 14:33”，通过空格截断后取出具体的时间
        String degree=weather.now.temperature+"℃";//当前温度
        String weatherInfo=weather.now.more.info;//天气概况

        //将查询到的天气信息添加到相关组件中进行显示
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText=(TextView) view.findViewById(R.id.date_text);//要预测哪一天的天气
            TextView infoText=(TextView) view.findViewById(R.id.info_text);//显示嫌弃概况
            TextView maxText=(TextView) view.findViewById(R.id.max_text);//最高温
            TextView minText=(TextView) view.findViewById(R.id.min_text);//最低温
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);//将子项布局添加到forecastLayout中
        }
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度："+weather.suggestion.comfort.info;
        String carWash="洗车指数："+weather.suggestion.carWash.info;
        String sport="运动建议："+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);

        weatherLayout.setVisibility(View.VISIBLE);

        //最后启动AutoUpdateService这个服务
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}