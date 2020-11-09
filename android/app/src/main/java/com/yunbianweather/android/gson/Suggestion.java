package com.yunbianweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;

    public class Comfort{
        @SerializedName("txt")
        public String info;//对舒适度的建议
    }

    public class CarWash{
        @SerializedName("txt")
        public String info;//对洗车的建议
    }

    public class Sport{
        @SerializedName("txt")
        public String info;//对运动的建议
    }

}
