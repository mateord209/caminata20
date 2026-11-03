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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
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
            else -> stickman(visual, color, accent, p = (t + 1f) / 2f)
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

// ---------- Stickman básico (pausas activas) ----------

/** Pose del stickman en unidades del cuerpo: x a la derecha, y hacia arriba desde el suelo (0..~1.1). */
private data class Pose(
    val hip: Offset = Offset(0f, 0.50f),
    val neck: Offset = Offset(0f, 0.86f),
    val head: Offset = Offset(0f, 0.96f),
    val lElbow: Offset = Offset(-0.12f, 0.70f), val lHand: Offset = Offset(-0.15f, 0.54f),
    val rElbow: Offset = Offset(0.12f, 0.70f), val rHand: Offset = Offset(0.15f, 0.54f),
    val lKnee: Offset = Offset(-0.07f, 0.25f), val lFoot: Offset = Offset(-0.09f, 0f),
    val rKnee: Offset = Offset(0.07f, 0.25f), val rFoot: Offset = Offset(0.09f, 0f),
)

private fun lerp(a: Offset, b: Offset, f: Float) = Offset(a.x + (b.x - a.x) * f, a.y + (b.y - a.y) * f)
private fun lerp(a: Float, b: Float, f: Float) = a + (b - a) * f
private fun Offset.up(dy: Float) = Offset(x, y + dy)

