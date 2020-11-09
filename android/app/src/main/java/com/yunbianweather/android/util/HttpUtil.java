package com.yunbianweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

//发送HTTP请求的工具类
public class HttpUtil {
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        //使用OkHttp的方式发送请求
        //1.首先获取OkHttpClient对象
        OkHttpClient client=new OkHttpClient();
        //2.要想发送请求，需要创建一个request对象（如果没有跟上.post()，则默认使用的是get方式发送请求）
        Request request=new Request.Builder().url(address).build();
        //3.发送请求并注册一个回调服务来处理响应
        client.newCall(request).enqueue(callback);
    }
}
