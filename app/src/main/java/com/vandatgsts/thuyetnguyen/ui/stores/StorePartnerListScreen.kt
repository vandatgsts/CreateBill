package com.vandatgsts.thuyetnguyen.ui.stores

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vandatgsts.thuyetnguyen.data.model.InvoiceType
import com.vandatgsts.thuyetnguyen.data.model.StorePartner
import com.vandatgsts.thuyetnguyen.data.model.StorePartnerSummary
import com.vandatgsts.thuyetnguyen.generator.FormatHelper
import com.vandatgsts.thuyetnguyen.ui.components.AppTextField
import com.vandatgsts.thuyetnguyen.ui.theme.AccentGreen
import com.vandatgsts.thuyetnguyen.ui.theme.BackgroundLight
import com.vandatgsts.thuyetnguyen.ui.theme.BorderColor
import com.vandatgsts.thuyetnguyen.ui.theme.DeleteRed
import com.vandatgsts.thuyetnguyen.ui.theme.PrimaryBlue
import com.vandatgsts.thuyetnguyen.ui.theme.PrimaryBlueDark
import com.vandatgsts.thuyetnguyen.ui.theme.TextPrimary
import com.vandatgsts.thuyetnguyen.ui.theme.TextSecondary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorePartnerListScreen(
    viewModel: StorePartnerViewModel,
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onSelectStore: ((StorePartner) -> Unit)? = null
) {
    val summaries by viewModel.filteredSummaries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    var editingStore by remember { mutableStateOf<StorePartner?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var storeToDelete by remember { mutableStateOf<StorePartner?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (onSelectStore != null) "Chọn Cửa Hàng / Đại Lý" else "Cửa Hàng / Đại Lý Đối Tác",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingStore = StorePartner(id = UUID.randomUUID().toString())
                    showDialog = true
                },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm mới")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Thêm Cửa Hàng", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
        ) {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Tìm theo tên Cửa Hàng, SĐT, địa chỉ, người liên hệ...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (summaries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Chưa có Cửa Hàng / Đại Lý nào", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Bấm 'Thêm Cửa Hàng' để lưu và theo dõi các kỳ sổ giao hàng & công nợ", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(summaries, key = { it.store.id }) { summary ->
                        StoreCard(
                            summary = summary,
                            onCardClick = {
                                if (onSelectStore != null) {
                                    onSelectStore(summary.store)
                                } else {
                                    onNavigateToDetail(summary.store.id)
                                }
                            },
                            onEdit = {
                                editingStore = summary.store
                                showDialog = true
                            },
                            onDelete = {
                                storeToDelete = summary.store
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showDialog && editingStore != null) {
        var tempName by remember { mutableStateOf(editingStore!!.name) }
        var tempPhone by remember { mutableStateOf(editingStore!!.phone) }
        var tempAddress by remember { mutableStateOf(editingStore!!.address) }
        var tempContact by remember { mutableStateOf(editingStore!!.contactPerson) }
        var tempType by remember { mutableStateOf(editingStore!!.defaultType) }
        var tempNote by remember { mutableStateOf(editingStore!!.note) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    if (editingStore!!.name.isBlank()) "Thêm Cửa Hàng / Đại Lý Mới" else "Chỉnh Sửa Cửa Hàng",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AppTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = "Tên Cửa Hàng / Đại Lý *",
                        placeholder = "Ví dụ: CỬA HÀNG KIỀU PHÁT"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = tempType == InvoiceType.DELIVERY_DEBT,
                            onClick = { tempType = InvoiceType.DELIVERY_DEBT },
                            label = { Text("Mẫu 1: Giao hàng", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryBlue, selectedLabelColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = tempType == InvoiceType.QUOTATION_A4,
                            onClick = { tempType = InvoiceType.QUOTATION_A4 },
                            label = { Text("Mẫu 2: Báo giá A4", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryBlue, selectedLabelColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AppTextField(
                        value = tempPhone,
                        onValueChange = { tempPhone = it },
                        label = "Số Điện Thoại",
                        placeholder = "0914567206"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AppTextField(
                        value = tempAddress,
                        onValueChange = { tempAddress = it },
                        label = "Địa Chỉ Cửa Hàng",
                        placeholder = "Đông Thạnh, Hóc Môn..."
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AppTextField(
                        value = tempContact,
                        onValueChange = { tempContact = it },
                        label = "Người Liên Hệ / Phụ Trách",
                        placeholder = "A Hưng..."
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AppTextField(
                        value = tempNote,
                        onValueChange = { tempNote = it },
                        label = "Ghi Chú",
                        placeholder = "Quy cách giao hàng, chu kỳ thanh toán...",
                        singleLine = false,
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.trim().isEmpty()) {
                            Toast.makeText(context, "Vui lòng nhập tên Cửa Hàng!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val updated = editingStore!!.copy(
                            name = tempName.trim(),
                            phone = tempPhone.trim(),
                            address = tempAddress.trim(),
                            contactPerson = tempContact.trim(),
                            defaultType = tempType,
                            note = tempNote.trim()
                        )
                        viewModel.saveStore(updated)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Lưu Cửa Hàng", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Delete confirmation
    if (storeToDelete != null) {
        AlertDialog(
            onDismissRequest = { storeToDelete = null },
            title = { Text("Xác nhận xóa Cửa Hàng", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa Cửa Hàng '${storeToDelete?.name}' khỏi danh sách?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        storeToDelete?.let { viewModel.deleteStore(it.id) }
                        storeToDelete = null
                    }
                ) {
                    Text("Xóa", color = DeleteRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { storeToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun StoreCard(
    summary: StorePartnerSummary,
    onCardClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val store = summary.store

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = store.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "${summary.invoiceCount} Kỳ Hóa Đơn",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (store.contactPerson.isNotBlank() || store.phone.isNotBlank()) {
                val contactText = buildString {
                    if (store.contactPerson.isNotBlank()) append("Liên hệ: ${store.contactPerson}")
                    if (store.phone.isNotBlank()) {
                        if (isNotEmpty()) append(" - ")
                        append("SĐT: ${store.phone}")
                    }
                }
                Text(contactText, fontSize = 13.sp, color = TextSecondary)
            }
            if (store.address.isNotBlank()) {
                Text("Địa chỉ: ${store.address}", fontSize = 12.sp, color = TextSecondary, maxLines = 1)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Dư nợ kỳ gần nhất:", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        "${FormatHelper.formatMoney(summary.currentDebt)} đ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (summary.currentDebt > 0) DeleteRed else AccentGreen
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = PrimaryBlueDark)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = DeleteRed)
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Chi tiết",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
