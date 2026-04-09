package com.example.food_order_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.R;
import com.example.food_order_app.adapter.AdminCategoryAdapter;
import com.example.food_order_app.model.Category;
import com.example.food_order_app.model.Food;
import com.example.food_order_app.model.Order;
import com.example.food_order_app.model.OrderItem;
import com.example.food_order_app.network.RetrofitClient;
import com.example.food_order_app.network.SupabaseDbService;
import com.example.food_order_app.utils.AdminBottomNavHelper;
import com.example.food_order_app.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.text.Normalizer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCategoryActivity extends AppCompatActivity implements AdminCategoryAdapter.OnCategoryActionListener {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private RecyclerView rvCategories;
    private EditText edtSearch;
    private ImageView btnSearch;
    private android.widget.TextView tvEmpty;
    private LinearLayout layoutEmpty;
    private FloatingActionButton fabAdd;
    private Button btnAddNow;
    private Spinner spFilterStatus;
    private Spinner spSortMode;
    private LinearLayout layoutBulkActions;
    private TextView tvBulkSelectionCount;
    private Button btnSelectAllCategories;
    private Button btnBulkToggleActive;
    private Button btnBulkSoftDelete;

    private AdminCategoryAdapter adapter;
    private SupabaseDbService dbService;
    private SessionManager sessionManager;
    private ItemTouchHelper itemTouchHelper;

    private List<Category> allCategories = new ArrayList<>();
    private List<Food> allFoods = new ArrayList<>();
    private boolean canManage;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 12;
    private List<Category> currentFiltered = new ArrayList<>();

    private static final String[] STATUS_OPTIONS = {"Tất cả trạng thái", "Đang hoạt động", "Đang ẩn"};
    private static final String[] SORT_OPTIONS = {"Thứ tự tăng", "Thứ tự giảm", "Tên A-Z", "Tên Z-A"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_category);

        dbService = RetrofitClient.getDbService();
        sessionManager = new SessionManager(this);
        canManage = sessionManager.isAdmin() || "manager".equalsIgnoreCase(sessionManager.getRole());

        initViews();
        setupRecycler();
        setupSpinners();
        setupListeners();
        setupDragDrop();
        applyRolePermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdminBottomNavHelper.setup(this, AdminBottomNavHelper.TAB_CATEGORY);
        loadDashboardData();
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rvAdminCategories);
        edtSearch = findViewById(R.id.edtCategorySearch);
        btnSearch = findViewById(R.id.btnCategorySearch);
        tvEmpty = findViewById(R.id.tvCategoryEmpty);
        layoutEmpty = findViewById(R.id.layoutCategoryEmpty);
        fabAdd = findViewById(R.id.fabAddCategory);
        btnAddNow = findViewById(R.id.btnAddCategoryNow);
        spFilterStatus = findViewById(R.id.spFilterStatus);
        spSortMode = findViewById(R.id.spSortMode);
        layoutBulkActions = findViewById(R.id.layoutBulkActions);
        tvBulkSelectionCount = findViewById(R.id.tvBulkSelectionCount);
        btnSelectAllCategories = findViewById(R.id.btnSelectAllCategories);
        btnBulkToggleActive = findViewById(R.id.btnBulkToggleActive);
        btnBulkSoftDelete = findViewById(R.id.btnBulkSoftDelete);
    }

    private void setupRecycler() {
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCategoryAdapter(this, this);
        adapter.setAllowEdit(canManage);
        rvCategories.setAdapter(adapter);
        rvCategories.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) {
                    return;
                }
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm == null) {
                    return;
                }
                int lastVisible = lm.findLastVisibleItemPosition();
                int total = adapter.getItemCount();
                if (total > 0 && lastVisible >= total - 2) {
                    appendNextPage();
                }
            }
        });
    }

    private void setupSpinners() {
        spFilterStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, STATUS_OPTIONS));
        spSortMode.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SORT_OPTIONS));
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> showAddEditDialog(null));
        btnAddNow.setOnClickListener(v -> showAddEditDialog(null));
        btnSearch.setOnClickListener(v -> performSearch());

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        edtSearch.addTextChangedListener(new SimpleTextWatcher(this::performSearch));
        spFilterStatus.setOnItemSelectedListener(new SimpleItemSelectedListener(this::performSearch));
        spSortMode.setOnItemSelectedListener(new SimpleItemSelectedListener(this::performSearch));

        btnSelectAllCategories.setOnClickListener(v -> {
            if (adapter.getSelectedCount() > 0) {
                adapter.clearSelection();
            } else {
                adapter.toggleSelectAll(true);
            }
        });
        btnBulkToggleActive.setOnClickListener(v -> showBulkToggleDialog());
        btnBulkSoftDelete.setOnClickListener(v -> confirmBulkSoftDelete());
    }

    private void setupDragDrop() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                adapter.moveItem(viewHolder.getBindingAdapterPosition(), target.getBindingAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // No swipe action.
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                persistCurrentOrder();
            }
        };
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvCategories);
    }

    private void applyRolePermissions() {
        if (!canManage) {
            fabAdd.setVisibility(View.GONE);
            btnAddNow.setVisibility(View.GONE);
            btnSelectAllCategories.setEnabled(false);
            btnBulkToggleActive.setEnabled(false);
            btnBulkSoftDelete.setEnabled(false);
            Toast.makeText(this, "Bạn chỉ có quyền xem danh mục", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDashboardData() {
        loadCategories(() -> loadFoodsAndRevenueStats(this::performSearch));
    }

    private void loadCategories(Runnable onLoaded) {
        dbService.getAdminCategories("eq.false", "sort_order.asc").enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCategories = response.body();
                    if (onLoaded != null) {
                        onLoaded.run();
                    }
                } else {
                    fallbackLoadCategories(onLoaded);
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                fallbackLoadCategories(onLoaded);
            }
        });
    }

    private void fallbackLoadCategories(Runnable onLoaded) {
        dbService.getCategories(null, "sort_order.asc").enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCategories = response.body();
                    if (onLoaded != null) {
                        onLoaded.run();
                    }
                } else {
                    Toast.makeText(AdminCategoryActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(AdminCategoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFoodsAndRevenueStats(Runnable onDone) {
        dbService.getAdminAllFoods("id,category_id,is_available", "created_at.desc").enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                allFoods = response.isSuccessful() && response.body() != null ? response.body() : new ArrayList<>();
                fillFoodCounters();
                loadRevenueLast7Days(onDone);
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                fillFoodCounters();
                loadRevenueLast7Days(onDone);
            }
        });
    }

    private void fillFoodCounters() {
        Map<String, Integer> totalByCategory = new HashMap<>();
        Map<String, Integer> activeByCategory = new HashMap<>();

        for (Food food : allFoods) {
            if (TextUtils.isEmpty(food.getCategoryId())) {
                continue;
            }
            totalByCategory.put(food.getCategoryId(), totalByCategory.getOrDefault(food.getCategoryId(), 0) + 1);
            if (food.isAvailable()) {
                activeByCategory.put(food.getCategoryId(), activeByCategory.getOrDefault(food.getCategoryId(), 0) + 1);
            }
        }

        for (Category category : allCategories) {
            category.setTotalFoods(totalByCategory.getOrDefault(category.getId(), 0));
            category.setActiveFoods(activeByCategory.getOrDefault(category.getId(), 0));
            category.setRevenueLast7Days(0d);
        }
    }

    private void loadRevenueLast7Days(Runnable onDone) {
        dbService.getAllOrders("id,created_at", "created_at.desc").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    if (onDone != null) {
                        onDone.run();
                    }
                    return;
                }

                long cutoff = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000;
                List<Order> recentOrders = new ArrayList<>();
                for (Order order : response.body()) {
                    if (parseServerDate(order.getCreatedAt()) >= cutoff) {
                        recentOrders.add(order);
                    }
                }

                if (recentOrders.isEmpty()) {
                    if (onDone != null) {
                        onDone.run();
                    }
                    return;
                }

                StringBuilder inBuilder = new StringBuilder("in.(");
                for (int i = 0; i < recentOrders.size(); i++) {
                    inBuilder.append(recentOrders.get(i).getId());
                    if (i < recentOrders.size() - 1) {
                        inBuilder.append(",");
                    }
                }
                inBuilder.append(")");

                dbService.getOrderItems(inBuilder.toString(), "food_id,subtotal").enqueue(new Callback<List<OrderItem>>() {
                    @Override
                    public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, String> foodToCategory = new HashMap<>();
                            for (Food food : allFoods) {
                                if (!TextUtils.isEmpty(food.getId()) && !TextUtils.isEmpty(food.getCategoryId())) {
                                    foodToCategory.put(food.getId(), food.getCategoryId());
                                }
                            }

                            Map<String, Double> revenueByCategory = new HashMap<>();
                            for (OrderItem item : response.body()) {
                                String categoryId = foodToCategory.get(item.getFoodId());
                                if (categoryId != null) {
                                    revenueByCategory.put(categoryId,
                                            revenueByCategory.getOrDefault(categoryId, 0d) + item.getSubtotal());
                                }
                            }

                            for (Category category : allCategories) {
                                category.setRevenueLast7Days(revenueByCategory.getOrDefault(category.getId(), 0d));
                            }
                        }

                        if (onDone != null) {
                            onDone.run();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<OrderItem>> call, Throwable t) {
                        if (onDone != null) {
                            onDone.run();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                if (onDone != null) {
                    onDone.run();
                }
            }
        });
    }

    private void performSearch() {
        String query = removeDiacritics(edtSearch.getText().toString().trim().toLowerCase(Locale.ROOT));
        String statusFilter = String.valueOf(spFilterStatus.getSelectedItem());
        String sortMode = String.valueOf(spSortMode.getSelectedItem());

        List<Category> filtered = new ArrayList<>();
        for (Category category : allCategories) {
            String displayName = removeDiacritics((category.getName() == null ? "" : category.getName()).toLowerCase(Locale.ROOT));
            boolean matchesQuery = query.isEmpty() || displayName.contains(query);
            boolean matchesStatus = "Tất cả trạng thái".equals(statusFilter)
                    || ("Đang hoạt động".equals(statusFilter) && category.isActive())
                    || ("Đang ẩn".equals(statusFilter) && !category.isActive());

            if (matchesQuery && matchesStatus) {
                filtered.add(category);
            }
        }

        applySort(filtered, sortMode);
        currentFiltered = filtered;
        currentPage = 0;

        List<Category> firstPage = new ArrayList<>();
        int limit = Math.min(PAGE_SIZE, currentFiltered.size());
        for (int i = 0; i < limit; i++) {
            firstPage.add(currentFiltered.get(i));
        }
        adapter.setCategories(firstPage);
        updateEmptyState(query);
    }

    private void appendNextPage() {
        int loaded = adapter.getItemCount();
        if (loaded >= currentFiltered.size()) {
            return;
        }

        currentPage++;
        int nextLimit = Math.min((currentPage + 1) * PAGE_SIZE, currentFiltered.size());
        List<Category> nextData = new ArrayList<>();
        for (int i = 0; i < nextLimit; i++) {
            nextData.add(currentFiltered.get(i));
        }
        adapter.setCategories(nextData);
    }

    private void updateEmptyState(String query) {
        boolean isEmpty = currentFiltered.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (!isEmpty) {
            return;
        }
        if (TextUtils.isEmpty(query)) {
            tvEmpty.setText("Chưa có danh mục nào");
        } else {
            tvEmpty.setText("Không tìm thấy danh mục phù hợp");
        }
    }

    private void applySort(List<Category> list, String sortMode) {
        if ("Thứ tự giảm".equals(sortMode)) {
            list.sort((a, b) -> Integer.compare(b.getSortOrder(), a.getSortOrder()));
        } else if ("Tên A-Z".equals(sortMode)) {
            list.sort((a, b) -> {
                String an = sortNameKey(a.getName());
                String bn = sortNameKey(b.getName());
                int normalizedCompare = an.compareTo(bn);
                if (normalizedCompare != 0) {
                    return normalizedCompare;
                }
                String aRaw = a.getName() == null ? "" : a.getName();
                String bRaw = b.getName() == null ? "" : b.getName();
                return aRaw.compareToIgnoreCase(bRaw);
            });
        } else if ("Tên Z-A".equals(sortMode)) {
            list.sort((a, b) -> {
                String an = sortNameKey(a.getName());
                String bn = sortNameKey(b.getName());
                int normalizedCompare = bn.compareTo(an);
                if (normalizedCompare != 0) {
                    return normalizedCompare;
                }
                String aRaw = a.getName() == null ? "" : a.getName();
                String bRaw = b.getName() == null ? "" : b.getName();
                return bRaw.compareToIgnoreCase(aRaw);
            });
        } else {
            list.sort(Comparator.comparingInt(Category::getSortOrder));
        }
    }

    private String sortNameKey(String rawName) {
        return removeDiacritics(rawName == null ? "" : rawName)
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    @Override
    public void onEdit(Category category) {
        if (!canManage) {
            Toast.makeText(this, "Bạn không có quyền sửa", Toast.LENGTH_SHORT).show();
            return;
        }
        showAddEditDialog(category);
    }

    @Override
    public void onDelete(Category category) {
        if (!canManage) {
            Toast.makeText(this, "Bạn không có quyền xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        int activeFoodCount = countActiveFoodsInCategory(category.getId());
        if (activeFoodCount > 0) {
            Toast.makeText(this,
                    "Không thể xóa vì còn " + activeFoodCount + " món đang bán trong danh mục này",
                    Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa mềm")
                .setMessage("Danh mục \"" + category.getName() + "\" sẽ được ẩn khỏi hệ thống (không xóa vĩnh viễn).")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCategoryWithUndo(category))
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onCategoryClick(Category category) {
        Intent intent = new Intent(this, AdminCategoryDetailActivity.class);
        intent.putExtra(AdminCategoryDetailActivity.EXTRA_CATEGORY_ID, category.getId());
        intent.putExtra(AdminCategoryDetailActivity.EXTRA_CATEGORY_NAME, category.getName());
        startActivity(intent);
    }

    @Override
    public void onToggleActive(Category category, boolean isActive) {
        if (!canManage) {
            return;
        }
        boolean before = category.isActive();
        category.setActive(isActive);

        Map<String, Object> updates = new HashMap<>();
        updates.put("is_active", isActive);
        dbService.updateCategory("eq." + category.getId(), updates).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful()) {
                    Snackbar.make(rvCategories, "Đã cập nhật trạng thái", Snackbar.LENGTH_LONG)
                            .setAction("Hoàn tác", v -> updateActiveState(category, before, true))
                            .show();
                    performSearch();
                } else {
                    category.setActive(before);
                    performSearch();
                    Toast.makeText(AdminCategoryActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                category.setActive(before);
                performSearch();
                Toast.makeText(AdminCategoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder holder) {
        if (canManage && itemTouchHelper != null) {
            itemTouchHelper.startDrag(holder);
        }
    }

    @Override
    public void onMoreOptions(Category category, View anchorView) {
        if (!canManage) {
            return;
        }
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_admin_category_more, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit_category) {
                onEdit(category);
                return true;
            }
            if (itemId == R.id.action_delete_category) {
                onDelete(category);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    @Override
    public void onSelectionChanged(int selectedCount) {
        if (selectedCount > 0) {
            tvBulkSelectionCount.setText("Đã chọn " + selectedCount + " danh mục");
            btnSelectAllCategories.setText("Bỏ chọn hết");
        } else {
            tvBulkSelectionCount.setText("Chưa chọn danh mục (tick ô vuông để chọn)");
            btnSelectAllCategories.setText("Chọn hết");
        }
    }

    private void persistCurrentOrder() {
        List<Category> ordered = adapter.getCategories();
        for (int i = 0; i < ordered.size(); i++) {
            Category category = ordered.get(i);
            int newOrder = i + 1;
            category.setSortOrder(newOrder);
            Map<String, Object> updates = new HashMap<>();
            updates.put("sort_order", newOrder);
            dbService.updateCategory("eq." + category.getId(), updates).enqueue(new EmptyCategoryCallback());
        }
        performSearch();
    }

    private void deleteCategoryWithUndo(Category category) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("is_deleted", true);
        updates.put("is_active", false);
        dbService.updateCategory("eq." + category.getId(), updates).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful()) {
                    allCategories.remove(category);
                    performSearch();
                    adapter.clearSelection();
                    Snackbar.make(rvCategories, "Đã xóa mềm danh mục", Snackbar.LENGTH_LONG)
                            .setAction("Hoàn tác", v -> restoreDeletedCategory(category))
                            .show();
                } else {
                    Toast.makeText(AdminCategoryActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(AdminCategoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restoreDeletedCategory(Category category) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("is_deleted", false);
        updates.put("is_active", true);
        dbService.updateCategory("eq." + category.getId(), updates).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful()) {
                    loadDashboardData();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(AdminCategoryActivity.this, "Hoàn tác thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteWithFoodConstraintDialog(Category category, int foodCount) {
        List<Category> targets = getTargetCategories(category.getId());
        String[] targetNames = new String[targets.size()];
        for (int i = 0; i < targets.size(); i++) {
            targetNames[i] = targets.get(i).getName();
        }
        final int[] selectedTarget = {-1};

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Danh mục còn " + foodCount + " món")
                .setMessage("Chọn cách xử lý trước khi xóa")
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Xóa cả món", (dialog, which) -> {
                    dbService.deleteFoodsByCategory("eq." + category.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                deleteCategoryWithUndo(category);
                            } else {
                                Toast.makeText(AdminCategoryActivity.this, "Không thể xóa món", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(AdminCategoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

        if (!targets.isEmpty()) {
            builder.setSingleChoiceItems(targetNames, -1, (dialog, which) -> selectedTarget[0] = which);
            builder.setPositiveButton("Chuyển món rồi xóa", (dialog, which) -> {
                if (selectedTarget[0] < 0 || selectedTarget[0] >= targets.size()) {
                    Toast.makeText(this, "Vui lòng chọn danh mục đích", Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String, Object> updates = new HashMap<>();
                updates.put("category_id", targets.get(selectedTarget[0]).getId());
                dbService.updateFoodsByCategory("eq." + category.getId(), updates).enqueue(new Callback<List<Food>>() {
                    @Override
                    public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                        if (response.isSuccessful()) {
                            deleteCategoryWithUndo(category);
                        } else {
                            Toast.makeText(AdminCategoryActivity.this, "Chuyển món thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Food>> call, Throwable t) {
                        Toast.makeText(AdminCategoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
        builder.show();
    }

    private void showAddEditDialog(Category category) {
        if (!canManage) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_category, null);
        builder.setView(view);

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextInputEditText edtName = view.findViewById(R.id.edtDialogCategoryName);
        TextInputEditText edtIcon = view.findViewById(R.id.edtDialogCategoryIcon);
        TextInputEditText edtSort = view.findViewById(R.id.edtDialogCategorySort);
        CheckBox cbActive = view.findViewById(R.id.cbDialogCategoryActive);
        Button btnCancel = view.findViewById(R.id.btnDialogCancel);
        Button btnSave = view.findViewById(R.id.btnDialogSave);

        AlertDialog dialog = builder.create();

        if (category != null) {
            tvTitle.setText("Sửa danh mục");
            edtName.setText(category.getName());
            edtIcon.setText(category.getIconUrl());
            edtSort.setText(String.valueOf(category.getSortOrder()));
            cbActive.setChecked(category.isActive());
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = textOf(edtName);
            String icon = textOf(edtIcon);
            int sort = parseIntSafe(textOf(edtSort));
            boolean active = cbActive.isChecked();

            if (name.isEmpty()) {
                edtName.setError("Không được để trống");
                return;
            }

            if (category == null) {
                Category newCat = new Category();
                newCat.setName(name);
                newCat.setIconUrl(icon);
                newCat.setSortOrder(sort);
                newCat.setActive(active);

                dbService.createCategory(newCat).enqueue(new Callback<List<Category>>() {
                    @Override
                    public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Toast.makeText(AdminCategoryActivity.this, "Đã thêm danh mục", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadDashboardData();
                        } else {
                            Toast.makeText(AdminCategoryActivity.this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Category>> call, Throwable t) {
                        Toast.makeText(AdminCategoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Category before = cloneCategory(category);
                Map<String, Object> updates = new HashMap<>();
                updates.put("name", name);
                updates.put("icon_url", icon);
                updates.put("sort_order", sort);
                updates.put("is_active", active);

                dbService.updateCategory("eq." + category.getId(), updates).enqueue(new Callback<List<Category>>() {
                    @Override
                    public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminCategoryActivity.this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadDashboardData();
                            Snackbar.make(rvCategories, "Đã cập nhật danh mục", Snackbar.LENGTH_LONG)
                                    .setAction("Hoàn tác", v -> undoCategoryEdit(before))
                                    .show();
                        } else {
                            Toast.makeText(AdminCategoryActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Category>> call, Throwable t) {
                        Toast.makeText(AdminCategoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.show();
    }

    private void undoCategoryEdit(Category before) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", before.getName());
        updates.put("icon_url", before.getIconUrl());
        updates.put("sort_order", before.getSortOrder());
        updates.put("is_active", before.isActive());
        dbService.updateCategory("eq." + before.getId(), updates).enqueue(new EmptyCategoryCallback());
        loadDashboardData();
    }

    private void updateActiveState(Category category, boolean active, boolean reload) {
        category.setActive(active);
        Map<String, Object> updates = new HashMap<>();
        updates.put("is_active", active);
        dbService.updateCategory("eq." + category.getId(), updates).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (reload) {
                    loadDashboardData();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                // Keep UI responsive; explicit error already shown in caller.
            }
        });
    }

    private int countFoodsInCategory(String categoryId) {
        int count = 0;
        for (Food food : allFoods) {
            if (categoryId != null && categoryId.equals(food.getCategoryId())) {
                count++;
            }
        }
        return count;
    }

    private int countActiveFoodsInCategory(String categoryId) {
        int count = 0;
        for (Food food : allFoods) {
            if (categoryId != null && categoryId.equals(food.getCategoryId()) && food.isAvailable()) {
                count++;
            }
        }
        return count;
    }

    private void showBulkToggleDialog() {
        List<Category> selected = adapter.getSelectedCategories();
        if (selected.isEmpty()) {
            return;
        }
        String[] options = {"Ẩn danh mục đã chọn", "Hiện danh mục đã chọn"};
        new AlertDialog.Builder(this)
                .setTitle("Cập nhật hàng loạt")
                .setItems(options, (dialog, which) -> applyBulkActiveUpdate(selected, which == 1))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void applyBulkActiveUpdate(List<Category> selected, boolean active) {
        if (selected.isEmpty()) {
            return;
        }
        AtomicInteger done = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        for (Category category : selected) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("is_active", active);
            dbService.updateCategory("eq." + category.getId(), updates).enqueue(new Callback<List<Category>>() {
                @Override
                public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                    if (response.isSuccessful()) {
                        success.incrementAndGet();
                    }
                    if (done.incrementAndGet() == selected.size()) {
                        onBulkFinished("Đã cập nhật " + success.get() + "/" + selected.size() + " danh mục");
                    }
                }

                @Override
                public void onFailure(Call<List<Category>> call, Throwable t) {
                    if (done.incrementAndGet() == selected.size()) {
                        onBulkFinished("Đã cập nhật " + success.get() + "/" + selected.size() + " danh mục");
                    }
                }
            });
        }
    }

    private void confirmBulkSoftDelete() {
        List<Category> selected = adapter.getSelectedCategories();
        if (selected.isEmpty()) {
            return;
        }

        List<String> blockedNames = new ArrayList<>();
        for (Category category : selected) {
            if (countActiveFoodsInCategory(category.getId()) > 0) {
                blockedNames.add(category.getName());
            }
        }
        if (!blockedNames.isEmpty()) {
            Toast.makeText(this,
                    "Không thể xóa các danh mục còn món đang bán: " + TextUtils.join(", ", blockedNames),
                    Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa mềm hàng loạt")
                .setMessage("Xác nhận xóa mềm " + selected.size() + " danh mục đã chọn?")
                .setPositiveButton("Xóa", (d, w) -> applyBulkSoftDelete(selected))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void applyBulkSoftDelete(List<Category> selected) {
        AtomicInteger done = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        for (Category category : selected) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("is_deleted", true);
            updates.put("is_active", false);
            dbService.updateCategory("eq." + category.getId(), updates).enqueue(new Callback<List<Category>>() {
                @Override
                public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                    if (response.isSuccessful()) {
                        success.incrementAndGet();
                    }
                    if (done.incrementAndGet() == selected.size()) {
                        onBulkFinished("Đã xóa mềm " + success.get() + "/" + selected.size() + " danh mục");
                    }
                }

                @Override
                public void onFailure(Call<List<Category>> call, Throwable t) {
                    if (done.incrementAndGet() == selected.size()) {
                        onBulkFinished("Đã xóa mềm " + success.get() + "/" + selected.size() + " danh mục");
                    }
                }
            });
        }
    }

    private void onBulkFinished(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        adapter.clearSelection();
        loadDashboardData();
    }

    

    private List<Category> getTargetCategories(String excludedId) {
        List<Category> targets = new ArrayList<>();
        for (Category category : allCategories) {
            if (!TextUtils.equals(category.getId(), excludedId)) {
                targets.add(category);
            }
        }
        return targets;
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String removeDiacritics(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String result = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
        return result.replace('đ', 'd').replace('Đ', 'D');
    }

    private long parseServerDate(String iso) {
        if (TextUtils.isEmpty(iso)) {
            return 0;
        }
        String normalized = iso.replace("Z", "+0000");
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd HH:mm:ss"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(normalized);
                if (date != null) {
                    return date.getTime();
                }
            } catch (ParseException ignored) {
            }
        }
        return 0;
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private Category cloneCategory(Category source) {
        Category copy = new Category();
        copy.setId(source.getId());
        copy.setName(source.getName());
        copy.setIconUrl(source.getIconUrl());
        copy.setSortOrder(source.getSortOrder());
        copy.setActive(source.isActive());
        return copy;
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable onChange;

        SimpleTextWatcher(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onChange.run();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private static class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {
        private final Runnable action;

        SimpleItemSelectedListener(Runnable action) {
            this.action = action;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            action.run();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private static class EmptyCategoryCallback implements Callback<List<Category>> {
        @Override
        public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
        }

        @Override
        public void onFailure(Call<List<Category>> call, Throwable t) {
        }
    }
}