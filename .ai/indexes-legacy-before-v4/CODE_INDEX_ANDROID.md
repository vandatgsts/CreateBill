# Code Index - Android Native (TaoHoaDon)

## Project Overview
- **Package**: `com.vandatgsts.thuyetnguyen`
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: MVVM + StateFlow + Kotlin Coroutines
- **Export Engine**: Android Native `PdfDocument` & Canvas `Bitmap` / PNG Renderer

## Architecture Tree

### 1. Data Layer (`com.vandatgsts.thuyetnguyen.data`)
- **Models** (`data/model/`):
  - [InvoiceType.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/model/InvoiceType.kt): Enum representing `DELIVERY_DEBT` (Mẫu 1) and `QUOTATION_A4` (Mẫu 2).
  - [CompanyProfile.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/model/CompanyProfile.kt): Model for default store/company profile & bank account settings.
  - [CustomerInfo.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/model/CustomerInfo.kt): Customer details (Name, Address, Phone, Tax code).
  - [InvoiceItem.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/model/InvoiceItem.kt): Line items with reactive line total calculations (`lineTotalM1`, `lineTotalM2`).
  - [InvoiceDocument.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/model/InvoiceDocument.kt): Aggregated invoice entity with grand total, items, debt, and terms.
  - [ProductTemplate.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/model/ProductTemplate.kt): Product catalog item with default name, unit, and unit price.
  - [CustomerProfile.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/model/CustomerProfile.kt): Customer profile entity (Name, Phone, Address, Tax code, Notes).
  - [CustomerSummary.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/model/CustomerSummary.kt): Aggregated financial overview per customer (Invoices, Total spent, Total paid, Total debt).
  - [StorePartner.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/model/StorePartner.kt): Store / Dealer partner profile (Store name, Phone, Address, Contact person, Default template type).
  - [StorePartnerSummary.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/model/StorePartnerSummary.kt): Multi-period invoice summary and rolling debt balance for a store.
  - [AppBackupData.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/model/AppBackupData.kt): Model for JSON backup data and `ImportMode` (Merge vs Replace All).
- **Repositories** (`data/repository/`):
  - [CompanyProfileRepository.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/repository/CompanyProfileRepository.kt): Persistent store for default company settings using `SharedPreferences` + `Gson`.
  - [InvoiceRepository.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/repository/InvoiceRepository.kt): Persistent CRUD storage for invoice list with sample data matching user's templates.
  - [ProductRepository.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/repository/ProductRepository.kt): Persistent catalog storage for reusable product/service templates.
  - [CustomerRepository.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/repository/CustomerRepository.kt): Persistent storage and directory for customer profiles.
  - [StorePartnerRepository.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/repository/StorePartnerRepository.kt): Persistent storage for Store/Dealer partner profiles with rolling debt tracking.
  - [BackupRepository.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/data/repository/BackupRepository.kt): Full data export, SHA-256 hash calculation, duplicate prevention, and JSON restore engine.

### 2. Generator & Export Engine (`com.vandatgsts.thuyetnguyen.generator`)
- [FormatHelper.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/generator/FormatHelper.kt): Currency, number, and date formatters.
- [InvoiceCanvasDrawer.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/generator/InvoiceCanvasDrawer.kt): Core canvas drawing engine for both Landscape (Mẫu 1) and Portrait A4 (Mẫu 2).
- [PdfInvoiceRenderer.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/generator/PdfInvoiceRenderer.kt): Android `PdfDocument` page renderer.
- [ImageInvoiceRenderer.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/generator/ImageInvoiceRenderer.kt): High-resolution `Bitmap` & PNG exporter.
- [InvoiceExportManager.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/generator/InvoiceExportManager.kt): Intent sharing (Zalo, Facebook, Email), PDF viewer, and Gallery storage.

### 3. UI Layer (`com.vandatgsts.thuyetnguyen.ui`)
- **Theme** (`ui/theme/`): [Color.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/theme/Color.kt), [Type.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/theme/Type.kt), [Theme.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/theme/Theme.kt).
- **Components** (`ui/components/`): [CommonComponents.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/components/CommonComponents.kt) (`AppTextField`, `CurrencyField`, `NumberField`, `FormSectionHeader`).
- **Home Screen** (`ui/home/`): [HomeScreen.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/home/HomeScreen.kt), [HomeViewModel.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/home/HomeViewModel.kt).
- **Product Catalog Screen** (`ui/products/`): [ProductListScreen.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/products/ProductListScreen.kt), [ProductListViewModel.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/products/ProductListViewModel.kt).
- **Store / Dealer Partners Screen** (`ui/stores/`): [StorePartnerListScreen.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/stores/StorePartnerListScreen.kt), [StorePartnerDetailScreen.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/stores/StorePartnerDetailScreen.kt), [StorePartnerViewModel.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/stores/StorePartnerViewModel.kt).
- **Invoice Editor** (`ui/editor/`): [InvoiceEditorScreen.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/editor/InvoiceEditorScreen.kt), [InvoiceEditorViewModel.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/editor/InvoiceEditorViewModel.kt).
- **Invoice Preview & Export** (`ui/preview/`): [InvoicePreviewScreen.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/preview/InvoicePreviewScreen.kt), [InvoicePreviewViewModel.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/preview/InvoicePreviewViewModel.kt).
- **Settings Screen** (`ui/settings/`): [CompanyProfileScreen.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/settings/CompanyProfileScreen.kt), [CompanyProfileViewModel.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/ui/settings/CompanyProfileViewModel.kt).
- **Entry Point**: [MainActivity.kt](file:///E:/test/TaoHoaDon/app/src/main/java/com/vandatgsts/thuyetnguyen/MainActivity.kt).




