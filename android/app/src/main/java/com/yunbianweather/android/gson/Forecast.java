package com.yunbianweather.android.gson;


import com.google.gson.annotations.SerializedName;

public class Forecast {
    public String date;//要预测哪一天的天气

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature{
        public String max;//最高温
        public String min;//最低温
    }

    public class More{
        @SerializedName("txt_d")
        public String info;//天气情况（多云、晴、小雨...）
    }
}
