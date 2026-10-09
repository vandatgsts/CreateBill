package com.vandatgsts.thuyetnguyen.ui.home

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vandatgsts.thuyetnguyen.data.model.InvoiceDocument
import com.vandatgsts.thuyetnguyen.data.model.InvoiceType
import com.vandatgsts.thuyetnguyen.data.model.StorePartnerSummary
import com.vandatgsts.thuyetnguyen.data.repository.InvoiceRepository
import com.vandatgsts.thuyetnguyen.data.repository.ProductRepository
import com.vandatgsts.thuyetnguyen.data.repository.StorePartnerRepository
import com.vandatgsts.thuyetnguyen.generator.FormatHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.util.Calendar

enum class DateFilterPeriod(val label: String) {
    ALL("Tất cả"),
    TODAY("Hôm nay"),
    THIS_WEEK("Tuần này"),
    THIS_MONTH("Tháng này"),
    LAST_MONTH("Tháng trước"),
    CUSTOM("Tùy chọn ngày")
}

data class HomeDashboardStats(
    val totalStores: Int = 0,
    val totalInvoices: Int = 0,
    val totalProducts: Int = 0,
    val totalOutstandingDebt: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val totalPaid: Double = 0.0,
    val periodLabel: String = "TỔNG QUAN TÀI CHÍNH"
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val invoiceRepo = InvoiceRepository.getInstance(application)
    private val storeRepo = StorePartnerRepository.getInstance(application)
    private val productRepo = ProductRepository.getInstance(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filterType = MutableStateFlow<InvoiceType?>(null)
    val filterType: StateFlow<InvoiceType?> = _filterType

    private val _datePeriod = MutableStateFlow(DateFilterPeriod.ALL)
    val datePeriod: StateFlow<DateFilterPeriod> = _datePeriod

    private val _customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val customDateRange: StateFlow<Pair<Long, Long>?> = _customDateRange

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
        productRepo.products,
        _datePeriod,
        _customDateRange
    ) { stores, invoices, products, period, customRange ->
        val range = getPeriodTimestampRange(period, customRange)
        val periodInvoices = if (range == null) {
            invoices
        } else {
            invoices.filter { it.updatedAt in range.first..range.second || it.createdAt in range.first..range.second }
        }

        val totalRevenue = periodInvoices.sumOf { it.totalAmount }
        val totalPaid = periodInvoices.sumOf { it.totalPaid }
        val totalDebt = periodInvoices.sumOf { it.remainingDebt }

        val periodLabel = when (period) {
            DateFilterPeriod.ALL -> "TỔNG QUAN TÀI CHÍNH"
            DateFilterPeriod.TODAY -> "TỔNG QUAN HÔM NAY"
            DateFilterPeriod.THIS_WEEK -> "TỔNG QUAN TUẦN NÀY"
            DateFilterPeriod.THIS_MONTH -> "TỔNG QUAN THÁNG ${Calendar.getInstance().get(Calendar.MONTH) + 1}"
            DateFilterPeriod.LAST_MONTH -> "TỔNG QUAN THÁNG TRƯỚC"
            DateFilterPeriod.CUSTOM -> {
                if (customRange != null) {
                    "TỔNG QUAN (${FormatHelper.formatDate(customRange.first)} - ${FormatHelper.formatDate(customRange.second)})"
                } else "TỔNG QUAN TÙY CHỌN NGÀY"
            }
        }

        HomeDashboardStats(
            totalStores = stores.size,
            totalInvoices = periodInvoices.size,
            totalProducts = products.size,
            totalOutstandingDebt = totalDebt,
            totalRevenue = totalRevenue,
            totalPaid = totalPaid,
            periodLabel = periodLabel
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeDashboardStats())

    val filteredInvoices: StateFlow<List<InvoiceDocument>> = combine(
        invoiceRepo.invoices,
        _searchQuery,
        _filterType,
        _datePeriod,
        _customDateRange
    ) { list, query, filter, period, customRange ->
        val range = getPeriodTimestampRange(period, customRange)
        list.filter { doc ->
            val matchesFilter = (filter == null || doc.type == filter)
            val matchesPeriod = if (range == null) true else (doc.updatedAt in range.first..range.second || doc.createdAt in range.first..range.second)
            val matchesQuery = query.isBlank() ||
                    doc.title.contains(query, ignoreCase = true) ||
                    doc.storeOrCompanyName.contains(query, ignoreCase = true) ||
                    doc.customer.name.contains(query, ignoreCase = true) ||
                    doc.items.any { it.productName.contains(query, ignoreCase = true) || it.receiver.contains(query, ignoreCase = true) }
            matchesFilter && matchesPeriod && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: InvoiceType?) {
        _filterType.value = type
    }

    fun setDatePeriod(period: DateFilterPeriod) {
        _datePeriod.value = period
        if (period != DateFilterPeriod.CUSTOM) {
            _customDateRange.value = null
        }
    }

    fun setCustomDateRange(start: Long, end: Long) {
        _customDateRange.value = Pair(start, end)
        _datePeriod.value = DateFilterPeriod.CUSTOM
    }

    fun deleteInvoice(id: String) {
        viewModelScope.launch {
            try {
                invoiceRepo.deleteInvoice(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(getApplication<Application>(), "Không thể xóa hóa đơn. Vui lòng thử lại.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun duplicateInvoice(id: String) {
        viewModelScope.launch {
            try {
                invoiceRepo.duplicateInvoice(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(getApplication<Application>(), "Không thể nhân bản hóa đơn. Vui lòng kiểm tra dung lượng và thử lại.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getPeriodTimestampRange(period: DateFilterPeriod, customRange: Pair<Long, Long>?): Pair<Long, Long>? {
        return when (period) {
            DateFilterPeriod.ALL -> null
            DateFilterPeriod.TODAY -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            DateFilterPeriod.THIS_WEEK -> {
                val cal = Calendar.getInstance()
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            DateFilterPeriod.THIS_MONTH -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            DateFilterPeriod.LAST_MONTH -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            DateFilterPeriod.CUSTOM -> customRange
        }
    }
}
