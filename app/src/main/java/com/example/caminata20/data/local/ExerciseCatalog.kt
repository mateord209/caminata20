package com.example.caminata20.data.local

import com.example.caminata20.domain.model.Exercise
import com.example.caminata20.domain.model.PauseType

/** HU-16: catálogo local (12 ejercicios por tipo de pausa, 48 en total). */
object ExerciseCatalog {
    val all: List<Exercise> = listOf(
        Exercise("est_01", PauseType.ESTIRAMIENTO, "Estiramiento de cuello", "Cuello y hombros", 120, listOf("Siéntate erguido con los hombros relajados.", "Inclina la cabeza hacia el hombro derecho 15 s.", "Repite hacia el lado izquierdo 15 s.", "Gira suavemente el cuello en círculos 5 veces.")),
        Exercise("est_02", PauseType.ESTIRAMIENTO, "Rotación de hombros", "Hombros", 60, listOf("Ponte de pie o sentado con la espalda recta.", "Eleva los hombros hacia las orejas.", "Llévalos hacia atrás y abajo en círculo 10 veces.", "Invierte el sentido 10 veces.")),
        Exercise("est_03", PauseType.ESTIRAMIENTO, "Estiramiento de muñecas", "Muñecas y antebrazos", 60, listOf("Extiende el brazo derecho con la palma hacia afuera.", "Con la otra mano lleva los dedos hacia ti 15 s.", "Gira la palma hacia abajo y repite 15 s.", "Cambia de brazo.")),
        Exercise("est_04", PauseType.ESTIRAMIENTO, "Torsión de espalda sentado", "Espalda baja", 90, listOf("Siéntate al borde de la silla, pies apoyados.", "Gira el tronco a la derecha sujetando el respaldo 20 s.", "Vuelve al centro y respira.", "Repite hacia la izquierda 20 s.")),
        Exercise("est_05", PauseType.ESTIRAMIENTO, "Estiramiento de pecho", "Pecho y hombros", 60, listOf("Entrelaza las manos detrás de la espalda.", "Estira los brazos y abre el pecho.", "Mantén 20 s respirando profundo.", "Suelta despacio y repite una vez.")),
        Exercise("est_06", PauseType.ESTIRAMIENTO, "Estiramiento lateral", "Costados", 60, listOf("De pie, eleva el brazo derecho sobre la cabeza.", "Inclínate hacia la izquierda 15 s.", "Vuelve al centro.", "Repite con el brazo izquierdo.")),
        Exercise("est_07", PauseType.ESTIRAMIENTO, "Estiramiento de tríceps", "Brazos", 60, listOf("Lleva la mano derecha detrás de la nuca.", "Con la mano izquierda empuja el codo suavemente 15 s.", "Cambia de brazo 15 s.", "Sacude los brazos para soltar.")),
        Exercise("est_08", PauseType.ESTIRAMIENTO, "Flexión hacia adelante", "Espalda e isquiotibiales", 90, listOf("De pie, separa los pies al ancho de cadera.", "Flexiona el tronco hacia adelante con rodillas suaves.", "Deja colgar brazos y cabeza 20 s.", "Sube vértebra por vértebra.")),
        Exercise("est_09", PauseType.ESTIRAMIENTO, "Estiramiento de cuádriceps", "Piernas", 90, listOf("Apóyate en la pared o en la silla.", "Toma el pie derecho y llévalo al glúteo 20 s.", "Mantén las rodillas juntas.", "Cambia de pierna 20 s.")),
        Exercise("est_10", PauseType.ESTIRAMIENTO, "Elevación de talones", "Pantorrillas", 60, listOf("De pie, apoya las manos en el escritorio.", "Sube en puntas de pie.", "Baja lentamente.", "Repite 15 veces.")),
        Exercise("est_11", PauseType.ESTIRAMIENTO, "Gato-vaca sentado", "Columna", 60, listOf("Siéntate con las manos sobre las rodillas.", "Al inhalar arquea la espalda y mira arriba.", "Al exhalar redondea la espalda y mira al ombligo.", "Repite 8 veces.")),
        Exercise("est_12", PauseType.ESTIRAMIENTO, "Apertura de cadera sentado", "Cadera", 90, listOf("Siéntate y cruza el tobillo derecho sobre la rodilla izquierda.", "Inclina el tronco hacia adelante 20 s.", "Vuelve con la espalda recta.", "Cambia de pierna.")),
        Exercise("vis_01", PauseType.DESCANSO_VISUAL, "Regla 20-20-20", "Ojos", 20, listOf("Aparta la vista de la pantalla.", "Mira un objeto a unos 6 m (20 pies).", "Mantén la mirada 20 segundos.", "Parpadea suavemente y vuelve.")),
        Exercise("vis_02", PauseType.DESCANSO_VISUAL, "Parpadeo consciente", "Ojos", 30, listOf("Cierra los ojos suavemente 2 s.", "Ábrelos y parpadea rápido 10 veces.", "Repite el ciclo 3 veces.", "Respira tranquilo.")),
        Exercise("vis_03", PauseType.DESCANSO_VISUAL, "Palming", "Ojos", 60, listOf("Frota las palmas hasta sentirlas tibias.", "Cubre los ojos cerrados sin presionar.", "Respira profundo 30 s en la oscuridad.", "Retira las manos despacio.")),
        Exercise("vis_04", PauseType.DESCANSO_VISUAL, "Enfoque cerca-lejos", "Ojos", 60, listOf("Pon el pulgar a 25 cm de tu cara.", "Enfoca el pulgar 5 s.", "Enfoca un objeto lejano 5 s.", "Alterna 10 veces.")),
        Exercise("vis_05", PauseType.DESCANSO_VISUAL, "Ocho imaginario", "Músculos oculares", 45, listOf("Imagina un 8 acostado frente a ti.", "Recórrelo lentamente con la mirada.", "Hazlo 5 veces en un sentido.", "Luego 5 veces en el otro.")),
        Exercise("vis_06", PauseType.DESCANSO_VISUAL, "Mirada en reloj", "Músculos oculares", 45, listOf("Sin mover la cabeza mira a las 12.", "Recorre las horas del reloj con los ojos.", "Da una vuelta completa en cada sentido.", "Cierra los ojos 5 s al terminar.")),
        Exercise("vis_07", PauseType.DESCANSO_VISUAL, "Mirar por la ventana", "Ojos", 60, listOf("Acércate a una ventana o espacio abierto.", "Observa el horizonte o el punto más lejano.", "Identifica 3 detalles lejanos.", "Mantén 1 minuto sin pantalla.")),
        Exercise("vis_08", PauseType.DESCANSO_VISUAL, "Masaje de sienes", "Sienes y cejas", 60, listOf("Coloca los dedos en las sienes.", "Haz círculos suaves 15 s.", "Presiona suavemente bajo las cejas.", "Termina con los ojos cerrados 10 s.")),
        Exercise("vis_09", PauseType.DESCANSO_VISUAL, "Movimiento lateral de ojos", "Músculos oculares", 30, listOf("Mira a la derecha sin girar la cabeza 2 s.", "Mira a la izquierda 2 s.", "Repite 10 veces.", "Parpadea al terminar.")),
        Exercise("vis_10", PauseType.DESCANSO_VISUAL, "Movimiento vertical de ojos", "Músculos oculares", 30, listOf("Mira hacia arriba 2 s.", "Mira hacia abajo 2 s.", "Repite 10 veces.", "Cierra los ojos y relaja.")),
        Exercise("vis_11", PauseType.DESCANSO_VISUAL, "Ojos cerrados y respiración", "Ojos", 60, listOf("Cierra los ojos.", "Inhala 4 s y exhala 6 s.", "Relaja la frente y la mandíbula.", "Abre los ojos lentamente al minuto.")),
        Exercise("vis_12", PauseType.DESCANSO_VISUAL, "Ajuste de pantalla", "Ergonomía visual", 30, listOf("Aleja la pantalla a un brazo de distancia.", "Ajusta el borde superior a la altura de tus ojos.", "Baja el brillo si deslumbra.", "Parpadea 5 veces.")),
        Exercise("res_01", PauseType.RESPIRACION, "Respiración 4-7-8", "Respiración", 90, listOf("Exhala todo el aire por la boca.", "Inhala por la nariz contando 4.", "Retén el aire contando 7.", "Exhala por la boca contando 8. Repite 4 ciclos.")),
        Exercise("res_02", PauseType.RESPIRACION, "Respiración en caja", "Respiración", 90, listOf("Inhala contando 4.", "Retén contando 4.", "Exhala contando 4.", "Espera contando 4. Repite 5 veces.")),
        Exercise("res_03", PauseType.RESPIRACION, "Respiración diafragmática", "Abdomen", 120, listOf("Pon una mano en el pecho y otra en el abdomen.", "Inhala llevando el aire al abdomen.", "El pecho casi no debe moverse.", "Exhala lento. Repite 8 veces.")),
        Exercise("res_04", PauseType.RESPIRACION, "Respiración alterna", "Respiración", 90, listOf("Tapa la fosa nasal derecha e inhala por la izquierda.", "Tapa la izquierda y exhala por la derecha.", "Inhala por la derecha.", "Exhala por la izquierda. Repite 6 ciclos.")),
        Exercise("res_05", PauseType.RESPIRACION, "Suspiro fisiológico", "Respiración", 45, listOf("Inhala por la nariz profundo.", "Haz una segunda inhalación corta.", "Exhala largo por la boca.", "Repite 3 veces.")),
        Exercise("res_06", PauseType.RESPIRACION, "Respiración coherente", "Ritmo cardíaco", 120, listOf("Inhala 5 segundos.", "Exhala 5 segundos.", "Mantén el ritmo constante.", "Continúa 2 minutos.")),
        Exercise("res_07", PauseType.RESPIRACION, "Respiración con hombros", "Hombros y respiración", 60, listOf("Inhala subiendo los hombros.", "Retén 2 s arriba.", "Exhala dejando caer los hombros.", "Repite 6 veces.")),
        Exercise("res_08", PauseType.RESPIRACION, "Exhalación prolongada", "Relajación", 60, listOf("Inhala contando 3.", "Exhala contando 6.", "Siente cómo baja la tensión.", "Repite 8 veces.")),
        Exercise("res_09", PauseType.RESPIRACION, "Respiración de abeja", "Relajación", 60, listOf("Tapa suavemente los oídos.", "Inhala por la nariz.", "Exhala emitiendo un zumbido 'mmm'.", "Repite 5 veces.")),
        Exercise("res_10", PauseType.RESPIRACION, "Escaneo corporal breve", "Mente y cuerpo", 120, listOf("Cierra los ojos y respira normal.", "Recorre mentalmente de la cabeza a los pies.", "Suelta la tensión donde la notes.", "Termina con 3 respiraciones profundas.")),
        Exercise("res_11", PauseType.RESPIRACION, "Respiración contando", "Concentración", 60, listOf("Inhala y cuenta 1 al exhalar.", "Continúa hasta 10.", "Si te distraes, vuelve a 1.", "Haz 2 rondas.")),
        Exercise("res_12", PauseType.RESPIRACION, "Respiración con brazos", "Pecho", 60, listOf("Inhala abriendo los brazos en cruz.", "Exhala cruzándolos frente al pecho.", "Mantén un ritmo lento.", "Repite 8 veces.")),
        Exercise("hid_01", PauseType.HIDRATACION, "Vaso de agua", "Hidratación", 60, listOf("Levántate y ve por agua.", "Sirve un vaso (250 ml).", "Bébelo a sorbos, sin prisa.", "Vuelve caminando.")),
        Exercise("hid_02", PauseType.HIDRATACION, "Rellena tu botella", "Hidratación", 90, listOf("Revisa tu botella.", "Camina hasta el dispensador.", "Rellénala por completo.", "Toma unos sorbos antes de volver.")),
        Exercise("hid_03", PauseType.HIDRATACION, "Agua con estiramiento", "Hidratación y movilidad", 90, listOf("Toma un vaso de agua.", "Mientras bebes, ponte de pie.", "Estira los brazos hacia arriba 10 s.", "Termina el vaso.")),
        Exercise("hid_04", PauseType.HIDRATACION, "Infusión sin azúcar", "Hidratación", 120, listOf("Prepara una infusión o té suave.", "Espera a que entibie caminando un poco.", "Bébela lentamente.", "Evita añadir azúcar.")),
        Exercise("hid_05", PauseType.HIDRATACION, "Agua con fruta", "Hidratación", 60, listOf("Añade limón, pepino o menta al agua.", "Remueve.", "Bebe un vaso completo.", "Deja la botella a la vista.")),
        Exercise("hid_06", PauseType.HIDRATACION, "Sorbos conscientes", "Hidratación", 45, listOf("Toma 5 sorbos de agua.", "Siente la temperatura del agua.", "Respira entre sorbos.", "Deja el vaso lejos del teclado.")),
        Exercise("hid_07", PauseType.HIDRATACION, "Caminata al dispensador", "Hidratación y movimiento", 120, listOf("Elige el dispensador más lejano.", "Camina a paso ligero.", "Bebe un vaso allí.", "Regresa por las escaleras si puedes.")),
        Exercise("hid_08", PauseType.HIDRATACION, "Chequeo de hidratación", "Autocuidado", 30, listOf("Pregúntate: ¿tengo sed o boca seca?", "Revisa cuánta agua llevas hoy.", "Bebe medio vaso ahora.", "Programa el próximo vaso.")),
        Exercise("hid_09", PauseType.HIDRATACION, "Agua y postura", "Hidratación y postura", 60, listOf("Bebe un vaso de agua de pie.", "Alinea orejas, hombros y cadera.", "Respira profundo 3 veces.", "Vuelve a sentarte bien apoyado.")),
        Exercise("hid_10", PauseType.HIDRATACION, "Snack hidratante", "Nutrición", 120, listOf("Elige fruta con agua (sandía, naranja, pepino).", "Cómela con calma lejos de la pantalla.", "Acompáñala con agua.", "Lávate las manos al volver.")),
        Exercise("hid_11", PauseType.HIDRATACION, "Agua tibia", "Hidratación", 60, listOf("Sirve agua tibia.", "Bébela despacio.", "Relaja hombros mientras bebes.", "Termina con 2 respiraciones profundas.")),
        Exercise("hid_12", PauseType.HIDRATACION, "Meta de vasos", "Hidratación", 30, listOf("Cuenta los vasos que llevas hoy.", "Bebe uno más ahora.", "Apúntalo mentalmente.", "La meta sugerida es 8 vasos al día.")),
    )

    fun byType(type: PauseType): List<Exercise> = all.filter { it.type == type }

    fun byId(id: String?): Exercise? = all.firstOrNull { it.id == id }

    /** Rota por el catálogo del tipo para no repetir el mismo ejercicio. */
    fun next(type: PauseType, lastId: String?): Exercise {
        val list = byType(type)
        val idx = list.indexOfFirst { it.id == lastId }
        return list[(idx + 1).mod(list.size)]
    }
}
