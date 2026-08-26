package com.vandatgsts.thuyetnguyen.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vandatgsts.thuyetnguyen.data.model.CustomerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class CustomerRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("customer_profiles_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _customers = MutableStateFlow<List<CustomerProfile>>(emptyList())
    val customers: StateFlow<List<CustomerProfile>> = _customers.asStateFlow()

    init {
        loadCustomers()
    }

    private fun loadCustomers() {
        val json = prefs.getString(KEY_CUSTOMERS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<CustomerProfile>>() {}.type
                val list: List<CustomerProfile> = gson.fromJson(json, type) ?: emptyList()
                if (list.isNotEmpty()) {
                    _customers.value = list.sortedBy { it.name }
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val defaultList = createDefaultCustomers()
        _customers.value = defaultList
        saveToPrefs(defaultList)
    }

    private fun saveToPrefs(list: List<CustomerProfile>) {
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_CUSTOMERS, json).apply()
    }

    fun getCustomerById(id: String): CustomerProfile? {
        return _customers.value.find { it.id == id }
    }

    suspend fun saveCustomer(customer: CustomerProfile) = withContext(Dispatchers.IO) {
        val current = _customers.value.toMutableList()
        val index = current.indexOfFirst { it.id == customer.id || (it.name.equals(customer.name.trim(), ignoreCase = true) && it.phone == customer.phone.trim()) }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(
                name = customer.name.trim(),
                phone = if (customer.phone.isNotBlank()) customer.phone.trim() else existing.phone,
                address = if (customer.address.isNotBlank()) customer.address.trim() else existing.address,
                taxCode = if (customer.taxCode.isNotBlank()) customer.taxCode.trim() else existing.taxCode,
                note = if (customer.note.isNotBlank()) customer.note.trim() else existing.note
            )
        } else {
            current.add(customer.copy(name = customer.name.trim(), phone = customer.phone.trim()))
        }
        val sorted = current.sortedBy { it.name }
        _customers.value = sorted
        saveToPrefs(sorted)
    }

    suspend fun deleteCustomer(id: String) = withContext(Dispatchers.IO) {
        val current = _customers.value.filterNot { it.id == id }
        _customers.value = current
        saveToPrefs(current)
    }

    suspend fun replaceAll(newList: List<CustomerProfile>) = withContext(Dispatchers.IO) {
        val sorted = newList.sortedBy { it.name }
        _customers.value = sorted
        saveToPrefs(sorted)
    }

    suspend fun mergeAll(incomingList: List<CustomerProfile>) = withContext(Dispatchers.IO) {
        val current = _customers.value.toMutableList()
        for (incoming in incomingList) {
            val index = current.indexOfFirst { it.id == incoming.id || (it.name.equals(incoming.name.trim(), ignoreCase = true) && it.phone == incoming.phone) }
            if (index >= 0) {
                current[index] = incoming
            } else {
                current.add(incoming)
            }
        }
        val sorted = current.sortedBy { it.name }
        _customers.value = sorted
        saveToPrefs(sorted)
    }

    private fun createDefaultCustomers(): List<CustomerProfile> {
        return listOf(
            CustomerProfile(
                id = "c-1",
                name = "Công Ty MDK",
                phone = "0901234567",
                address = "số 9 tô ký , Trung Mỹ Tây , quận 12 , Thành phố Hồ Chí Minh",
                taxCode = "0314567890",
                note = "Khách hàng dịch vụ vệ sinh hàn bồn"
            ),
            CustomerProfile(
                id = "c-2",
                name = "A Hưng",
                phone = "0914567206",
                address = "Đông Thạnh",
                note = "Khách mua máy NLMT Bình Minh & bình phụ"
            ),
            CustomerProfile(
                id = "c-3",
                name = "Chị Oanh",
                phone = "0984674687",
                address = "Xuân Thới Sơn",
                note = "Khách mua bồn 500 nằm TP"
            )
        )
    }

    companion object {
        private const val KEY_CUSTOMERS = "key_customers_list"

        @Volatile
        private var instance: CustomerRepository? = null

        fun getInstance(context: Context): CustomerRepository {
            return instance ?: synchronized(this) {
                instance ?: CustomerRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
