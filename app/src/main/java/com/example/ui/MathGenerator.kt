package com.example.ui

import kotlin.random.Random

enum class MathDifficulty {
    BEGINNER, INTERMEDIATE, MASTER
}

enum class OperationType {
    ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, PERCENTAGE, MIXED
}

data class GeneratedEquation(
    val expression: String,
    val answer: String,
    val category: String
)

object MathGenerator {

    fun generate(
        difficulty: MathDifficulty,
        operationType: OperationType
    ): GeneratedEquation {
        val op = if (operationType == OperationType.MIXED) {
            listOf(
                OperationType.ADDITION,
                OperationType.SUBTRACTION,
                OperationType.MULTIPLICATION,
                OperationType.DIVISION,
                OperationType.PERCENTAGE
            ).random()
        } else {
            operationType
        }

        return when (op) {
            OperationType.ADDITION -> generateAddition(difficulty)
            OperationType.SUBTRACTION -> generateSubtraction(difficulty)
            OperationType.MULTIPLICATION -> generateMultiplication(difficulty)
            OperationType.DIVISION -> generateDivision(difficulty)
            OperationType.PERCENTAGE -> generatePercentage(difficulty)
            else -> generateAddition(difficulty)
        }
    }

    private fun generateAddition(difficulty: MathDifficulty): GeneratedEquation {
        val (a, b) = when (difficulty) {
            MathDifficulty.BEGINNER -> {
                Pair(Random.nextInt(2, 20), Random.nextInt(2, 20))
            }
            MathDifficulty.INTERMEDIATE -> {
                Pair(Random.nextInt(15, 99), Random.nextInt(15, 99))
            }
            MathDifficulty.MASTER -> {
                Pair(Random.nextInt(100, 999), Random.nextInt(10, 99))
            }
        }
        return GeneratedEquation("$a + $b", (a + b).toString(), "Addition")
    }

    private fun generateSubtraction(difficulty: MathDifficulty): GeneratedEquation {
        val (a, b) = when (difficulty) {
            MathDifficulty.BEGINNER -> {
                val x = Random.nextInt(5, 25)
                val y = Random.nextInt(1, x)
                Pair(x, y)
            }
            MathDifficulty.INTERMEDIATE -> {
                val x = Random.nextInt(30, 150)
                val y = Random.nextInt(10, x)
                Pair(x, y)
            }
            MathDifficulty.MASTER -> {
                val x = Random.nextInt(100, 1000)
                val y = Random.nextInt(50, x)
                Pair(x, y)
            }
        }
        return GeneratedEquation("$a − $b", (a - b).toString(), "Subtraction")
    }

    private fun generateMultiplication(difficulty: MathDifficulty): GeneratedEquation {
        val (a, b) = when (difficulty) {
            MathDifficulty.BEGINNER -> {
                Pair(Random.nextInt(2, 10), Random.nextInt(2, 10))
            }
            MathDifficulty.INTERMEDIATE -> {
                Pair(Random.nextInt(2, 12), Random.nextInt(11, 40))
            }
            MathDifficulty.MASTER -> {
                Pair(Random.nextInt(11, 30), Random.nextInt(11, 50))
            }
        }
        return GeneratedEquation("$a × $b", (a * b).toString(), "Multiplication")
    }

    private fun generateDivision(difficulty: MathDifficulty): GeneratedEquation {
        val (dividend, divisor) = when (difficulty) {
            MathDifficulty.BEGINNER -> {
                val divisorVal = Random.nextInt(2, 9)
                val quotient = Random.nextInt(2, 10)
                Pair(divisorVal * quotient, divisorVal)
            }
            MathDifficulty.INTERMEDIATE -> {
                val divisorVal = Random.nextInt(3, 12)
                val quotient = Random.nextInt(10, 25)
                Pair(divisorVal * quotient, divisorVal)
            }
            MathDifficulty.MASTER -> {
                val divisorVal = Random.nextInt(4, 25)
                val quotient = Random.nextInt(12, 50)
                Pair(divisorVal * quotient, divisorVal)
            }
        }
        return GeneratedEquation("$dividend ÷ $divisor", (dividend / divisor).toString(), "Division")
    }

    private fun generatePercentage(difficulty: MathDifficulty): GeneratedEquation {
        return when (difficulty) {
            MathDifficulty.BEGINNER -> {
                val base = listOf(10, 20, 30, 40, 50, 60, 70, 80, 90, 100).random()
                val pct = listOf(10, 50, 100).random()
                GeneratedEquation("$pct% of $base", ((base * pct) / 100).toString(), "Percentages")
            }
            MathDifficulty.INTERMEDIATE -> {
                val base = listOf(20, 40, 60, 80, 100, 120, 150, 200).random()
                val pct = listOf(25, 20, 15, 75).random()
                val ans = (base * pct) / 100.0
                val ansStr = if (ans % 1.0 == 0.0) ans.toInt().toString() else ans.toString()
                GeneratedEquation("$pct% of $base", ansStr, "Percentages")
            }
            MathDifficulty.MASTER -> {
                val base = Random.nextInt(50, 500)
                val pct = listOf(15, 35, 45, 65, 85).random()
                val ans = (base * pct) / 100.0
                val ansStr = if (ans % 1.0 == 0.0) ans.toInt().toString() else ans.toString()
                GeneratedEquation("$pct% of $base", ansStr, "Percentages")
            }
        }
    }
}
