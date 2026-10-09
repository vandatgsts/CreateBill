package com.vandatgsts.thuyetnguyen.ui.editor

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.vandatgsts.thuyetnguyen.data.model.InvoiceItem
import com.vandatgsts.thuyetnguyen.ui.theme.PrimaryBlue
import kotlinx.coroutines.isActive

@Composable
internal fun ReorderableInvoiceItems(
    items: List<InvoiceItem>,
    scrollState: ScrollState,
    viewportBounds: Rect,
    onMove: (String, Int) -> Unit,
    content: @Composable (Int, InvoiceItem, Modifier) -> Unit
) {
    val currentItems by rememberUpdatedState(items)
    val currentOnMove by rememberUpdatedState(onMove)
    val currentViewport by rememberUpdatedState(viewportBounds)
    val itemBounds = remember { mutableStateMapOf<String, Rect>() }
    var draggedItemId by remember { mutableStateOf<String?>(null) }
    var initialItemBounds by remember { mutableStateOf(Rect.Zero) }
    var initialScroll by remember { mutableIntStateOf(0) }
    var dragDistance by remember { mutableFloatStateOf(0f) }
    var initialPointerY by remember { mutableFloatStateOf(0f) }
    var listCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val edgeSize = with(LocalDensity.current) { 72.dp.toPx() }
    val maxScrollSpeed = with(LocalDensity.current) { 640.dp.toPx() }

    fun dropPosition(): Float {
        val coordinates = listCoordinates
        return if (coordinates != null && coordinates.isAttached) {
            initialPointerY + dragDistance - coordinates.positionInWindow().y
        } else initialItemBounds.top + dragDistance + (scrollState.value - initialScroll)
    }

    fun dropIndex(): Int {
        val position = dropPosition()
        return currentItems.count { item ->
            item.id != draggedItemId && (itemBounds[item.id]?.center?.y ?: Float.MAX_VALUE) < position
        }
    }

    fun finishDrag(commit: Boolean) {
        val itemId = draggedItemId
        val target = if (commit && itemId != null) dropIndex() else null
        draggedItemId = null
        dragDistance = 0f
        if (itemId != null && target != null) currentOnMove(itemId, target)
    }

    LaunchedEffect(items.map { it.id }) {
        val ids = items.map { it.id }.toSet()
        itemBounds.keys.toList().filterNot { it in ids }.forEach { itemBounds.remove(it) }
        if (draggedItemId != null && draggedItemId !in ids) finishDrag(commit = false)
    }

    LaunchedEffect(draggedItemId) {
        if (draggedItemId == null) return@LaunchedEffect
        var previousFrame = withFrameNanos { it }
        while (isActive && draggedItemId != null) {
            val frame = withFrameNanos { it }
            val elapsedSeconds = ((frame - previousFrame) / 1_000_000_000f).coerceAtMost(0.032f)
            previousFrame = frame
            val viewport = currentViewport
            if (viewport.height <= 0f) continue
            val pointerY = initialPointerY + dragDistance
            val speed = when {
                pointerY < viewport.top + edgeSize ->
                    -maxScrollSpeed * ((viewport.top + edgeSize - pointerY) / edgeSize).coerceIn(0f, 1f)
                pointerY > viewport.bottom - edgeSize ->
                    maxScrollSpeed * ((pointerY - viewport.bottom + edgeSize) / edgeSize).coerceIn(0f, 1f)
                else -> 0f
            }
            if (speed != 0f) scrollState.scrollBy(speed * elapsedSeconds)
        }
    }

    val insertionBeforeId = if (draggedItemId != null) {
        currentItems.filterNot { it.id == draggedItemId }.getOrNull(dropIndex())?.id
    } else null
    val insertionAtEnd = draggedItemId != null && insertionBeforeId == null

    Column(
        modifier = Modifier.onGloballyPositioned { listCoordinates = it },
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEachIndexed { index, item ->
            key(item.id) {
                var handleCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
                val isDragging = draggedItemId == item.id
                val dragHandleModifier = Modifier
                    .onGloballyPositioned { handleCoordinates = it }
                    .semantics(mergeDescendants = true) {
                        customActions = buildList {
                            if (index > 0) add(CustomAccessibilityAction("Di chuyển lên") {
                                currentOnMove(item.id, currentItems.indexOfFirst { it.id == item.id } - 1)
                                true
                            })
                            if (index < items.lastIndex) add(CustomAccessibilityAction("Di chuyển xuống") {
                                currentOnMove(item.id, currentItems.indexOfFirst { it.id == item.id } + 1)
                                true
                            })
                        }
                    }
                    .pointerInput(item.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { position ->
                                val bounds = itemBounds[item.id]
                                val coordinates = handleCoordinates
                                if (bounds != null && coordinates != null && coordinates.isAttached) {
                                    initialItemBounds = bounds
                                    initialScroll = scrollState.value
                                    dragDistance = 0f
                                    initialPointerY = coordinates.localToWindow(position).y
                                    draggedItemId = item.id
                                }
                            },
                            onDrag = { change, amount ->
                                if (draggedItemId == item.id) {
                                    change.consume()
                                    dragDistance += amount.y
                                }
                            },
                            onDragEnd = { finishDrag(commit = true) },
                            onDragCancel = { finishDrag(commit = false) }
                        )
                    }

                // Measure an untransformed anchor so the dragged card cannot alter its own drop geometry.
                Box(
                    modifier = Modifier
                        .onGloballyPositioned { itemBounds[item.id] = it.boundsInParent() }
                        .zIndex(if (isDragging) 1f else 0f)
                        .drawWithContent {
                            drawContent()
                            val lineY = when {
                                item.id == insertionBeforeId -> 0f
                                insertionAtEnd && index == items.lastIndex -> size.height
                                else -> null
                            }
                            if (lineY != null) drawLine(
                                color = PrimaryBlue,
                                start = Offset(0f, lineY),
                                end = Offset(size.width, lineY),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                ) {
                    Box(
                        modifier = Modifier.graphicsLayer {
                            translationY = if (isDragging) {
                                initialItemBounds.top + dragDistance + (scrollState.value - initialScroll) -
                                    (itemBounds[item.id]?.top ?: initialItemBounds.top)
                            } else 0f
                            alpha = if (isDragging) 0.92f else 1f
                            shadowElevation = if (isDragging) 8.dp.toPx() else 0f
                        }
                    ) {
                        content(index, item, dragHandleModifier)
                    }
                }
            }
        }
    }
}
