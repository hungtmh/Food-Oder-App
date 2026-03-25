package com.example.food_order_app.utils;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.example.food_order_app.R;

public class LocationHelper {
    private JSONArray rawData;

    public static class LocationItem {
        public String id;
        public String name;

        public LocationItem(String id, String name) {
            this.id = id;
            this.name = name;
        }
        @Override
        public String toString() {
            return name;
        }
    }

    public LocationHelper(Context context) {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.vn_locations);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonStr = new String(buffer, "UTF-8");
            rawData = new JSONArray(jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
            rawData = new JSONArray();
        }
    }

    public List<LocationItem> getProvinces() {
        List<LocationItem> list = new ArrayList<>();
        list.add(new LocationItem("", "-- Chọn Tỉnh/Thành phố --"));
        if (rawData == null) return list;
        try {
            for (int i = 0; i < rawData.length(); i++) {
                JSONObject obj = rawData.getJSONObject(i);
                list.add(new LocationItem(obj.getString("Id"), obj.getString("Name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<LocationItem> getDistricts(String provinceId) {
        List<LocationItem> list = new ArrayList<>();
        list.add(new LocationItem("", "-- Chọn Quận/Huyện --"));
        if (provinceId == null || provinceId.isEmpty()) return list;
        try {
            for (int i = 0; i < rawData.length(); i++) {
                JSONObject prov = rawData.getJSONObject(i);
                if (prov.getString("Id").equals(provinceId)) {
                    JSONArray districts = prov.getJSONArray("Districts");
                    for (int j = 0; j < districts.length(); j++) {
                        JSONObject dist = districts.getJSONObject(j);
                        list.add(new LocationItem(dist.getString("Id"), dist.getString("Name")));
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<LocationItem> getWards(String provinceId, String districtId) {
        List<LocationItem> list = new ArrayList<>();
        list.add(new LocationItem("", "-- Chọn Phường/Xã --"));
        if (provinceId == null || districtId == null || provinceId.isEmpty() || districtId.isEmpty()) return list;
        try {
            for (int i = 0; i < rawData.length(); i++) {
                JSONObject prov = rawData.getJSONObject(i);
                if (prov.getString("Id").equals(provinceId)) {
                    JSONArray districts = prov.getJSONArray("Districts");
                    for (int j = 0; j < districts.length(); j++) {
                        JSONObject dist = districts.getJSONObject(j);
                        if (dist.getString("Id").equals(districtId)) {
                            JSONArray wards = dist.getJSONArray("Wards");
                            for (int k = 0; k < wards.length(); k++) {
                                JSONObject ward = wards.getJSONObject(k);
                                list.add(new LocationItem(ward.getString("Id"), ward.getString("Name")));
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
