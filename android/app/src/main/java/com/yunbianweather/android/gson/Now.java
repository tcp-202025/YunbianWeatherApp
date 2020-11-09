package com.yunbianweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    @SerializedName("tmp")
    public String temperature;//当前温度

    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;//天气情况（多云、晴、小雨...）
    }
}