/** Calcula la pose de cada ejercicio; p va de 0 (inicio) a 1 (máximo estiramiento). */
private fun poseFor(v: ExerciseVisual, p: Float): Pose {
    val base = Pose()
    return when (v) {
        // Brazo derecho cruza el pecho; la mano izquierda lo sujeta por el codo
        ExerciseVisual.CROSS_ARM -> base.copy(
            rElbow = lerp(base.rElbow, Offset(-0.02f, 0.76f), p), rHand = lerp(base.rHand, Offset(-0.22f, 0.78f), p),
            lElbow = lerp(base.lElbow, Offset(-0.12f, 0.66f), p), lHand = lerp(base.lHand, Offset(0.0f, 0.75f), p),
        )
        // Codo derecho arriba, mano detrás de la nuca; la izquierda empuja el codo
        ExerciseVisual.TRICEPS -> base.copy(
            rElbow = lerp(base.rElbow, Offset(0.07f, 1.10f), p), rHand = lerp(base.rHand, Offset(-0.02f, 0.88f), p),
            lElbow = lerp(base.lElbow, Offset(-0.10f, 0.98f), p), lHand = lerp(base.lHand, Offset(0.05f, 1.08f), p),
        )
        // Manos juntas al frente → brazos abiertos en T (también estiramiento de pecho)
        ExerciseVisual.CHEST_OPEN -> base.copy(
            lElbow = lerp(Offset(-0.10f, 0.78f), Offset(-0.20f, 0.84f), p), lHand = lerp(Offset(-0.02f, 0.76f), Offset(-0.38f, 0.84f), p),
            rElbow = lerp(Offset(0.10f, 0.78f), Offset(0.20f, 0.84f), p), rHand = lerp(Offset(0.02f, 0.76f), Offset(0.38f, 0.84f), p),
        )
        // Rodilla derecha sube al pecho, abrazada con ambas manos
        ExerciseVisual.KNEE_TO_CHEST -> {
            val knee = lerp(base.rKnee, Offset(0.06f, 0.66f), p)
            base.copy(
                rKnee = knee, rFoot = lerp(base.rFoot, Offset(0.10f, 0.40f), p),
                lElbow = lerp(base.lElbow, Offset(-0.10f, 0.66f), p), lHand = lerp(base.lHand, knee.up(-0.02f), p),
                rElbow = lerp(base.rElbow, Offset(0.16f, 0.66f), p), rHand = lerp(base.rHand, knee.up(-0.04f), p),
            )
        }
        // De pie sobre la izquierda, el pie derecho va al glúteo y la mano lo sujeta
        ExerciseVisual.QUAD_STAND -> {
            val foot = lerp(base.rFoot, Offset(0.14f, 0.46f), p)
            base.copy(
                rKnee = lerp(base.rKnee, Offset(0.08f, 0.22f), p), rFoot = foot,
                rElbow = lerp(base.rElbow, Offset(0.18f, 0.62f), p), rHand = lerp(base.rHand, foot, p),
                lElbow = lerp(base.lElbow, Offset(-0.22f, 0.80f), p), lHand = lerp(base.lHand, Offset(-0.36f, 0.86f), p),
            )
        }
        // Todo el cuerpo sube sobre las puntas (talones se despegan)
        ExerciseVisual.HEEL_RAISE -> base.lift(0.07f * p)
        // Manos entrelazadas arriba + puntillas
        ExerciseVisual.FULL_REACH -> base.lift(0.06f * p).let {
            val top = Offset(0f, lerp(1.0f, 1.22f, p) + 0.06f * p)
            it.copy(
                lElbow = lerp(it.lElbow, Offset(-0.06f, 1.04f + 0.06f * p), p), lHand = lerp(it.lHand, top, p),
                rElbow = lerp(it.rElbow, Offset(0.06f, 1.04f + 0.06f * p), p), rHand = lerp(it.rHand, top, p),
            )
        }
        // Cadera baja y atrás, rodillas flexionadas, brazos al frente
        ExerciseVisual.SQUAT -> {
            val drop = 0.20f * p
            base.copy(
                hip = Offset(0f, 0.50f - drop), neck = Offset(0f, 0.86f - drop), head = Offset(0f, 0.96f - drop),
                lKnee = lerp(base.lKnee, Offset(-0.16f, 0.22f), p), rKnee = lerp(base.rKnee, Offset(0.16f, 0.22f), p),
                lElbow = lerp(base.lElbow, Offset(-0.12f, 0.70f - drop), p), lHand = lerp(base.lHand, Offset(-0.08f, 0.66f - drop * 0.5f), p),
                rElbow = lerp(base.rElbow, Offset(0.12f, 0.70f - drop), p), rHand = lerp(base.rHand, Offset(0.08f, 0.66f - drop * 0.5f), p),
            )
        }
        // Brazos en cruz trazando pequeños círculos
        ExerciseVisual.ARM_CIRCLES -> {
            val a = p * 2f * PI.toFloat()
            val dx = cos(a) * 0.05f
            val dy = sin(a) * 0.05f
            base.copy(
                lElbow = Offset(-0.20f, 0.83f), lHand = Offset(-0.38f + dx, 0.83f + dy),
                rElbow = Offset(0.20f, 0.83f), rHand = Offset(0.38f - dx, 0.83f + dy),
            )
        }
        // Vista lateral: la cabeza se desplaza hacia atrás (barbilla adentro)
        ExerciseVisual.CHIN_TUCK -> base.copy(
            head = Offset(lerp(0.07f, -0.01f, p), 0.96f),
            lElbow = Offset(0.02f, 0.68f), lHand = Offset(0.10f, 0.56f),
            rElbow = Offset(0.02f, 0.68f), rHand = Offset(0.10f, 0.56f),
            lKnee = Offset(0.01f, 0.25f), lFoot = Offset(0f, 0f), rKnee = Offset(0.01f, 0.25f), rFoot = Offset(0.04f, 0f),
        )
        // Rodillas alternas + brazos opuestos
        ExerciseVisual.MARCH -> {
            val l = if (p < 0.5f) p * 2f else 0f
            val r = if (p >= 0.5f) (p - 0.5f) * 2f else 0f
            val ul = sin(l * PI.toFloat()); val ur = sin(r * PI.toFloat())
            base.copy(
                lKnee = lerp(base.lKnee, Offset(-0.07f, 0.50f), ul), lFoot = lerp(base.lFoot, Offset(-0.09f, 0.22f), ul),
                rKnee = lerp(base.rKnee, Offset(0.07f, 0.50f), ur), rFoot = lerp(base.rFoot, Offset(0.09f, 0.22f), ur),
                rHand = lerp(base.rHand, Offset(0.12f, 0.78f), ul), lHand = lerp(base.lHand, Offset(-0.12f, 0.78f), ur),
            )
        }
        // Golpes alternos al frente (el brazo se extiende hacia el espectador → mano sube al centro)
        ExerciseVisual.PUNCH -> {
            val l = if (p < 0.5f) sin(p * 2f * PI.toFloat()) else 0f
            val r = if (p >= 0.5f) sin((p - 0.5f) * 2f * PI.toFloat()) else 0f
            val guardL = Offset(-0.06f, 0.80f); val guardR = Offset(0.06f, 0.80f)
            base.copy(
                lElbow = Offset(-0.14f, 0.70f), lHand = lerp(guardL, Offset(-0.03f, 0.84f), l),
                rElbow = Offset(0.14f, 0.70f), rHand = lerp(guardR, Offset(0.03f, 0.84f), r),
                lKnee = Offset(-0.10f, 0.24f), lFoot = Offset(-0.13f, 0f), rKnee = Offset(0.10f, 0.24f), rFoot = Offset(0.13f, 0f),
            )
        }
        // Manos en la cintura, la cadera se mueve de lado a lado
        ExerciseVisual.HIP_SWAY -> {
            val dx = (p - 0.5f) * 0.14f
            val hip = Offset(dx, 0.50f)
            base.copy(
                hip = hip,
                lElbow = Offset(-0.16f, 0.66f), lHand = Offset(dx - 0.06f, 0.54f),
                rElbow = Offset(0.16f, 0.66f), rHand = Offset(dx + 0.06f, 0.54f),
                lKnee = Offset(dx * 0.5f - 0.07f, 0.25f), rKnee = Offset(dx * 0.5f + 0.07f, 0.25f),
            )
        }
        else -> base
    }
}

