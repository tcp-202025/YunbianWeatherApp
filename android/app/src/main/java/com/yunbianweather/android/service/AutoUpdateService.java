package com.yunbianweather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.yunbianweather.android.gson.Weather;
import com.yunbianweather.android.util.HttpUtil;
import com.yunbianweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

//自用更新后台天气的服务
public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * onStartCommand()方法会在每次服务启动的时候调用
     * 将定时任务写在此方法中，因为一旦第一次启动了AutoUpdateService，就会在 onStartCommand()方法里设定一个定时任务，
     * 这样六小时后将会再次启动AutoUpdateService从而也就形成了一个永久的循环，保证AutoUpdateService 的onStartCommand()方法可以每隔六小时就执行一次。
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();//更新天气
        updateBingPic();//更新背景图

        //创建定时任务
        AlarmManager manager=(AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour=6*60*60*1000;//这是6小时的毫秒数
        long triggerAtTime= SystemClock.elapsedRealtime()+anHour;
        Intent i=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);//执行定时任务:启动服务后，每隔六小时开始执行
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息
     */
    private void updateWeather(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        if(weatherString!=null){
            //从缓存中取出当前的天气数据进行解析，以便于拿到weatherId
            Weather weather= Utility.handleWeatherResponse(weatherString);
            String weatherId=weather.basic.weatherId;
            String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=59453da4fec2404caafa0666f29b99eb";
            //拿到weatherId后向服务端发送请求更新最新天气
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                //发送HTTP请求成功时的回调函数
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseText=response.body().string();
                    Weather weather=Utility.handleWeatherResponse(responseText);
                    if(weather!=null && "ok".equals(weather.status)){
                        SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseText);//向SharedPreferences.Editor对象中添加数据
                        editor.apply();//调用apply方法将数据提交，保存到SharedPreferences中
                    }

                }
                //发送HTTP请求失败时的回调函数
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 更新必应每日一图
     */
    private void updateBingPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";//接口地址
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            //发送HTTP请求成功时的回调函数
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);//向SharedPreferences.Editor对象中添加数据
                editor.apply();//将获取到的图片地址存入向SharedPreferences中
            }

            //发送HTTP请求失败时的回调函数
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
        });
    }
}
