package com.yunbianweather.android.util;

import android.text.TextUtils;

import com.yunbianweather.android.db.City;
import com.yunbianweather.android.db.County;
import com.yunbianweather.android.db.Province;
import com.yunbianweather.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

//解析和处理JSON格式的工具类
public class Utility {

    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces=new JSONArray(response);//将json格式数据先转换为JSONArray格式的
                for(int i=0;i<allProvinces.length();i++){
                    //循环遍历JSONArray数组，从中取出的每一个对象都是JSONObject
                    JSONObject provinceObject=allProvinces.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();//保存到数据库中
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities=new JSONArray(response);
                for(int i=0;i<allCities.length();i++){
                    JSONObject cityObject=allCities.getJSONObject(i);
                    City city=new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();//将城市信息保存到数据库中
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的区、县级数据
     */
    public static boolean handleCountyResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties=new JSONArray(response);
                for(int i=0;i<allCounties.length();i++){
                    JSONObject countyObject=allCounties.getJSONObject(i);
                    County county=new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();//将区县的数据保存到数据库中
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将返回的JSON数据解析成Weather实体类
     */
    /*
    返回的数据格式：
    {
        HeWeather:[
            {
                {},{},{}...
            }
        ]
    }
     */
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject=new JSONObject(response);//将返回的字符串转换为JsonObject对象
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");//提取出HeWeather部分，是一个JSON数组
            String weatherContent=jsonArray.getJSONObject(0).toString();//取出数组的内容（只有一项）
            return new Gson().fromJson(weatherContent,Weather.class);//通过Gson封装到实体类中
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
