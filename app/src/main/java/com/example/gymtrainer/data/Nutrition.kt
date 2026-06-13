package com.example.gymtrainer.data

import kotlin.math.roundToInt

/**
 * Evidence-based nutrition targets.
 *  - BMR via the Mifflin-St Jeor equation
 *  - TDEE = BMR x activity multiplier
 *  - Calories adjusted for goal (cut / maintain / bulk)
 *  - Protein scaled to bodyweight, fat to a share of calories, carbs fill the rest
 */
object Nutrition {

    val activityLevels = listOf(
        "Sedentary (little exercise)" to 1.2,
        "Light (1-3 days/week)" to 1.375,
        "Moderate (3-5 days/week)" to 1.55,
        "Very active (6-7 days/week)" to 1.725,
        "Athlete (2x/day)" to 1.9
    )

    val goals = listOf("Cut", "Maintain", "Bulk")

    data class Targets(
        val bmr: Int,
        val tdee: Int,
        val calories: Int,
        val proteinG: Int,
        val carbsG: Int,
        val fatG: Int
    )

    fun compute(p: UserProfile): Targets {
        val s = if (p.sex.equals("Male", true)) 5.0 else -161.0
        val bmr = 10 * p.weightKg + 6.25 * p.heightCm - 5 * p.age + s
        val mult = activityLevels.firstOrNull { it.first == p.activity }?.second ?: 1.55
        val tdee = bmr * mult

        val calories = when (p.goal) {
            "Cut" -> tdee * 0.80
            "Bulk" -> tdee * 1.12
            else -> tdee
        }

        // Protein: 2.0 g/kg (a touch higher on a cut to protect muscle)
        val proteinPerKg = if (p.goal == "Cut") 2.2 else 2.0
        val proteinG = p.weightKg * proteinPerKg
        // Fat: 25% of calories
        val fatG = (calories * 0.25) / 9.0
        // Carbs: whatever calories remain
        val carbsCals = calories - (proteinG * 4) - (fatG * 9)
        val carbsG = (carbsCals / 4.0).coerceAtLeast(0.0)

        return Targets(
            bmr = bmr.roundToInt(),
            tdee = tdee.roundToInt(),
            calories = calories.roundToInt(),
            proteinG = proteinG.roundToInt(),
            carbsG = carbsG.roundToInt(),
            fatG = fatG.roundToInt()
        )
    }
}
