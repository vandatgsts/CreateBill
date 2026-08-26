package com.vandatgsts.thuyetnguyen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vandatgsts.thuyetnguyen.data.model.InvoiceType
import com.vandatgsts.thuyetnguyen.ui.editor.InvoiceEditorScreen
import com.vandatgsts.thuyetnguyen.ui.editor.InvoiceEditorViewModel
import com.vandatgsts.thuyetnguyen.ui.home.HomeScreen
import com.vandatgsts.thuyetnguyen.ui.home.HomeViewModel
import com.vandatgsts.thuyetnguyen.ui.preview.InvoicePreviewScreen
import com.vandatgsts.thuyetnguyen.ui.preview.InvoicePreviewViewModel
import com.vandatgsts.thuyetnguyen.ui.products.ProductListScreen
import com.vandatgsts.thuyetnguyen.ui.products.ProductListViewModel
import com.vandatgsts.thuyetnguyen.ui.settings.CompanyProfileScreen
import com.vandatgsts.thuyetnguyen.ui.settings.CompanyProfileViewModel
import com.vandatgsts.thuyetnguyen.ui.stores.StorePartnerDetailScreen
import com.vandatgsts.thuyetnguyen.ui.stores.StorePartnerListScreen
import com.vandatgsts.thuyetnguyen.ui.stores.StorePartnerViewModel
import com.vandatgsts.thuyetnguyen.ui.theme.PrimaryBlue
import com.vandatgsts.thuyetnguyen.ui.theme.TaoHoaDonTheme
import com.vandatgsts.thuyetnguyen.ui.theme.TextSecondary

