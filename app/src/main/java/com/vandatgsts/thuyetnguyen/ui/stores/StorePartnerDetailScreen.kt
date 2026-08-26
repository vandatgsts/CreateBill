package com.vandatgsts.thuyetnguyen.ui.stores

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vandatgsts.thuyetnguyen.data.model.InvoiceDocument
import com.vandatgsts.thuyetnguyen.data.model.InvoiceType
import com.vandatgsts.thuyetnguyen.data.model.StorePartner
import com.vandatgsts.thuyetnguyen.data.model.StorePartnerSummary
import com.vandatgsts.thuyetnguyen.generator.FormatHelper
import com.vandatgsts.thuyetnguyen.ui.theme.AccentGreen
import com.vandatgsts.thuyetnguyen.ui.theme.BackgroundLight
import com.vandatgsts.thuyetnguyen.ui.theme.DeleteRed
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.vandatgsts.thuyetnguyen.ui.components.AppTextField
import com.vandatgsts.thuyetnguyen.ui.components.CurrencyField
import com.vandatgsts.thuyetnguyen.ui.theme.PrimaryBlue
import com.vandatgsts.thuyetnguyen.ui.theme.PrimaryBlueDark
import com.vandatgsts.thuyetnguyen.ui.theme.TextPrimary
import com.vandatgsts.thuyetnguyen.ui.theme.TextSecondary



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorePartnerDetailScreen(
    storeId: String,
    viewModel: StorePartnerViewModel,
    onBack: () -> Unit,
    onCreateInvoiceForStore: (StorePartner, Double) -> Unit,
    onNavigateToInvoicePreview: (String) -> Unit,
    onNavigateToInvoiceEdit: (String) -> Unit
) {
    val summaries by viewModel.storeSummaries.collectAsState()
    val summary = summaries.find { it.store.id == storeId }

    if (summary == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy thông tin Cửa Hàng / Đại Lý")
        }
        return
    }

    val store = summary.store
    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentAmount by remember { mutableStateOf(0.0) }
    var paymentTitle by remember { mutableStateOf("Ck thanh toán") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(store.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (summary.currentDebt > 0) {
                        OutlinedButton(
                            onClick = {
                                paymentAmount = summary.currentDebt
                                paymentTitle = "Ck thanh toán"
                                showPaymentDialog = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGreen),
                            border = BorderStroke(1.dp, AccentGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = AccentGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GHI NHẬN CHUYỂN KHOẢN / THU NỢ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = { onCreateInvoiceForStore(store, summary.currentDebt) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TẠO KỲ HÓA ĐƠN MỚI (KẾ THỪA NỢ CŨ)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Thông tin Cửa Hàng
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(store.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Surface(
                                color = if (store.defaultType == InvoiceType.DELIVERY_DEBT) Color(0xFFE0F2FE) else Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (store.defaultType == InvoiceType.DELIVERY_DEBT) "Mẫu 1: Giao hàng & Nợ" else "Mẫu 2: Báo giá A4",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (store.defaultType == InvoiceType.DELIVERY_DEBT) PrimaryBlueDark else AccentGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (store.contactPerson.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Người liên hệ: ${store.contactPerson}", fontSize = 13.sp, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (store.phone.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SĐT: ${store.phone}", fontSize = 13.sp, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (store.address.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Địa chỉ: ${store.address}", fontSize = 13.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (store.note.isNotBlank()) {
                            Text("Ghi chú: ${store.note}", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // 2. Thống kê Tài Chính & Công Nợ Kỳ Này
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TỔNG QUAN CÔNG NỢ & CÁC KỲ GIAO HÀNG", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricBox(
                                title = "Số Kỳ Hóa Đơn",
                                value = "${summary.invoiceCount}",
                                color = PrimaryBlue,
                                modifier = Modifier.weight(1f)
                            )
                            MetricBox(
                                title = "Dư Nợ Kỳ Gần Nhất",
                                value = FormatHelper.formatMoney(summary.currentDebt),
                                color = if (summary.currentDebt > 0) DeleteRed else AccentGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricBox(
                                title = "Tổng Tiền Phát Sinh",
                                value = FormatHelper.formatMoney(summary.totalAmount),
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            MetricBox(
                                title = "Đã Thanh Toán",
                                value = FormatHelper.formatMoney(summary.totalPaid),
                                color = AccentGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. Tiêu đề danh sách hóa đơn
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "DANH SÁCH CÁC KỲ HÓA ĐƠN (${summary.invoices.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }

            if (summary.invoices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Chưa có kỳ hóa đơn nào cho Cửa Hàng này", fontSize = 14.sp, color = TextSecondary)
                        }
                    }
                }
            } else {
                items(summary.invoices, key = { it.id }) { invoice ->
                    InvoicePeriodCard(
                        invoice = invoice,
                        onPreview = { onNavigateToInvoicePreview(invoice.id) },
                        onEdit = { onNavigateToInvoiceEdit(invoice.id) }
                    )
                }
            }
        }
    }

    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Ghi Nhận Chuyển Khoản / Thu Nợ", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Nhập số tiền Cửa Hàng vừa chuyển khoản / thanh toán để trừ vào kỳ nợ gần nhất:", fontSize = 13.sp, color = TextSecondary)
                    AppTextField(
                        value = paymentTitle,
                        onValueChange = { paymentTitle = it },
                        label = "Nội dung (ví dụ: Ck lần 1, Ck ngày 25/7...)"
                    )
                    CurrencyField(
                        value = paymentAmount,
                        onValueChange = { paymentAmount = it },
                        label = "Số tiền thanh toán (đ)"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (paymentAmount > 0) {
                            viewModel.recordPaymentForStore(store.id, paymentAmount, paymentTitle)
                        }
                        showPaymentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Text("Ghi nhận", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Hủy", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun InvoicePeriodCard(
    invoice: InvoiceDocument,
    onPreview: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPreview)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.title.ifBlank { "Sổ Giao Hàng & Công Nợ" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = if (invoice.isFullyPaid) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (invoice.isFullyPaid) "✓ Đã thanh toán" else "Còn nợ: ${FormatHelper.formatMoney(invoice.totalAmount)} đ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (invoice.isFullyPaid) AccentGreen else DeleteRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = if (invoice.type == InvoiceType.DELIVERY_DEBT) Color(0xFFE0F2FE) else Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (invoice.type == InvoiceType.DELIVERY_DEBT) "Mẫu 1" else "Mẫu 2",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (invoice.type == InvoiceType.DELIVERY_DEBT) PrimaryBlueDark else AccentGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Ngày lập: ${FormatHelper.formatDate(invoice.updatedAt)} | ${invoice.items.size} chuyến giao | Nợ cũ: ${FormatHelper.formatMoney(invoice.effectiveOldDebt)} đ",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

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
                        fontSize = 15.sp,
                        color = if (invoice.isFullyPaid) AccentGreen else DeleteRed
                    )
                }

                Row {
                    IconButton(onClick = onPreview) {
                        Icon(Icons.Default.Preview, contentDescription = "Xem", tint = PrimaryBlue)
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = PrimaryBlueDark)
                    }
                }
            }
        }
    }
}
