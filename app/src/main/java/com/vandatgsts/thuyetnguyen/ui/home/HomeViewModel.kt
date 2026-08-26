package com.vandatgsts.thuyetnguyen.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vandatgsts.thuyetnguyen.data.model.InvoiceDocument
import com.vandatgsts.thuyetnguyen.data.model.InvoiceType
import com.vandatgsts.thuyetnguyen.data.model.StorePartnerSummary
import com.vandatgsts.thuyetnguyen.data.repository.InvoiceRepository
import com.vandatgsts.thuyetnguyen.data.repository.ProductRepository
import com.vandatgsts.thuyetnguyen.data.repository.StorePartnerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeDashboardStats(
    val totalStores: Int = 0,
    val totalInvoices: Int = 0,
    val totalProducts: Int = 0,
    val totalOutstandingDebt: Double = 0.0,
    val totalRevenue: Double = 0.0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val invoiceRepo = InvoiceRepository.getInstance(application)
    private val storeRepo = StorePartnerRepository.getInstance(application)
    private val productRepo = ProductRepository.getInstance(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filterType = MutableStateFlow<InvoiceType?>(null)
    val filterType: StateFlow<InvoiceType?> = _filterType

    val storeSummaries: StateFlow<List<StorePartnerSummary>> = combine(
        storeRepo.stores,
        invoiceRepo.invoices
    ) { storeList, invoiceList ->
        storeList.map { store ->
            val matchingInvoices = invoiceList.filter { doc ->
                val matchesStoreName = doc.storeOrCompanyName.isNotBlank() &&
                        doc.storeOrCompanyName.contains(store.name, ignoreCase = true)
                val matchesTitle = doc.title.isNotBlank() &&
                        doc.title.contains(store.name, ignoreCase = true)
                matchesStoreName || matchesTitle
            }.sortedByDescending { it.updatedAt }

            val totalAmount = matchingInvoices.sumOf { it.totalAmount }
            val totalPaid = matchingInvoices.sumOf { it.totalPaid }
            val latestInvoice = matchingInvoices.firstOrNull()
            val currentDebt = latestInvoice?.remainingDebt ?: 0.0

            StorePartnerSummary(
                store = store,
                invoiceCount = matchingInvoices.size,
                totalAmount = totalAmount,
                totalPaid = totalPaid,
                currentDebt = currentDebt,
                latestInvoice = latestInvoice,
                invoices = matchingInvoices
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardStats: StateFlow<HomeDashboardStats> = combine(
        storeRepo.stores,
        invoiceRepo.invoices,
        productRepo.products
    ) { stores, invoices, products ->
        val totalRevenue = invoices.sumOf { it.totalAmount }
        val totalDebt = stores.sumOf { store ->
            val matching = invoices.filter { doc ->
                doc.storeOrCompanyName.contains(store.name, ignoreCase = true) ||
                        doc.title.contains(store.name, ignoreCase = true)
            }.sortedByDescending { it.updatedAt }
            matching.firstOrNull()?.remainingDebt ?: 0.0
        }


        HomeDashboardStats(
            totalStores = stores.size,
            totalInvoices = invoices.size,
            totalProducts = products.size,
            totalOutstandingDebt = totalDebt,
            totalRevenue = totalRevenue
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeDashboardStats())

    val filteredInvoices: StateFlow<List<InvoiceDocument>> = combine(
        invoiceRepo.invoices,
        _searchQuery,
        _filterType
    ) { list, query, filter ->
        list.filter { doc ->
            val matchesFilter = (filter == null || doc.type == filter)
            val matchesQuery = query.isBlank() ||
                    doc.title.contains(query, ignoreCase = true) ||
                    doc.storeOrCompanyName.contains(query, ignoreCase = true) ||
                    doc.customer.name.contains(query, ignoreCase = true) ||
                    doc.items.any { it.productName.contains(query, ignoreCase = true) || it.receiver.contains(query, ignoreCase = true) }
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: InvoiceType?) {
        _filterType.value = type
    }

    fun deleteInvoice(id: String) {
        viewModelScope.launch {
            invoiceRepo.deleteInvoice(id)
        }
    }

    fun duplicateInvoice(id: String) {
        viewModelScope.launch {
            invoiceRepo.duplicateInvoice(id)
        }
    }
}
