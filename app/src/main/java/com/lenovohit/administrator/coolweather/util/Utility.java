package com.lenovohit.administrator.coolweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.lenovohit.administrator.coolweather.db.City;
import com.lenovohit.administrator.coolweather.db.County;
import com.lenovohit.administrator.coolweather.db.Province;
import com.lenovohit.administrator.coolweather.entity.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by SharkChao on 2017-08-08.
 * 此方法用于解析
 */

public class Utility {
    /**
     * 解析并且存储省份
     * @return
     */
    public static boolean parseAndSaveProvince(String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i <jsonArray.length(); i++){
                    JSONObject object = jsonArray.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(object.getString("name"));
                    province.setProvinceCode(object.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析并且存储城市
     * @return
     */
    public static boolean parseAndSaveCity(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i <jsonArray.length(); i++){
                    JSONObject object = jsonArray.getJSONObject(i);
                    City city = new City();
                    city.setCityName(object.getString("name"));
                    city.setCityCode(object.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析并且存储县城
     * @return
     */
    public static boolean parseAndSaveCounty(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i <jsonArray.length(); i++){
                    JSONObject object = jsonArray.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(object.getString("name"));
                    county.setWeatherId(object.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * 解析天气信息为weather实体
     */
    public static Weather parseWeather(String response){
        if (!TextUtils.isEmpty(response)){
            Gson gson = new Gson();
            Weather weather = gson.fromJson(response, Weather.class);
            return weather;
        }
        return null;
    }
}
