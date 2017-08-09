package com.lenovohit.administrator.coolweather.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lenovohit.administrator.coolweather.R;
import com.lenovohit.administrator.coolweather.activity.WeatherActivity;
import com.lenovohit.administrator.coolweather.db.City;
import com.lenovohit.administrator.coolweather.db.County;
import com.lenovohit.administrator.coolweather.db.Province;
import com.lenovohit.administrator.coolweather.util.HttpUtil;
import com.lenovohit.administrator.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by SharkChao on 2017-08-08.
 * 选择城市fragment
 */

public class ChooseAreaFragment extends Fragment{
    //当前的等级
    private int currentLenver = -1;
    public static final int LENVER_PROVINCE = 0;
    public static  final int LENVER_CITY = 1;
    public static final int LENVET_COUNTY = 2;
    private List<String>dataList = new ArrayList<>();
    private ArrayAdapter mAdapter;
    private TextView mTvTitle;
    private Button mBtnBack;
    private ListView mLvList;
    //选中的省份
    private Province selectProvince;
    //选中的城市
    private City selectCity;
    private ProgressDialog mProgressDialog;
    private List<Province> mProvinceList;
    private List<City> mCityList;
    private List<County> mCountyList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        mTvTitle = (TextView) view.findViewById(R.id.tv_title);
        mBtnBack = (Button) view.findViewById(R.id.back_btn);
        mLvList = (ListView) view.findViewById(R.id.lvList);
        mAdapter = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,dataList);
        mLvList.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLenver == LENVER_PROVINCE){
                    //获取城市
                    selectProvince = mProvinceList.get(position);
                    queryCity();
                }else if (currentLenver == LENVER_CITY){
                    //获取县城
                    selectCity = mCityList.get(position);
                    querycounty();
                }else if (currentLenver == LENVET_COUNTY){
                    WeatherActivity.startWeatherActivity(getActivity(),mCountyList.get(position).getWeatherId());
                }
            }
        });
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLenver == LENVER_CITY){
                    //获取省份
                    queryProvince();
                }else if (currentLenver == LENVET_COUNTY){
                    //获取城市
                    queryCity();
                }
            }
        });
        //查询并展示省份
        queryProvince();
    }

    /**
     * 查询省份，优先从数据库中查找，没有再从服务器获取
     */
    private void queryProvince(){
        mTvTitle.setText("中国");
        mBtnBack.setVisibility(View.GONE);
        mProvinceList = DataSupport.findAll(Province.class);
        if (mProvinceList !=null && mProvinceList.size() > 0){
            dataList.clear();
            for (Province province: mProvinceList){
                dataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mLvList.setSelection(0);
        }else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
        currentLenver = LENVER_PROVINCE;
    }

    /**
     * 查询城市，优先从数据库中查找
     */
    private void queryCity(){
        mTvTitle.setText(selectProvince.getProvinceName());
        mBtnBack.setVisibility(View.VISIBLE);
        mCityList = DataSupport.where("provinceid = ?", String.valueOf(selectProvince.getId())).find(City.class);
        if (mCityList != null && mCityList.size()>0){
            dataList.clear();
            for (City city: mCityList){
                dataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mLvList.setSelection(0);
        }else {
            String address = "http://guolin.tech/api/china/"+selectProvince.getProvinceCode();
            queryFromServer(address,"city");
        }
        currentLenver = LENVER_CITY;
    }

    /**
     * 查询县城，优先从数据库中查找
     */
    private void querycounty(){
        mTvTitle.setText(selectCity.getCityName());
        mBtnBack.setVisibility(View.VISIBLE);
        mCountyList = DataSupport.where("cityid = ?", String.valueOf(selectCity.getId())).find(County.class);
        if (mCountyList != null && mCountyList.size()>0){
            dataList.clear();
            for (County county: mCountyList){
                dataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mLvList.setSelection(0);
        }else {
            String address = "http://guolin.tech/api/china/"+selectProvince.getProvinceCode()+"/"+selectCity.getCityCode();
            queryFromServer(address,"county");
        }
        currentLenver = LENVET_COUNTY;
    }

    /**
     * 从服务器获取省市县信息
     */
    private void queryFromServer(String address, final String tag){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "获取信息失败!", Toast.LENGTH_SHORT).show();
                        closeProgressDialog();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                boolean result = false;
                if (tag.equals("province")){
                    result = Utility.parseAndSaveProvince(json);
                }else if (tag.equals("city")){
                    result = Utility.parseAndSaveCity(json,selectProvince.getId());
                }else if (tag.equals("county")){
                    result = Utility.parseAndSaveCounty(json,selectCity.getId());
                }
                if (result){
                    closeProgressDialog();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tag.equals("province")){
                                queryProvince();
                            }else if (tag.equals("city")){
                                queryCity();
                            }else if (tag.equals("county")){
                                querycounty();
                            }
                        }
                    });
                }
            }
        });
    }
    public void showProgressDialog(){
        if (mProgressDialog == null){
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }
    public void closeProgressDialog(){
        if (mProgressDialog != null){
            mProgressDialog.dismiss();
        }
    }
}
