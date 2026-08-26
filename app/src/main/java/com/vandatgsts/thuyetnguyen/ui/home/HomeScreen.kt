package com.vandatgsts.thuyetnguyen.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vandatgsts.thuyetnguyen.data.model.InvoiceDocument
import com.vandatgsts.thuyetnguyen.data.model.InvoiceType
import com.vandatgsts.thuyetnguyen.data.model.StorePartnerSummary
import com.vandatgsts.thuyetnguyen.generator.FormatHelper
import com.vandatgsts.thuyetnguyen.ui.theme.AccentGreen
import com.vandatgsts.thuyetnguyen.ui.theme.BackgroundLight
import com.vandatgsts.thuyetnguyen.ui.theme.BorderColor
import com.vandatgsts.thuyetnguyen.ui.theme.DeleteRed
import com.vandatgsts.thuyetnguyen.ui.theme.PrimaryBlue
import com.vandatgsts.thuyetnguyen.ui.theme.PrimaryBlueDark
import com.vandatgsts.thuyetnguyen.ui.theme.TextPrimary
import com.vandatgsts.thuyetnguyen.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCreate: (InvoiceType) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToPreview: (String) -> Unit,
    onNavigateToStores: () -> Unit,
    onNavigateToStoreDetail: (String) -> Unit
) {
    val invoices by viewModel.filteredInvoices.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val dashboardStats by viewModel.dashboardStats.collectAsState()
    val storeSummaries by viewModel.storeSummaries.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var invoiceToDelete by remember { mutableStateOf<InvoiceDocument?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tạo Hóa Đơn & Báo Giá", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Quản lý theo Cửa Hàng & Sổ Giao Hàng", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tạo mới")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tạo Hóa Đơn", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. DASHBOARD BANNER TỔNG QUAN TÀI CHÍNH
            item {
                DashboardOverviewBanner(stats = dashboardStats)
            }

            // 2. PHÂN CẤP: CỬA HÀNG / ĐẠI LÝ ĐỐI TÁC
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "CỬA HÀNG / ĐẠI LÝ (${storeSummaries.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryBlueDark
                        )
                        TextButton(onClick = onNavigateToStores) {
                            Text("Xem tất cả", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(11.dp), tint = PrimaryBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (storeSummaries.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Chưa có Cửa Hàng nào. Hãy bấm 'Xem tất cả' để thêm Cửa Hàng đối tác.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            storeSummaries.take(5).forEach { summary ->
                                StoreQuickCard(
                                    summary = summary,
                                    onClick = { onNavigateToStoreDetail(summary.store.id) }
                                )
                            }
                        }
                    }
                }
            }

            // 3. TÌM KIẾM VÀ BỘ LỌC HÓA ĐƠN
            item {
                Surface(
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Tìm theo Cửa Hàng, Khách hàng, Sản phẩm, SĐT...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = filterType == null,
                                onClick = { viewModel.setFilterType(null) },
                                label = { Text("Tất cả (${invoices.size})", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = filterType == InvoiceType.DELIVERY_DEBT,
                                onClick = { viewModel.setFilterType(if (filterType == InvoiceType.DELIVERY_DEBT) null else InvoiceType.DELIVERY_DEBT) },
                                label = { Text("Mẫu 1: Giao hàng", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = filterType == InvoiceType.QUOTATION_A4,
                                onClick = { viewModel.setFilterType(if (filterType == InvoiceType.QUOTATION_A4) null else InvoiceType.QUOTATION_A4) },
                                label = { Text("Mẫu 2: Báo giá A4", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // 4. DANH SÁCH HÓA ĐƠN GẦN ĐÂY
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "HÓA ĐƠN & BẢNG KÊ GẦN ĐÂY (${invoices.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }

            if (invoices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Chưa có hóa đơn nào", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Bấm 'Tạo Hóa Đơn' ở góc dưới để bắt đầu", fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }
            } else {
                items(invoices, key = { it.id }) { invoice ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        InvoiceItemCard(
                            invoice = invoice,
                            onPreview = { onNavigateToPreview(invoice.id) },
                            onEdit = { onNavigateToEdit(invoice.id) },
                            onDuplicate = { viewModel.duplicateInvoice(invoice.id) },
                            onDelete = { invoiceToDelete = invoice }
                        )
                    }
                }
            }
        }
    }

    // Modal chọn loại mẫu khi bấm tạo mới
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Chọn Mẫu Hóa Đơn Cần Tạo", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TemplateSelectionCard(
                        title = "Mẫu 1: Sổ Giao Hàng & Công Nợ",
                        subtitle = "Khổ ngang • STT, Ngày, Sản phẩm, SL, Giá, Địa chỉ, Người nhận, SĐT, Đã thu, Nợ cũ",
                        color = PrimaryBlue,
                        onClick = {
                            showCreateDialog = false
                            onNavigateToCreate(InvoiceType.DELIVERY_DEBT)
                        }
                    )

                    TemplateSelectionCard(
                        title = "Mẫu 2: Bảng Báo Giá A4",
                        subtitle = "Khổ dọc A4 • Tên công ty, MST, Bảng STT / Dịch vụ / ĐVT / SL / Đơn giá / Thành tiền, Ngân hàng",
                        color = AccentGreen,
                        onClick = {
                            showCreateDialog = false
                            onNavigateToCreate(InvoiceType.QUOTATION_A4)
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    // Modal xác nhận xóa
    if (invoiceToDelete != null) {
        AlertDialog(
            onDismissRequest = { invoiceToDelete = null },
            title = { Text("Xác nhận xóa hóa đơn", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa hóa đơn '${invoiceToDelete?.title}' không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        invoiceToDelete?.let { viewModel.deleteInvoice(it.id) }
                        invoiceToDelete = null
                    }
                ) {
                    Text("Xóa", color = DeleteRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun DashboardOverviewBanner(stats: HomeDashboardStats) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryBlueDark, PrimaryBlue, Color(0xFF007799))
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TỔNG QUAN TÀI CHÍNH & ĐƠN HÀNG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "${stats.totalStores} Cửa Hàng",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Dư nợ cần thu", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${FormatHelper.formatMoney(stats.totalOutstandingDebt)} đ",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD166),
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Tổng doanh số", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${FormatHelper.formatMoney(stats.totalRevenue)} đ",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tổng số hóa đơn đã tạo: ${stats.totalInvoices}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    Text("Kho hàng: ${stats.totalProducts} mặt hàng", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                }
            }
        }
    }
}

@Composable
private fun StoreQuickCard(
    summary: StorePartnerSummary,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Store, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                Surface(
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "${summary.invoiceCount} Kỳ Đơn",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = summary.store.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text("Dư nợ kỳ gần nhất:", fontSize = 10.sp, color = TextSecondary)
            Text(
                "${FormatHelper.formatMoney(summary.currentDebt)} đ",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (summary.currentDebt > 0) DeleteRed else AccentGreen,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun InvoiceItemCard(
    invoice: InvoiceDocument,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPreview)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.title.ifBlank { "Hóa đơn" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = if (invoice.isFullyPaid) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (invoice.isFullyPaid) "✓ Đã thu đủ" else "Còn nợ: ${FormatHelper.formatMoney(invoice.totalAmount)} đ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (invoice.isFullyPaid) AccentGreen else DeleteRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        color = if (invoice.type == InvoiceType.DELIVERY_DEBT) Color(0xFFE0F2FE) else Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (invoice.type == InvoiceType.DELIVERY_DEBT) "Mẫu 1" else "Mẫu 2",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (invoice.type == InvoiceType.DELIVERY_DEBT) PrimaryBlueDark else AccentGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val subtitle = buildString {
                append("Ngày lập: ${FormatHelper.formatDate(invoice.updatedAt)}")
                append(" • ${invoice.items.size} chuyến giao")
                if (invoice.type == InvoiceType.DELIVERY_DEBT && invoice.effectiveOldDebt > 0) {
                    append(" • Nợ cũ: ${FormatHelper.formatMoney(invoice.effectiveOldDebt)} đ")
                }
            }
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tổng dư nợ kỳ này:", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        "${FormatHelper.formatMoney(invoice.totalAmount)} đ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (invoice.isFullyPaid) AccentGreen else DeleteRed
                    )
                }


                Row {
                    IconButton(onClick = onPreview) {
                        Icon(Icons.Default.Preview, contentDescription = "Xem trước", tint = PrimaryBlue)
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa", tint = PrimaryBlueDark)
                    }
                    IconButton(onClick = onDuplicate) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Nhân bản", tint = TextSecondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = DeleteRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateSelectionCard(
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
    }
}
