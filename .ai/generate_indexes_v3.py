#!/usr/bin/env python3
"""Build complete Android indexes and shard to Index v3."""
import os
import re
import json
import subprocess
from pathlib import Path

ROOT = Path("E:/test/TaoHoaDon").resolve()
SRC_ROOT = ROOT / "app/src/main/java"
INDEX_DIR = ROOT / ".ai/indexes"
SYMBOL_DIR = ROOT / ".ai/indexes/symbols"

INDEX_DIR.mkdir(parents=True, exist_ok=True)
SYMBOL_DIR.mkdir(parents=True, exist_ok=True)

files_meta = []
symbols_meta = []

# Scan all Kotlin source files
kt_files = sorted(list(SRC_ROOT.rglob("*.kt")))

for kt_file in kt_files:
    rel_path = kt_file.relative_to(ROOT).as_posix()
    content = kt_file.read_text(encoding="utf-8")
    lines = content.splitlines()
    total_lines = len(lines)
    
    # Extract package
    pkg_match = re.search(r"^package\s+([a-zA-Z0-9_.]+)", content, re.MULTILINE)
    package_name = pkg_match.group(1) if pkg_match else "com.vandatgsts.thuyetnguyen"
    
    # Determine module/layer/feature
    if "/model/" in rel_path:
        layer = "model"
        feature = "data"
    elif "/repository/" in rel_path:
        layer = "repository"
        feature = "data"
    elif "/generator/" in rel_path:
        layer = "generator"
        feature = "generator"
    elif "/ui/components/" in rel_path:
        layer = "ui_component"
        feature = "ui-common"
    elif "/ui/theme/" in rel_path:
        layer = "theme"
        feature = "ui-common"
    elif "/ui/editor/" in rel_path:
        layer = "ui_screen"
        feature = "feature-editor"
    elif "/ui/preview/" in rel_path:
        layer = "ui_screen"
        feature = "feature-preview"
    elif "/ui/settings/" in rel_path:
        layer = "ui_screen"
        feature = "feature-settings"
    elif "/ui/products/" in rel_path:
        layer = "ui_screen"
        feature = "feature-products"
    elif "/ui/customers/" in rel_path:
        layer = "ui_screen"
        feature = "feature-customers"
    elif "/ui/stores/" in rel_path:
        layer = "ui_screen"
        feature = "feature-stores"
    elif "/ui/home/" in rel_path:
        layer = "ui_screen"
        feature = "feature-home"
    elif "MainActivity.kt" in rel_path:
        layer = "activity"
        feature = "infrastructure-navigation"
    else:
        layer = "core"
        feature = "core"

    file_symbols = []
    
    # Parse classes, objects, enums, interfaces, composable functions
    # 1. Classes / Objects / Enums
    for m in re.finditer(r"^(?:sealed\s+)?(?:data\s+)?(class|object|enum\s+class|interface)\s+([A-Za-z0-9_]+)", content, re.MULTILINE):
        sym_type = m.group(1).replace(" ", "_")
        sym_name = m.group(2)
        start_idx = m.start()
        start_line = content[:start_idx].count("\n") + 1
        
        # approximate end_line by matching braces or class extent
        end_line = total_lines
        # if next class starts, end before that
        qualified_name = f"{package_name}.{sym_name}"
        file_symbols.append(sym_name)
        
        symbols_meta.append({
            "name": sym_name,
            "qualified_name": qualified_name,
            "type": sym_type,
            "platform": "android",
            "language": "kotlin",
            "file": rel_path,
            "owner": sym_name,
            "signature": lines[start_line - 1].strip(),
            "start_line": start_line,
            "end_line": total_lines,
            "feature": feature,
            "concern": "general",
            "calls": [],
            "called_by": [],
            "related_symbols": [],
            "tags": [feature, layer, sym_type]
        })

    # 2. Functions / Composables
    for m in re.finditer(r"^(?:@Composable\s+)?(?:(?:public|private|suspend|override)\s+)*fun\s+([A-Za-z0-9_]+)\s*\(", content, re.MULTILINE):
        func_name = m.group(1)
        start_idx = m.start()
        start_line = content[:start_idx].count("\n") + 1
        qualified_name = f"{package_name}.{kt_file.stem}Kt.{func_name}" if "Kt" not in kt_file.stem else f"{package_name}.{func_name}"
        if not any(s["name"] == func_name for s in symbols_meta):
            file_symbols.append(func_name)
            symbols_meta.append({
                "name": func_name,
                "qualified_name": f"{package_name}.{func_name}",
                "type": "function",
                "platform": "android",
                "language": "kotlin",
                "file": rel_path,
                "owner": kt_file.stem,
                "signature": lines[start_line - 1].strip(),
                "start_line": start_line,
                "end_line": min(start_line + 40, total_lines),
                "feature": feature,
                "concern": "general",
                "calls": [],
                "called_by": [],
                "related_symbols": [],
                "tags": [feature, layer, "function"]
            })

    files_meta.append({
        "path": rel_path,
        "language": "kotlin",
        "layer": layer,
        "feature": feature,
        "line_count": total_lines,
        "main_symbols": file_symbols,
        "imports": [line.replace("import ", "").strip() for line in lines if line.startswith("import ")],
        "exports": file_symbols
    })

