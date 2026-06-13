package com.example.gymtrainer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

data class ChartPoint(val label: String, val value: Float)

/**
 * Minimal dependency-free line chart drawn on a Compose Canvas.
 * Plots [points] left-to-right with a filled area, line, and dots, plus
 * min/max value labels and the first/last x labels.
 */
@Composable
fun LineChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    unitSuffix: String = ""
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val fill = lineColor.copy(alpha = 0.15f)
    val axis = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val measurer = rememberTextMeasurer()

    Canvas(
        modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (points.isEmpty()) return@Canvas

        val padL = 8f; val padR = 8f; val padTop = 22f; val padBottom = 26f
        val chartW = size.width - padL - padR
        val chartH = size.height - padTop - padBottom

        val values = points.map { it.value }
        val maxV = (values.maxOrNull() ?: 1f)
        val minV = (values.minOrNull() ?: 0f)
        val range = (maxV - minV).takeIf { it > 0f } ?: 1f

        fun x(i: Int): Float =
            if (points.size == 1) padL + chartW / 2f
            else padL + chartW * i / (points.size - 1)

        fun y(v: Float): Float = padTop + chartH * (1f - (v - minV) / range)

        // baseline
        drawLine(axis, Offset(padL, padTop + chartH), Offset(padL + chartW, padTop + chartH), 2f)

        // filled area
        val area = Path().apply {
            moveTo(x(0), padTop + chartH)
            points.forEachIndexed { i, p -> lineTo(x(i), y(p.value)) }
            lineTo(x(points.size - 1), padTop + chartH)
            close()
        }
        drawPath(area, fill)

        // line
        val path = Path().apply {
            points.forEachIndexed { i, p ->
                if (i == 0) moveTo(x(i), y(p.value)) else lineTo(x(i), y(p.value))
            }
        }
        drawPath(path, lineColor, style = Stroke(width = 4f, cap = StrokeCap.Round))

        // dots
        points.forEachIndexed { i, p ->
            drawCircle(lineColor, 6f, Offset(x(i), y(p.value)))
            drawCircle(Color.White, 2.5f, Offset(x(i), y(p.value)))
        }

        // value labels (max + min)
        val style = TextStyle(color = labelColor, fontSize = 11.sp)
        drawText(measurer, "${trim(maxV)}$unitSuffix", topLeft = Offset(padL, 2f), style = style)
        drawText(measurer, "${trim(minV)}$unitSuffix",
            topLeft = Offset(padL, padTop + chartH + 4f), style = style)

        // first / last x labels
        if (points.size > 1) {
            val first = measurer.measure(points.first().label, style)
            val last = measurer.measure(points.last().label, style)
            drawText(measurer, points.first().label,
                topLeft = Offset(x(0), padTop + chartH + 4f), style = style)
            drawText(measurer, points.last().label,
                topLeft = Offset(x(points.size - 1) - last.size.width, padTop + chartH + 4f), style = style)
        }
    }
}

private fun trim(v: Float): String =
    if (v % 1f == 0f) v.toInt().toString() else String.format("%.1f", v)
