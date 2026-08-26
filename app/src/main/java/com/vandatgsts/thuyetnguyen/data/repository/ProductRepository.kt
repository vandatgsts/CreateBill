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
        val json = prefs.getString(KEY_PRODUCTS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<ProductTemplate>>() {}.type
                val list: List<ProductTemplate> = gson.fromJson(json, type) ?: emptyList()
                if (list.isNotEmpty()) {
                    _products.value = list.sortedBy { it.name }
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Nạp danh sách sản phẩm chuẩn từ .ai/.default
        val defaultList = createDefaultProducts()
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
        return listOf(
            ProductTemplate(
                id = "p-1",
                name = "Bồn 500 nằm TP",
                unit = "Bồn",
                defaultPrice = 2090000.0,
                note = "Bồn chứa nước nằm"
            ),
            ProductTemplate(
                id = "p-2",
                name = "Bồn 500 nằm",
                unit = "Bồn",
                defaultPrice = 2150000.0,
                note = "Bồn chứa nước nằm"
            ),
            ProductTemplate(
                id = "p-3",
                name = "Bồn ĐA 500 nằm",
                unit = "Bồn",
                defaultPrice = 2150000.0,
                note = "Bồn Đại Ánh 500 nằm"
            ),
            ProductTemplate(
                id = "p-4",
                name = "Bồn 500 đứng ĐA",
                unit = "Bồn",
                defaultPrice = 1970000.0,
                note = "Bồn Đại Ánh 500 đứng"
            ),
            ProductTemplate(
                id = "p-5",
                name = "Bồn 1000 đứng",
                unit = "Bồn",
                defaultPrice = 2980000.0,
                note = "Bồn inox 1000 lít đứng"
            ),
            ProductTemplate(
                id = "p-6",
                name = "Máy 12",
                unit = "Máy",
                defaultPrice = 2340000.0,
                note = "Máy NLMT 12 ống"
            ),
            ProductTemplate(
                id = "p-7",
                name = "Máy NLMT 130 lít",
                unit = "Máy",
                defaultPrice = 4480000.0,
                note = "Máy năng lượng mặt trời 130L"
            ),
            ProductTemplate(
                id = "p-8",
                name = "Máy NLMT 15",
                unit = "Máy",
                defaultPrice = 3900000.0,
                note = "Máy NLMT 15 ống"
            ),
            ProductTemplate(
                id = "p-9",
                name = "Máy NLMT 160 lít",
                unit = "Máy",
                defaultPrice = 5475000.0,
                note = "Máy năng lượng mặt trời 160L"
            ),
            ProductTemplate(
                id = "p-10",
                name = "Máy NLMT Bình Minh 130 lít",
                unit = "Máy",
                defaultPrice = 3300000.0,
                note = "Máy NLMT Bình Minh 130L"
            ),
            ProductTemplate(
                id = "p-11",
                name = "Máy NLMT Bình Minh 140 lít",
                unit = "Máy",
                defaultPrice = 4020000.0,
                note = "Máy NLMT Bình Minh 140L"
            ),
            ProductTemplate(
                id = "p-12",
                name = "Bình phụ 20 lít",
                unit = "Bình",
                defaultPrice = 700000.0,
                note = "Bình phụ cấp nước 20L"
            ),
            ProductTemplate(
                id = "p-13",
                name = "Vệ sinh & Hàn & Phủ silicon cho 1 bồn",
                unit = "Bồn",
                defaultPrice = 4000000.0,
                note = "Dịch vụ kỹ thuật & bảo trì bồn nước"
            )
        )
    }


    companion object {
        private const val KEY_PRODUCTS = "key_products_default_v2"

        @Volatile
        private var instance: ProductRepository? = null

        fun getInstance(context: Context): ProductRepository {
            return instance ?: synchronized(this) {
                instance ?: ProductRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