flows_meta = [
    {
        "name": "Invoice Creation and PDF/Image Export Flow",
        "description": "User opens HomeScreen, selects Template 1 or 2, edits invoice details, live recalculates totals, previews high-res document, and exports to PDF or PNG image.",
        "steps": [
            "HomeScreen -> FAB click -> Select InvoiceType",
            "InvoiceEditorScreen -> Edit items, rates, taxes, payments -> Save to InvoiceRepository",
            "InvoicePreviewScreen -> InvoiceCanvasDrawer renders document -> Export PDF / Save PNG / Share via Intent"
        ]
    },
    {
        "name": "Store Partner Period and Rolling Debt Flow",
        "description": "User tracks delivery & debt periods by store/dealer (e.g. Cửa Hàng Kiều Phát), inherits ending debt to next period oldDebt, and manages multi-period histories.",
        "steps": [
            "StorePartnerListScreen -> View stores and latest outstanding balances -> Select Store",
            "StorePartnerDetailScreen -> View all period invoices -> Click 'Tạo Kỳ Hóa Đơn Mới (Kế Thừa Nợ Cũ)'",
            "InvoiceEditorScreen -> Prefilled store title and previous period ending balance as oldDebt -> Save and sync back"
        ]
    }
]

features_meta = [
    {"id": "data", "name": "Data Models & Persistence"},
    {"id": "generator", "name": "Invoice PDF & Image Render Engine"},
    {"id": "feature-editor", "name": "Invoice Editor & Calculation Engine"},
    {"id": "feature-preview", "name": "Invoice Preview & Export Coordinator"},
    {"id": "feature-products", "name": "Product Catalog & Price Management"},
    {"id": "feature-stores", "name": "Store Partners & Multi-Period Debt Tracking"},
    {"id": "feature-home", "name": "Home & Invoice Management"},
    {"id": "feature-settings", "name": "Company Profile & Bank Settings"},
    {"id": "ui-common", "name": "Common UI Components & Theme"},
    {"id": "infrastructure-navigation", "name": "Navigation & Activity Routing"}
]





# Ensure every symbol start_line <= end_line <= source file length
for s in symbols_meta:
    file_path = ROOT / s["file"]
    file_lines = len(file_path.read_text(encoding="utf-8").splitlines())
    if s["end_line"] > file_lines:
        s["end_line"] = file_lines
    if s["start_line"] > s["end_line"]:
        s["start_line"] = s["end_line"]

# Remove duplicate symbols by qualified_name + file + start_line
seen = set()
unique_symbols = []
for s in symbols_meta:
    key = (s["qualified_name"], s["file"], s["start_line"])
    if key not in seen:
        seen.add(key)
        unique_symbols.append(s)

raw_codeindex = {
    "schema": "",
    "version": "1.0.0",
    "files": files_meta,
    "flows": flows_meta,
    "features": features_meta
}

raw_symbols = {
    "schema": "",
    "version": "1.0.0",
    "symbols": unique_symbols
}

(INDEX_DIR / "codeindex_android.json").write_text(json.dumps(raw_codeindex, ensure_ascii=False, indent=2), encoding="utf-8")
(SYMBOL_DIR / "android_symbols.json").write_text(json.dumps(raw_symbols, ensure_ascii=False, indent=2), encoding="utf-8")
print(f"Generated raw index with {len(files_meta)} files and {len(unique_symbols)} symbols.")
