# Chỉ mục Android — TaoHoaDon

## Kiến trúc và phạm vi

- Android Kotlin thuần, module `app`, package `com.vandatgsts.thuyetnguyen`.
- Jetpack Compose / Material 3; ViewModel + StateFlow + coroutine.
- Danh mục và cấu hình dùng SharedPreferences + Gson; hóa đơn dùng JSON trong bộ nhớ riêng của ứng dụng, ghi bằng AtomicFile; không dùng Room.
- Chỉ mục bao phủ mã sản phẩm Kotlin/Java trong `app/src/main`; loại trừ test, build và mã sinh tự động.
- Không có Flutter hoặc MethodChannel trong mã sản phẩm hiện tại.
- Mỗi file có shard kiến trúc và các shard symbol riêng. Manifest V4 chỉ giữ cấu hình, layout và thống kê.

## Tra cứu

Entry points: `.ai/indexes/codeindex_android.json` và `.ai/indexes/symbols/android_symbols.json`.

```powershell
python C:/Users/Chand/.codex/skills/android-kotlin-code-index/scripts/index_android_v4.py . lookup --qualified-name com.vandatgsts.thuyetnguyen.ui.editor.InvoiceEditorViewModel.saveInvoice --record
python C:/Users/Chand/.codex/skills/android-kotlin-code-index/scripts/index_android_v4.py . lookup --source app/src/main/java/com/vandatgsts/thuyetnguyen/MainActivity.kt --record
python C:/Users/Chand/.codex/skills/android-kotlin-code-index/scripts/index_android_v4.py . validate
```

Đọc route qualified-name → shard symbol → shard kiến trúc/flow/feature → mã nguồn khi cần.
Quan hệ là tham chiếu qualified name; framework bên ngoài không có shard nguồn trong dự án.
Liên kết được đối chiếu tĩnh; callback/lambda và dispatch động không phải call graph từ compiler.

## Các nhóm nguồn

| Nhóm | Đường dẫn | Vai trò |
| --- | --- | --- |
| Điều hướng | `MainActivity.kt` | `AppScreen`, back stack Compose, mở editor/preview/settings và các tab |
| Model | `data/model/` | Hóa đơn, dòng hàng, thanh toán nợ, cửa hàng, khách hàng, sản phẩm, cấu hình và backup |
| Repository | `data/repository/` | CRUD SharedPreferences, StateFlow, backup JSON, merge/replace và lịch sử hash import |
| Trang chủ | `ui/home/` | Danh sách hóa đơn, tìm kiếm/lọc, tạo/xóa/sao chép và tổng quan công nợ |
| Editor | `ui/editor/` | Chỉnh hai mẫu hóa đơn, dòng hàng, VAT, nợ, cờ hiển thị và lưu dữ liệu |
| Preview | `ui/preview/` | Bitmap xem trước, zoom/pan, loading và callback xuất/chia sẻ |
| Cửa hàng | `ui/stores/` | Danh sách/tổng hợp, lịch sử kỳ, công nợ mới nhất và ghi nhận thanh toán |
| Sản phẩm | `ui/products/` | 30 sản phẩm theo bảng giá Đông Á/Bình Minh, tìm kiếm, thêm/sửa/xóa và giá mặc định |
| Cấu hình | `ui/settings/` | Thông tin công ty, ngân hàng mặc định, sao lưu và nhập dữ liệu |
| UI chung | `ui/components/`, `ui/theme/` | Trường nhập, header, màu sắc, typography, theme |
| Xuất hóa đơn | `generator/` | Canvas hai mẫu, format, Bitmap/PNG, PdfDocument, FileProvider, MediaStore và Intent |

## Luồng chính

### Tạo và lưu hóa đơn

`MainActivity.onCreate` mở `InvoiceEditorScreen`; `InvoiceEditorViewModel` nạp hóa đơn hoặc khởi tạo mẫu.
Danh sách dòng hàng dùng `ReorderableInvoiceItems`: nhấn giữ nút kéo 48dp, kéo lên/xuống và thả;
tự cuộn sát mép viewport, chỉ báo vị trí chèn và thao tác lên/xuống cho trình đọc màn hình.
`InvoiceEditorViewModel.moveItem` tìm dòng theo ID, kiểm tra chỉ số và đánh lại STT 1..n;
giữ nguyên ID, nội dung và dữ liệu tài chính. Hủy kéo không đổi danh sách.
Các thao tác chỉnh dữ liệu cập nhật StateFlow; `InvoiceDocument` / `InvoiceItem` tính tổng, VAT và nợ.
`InvoiceItem.note` lưu ghi chú riêng từng dòng, nhập nhiều dòng qua `ItemCard`.
Ghi chú đi cùng dòng hàng khi kéo thả hoặc nhân bản; dữ liệu cũ dùng giá trị rỗng mặc định.
Lưu qua `InvoiceRepository` trên IO, dùng Mutex để tuần tự hóa toàn bộ thao tác đọc–sửa–ghi.
AtomicFile ghi UTF-8 và rollback khi ghi lỗi; chỉ cập nhật StateFlow sau commit. Danh sách rỗng được giữ sau khi xóa hết.
Editor dùng `isSaving` chặn lưu lặp, khóa nút lưu/preview và báo lỗi qua callback; chỉ điều hướng khi lưu thành công.
Trang chủ và màn hình thanh toán bắt lỗi lưu và hiển thị thông báo; cancellation được ném lại.

