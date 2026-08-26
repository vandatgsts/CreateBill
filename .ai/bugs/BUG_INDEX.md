# Bug Memory Index - Android Native

| Bug ID | Title | Feature | Status | Root Cause | Fix Summary |
|---|---|---|---|---|---|
| BUG-001 | AAR Metadata check compileSdk mismatch | build | FIXED | compileSdk was set to 35 while newer AndroidX dependencies required 36 | Updated compileSdk to 36 and synchronized stable AndroidX dependency versions in libs.versions.toml |
| BUG-002 | Missing Compose imports in InvoiceEditorScreen | ui-editor | FIXED | Missing Compose imports for AlertDialog, TextButton, size, and delegated property smart cast | Added missing imports and resolved delegated property casting |
| BUG-003 | Tax code line overlaps with wrapped company address | generator | FIXED | Y-coordinate calculation in drawWrappedText didn't account for multi-line address height when drawing subsequent tax code label | Implemented drawInfoRow to dynamically calculate height per field and advance Y-coordinate accurately |
| BUG-004 | Service content text is drawn top-aligned instead of vertically centered in Quotation table row | generator | FIXED | drawCenteredWrappedText used a fixed top Y offset (rowTop + 40f) instead of calculating baseline startY centered vertically around the cell's center line | Implemented getWrappedLines and drawVerticallyCenteredWrappedText to dynamically balance multi-line text vertically around rowTop + rowHeight / 2 |
