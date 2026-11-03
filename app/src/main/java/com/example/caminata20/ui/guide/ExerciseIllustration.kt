package com.example.caminata20.ui.guide

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.caminata20.domain.model.ExerciseVisual

/**
 * HU-16 CA-03: gráfico animado que explica el movimiento del ejercicio sugerido.
 * Dibujo vectorial (sin assets externos), se anima mientras el temporizador corre.
 */
@Composable
fun ExerciseIllustration(
    visual: ExerciseVisual,
    color: Color,
    accent: Color,
    animate: Boolean,
    description: String,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "exercise")
    val anim by transition.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "phase",
    )
    val t = if (animate) anim else 0.6f
    Canvas(modifier.semantics { contentDescription = description }) {
        when (visual) {
            ExerciseVisual.NECK_TILT -> figure(color, headTilt = 25f * t)
            ExerciseVisual.SHOULDER_ROLL -> {
                figure(color, shoulderLift = 10f * t)
                arc(accent, center = Offset(size.width * 0.5f, size.height * 0.40f), r = size.minDimension * 0.22f)
            }
            ExerciseVisual.ARM_STRETCH -> figure(color, armsUp = (t + 1f) / 2f)
            ExerciseVisual.SIDE_BEND -> figure(color, torsoTilt = 18f * t, armsUp = 1f)
            ExerciseVisual.TORSO_TWIST -> {
                figure(color, torsoTilt = 6f * t)
                arc(accent, center = Offset(size.width * 0.5f, size.height * 0.55f), r = size.minDimension * 0.25f)
            }
            ExerciseVisual.FORWARD_BEND -> figure(color, torsoTilt = 45f * (t + 1f) / 2f)
            ExerciseVisual.LEG_STRETCH -> figure(color, legLift = (t + 1f) / 2f)
            ExerciseVisual.EYE_DISTANCE -> eyes(color, accent, pupilX = 0f, focus = (t + 1f) / 2f)
            ExerciseVisual.EYE_MOVE -> eyes(color, accent, pupilX = t, focus = 0f)
            ExerciseVisual.EYES_CLOSED -> eyes(color, accent, pupilX = 0f, focus = 0f, closed = true)
            ExerciseVisual.BREATH -> {
                val r = size.minDimension * (0.22f + 0.12f * (t + 1f) / 2f)
                drawCircle(accent.copy(alpha = 0.35f), r * 1.25f)
                drawCircle(color, r, style = Stroke(width = size.minDimension * 0.03f))
            }
            ExerciseVisual.WATER -> glass(color, accent, level = 0.35f + 0.5f * (t + 1f) / 2f)
        }
    }
}

private fun DrawScope.figure(
    c: Color,
    headTilt: Float = 0f,
    shoulderLift: Float = 0f,
    armsUp: Float = 0f,
    torsoTilt: Float = 0f,
    legLift: Float = 0f,
) {
    val u = size.minDimension
    val w = u * 0.035f
    val cx = size.width / 2f
    val hip = Offset(cx, size.height * 0.62f)
    // piernas
    val footY = size.height * 0.92f
    drawLine(c, hip, Offset(cx - u * 0.10f, footY), w, StrokeCap.Round)
    val kneeLeft = Offset(cx + u * 0.08f, hip.y + (footY - hip.y) * (0.5f - 0.15f * legLift))
    drawLine(c, hip, kneeLeft, w, StrokeCap.Round)
    drawLine(c, kneeLeft, Offset(cx + u * (0.10f - 0.12f * legLift), footY - (footY - hip.y) * 0.45f * legLift), w, StrokeCap.Round)
    rotate(torsoTilt, pivot = hip) {
        val neck = Offset(cx, size.height * 0.30f - shoulderLift)
        drawLine(c, hip, neck, w, StrokeCap.Round)
        val shoulder = Offset(cx, neck.y + u * 0.04f)
        val armLen = u * 0.24f
        listOf(-1f, 1f).forEach { s ->
            val down = Offset(cx + s * u * 0.14f, shoulder.y + armLen * 0.9f)
            val up = Offset(cx + s * u * 0.08f, shoulder.y - armLen)
            val hand = Offset(down.x + (up.x - down.x) * armsUp, down.y + (up.y - down.y) * armsUp)
            drawLine(c, shoulder, hand, w, StrokeCap.Round)
        }
        rotate(headTilt, pivot = neck) {
            drawCircle(c, u * 0.08f, Offset(cx, neck.y - u * 0.09f), style = Stroke(width = w))
        }
    }
}

private fun DrawScope.arc(c: Color, center: Offset, r: Float) {
    drawArc(
        c, startAngle = 200f, sweepAngle = 280f, useCenter = false,
        topLeft = Offset(center.x - r, center.y - r), size = Size(r * 2, r * 2),
        style = Stroke(width = size.minDimension * 0.02f, cap = StrokeCap.Round),
    )
}

private fun DrawScope.eyes(c: Color, accent: Color, pupilX: Float, focus: Float, closed: Boolean = false) {
    val u = size.minDimension
    val y = size.height * 0.45f
    listOf(-1f, 1f).forEach { s ->
        val center = Offset(size.width / 2f + s * u * 0.2f, y)
        if (closed) {
            drawArc(
                c, 0f, 180f, false, topLeft = Offset(center.x - u * 0.13f, center.y - u * 0.06f),
                size = Size(u * 0.26f, u * 0.12f), style = Stroke(width = u * 0.03f, cap = StrokeCap.Round),
            )
        } else {
            drawOval(c, topLeft = Offset(center.x - u * 0.14f, center.y - u * 0.08f), size = Size(u * 0.28f, u * 0.16f), style = Stroke(width = u * 0.03f))
            drawCircle(c, u * (0.05f - 0.02f * focus), Offset(center.x + pupilX * u * 0.07f, center.y))
        }
    }
    if (focus > 0f || closed) {
        // objeto lejano (20 pies) que se enfoca
        val r = u * (0.03f + 0.04f * focus)
        drawCircle(accent, r, Offset(size.width / 2f, size.height * 0.82f))
    }
}

private fun DrawScope.glass(c: Color, accent: Color, level: Float) {
    val u = size.minDimension
    val left = size.width / 2f - u * 0.18f
    val top = size.height * 0.15f
    val gw = u * 0.36f
    val gh = size.height * 0.72f
    drawRect(accent.copy(alpha = 0.6f), topLeft = Offset(left, top + gh * (1f - level)), size = Size(gw, gh * level))
    drawRect(c, topLeft = Offset(left, top), size = Size(gw, gh), style = Stroke(width = u * 0.03f))
}
