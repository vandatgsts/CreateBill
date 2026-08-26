package com.vandatgsts.thuyetnguyen.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vandatgsts.thuyetnguyen.data.model.InvoiceType
import com.vandatgsts.thuyetnguyen.data.model.StorePartner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class StorePartnerRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("store_partners_prefs_v2", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _stores = MutableStateFlow<List<StorePartner>>(emptyList())
    val stores: StateFlow<List<StorePartner>> = _stores.asStateFlow()

    init {
        loadStores()
    }

    private fun loadStores() {
        val json = prefs.getString(KEY_STORES, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<StorePartner>>() {}.type
                val list: List<StorePartner> = gson.fromJson(json, type) ?: emptyList()
                if (list.isNotEmpty()) {
                    _stores.value = list.sortedBy { it.name }
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val defaultList = createDefaultStores()
        _stores.value = defaultList
        saveToPrefs(defaultList)
    }

    private fun saveToPrefs(list: List<StorePartner>) {
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_STORES, json).apply()
    }

    fun getStoreById(id: String): StorePartner? {
        return _stores.value.find { it.id == id }
    }

    fun getStoreByName(name: String): StorePartner? {
        return _stores.value.find { it.name.equals(name.trim(), ignoreCase = true) }
    }

    suspend fun saveStore(store: StorePartner) = withContext(Dispatchers.IO) {
        val current = _stores.value.toMutableList()
        val index = current.indexOfFirst { it.id == store.id || it.name.equals(store.name.trim(), ignoreCase = true) }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(
                name = store.name.trim(),
                phone = if (store.phone.isNotBlank()) store.phone else existing.phone,
                address = if (store.address.isNotBlank()) store.address else existing.address,
                contactPerson = if (store.contactPerson.isNotBlank()) store.contactPerson else existing.contactPerson,
                defaultType = store.defaultType,
                note = if (store.note.isNotBlank()) store.note else existing.note
            )
        } else {
            current.add(store.copy(name = store.name.trim()))
        }
        val sorted = current.sortedBy { it.name }
        _stores.value = sorted
        saveToPrefs(sorted)
    }

    suspend fun deleteStore(id: String) = withContext(Dispatchers.IO) {
        val current = _stores.value.filterNot { it.id == id }
        _stores.value = current
        saveToPrefs(current)
    }

    suspend fun replaceAll(newList: List<StorePartner>) = withContext(Dispatchers.IO) {
        val sorted = newList.sortedBy { it.name }
        _stores.value = sorted
        saveToPrefs(sorted)
    }

    suspend fun mergeAll(incomingList: List<StorePartner>) = withContext(Dispatchers.IO) {
        val current = _stores.value.toMutableList()
        for (incoming in incomingList) {
            val index = current.indexOfFirst { it.id == incoming.id || it.name.equals(incoming.name.trim(), ignoreCase = true) }
            if (index >= 0) {
                current[index] = incoming
            } else {
                current.add(incoming)
            }
        }
        val sorted = current.sortedBy { it.name }
        _stores.value = sorted
        saveToPrefs(sorted)
    }

    private fun createDefaultStores(): List<StorePartner> {
        return listOf(
            StorePartner(
                id = "store-thang-loi",
                name = "Cửa Hàng Thắng Lợi",
                phone = "",
                address = "Trung Mỹ Tây (theo định vị), Trịnh Thị Miếng",
                contactPerson = "Chị Lụa",
                defaultType = InvoiceType.DELIVERY_DEBT,
                note = "Sổ giao hàng & công nợ"
            ),
            StorePartner(
                id = "store-tien-dung",
                name = "Cửa Hàng Tiến Dũng",
                phone = "",
                address = "39/11 Đông Hưng Thuận 11B",
                contactPerson = "Tiến Dũng",
                defaultType = InvoiceType.DELIVERY_DEBT,
                note = "Sổ giao hàng & công nợ"
            ),
            StorePartner(
                id = "store-hoang-van-thu",
                name = "Cửa Hàng Hoàng Văn Thụ",
                phone = "",
                address = "Giao Thới Tứ / Giao tại cửa hàng",
                contactPerson = "Hoàng Văn Thụ",
                defaultType = InvoiceType.DELIVERY_DEBT,
                note = "Sổ giao hàng & công nợ"
            ),
            StorePartner(
                id = "store-kieu-phat",
                name = "Cửa Hàng Kiều Phát",
                phone = "0914567206",
                address = "Đông Thạnh, Hóc Môn",
                contactPerson = "A Hưng",
                defaultType = InvoiceType.DELIVERY_DEBT,
                note = "Đại lý phân phối bồn & máy NLMT"
            ),
            StorePartner(
                id = "store-trung-tin",
                name = "Cửa Hàng Trung Tín",
                phone = "0981126294",
                address = "Long An",
                contactPerson = "A Bình",
                defaultType = InvoiceType.DELIVERY_DEBT,
                note = "Sổ giao hàng & công nợ - Nợ cũ lũy kế"
            ),
            StorePartner(
                id = "store-long-dai-thanh",
                name = "Công Ty TNHH SX TM XD Long Đại Thành",
                phone = "0312203397",
                address = "19C Đường 116, Ấp 4, Xã Phú Hòa Đông, TP.HCM",
                contactPerson = "Ban Giám Đốc",
                defaultType = InvoiceType.QUOTATION_A4,
                note = "Đơn vị cung cấp dịch vụ kỹ thuật & báo giá A4"
            )
        )
    }


    companion object {
        private const val KEY_STORES = "key_store_partners_default_v2"

        @Volatile
        private var instance: StorePartnerRepository? = null

        fun getInstance(context: Context): StorePartnerRepository {
            return instance ?: synchronized(this) {
                instance ?: StorePartnerRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
