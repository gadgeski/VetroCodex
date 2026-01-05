package com.gadgeski.vetrocodex.ui.screens

import android.content.Context
import android.graphics.BlurMaskFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.gadgeski.vetrocodex.R
import com.gadgeski.vetrocodex.ui.theme.BBHBartleFontFamily
import com.gadgeski.vetrocodex.ui.theme.OrbitronFontFamily
import com.gadgeski.vetrocodex.ui.viewmodel.ClockViewModel
import com.gadgeski.vetrocodex.util.HingePosture
import com.gadgeski.vetrocodex.util.rememberHingePosture
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

// Cyberpunk Color Palette
val NeonCyan = Color(0xFF00FFFF)
val HotPink = Color(0xFFFF00FF)
val DeepBlack = Color(0xFF050505)
val HudGray = Color(0xFF333333)

@Composable
fun CyberpunkClockScreen(
    viewModel: ClockViewModel = hiltViewModel()
) {
    val currentTime by viewModel.currentTime.collectAsState()
    val hingePosture by rememberHingePosture()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        CyberpunkBackground()

        AnimatedContent(
            targetState = hingePosture,
            transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
            label = "LayoutSwitch"
        ) { posture ->
            when (posture) {
                // 1. 本のように開いた状態 (左右分割)
                HingePosture.BOOK_MODE -> BookMode(currentTime)

                // 2. ノートPCのように開いた状態 (上下分割)
                HingePosture.TABLETOP_MODE -> TabletopMode(currentTime)

                // 3. 完全に開いた状態 (FLAT)
                else -> FlatModeSelector(currentTime)
            }
        }
    }
}

// --- Mode Selector for Flat State ---
@Composable
fun FlatModeSelector(time: LocalTime) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // 幅が600dp以上ならメイン画面(広)とみなしてLandscape、それ以外はサブ画面(狭)としてMonolith
        // Galaxy Z Fold 7のメイン幅は約2184px(800dp以上)、サブは約1080px(400dp前後)と想定
        if (maxWidth > 600.dp) {
            LandscapeMode(time)
        } else {
            MonolithMode(time)
        }
    }
}

// --- Mode A: Tabletop Mode (上下半開き / Flipスタイル) ---
@Composable
fun TabletopMode(time: LocalTime) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, HudGray.copy(alpha = 0.5f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            HorizontalClock(time)
            CornerFrame(Modifier.matchParentSize())
        }
        HorizontalHingeSpacer()
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            DashboardPanel()
        }
    }
}

// --- Mode B: Book Mode (左右半開き / Foldスタイル) ---
@Composable
fun BookMode(time: LocalTime) {
    Row(modifier = Modifier.fillMaxSize()) {
        // 左ページ: 時計 (Monolithの時計部分を流用)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(1.dp, HudGray.copy(alpha = 0.5f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // 縦積みの時計を表示
            VerticalClockOnly(time)
            CornerFrame(Modifier.matchParentSize())
        }

        // 中央ヒンジ
        VerticalHingeSpacer()

        // 右ページ: 情報パネル
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            DashboardPanel()
        }
    }
}

// --- Mode C: Monolith Mode (サブ画面 / 縦長) ---
@Composable
fun MonolithMode(time: LocalTime) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            VerticalDivider(
                color = HudGray.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxHeight()
            )
            VerticalDivider(
                color = NeonCyan.copy(alpha = 0.5f),
                thickness = 2.dp,
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .alpha(0.5f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 48.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.weight(2f).fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                DynamicNeonText(
                    text = time.format(DateTimeFormatter.ofPattern("HH")),
                    color = NeonCyan,
                    scaleFactor = 0.55f,
                    isBold = true
                )
            }

            HorizontalDivider(color = HotPink.copy(alpha = 0.5f), thickness = 2.dp, modifier = Modifier.width(50.dp))

            Box(
                modifier = Modifier.weight(2f).fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                DynamicNeonText(
                    text = time.format(DateTimeFormatter.ofPattern("mm")),
                    color = NeonCyan,
                    scaleFactor = 0.55f,
                    isBold = true
                )
            }

            Box(modifier = Modifier.weight(1.5f).fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = time.format(DateTimeFormatter.ofPattern("ss")),
                        color = HotPink,
                        fontSize = 40.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    BatteryStatusRow()
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.height(100.dp)) {
                        SystemLogView()
                    }
                }
            }
        }
    }
}

// --- Mode D: Landscape Mode (メイン全開 / 横長) ---
@Composable
fun LandscapeMode(time: LocalTime) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 左側: 時計エリア (画面の60%)
        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .border(1.dp, HudGray.copy(alpha = 0.5f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            HorizontalClock(time)
            CornerFrame(Modifier.matchParentSize())
        }

        // 右側: 情報エリア (画面の40%)
        Box(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        ) {
            DashboardPanel()
        }
    }
}

