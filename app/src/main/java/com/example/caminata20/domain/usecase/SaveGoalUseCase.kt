package com.example.caminata20.domain.usecase

import com.example.caminata20.domain.model.Profile
import com.example.caminata20.domain.repository.GoalHistoryRepository
import com.example.caminata20.domain.repository.ProfileRepository
import com.example.caminata20.domain.time.DayWindowCalculator
import javax.inject.Inject

/**
 * Guarda la meta diaria y, si cambió, registra desde qué día rige
 * para no reevaluar los días ya cerrados con la meta nueva (HU-17, Adenda C2).
 */
class SaveGoalUseCase @Inject constructor(
    private val profiles: ProfileRepository,
    private val history: GoalHistoryRepository,
    private val dayWindow: DayWindowCalculator,
    private val clock: Clock,
) {
    suspend operator fun invoke(oldGoal: Int, newGoal: Int) {
        val goal = newGoal.coerceIn(Profile.GOAL_RANGE)
        profiles.saveGoal(goal)
        if (oldGoal != goal) {
            history.recordChange(dayWindow.startOfDay(clock.now()), oldGoal, goal)
        }
    }
}
