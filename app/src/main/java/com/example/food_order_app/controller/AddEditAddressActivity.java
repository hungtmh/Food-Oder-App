package com.example.food_order_app.controller;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;
import com.example.food_order_app.model.Address;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.LocationHelper;
import com.example.food_order_app.utils.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditAddressActivity extends AppCompatActivity {
    private ImageView btnBack;
    private TextView tvTitle;
    private EditText edtReceiverName, edtPhone, edtDetailAddress;
    private Spinner spnProvince, spnDistrict, spnWard;
    private Button btnSave;

    private LocationHelper locationHelper;
    private SupabaseDbService dbService;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;

    private List<LocationHelper.LocationItem> listProvinces, listDistricts, listWards;
    private ArrayAdapter<LocationHelper.LocationItem> adapterProvince, adapterDistrict, adapterWard;

    // Intents
    private String addressId = null;
    private boolean isDefault = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_address);

        sessionManager = new SessionManager(this);
        dbService = RetrofitClient.getDbService();
        locationHelper = new LocationHelper(this);

        initViews();
        setupSpinners();
        loadIntentData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        edtReceiverName = findViewById(R.id.edtReceiverName);
        edtPhone = findViewById(R.id.edtPhone);
        edtDetailAddress = findViewById(R.id.edtDetailAddress);
        spnProvince = findViewById(R.id.spnProvince);
        spnDistrict = findViewById(R.id.spnDistrict);
        spnWard = findViewById(R.id.spnWard);
        btnSave = findViewById(R.id.btnSave);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveAddress());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);
    }

    private void setupSpinners() {
        listProvinces = locationHelper.getProvinces();
        adapterProvince = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listProvinces);
        spnProvince.setAdapter(adapterProvince);

        spnProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LocationHelper.LocationItem item = listProvinces.get(position);
                listDistricts = locationHelper.getDistricts(item.id);
                adapterDistrict = new ArrayAdapter<>(AddEditAddressActivity.this, android.R.layout.simple_spinner_dropdown_item, listDistricts);
                spnDistrict.setAdapter(adapterDistrict);
                spnDistrict.setSelection(0);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spnDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LocationHelper.LocationItem provinceItem = (LocationHelper.LocationItem) spnProvince.getSelectedItem();
                LocationHelper.LocationItem districtItem = listDistricts.get(position);
                
                if (provinceItem != null && districtItem != null) {
                    listWards = locationHelper.getWards(provinceItem.id, districtItem.id);
                    adapterWard = new ArrayAdapter<>(AddEditAddressActivity.this, android.R.layout.simple_spinner_dropdown_item, listWards);
                    spnWard.setAdapter(adapterWard);
                    spnWard.setSelection(0);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadIntentData() {
        if (getIntent() != null) {
            addressId = getIntent().getStringExtra("addressId");
            isDefault = getIntent().getBooleanExtra("isDefault", false);
            
            if (addressId != null) {
                tvTitle.setText("Sửa Địa Chỉ");
                edtReceiverName.setText(getIntent().getStringExtra("receiverName"));
                edtPhone.setText(getIntent().getStringExtra("phone"));
                String fullAddress = getIntent().getStringExtra("address");
                
                // Parse full address back into spinners if possible (A simple heuristic split)
                if (fullAddress != null) {
                    String[] parts = fullAddress.split(", ");
                    if (parts.length >= 4) {
                        edtDetailAddress.setText(parts[0]);
                        // Wait for spinners to populate, parsing tree is rough, skip the auto-select on spinners for brevity or implement if needed
                        Toast.makeText(this, "Vui lòng chọn lại Tỉnh/Thành/Phường/Xã", Toast.LENGTH_SHORT).show();
                    } else {
                        edtDetailAddress.setText(fullAddress);
                    }
                }
            } else {
                tvTitle.setText("Thêm Địa Chỉ Mới");
                edtReceiverName.setText(sessionManager.getFullName());
                edtPhone.setText(sessionManager.getPhone());
            }
        }
    }

    private void saveAddress() {
        String name = edtReceiverName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String detail = edtDetailAddress.getText().toString().trim();

        LocationHelper.LocationItem province = (LocationHelper.LocationItem) spnProvince.getSelectedItem();
        LocationHelper.LocationItem district = (LocationHelper.LocationItem) spnDistrict.getSelectedItem();
        LocationHelper.LocationItem ward = (LocationHelper.LocationItem) spnWard.getSelectedItem();

        if (name.isEmpty() || phone.isEmpty() || detail.isEmpty() || 
            province == null || province.id.isEmpty() || 
            district == null || district.id.isEmpty() || 
            ward == null || ward.id.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin và chọn vị trí", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullAddress = detail + ", " + ward.name + ", " + district.name + ", " + province.name;

        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("user_id", sessionManager.getUserId());
        addressMap.put("receiver_name", name);
        addressMap.put("phone", phone);
        addressMap.put("address", fullAddress);
        addressMap.put("is_default", isDefault);

        progressDialog.show();
        if (addressId == null) {
            // Create
            dbService.createAddress(addressMap).enqueue(new Callback<List<Address>>() {
                @Override
                public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(AddEditAddressActivity.this, "Thêm địa chỉ thành công", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddEditAddressActivity.this, "Lỗi thêm địa chỉ", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Address>> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(AddEditAddressActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Update
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("receiver_name", name);
            updateMap.put("phone", phone);
            updateMap.put("address", fullAddress);
            updateMap.put("is_default", isDefault);

            dbService.updateAddress("eq." + addressId, updateMap).enqueue(new Callback<List<Address>>() {
                @Override
                public void onResponse(Call<List<Address>> call, Response<List<Address>> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        Toast.makeText(AddEditAddressActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddEditAddressActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Address>> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(AddEditAddressActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