// --- Components ---

@Composable
fun HorizontalClock(time: LocalTime) {
    val hour = time.format(DateTimeFormatter.ofPattern("HH"))
    val minute = time.format(DateTimeFormatter.ofPattern("mm"))
    val second = time.format(DateTimeFormatter.ofPattern("ss"))

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BlinkingText(text = "SYSTEM TIME", color = NeonCyan, modifier = Modifier.padding(bottom = 8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            NeonText(text = hour, fontSize = 70.dp, color = NeonCyan, isBold = true)
            NeonText(text = ":", fontSize = 70.dp, color = NeonCyan, modifier = Modifier.padding(horizontal = 4.dp), isBold = true)
            NeonText(text = minute, fontSize = 70.dp, color = NeonCyan, isBold = true)
        }
        Text(
            text = "SEC.$second",
            color = HotPink,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}

// BookMode用の縦積み時計 (情報なし版)
@Composable
fun VerticalClockOnly(time: LocalTime) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BlinkingText(text = "SYSTEM TIME", color = NeonCyan, modifier = Modifier.padding(bottom = 16.dp))

        // HH
        DynamicNeonText(
            text = time.format(DateTimeFormatter.ofPattern("HH")),
            color = NeonCyan,
            scaleFactor = 0.6f,
            isBold = true
        )

        HorizontalDivider(color = HotPink.copy(alpha = 0.5f), thickness = 2.dp, modifier = Modifier.width(30.dp).padding(vertical = 16.dp))

        // MM
        DynamicNeonText(
            text = time.format(DateTimeFormatter.ofPattern("mm")),
            color = NeonCyan,
            scaleFactor = 0.6f,
            isBold = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SS
        Text(
            text = time.format(DateTimeFormatter.ofPattern("ss")),
            color = HotPink,
            fontSize = 32.sp,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DashboardPanel() {
    Column {
        Text(
            text = "STATUS MONITOR",
            color = HudGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp),
            fontFamily = BBHBartleFontFamily
        )
        HorizontalDivider(color = HudGray)
        Spacer(modifier = Modifier.height(8.dp))
        BatteryStatusRow()
        Spacer(modifier = Modifier.height(16.dp))
        SystemLogView()
    }
}

@Composable
fun BatteryStatusRow() {
    val context = LocalContext.current
    val batteryLevel = remember { getBatteryLevel(context) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "PWR",
            color = NeonCyan,
            modifier = Modifier.width(40.dp),
            fontFamily = BBHBartleFontFamily
        )
        LinearProgressIndicator(
            progress = { batteryLevel / 100f },
            modifier = Modifier.weight(1f).height(8.dp),
            color = if (batteryLevel > 20) NeonCyan else HotPink,
            trackColor = HudGray
        )
        Text(
            text = "${batteryLevel}%",
            color = NeonCyan,
            modifier = Modifier.padding(start = 8.dp),
            fontFamily = BBHBartleFontFamily
        )
    }
}

@Composable
fun SystemLogView() {
    val logs = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        while (true) {
            val hex = Random.nextInt(0, 999999).toString(16).uppercase().padStart(6, '0')
            val msg = listOf("SYNC...", "CHECKING...", "OK", "DATA FLOW", "PING", "VETRO SYS").random()
            logs.add("[$hex] :: $msg")
            if (logs.size > 20) logs.removeAt(0)
            listState.animateScrollToItem(logs.size - 1)
            delay(Random.nextLong(200, 800))
        }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items(logs) { log ->
            Text(
                text = log,
                color = NeonCyan.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontFamily = BBHBartleFontFamily
            )
        }
    }
}

// --- Visual Effects ---

@Composable
fun NeonText(
    text: String,
    fontSize: Dp,
    color: Color,
    modifier: Modifier = Modifier,
    isBold: Boolean = false
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val typeface = remember(isBold) {
        val fontRes = if (isBold) R.font.orbitron_bold else R.font.orbitron_medium
        try { ResourcesCompat.getFont(context, fontRes) } catch (_: Exception) { null }
    }

    val paint = remember(typeface) {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
            if (typeface != null) {
                this.typeface = typeface
            }
        }
    }
    val textSizePx = with(density) { fontSize.toPx() }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawIntoCanvas {
                paint.color = color.toArgb()
                paint.textSize = textSizePx
                it.nativeCanvas.drawText(text, 0f, size.height * 0.8f, paint)
            }
        }
        Text(
            text = text,
            fontSize = with(density) { fontSize.toSp() },
            color = Color.White,
            fontFamily = OrbitronFontFamily,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.alpha(0.9f)
        )
    }
}

