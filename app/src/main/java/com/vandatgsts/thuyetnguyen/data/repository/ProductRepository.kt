package com.vandatgsts.thuyetnguyen.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vandatgsts.thuyetnguyen.data.model.ProductTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class ProductRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("products_catalog_prefs_v2", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _products = MutableStateFlow<List<ProductTemplate>>(emptyList())
    val products: StateFlow<List<ProductTemplate>> = _products.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        if (prefs.getInt(KEY_CATALOG_VERSION, 0) < CURRENT_CATALOG_VERSION) {
            val defaultList = createDefaultProducts().sortedBy { it.name }
            prefs.edit()
                .putString(KEY_PRODUCTS, gson.toJson(defaultList))
                .putInt(KEY_CATALOG_VERSION, CURRENT_CATALOG_VERSION)
                .apply()
            _products.value = defaultList
            return
        }

        val json = prefs.getString(KEY_PRODUCTS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<ProductTemplate>>() {}.type
                val list: List<ProductTemplate> = gson.fromJson(json, type) ?: emptyList()
                _products.value = list.sortedBy { it.name }
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Khôi phục bảng giá mới nếu dữ liệu lưu bị thiếu hoặc không đọc được.
        val defaultList = createDefaultProducts().sortedBy { it.name }
        _products.value = defaultList
        saveToPrefs(defaultList)
    }

    private fun saveToPrefs(list: List<ProductTemplate>) {
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_PRODUCTS, json).apply()
    }

    suspend fun saveProduct(product: ProductTemplate) = withContext(Dispatchers.IO) {
        val current = _products.value.toMutableList()
        val index = current.indexOfFirst { it.id == product.id }
        if (index >= 0) {
            current[index] = product
        } else {
            current.add(product)
        }
        val sorted = current.sortedBy { it.name }
        _products.value = sorted
        saveToPrefs(sorted)
    }

    suspend fun deleteProduct(id: String) = withContext(Dispatchers.IO) {
        val current = _products.value.filterNot { it.id == id }
        _products.value = current
        saveToPrefs(current)
    }

    suspend fun replaceAll(newList: List<ProductTemplate>) = withContext(Dispatchers.IO) {
        val sorted = newList.sortedBy { it.name }
        _products.value = sorted
        saveToPrefs(sorted)
    }

    suspend fun mergeAll(incomingList: List<ProductTemplate>) = withContext(Dispatchers.IO) {
        val current = _products.value.toMutableList()
        for (incoming in incomingList) {
            val index = current.indexOfFirst { it.id == incoming.id || it.name.equals(incoming.name.trim(), ignoreCase = true) }
            if (index >= 0) {
                current[index] = incoming
            } else {
                current.add(incoming)
            }
        }
        val sorted = current.sortedBy { it.name }
        _products.value = sorted
        saveToPrefs(sorted)
    }

    private fun createDefaultProducts(): List<ProductTemplate> {
        // Bảng giá bồn Đông Á và máy nước nóng NLMT từ .ai/img_4.png, .ai/img_5.png.
        return listOf(
            ProductTemplate(
                id = "dong-a-tank-500-standing",
                name = "Bồn nước Đông Á 500 lít đứng",
                unit = "Bồn",
                defaultPrice = 1970000.0,
                note = "Bảo hành 12 năm"
            ),
            ProductTemplate(
                id = "dong-a-tank-500-horizontal",
                name = "Bồn nước Đông Á 500 lít nằm",
                unit = "Bồn",
                defaultPrice = 2150000.0,
                note = "Bảo hành 12 năm"
            ),
            ProductTemplate(
                id = "dong-a-tank-700-standing",
                name = "Bồn nước Đông Á 700 lít đứng",
                unit = "Bồn",
                defaultPrice = 2350000.0,
                note = "Bảo hành 12 năm"
            ),
            ProductTemplate(
                id = "dong-a-tank-700-horizontal",
                name = "Bồn nước Đông Á 700 lít nằm",
                unit = "Bồn",
                defaultPrice = 2580000.0,
                note = "Bảo hành 12 năm"
            ),
            ProductTemplate(
                id = "dong-a-tank-1000-standing",
                name = "Bồn nước Đông Á 1000 lít đứng",
                unit = "Bồn",
                defaultPrice = 2980000.0,
                note = "Bảo hành 12 năm"
            ),
            ProductTemplate(
                id = "dong-a-tank-1000-horizontal",
                name = "Bồn nước Đông Á 1000 lít nằm",
                unit = "Bồn",
                defaultPrice = 3180000.0,
                note = "Bảo hành 12 năm"
            ),
            ProductTemplate(
                id = "dong-a-tank-1500-standing",
                name = "Bồn nước Đông Á 1500 lít đứng",
                unit = "Bồn",
                defaultPrice = 4960000.0,
                note = "Bảo hành 12 năm"
            ),
            ProductTemplate(
                id = "dong-a-tank-1500-horizontal",
                name = "Bồn nước Đông Á 1500 lít nằm",
                unit = "Bồn",
                defaultPrice = 5160000.0,
                note = "Bảo hành 12 năm"
            ),
            ProductTemplate(
                id = "dong-a-tank-2000-standing",
                name = "Bồn nước Đông Á 2000 lít đứng",
                unit = "Bồn",
                defaultPrice = 5880000.0,
                note = "Bảo hành 12 năm"
            ),
            ProductTemplate(
                id = "dong-a-tank-2000-horizontal",
                name = "Bồn nước Đông Á 2000 lít nằm",
                unit = "Bồn",
                defaultPrice = 6180000.0,
                note = "Bảo hành 12 năm"
            ),
            ProductTemplate(
                id = "dong-a-tank-3000-standing",
                name = "Bồn nước Đông Á 3000 lít đứng",
                unit = "Bồn",
                defaultPrice = 8250000.0,
                note = "Bảo hành 12 năm"
            ),
            ProductTemplate(
                id = "dong-a-tank-3000-horizontal",
                name = "Bồn nước Đông Á 3000 lít nằm",
                unit = "Bồn",
                defaultPrice = 8620000.0,
                note = "Bảo hành 12 năm"
            ),
            ProductTemplate(
                id = "dong-a-tank-5000-standing",
                name = "Bồn nước Đông Á 5000 lít đứng",
                unit = "Bồn",
                defaultPrice = 13670000.0,
                note = "Bảo hành 12 năm"
            ),
            ProductTemplate(
                id = "dong-a-tank-5000-horizontal",
                name = "Bồn nước Đông Á 5000 lít nằm",
                unit = "Bồn",
                defaultPrice = 14350000.0,
                note = "Bảo hành 12 năm"
            ),
            ProductTemplate(
                id = "dong-a-solar-130",
                name = "Máy nước nóng NLMT Đông Á 130 lít",
                unit = "Máy",
                defaultPrice = 4480000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            ),
            ProductTemplate(
                id = "binh-minh-solar-130",
                name = "Máy nước nóng NLMT Bình Minh 130 lít",
                unit = "Máy",
                defaultPrice = 3620000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            ),
            ProductTemplate(
                id = "dong-a-solar-140",
                name = "Máy nước nóng NLMT Đông Á 140 lít",
                unit = "Máy",
                defaultPrice = 4920000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            ),
            ProductTemplate(
                id = "binh-minh-solar-140",
                name = "Máy nước nóng NLMT Bình Minh 140 lít",
                unit = "Máy",
                defaultPrice = 4020000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            ),
            ProductTemplate(
                id = "dong-a-solar-150",
                name = "Máy nước nóng NLMT Đông Á 150 lít",
                unit = "Máy",
                defaultPrice = 5250000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            ),
            ProductTemplate(
                id = "binh-minh-solar-150",
                name = "Máy nước nóng NLMT Bình Minh 150 lít",
                unit = "Máy",
                defaultPrice = 4250000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            ),
            ProductTemplate(
                id = "dong-a-solar-160",
                name = "Máy nước nóng NLMT Đông Á 160 lít",
                unit = "Máy",
                defaultPrice = 5475000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            ),
            ProductTemplate(
                id = "binh-minh-solar-160",
                name = "Máy nước nóng NLMT Bình Minh 160 lít",
                unit = "Máy",
                defaultPrice = 4430000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            ),
            ProductTemplate(
                id = "dong-a-solar-180",
                name = "Máy nước nóng NLMT Đông Á 180 lít",
                unit = "Máy",
                defaultPrice = 6380000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            ),
            ProductTemplate(
                id = "binh-minh-solar-180",
                name = "Máy nước nóng NLMT Bình Minh 180 lít",
                unit = "Máy",
                defaultPrice = 4890000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            ),
            ProductTemplate(
                id = "dong-a-solar-200",
                name = "Máy nước nóng NLMT Đông Á 200 lít",
                unit = "Máy",
                defaultPrice = 7050000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            ),
            ProductTemplate(
                id = "binh-minh-solar-200",
                name = "Máy nước nóng NLMT Bình Minh 200 lít",
                unit = "Máy",
                defaultPrice = 5350000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            ),
            ProductTemplate(
                id = "dong-a-solar-250",
                name = "Máy nước nóng NLMT Đông Á 250 lít",
                unit = "Máy",
                defaultPrice = 8660000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            ),
            ProductTemplate(
                id = "binh-minh-solar-250",
                name = "Máy nước nóng NLMT Bình Minh 250 lít",
                unit = "Máy",
                defaultPrice = 6440000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            ),
            ProductTemplate(
                id = "dong-a-solar-300",
                name = "Máy nước nóng NLMT Đông Á 300 lít",
                unit = "Máy",
                defaultPrice = 9570000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            ),
            ProductTemplate(
                id = "binh-minh-solar-300",
                name = "Máy nước nóng NLMT Bình Minh 300 lít",
                unit = "Máy",
                defaultPrice = 7850000.0,
                note = "Bảo hành 5 năm. Giá đã bao gồm chi phí lắp mái bằng phụ kiện nóng lạnh kết nối trên mái nhà."
            )
        )
    }

    companion object {
        private const val KEY_PRODUCTS = "key_products_default_v2"
        private const val KEY_CATALOG_VERSION = "key_products_catalog_version"
        private const val CURRENT_CATALOG_VERSION = 1

        @Volatile
        private var instance: ProductRepository? = null

        fun getInstance(context: Context): ProductRepository {
            return instance ?: synchronized(this) {
                instance ?: ProductRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