/** Sube todo el cuerpo [dy] manteniendo las puntas de los pies en el suelo. */
private fun Pose.lift(dy: Float) = copy(
    hip = hip.up(dy), neck = neck.up(dy), head = head.up(dy),
    lElbow = lElbow.up(dy), lHand = lHand.up(dy), rElbow = rElbow.up(dy), rHand = rHand.up(dy),
    lKnee = lKnee.up(dy), lFoot = lFoot.up(dy), rKnee = rKnee.up(dy), rFoot = rFoot.up(dy),
)

private fun DrawScope.stickman(v: ExerciseVisual, c: Color, accent: Color, p: Float) {
    val pose = poseFor(v, p)
    val k = size.height * 0.76f // unidad de cuerpo
    val ground = size.height * 0.95f
    val cx = size.width / 2f
    fun pt(o: Offset) = Offset(cx + o.x * k, ground - o.y * k)
    val w = k * 0.035f
    fun bone(a: Offset, b: Offset) = drawLine(c, pt(a), pt(b), w, StrokeCap.Round)

    // suelo
    drawLine(accent.copy(alpha = 0.4f), Offset(cx - k * 0.45f, ground), Offset(cx + k * 0.45f, ground), w * 0.6f, StrokeCap.Round)
    bone(pose.hip, pose.neck)
    bone(pose.hip, pose.lKnee); bone(pose.lKnee, pose.lFoot)
    bone(pose.hip, pose.rKnee); bone(pose.rKnee, pose.rFoot)
    val shoulder = Offset(pose.neck.x, pose.neck.y - 0.03f)
    bone(shoulder, pose.lElbow); bone(pose.lElbow, pose.lHand)
    bone(shoulder, pose.rElbow); bone(pose.rElbow, pose.rHand)
    drawCircle(c, k * 0.075f, pt(pose.head), style = Stroke(width = w))
    // marcador del punto de trabajo
    val focus = when (v) {
        ExerciseVisual.CHIN_TUCK -> pose.head
        ExerciseVisual.HEEL_RAISE, ExerciseVisual.FULL_REACH -> Offset(0f, 0.03f)
        ExerciseVisual.SQUAT, ExerciseVisual.HIP_SWAY -> pose.hip
        ExerciseVisual.KNEE_TO_CHEST, ExerciseVisual.MARCH -> null
        else -> null
    }
    focus?.let { drawCircle(accent.copy(alpha = 0.35f), k * 0.06f, pt(it)) }
}