@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@Composable
fun DynamicNeonText(
    text: String,
    color: Color,
    scaleFactor: Float,
    modifier: Modifier = Modifier,
    isBold: Boolean = false
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val typeface = remember(isBold) {
        val fontRes = if (isBold) R.font.orbitron_bold else R.font.orbitron_medium
        try { ResourcesCompat.getFont(context, fontRes) } catch (_: Exception) { null }
    }

    val paint = remember(typeface) {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
            if (typeface != null) {
                this.typeface = typeface
            }
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val constraintsWidth = constraints.maxWidth
        val dynamicFontSize = remember(constraintsWidth, density) {
            with(density) { (constraintsWidth * scaleFactor).toSp() }
        }
        val dynamicFontSizePx = remember(dynamicFontSize, density) {
            with(density) { dynamicFontSize.toPx() }
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxWidth().height(with(density){ dynamicFontSize.toDp() * 1.2f })) {
                drawIntoCanvas {
                    paint.color = color.toArgb()
                    paint.textSize = dynamicFontSizePx
                    val textWidth = paint.measureText(text)
                    val x = (size.width - textWidth) / 2
                    it.nativeCanvas.drawText(text, x, size.height * 0.75f, paint)
                }
            }
            Text(
                text = text,
                fontSize = dynamicFontSize,
                color = Color.White,
                fontFamily = OrbitronFontFamily,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.alpha(0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CyberpunkBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanlineY"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 50.dp.toPx()
        val width = size.width
        val height = size.height

        for (x in 0..width.toInt() step gridSize.toInt()) {
            drawLine(color = HudGray, start = Offset(x.toFloat(), 0f), end = Offset(x.toFloat(), height), strokeWidth = 1f, alpha = 0.2f)
        }
        for (y in 0..height.toInt() step gridSize.toInt()) {
            drawLine(color = HudGray, start = Offset(0f, y.toFloat()), end = Offset(width, y.toFloat()), strokeWidth = 1f, alpha = 0.2f)
        }

        val lineHeight = height * 0.1f
        val yPos = height * scanlineY
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, NeonCyan.copy(alpha = 0.1f), Color.Transparent),
                startY = yPos, endY = yPos + lineHeight
            ),
            topLeft = Offset(0f, yPos),
            size = size.copy(height = lineHeight)
        )
    }
}

@Composable
fun BlinkingText(text: String, color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(animation = tween(800, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "alpha"
    )
    Text(
        text = text,
        color = color.copy(alpha = alpha),
        modifier = modifier,
        letterSpacing = 2.sp,
        fontFamily = BBHBartleFontFamily
    )
}

@Composable
fun CornerFrame(modifier: Modifier) {
    val color = NeonCyan.copy(alpha = 0.5f)
    val length = 20.dp
    val thickness = 2.dp

    Canvas(modifier = modifier) {
        drawLine(color, Offset(0f, 0f), Offset(length.toPx(), 0f), thickness.toPx())
        drawLine(color, Offset(0f, 0f), Offset(0f, length.toPx()), thickness.toPx())
        drawLine(color, Offset(size.width, 0f), Offset(size.width - length.toPx(), 0f), thickness.toPx())
        drawLine(color, Offset(size.width, 0f), Offset(size.width, length.toPx()), thickness.toPx())
        drawLine(color, Offset(0f, size.height), Offset(length.toPx(), size.height), thickness.toPx())
        drawLine(color, Offset(0f, size.height), Offset(0f, size.height - length.toPx()), thickness.toPx())
        drawLine(color, Offset(size.width, size.height), Offset(size.width - length.toPx(), size.height), thickness.toPx())
        drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - length.toPx()), thickness.toPx())
    }
}

// ヒンジスペーサー（水平） - Tabletop用
@Composable
fun HorizontalHingeSpacer() {
    Box(
        modifier = Modifier.fillMaxWidth().height(24.dp).background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "--- FOLD AXIS ---",
            color = HudGray,
            fontSize = 10.sp,
            letterSpacing = 4.sp,
            fontFamily = BBHBartleFontFamily
        )
    }
}

// ヒンジスペーサー（垂直） - BookMode用
@Composable
fun VerticalHingeSpacer() {
    Box(
        modifier = Modifier.fillMaxHeight().width(24.dp).background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // 縦書きテキストはComposeで少し面倒なので、ここではライン装飾で代用
        VerticalDivider(color = HudGray, thickness = 1.dp)
    }
}

// バッテリー取得Util
fun getBatteryLevel(context: Context): Int {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}