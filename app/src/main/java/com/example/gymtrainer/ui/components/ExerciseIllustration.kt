package com.example.gymtrainer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A lightweight, fully-drawn illustration for each exercise.
 *
 * Real exercise photos carry size and licensing problems, so instead we draw a
 * clean equipment icon with Canvas. This keeps the APK self-contained and
 * copyright-safe. The icon is chosen by Exercise.imageKey.
 *
 * To use real photos instead: drop images into res/drawable and render them with
 * Image(painterResource(...)) here, keyed by the same imageKey.
 */
@Composable
fun ExerciseIllustration(
    imageKey: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val accent = MaterialTheme.colorScheme.primary
    val bg = MaterialTheme.colorScheme.surfaceVariant
    val line = MaterialTheme.colorScheme.onSurface

    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(size)) {
            drawRoundedBg(bg)
            when (imageKey) {
                "barbell" -> drawBarbell(accent, line)
                "dumbbell" -> drawDumbbell(accent, line)
                "cable" -> drawCable(accent, line)
                "machine" -> drawMachine(accent, line)
                "bodyweight" -> drawBodyweight(accent, line)
                "kettlebell" -> drawKettlebell(accent, line)
                else -> drawGeneric(accent, line)
            }
        }
    }
}

private fun DrawScope.drawRoundedBg(c: Color) {
    drawRect(c)
}

private fun DrawScope.drawBarbell(accent: Color, line: Color) {
    val w = size.width; val h = size.height
    val cy = h * 0.5f
    val barW = 4f
    drawLine(line, Offset(w * 0.18f, cy), Offset(w * 0.82f, cy), barW, StrokeCap.Round)
    val plateH = h * 0.42f
    listOf(0.22f to 0.34f, 0.27f to 0.42f).forEach { (x, hp) ->
        drawPlate(accent, w * x, cy, plateH * hp / 0.42f)
        drawPlate(accent, w * (1 - x), cy, plateH * hp / 0.42f)
    }
}

private fun DrawScope.drawPlate(c: Color, cx: Float, cy: Float, halfH: Float) {
    drawLine(c, Offset(cx, cy - halfH), Offset(cx, cy + halfH), 7f, StrokeCap.Round)
}

private fun DrawScope.drawDumbbell(accent: Color, line: Color) {
    val w = size.width; val h = size.height; val cy = h * 0.5f
    drawLine(line, Offset(w * 0.38f, cy), Offset(w * 0.62f, cy), 5f, StrokeCap.Round)
    listOf(0.30f, 0.70f).forEach { x ->
        drawLine(accent, Offset(w * x, cy - h * 0.22f), Offset(w * x, cy + h * 0.22f), 9f, StrokeCap.Round)
        drawLine(accent, Offset(w * (x + if (x < 0.5f) -0.07f else 0.07f), cy - h * 0.14f),
            Offset(w * (x + if (x < 0.5f) -0.07f else 0.07f), cy + h * 0.14f), 9f, StrokeCap.Round)
    }
}

private fun DrawScope.drawCable(accent: Color, line: Color) {
    val w = size.width; val h = size.height
    // pulley tower
    drawLine(line, Offset(w * 0.30f, h * 0.18f), Offset(w * 0.30f, h * 0.82f), 5f, StrokeCap.Round)
    drawCircle(accent, h * 0.07f, Offset(w * 0.30f, h * 0.28f))
    // cable
    drawLine(accent, Offset(w * 0.30f, h * 0.28f), Offset(w * 0.62f, h * 0.55f), 3f, StrokeCap.Round)
    // handle
    drawLine(line, Offset(w * 0.60f, h * 0.48f), Offset(w * 0.70f, h * 0.62f), 6f, StrokeCap.Round)
}

private fun DrawScope.drawMachine(accent: Color, line: Color) {
    val w = size.width; val h = size.height
    drawLine(line, Offset(w * 0.25f, h * 0.20f), Offset(w * 0.25f, h * 0.80f), 5f, StrokeCap.Round)
    // weight stack
    listOf(0.30f, 0.42f, 0.54f).forEach { y ->
        drawLine(accent, Offset(w * 0.40f, h * y), Offset(w * 0.62f, h * y), 7f, StrokeCap.Round)
    }
    // seat / arm
    drawLine(line, Offset(w * 0.62f, h * 0.66f), Offset(w * 0.78f, h * 0.66f), 5f, StrokeCap.Round)
}

private fun DrawScope.drawBodyweight(accent: Color, line: Color) {
    val w = size.width; val h = size.height
    // simple figure
    drawCircle(accent, h * 0.09f, Offset(w * 0.5f, h * 0.26f))
    drawLine(line, Offset(w * 0.5f, h * 0.35f), Offset(w * 0.5f, h * 0.62f), 5f, StrokeCap.Round)
    drawLine(line, Offset(w * 0.5f, h * 0.42f), Offset(w * 0.32f, h * 0.52f), 5f, StrokeCap.Round)
    drawLine(line, Offset(w * 0.5f, h * 0.42f), Offset(w * 0.68f, h * 0.52f), 5f, StrokeCap.Round)
    drawLine(line, Offset(w * 0.5f, h * 0.62f), Offset(w * 0.36f, h * 0.80f), 5f, StrokeCap.Round)
    drawLine(line, Offset(w * 0.5f, h * 0.62f), Offset(w * 0.64f, h * 0.80f), 5f, StrokeCap.Round)
}

private fun DrawScope.drawKettlebell(accent: Color, line: Color) {
    val w = size.width; val h = size.height
    drawArc(
        color = line, startAngle = 180f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(w * 0.38f, h * 0.28f),
        size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.22f),
        style = Stroke(width = 5f, cap = StrokeCap.Round)
    )
    drawCircle(accent, h * 0.20f, Offset(w * 0.5f, h * 0.62f))
}

private fun DrawScope.drawGeneric(accent: Color, line: Color) {
    val w = size.width; val h = size.height
    drawCircle(accent, h * 0.20f, Offset(w * 0.5f, h * 0.5f), style = Stroke(5f))
    drawLine(line, Offset(w * 0.5f, h * 0.34f), Offset(w * 0.5f, h * 0.66f), 5f, StrokeCap.Round)
    drawLine(line, Offset(w * 0.34f, h * 0.5f), Offset(w * 0.66f, h * 0.5f), 5f, StrokeCap.Round)
}