sealed class AppScreen {
    data object Home : AppScreen()
    data object Settings : AppScreen()
    data object ProductCatalog : AppScreen()
    data object StorePartnerList : AppScreen()
    data class StorePartnerDetail(val storeId: String) : AppScreen()
    data class Editor(val invoiceId: String?, val initialType: InvoiceType = InvoiceType.QUOTATION_A4) : AppScreen()
    data class Preview(val invoiceId: String) : AppScreen()
}

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val editorViewModel: InvoiceEditorViewModel by viewModels()
    private val previewViewModel: InvoicePreviewViewModel by viewModels()
    private val profileViewModel: CompanyProfileViewModel by viewModels()
    private val productViewModel: ProductListViewModel by viewModels()
    private val storePartnerViewModel: StorePartnerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TaoHoaDonTheme {
                val backStack = remember { mutableStateListOf<AppScreen>(AppScreen.Home) }
                val currentScreen = backStack.lastOrNull() ?: AppScreen.Home

                BackHandler(enabled = backStack.size > 1) {
                    backStack.removeLastOrNull()
                }

                // Check if current screen is one of the 4 main tab screens
                val isMainTabScreen = currentScreen is AppScreen.Home ||
                        currentScreen is AppScreen.StorePartnerList ||
                        currentScreen is AppScreen.ProductCatalog ||
                        currentScreen is AppScreen.Settings

                Scaffold(
                    bottomBar = {
                        if (isMainTabScreen) {
                            NavigationBar(
                                containerColor = Color.White,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen is AppScreen.Home,
                                    onClick = {
                                        if (currentScreen !is AppScreen.Home) {
                                            backStack.clear()
                                            backStack.add(AppScreen.Home)
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Trang Chủ") },
                                    label = { Text("Trang Chủ", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary,
                                        indicatorColor = PrimaryBlue.copy(alpha = 0.12f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentScreen is AppScreen.StorePartnerList,
                                    onClick = {
                                        if (currentScreen !is AppScreen.StorePartnerList) {
                                            backStack.clear()
                                            backStack.add(AppScreen.StorePartnerList)
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Store, contentDescription = "Cửa Hàng") },
                                    label = { Text("Cửa Hàng", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary,
                                        indicatorColor = PrimaryBlue.copy(alpha = 0.12f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentScreen is AppScreen.ProductCatalog,
                                    onClick = {
                                        if (currentScreen !is AppScreen.ProductCatalog) {
                                            backStack.clear()
                                            backStack.add(AppScreen.ProductCatalog)
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Kho Hàng") },
                                    label = { Text("Kho Hàng", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary,
                                        indicatorColor = PrimaryBlue.copy(alpha = 0.12f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentScreen is AppScreen.Settings,
                                    onClick = {
                                        if (currentScreen !is AppScreen.Settings) {
                                            backStack.clear()
                                            backStack.add(AppScreen.Settings)
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Cài Đặt") },
                                    label = { Text("Cài Đặt", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryBlue,
                                        selectedTextColor = PrimaryBlue,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary,
                                        indicatorColor = PrimaryBlue.copy(alpha = 0.12f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (val screen = currentScreen) {
                            is AppScreen.Home -> {
                                HomeScreen(
                                    viewModel = homeViewModel,
                                    onNavigateToCreate = { type ->
                                        editorViewModel.initInvoice(null, type)
                                        backStack.add(AppScreen.Editor(null, type))
                                    },
                                    onNavigateToEdit = { id ->
                                        editorViewModel.initInvoice(id)
                                        backStack.add(AppScreen.Editor(id))
                                    },
                                    onNavigateToPreview = { id ->
                                        backStack.add(AppScreen.Preview(id))
                                    },
                                    onNavigateToStores = {
                                        backStack.add(AppScreen.StorePartnerList)
                                    },
                                    onNavigateToStoreDetail = { storeId ->
                                        backStack.add(AppScreen.StorePartnerDetail(storeId))
                                    }
                                )
                            }

                            is AppScreen.Settings -> {
                                CompanyProfileScreen(
                                    viewModel = profileViewModel,
                                    onBack = {
                                        if (backStack.size > 1) backStack.removeLastOrNull()
                                        else {
                                            backStack.clear()
                                            backStack.add(AppScreen.Home)
                                        }
                                    }
                                )
                            }

                            is AppScreen.ProductCatalog -> {
                                ProductListScreen(
                                    viewModel = productViewModel,
                                    onBack = {
                                        if (backStack.size > 1) backStack.removeLastOrNull()
                                        else {
                                            backStack.clear()
                                            backStack.add(AppScreen.Home)
                                        }
                                    }
                                )
                            }

                            is AppScreen.StorePartnerList -> {
                                StorePartnerListScreen(
                                    viewModel = storePartnerViewModel,
                                    onBack = {
                                        if (backStack.size > 1) backStack.removeLastOrNull()
                                        else {
                                            backStack.clear()
                                            backStack.add(AppScreen.Home)
                                        }
                                    },
                                    onNavigateToDetail = { storeId ->
                                        backStack.add(AppScreen.StorePartnerDetail(storeId))
                                    }
                                )
                            }

                            is AppScreen.StorePartnerDetail -> {
                                StorePartnerDetailScreen(
                                    storeId = screen.storeId,
                                    viewModel = storePartnerViewModel,
                                    onBack = { backStack.removeLastOrNull() },
                                    onCreateInvoiceForStore = { store, rollingDebt ->
                                        editorViewModel.initInvoiceForStorePartner(
                                            store = store,
                                            rollingDebt = rollingDebt
                                        )
                                        backStack.add(AppScreen.Editor(null, store.defaultType))
                                    },
                                    onNavigateToInvoicePreview = { id ->
                                        backStack.add(AppScreen.Preview(id))
                                    },
                                    onNavigateToInvoiceEdit = { id ->
                                        editorViewModel.initInvoice(id)
                                        backStack.add(AppScreen.Editor(id))
                                    }
                                )
                            }

                            is AppScreen.Editor -> {
                                InvoiceEditorScreen(
                                    viewModel = editorViewModel,
                                    onBack = { backStack.removeLastOrNull() },
                                    onNavigateToPreview = { id ->
                                        backStack.removeLastOrNull()
                                        backStack.add(AppScreen.Preview(id))
                                    },
                                    onNavigateToProducts = {
                                        backStack.add(AppScreen.ProductCatalog)
                                    },
                                    onNavigateToStores = {
                                        backStack.add(AppScreen.StorePartnerList)
                                    }
                                )
                            }

                            is AppScreen.Preview -> {
                                InvoicePreviewScreen(
                                    invoiceId = screen.invoiceId,
                                    viewModel = previewViewModel,
                                    onBack = { backStack.removeLastOrNull() },
                                    onEdit = { id ->
                                        editorViewModel.initInvoice(id)
                                        backStack.add(AppScreen.Editor(id))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}