### Xem trước và xuất

`InvoicePreviewViewModel.loadInvoice` lấy hóa đơn từ repository và dựng bitmap qua
`ImageInvoiceRenderer.renderBitmap` trên Default. `InvoiceCanvasDrawer` chọn mẫu theo `InvoiceType`.
Mẫu A4 đo toàn bộ bố cục trên Canvas rỗng bằng cùng hàm vẽ: header, dòng hàng, VAT, ghi chú chung,
bảo hành, điều khoản, ngân hàng và phần con dấu đã xoay; cộng lề cuối trang.
Địa chỉ/ghi chú/điều khoản hỗ trợ xuống dòng thủ công và chuỗi dài liền nhau.
Cả hai mẫu có cột ghi chú từng dòng: mẫu giao hàng rộng 1400px; A4 giữ 1000px và chia lại
chiều rộng cột. Ghi chú xuống dòng theo chiều rộng ô, dùng chung phép đo chiều cao dòng
cho vẽ và tính kích thước PDF/PNG; hỗ trợ xuống dòng thủ công và chuỗi không có khoảng trắng.
Các lệnh xuất qua `InvoiceExportManager`: PDF/PNG vào cache exports trên IO;
chia sẻ/mở bằng FileProvider + URI read grant + chooser; lưu ảnh bằng MediaStore từ API 29,
hoặc Pictures trên API cũ. Thông báo lỗi Toast chuyển về Main.

### Công nợ cửa hàng

`StorePartnerViewModel.storeSummaries` kết hợp cửa hàng và hóa đơn, khớp tên cửa hàng/title,
sắp theo thời gian và lấy số dư kỳ mới nhất. Chi tiết cửa hàng mở kỳ mới với nợ kế thừa.
Ghi nhận thanh toán cập nhật `debtPayments` của hóa đơn mới nhất và lưu vào repository.

### Sao lưu và nhập

Màn hình cấu hình gọi `CompanyProfileViewModel.exportBackup` → `BackupRepository.createBackupData`
trên IO, đóng gói dữ liệu và xuất JSON.
Chia sẻ qua FileProvider. Nhập URI → parse JSON → chọn merge hoặc replace → cập nhật repository
và ghi hash đã nhập. Luồng dùng callback Activity Result trong Compose.

### Thay danh mục sản phẩm theo bảng giá

`ProductRepository.loadProducts` kiểm tra phiên bản danh mục trong SharedPreferences.
Lần mở đầu tiên với phiên bản 1 thay toàn bộ danh mục cũ bằng 30 sản phẩm: 14 bồn nước Đông Á
đứng/nằm từ `.ai/img_4.png`, 16 máy NLMT Đông Á/Bình Minh từ `.ai/img_5.png`.
JSON và marker phiên bản được ghi chung một editor. Những lần sau giữ dữ liệu đã chỉnh sửa,
kể cả danh sách rỗng sau khi xóa hết sản phẩm. Bảo hành và ghi chú lắp đặt được lưu trong `note`.

## Hợp đồng state và threading

- UI Compose thu thập StateFlow; ViewModel dùng `viewModelScope` cho tác vụ bất đồng bộ.
- Repository nạp dữ liệu đồng bộ khi khởi tạo; các mutator suspend dùng IO khi nguồn khai báo.
- CompanyProfileRepository lưu đồng bộ qua SharedPreferences.apply.
- Renderer bitmap dùng Default; PDF/PNG và thao tác thư viện ảnh dùng IO.
- Canvas/Paint thuộc lời gọi hiện tại; bên gọi quản lý vòng đời Bitmap/coroutine.

## Bảo trì

Upsert từng nguồn là thay thế toàn bộ record thuộc nguồn đó, bao gồm hash, range và routes.
Sau đổi mã: cập nhật nguồn bị ảnh hưởng và liên kết liên quan, rồi chạy validate đầy đủ.
Không dùng script V3 để ghi đè manifest V4. Chỉ mục cũ được giữ ở `.ai/indexes-legacy-before-v4/`
để tham khảo lịch sử; dữ liệu cũ không được dùng làm đầu vào xây dựng V4.
