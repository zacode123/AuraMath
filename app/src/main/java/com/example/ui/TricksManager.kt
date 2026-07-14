package com.example.ui

import kotlin.random.Random

data class MathTrick(
    val id: String,
    val category: String, // Addition, Subtraction, Multiplication, Division, Percentages
    val title: String,
    val summary: String,
    val conceptExplanation: String,
    val walkthroughProblem: String,
    val walkthroughSteps: List<WalkthroughStep>,
    val finalWalkthroughAnswer: String,
    val generateSandboxQuestion: () -> SandboxQuestion
)

data class WalkthroughStep(
    val instruction: String,
    val expectedInput: String
)

data class SandboxQuestion(
    val expression: String,
    val correctAnswer: String
)

object TricksManager {
    val tricks = listOf(
        MathTrick(
            id = "mult_by_11",
            category = "Multiplication",
            title = "Multiply by 11",
            summary = "Add the digits and insert in the middle.",
            conceptExplanation = "To multiply any 2-digit number by 11, add its two digits together. If the sum is single-digit, put it in the middle. If it is 10 or more, carry the 1 over. E.g., 42 * 11 -> 4+2=6 -> 462. 78 * 11 -> 7+8=15 -> (7+1) 5 8 -> 858.",
            walkthroughProblem = "53 × 11",
            walkthroughSteps = listOf(
                WalkthroughStep("Step 1: Add the digits of 53 together (5 + 3). What is the sum?", "8"),
                WalkthroughStep("Step 2: Place that sum (8) in the middle of 5 and 3. What is the final product?", "583")
            ),
            finalWalkthroughAnswer = "583",
            generateSandboxQuestion = {
                val num = Random.nextInt(12, 90)
                SandboxQuestion("$num × 11", (num * 11).toString())
            }
        ),
        MathTrick(
            id = "square_ends_5",
            category = "Multiplication",
            title = "Square Numbers Ending in 5",
            summary = "Multiply tens by (tens + 1) and append 25.",
            conceptExplanation = "To square any number ending in 5 (like 65), take the tens digit (6), multiply it by the next consecutive integer (6 * 7 = 42), and then append 25 at the end. E.g., 65² -> 6 * 7 = 42 -> 4225.",
            walkthroughProblem = "75²",
            walkthroughSteps = listOf(
                WalkthroughStep("Step 1: Take the tens digit (7) and multiply by (7 + 1 = 8). What is the result?", "56"),
                WalkthroughStep("Step 2: Append 25 to the previous step's result (56). What is the final square?", "5625")
            ),
            finalWalkthroughAnswer = "5625",
            generateSandboxQuestion = {
                val base = listOf(15, 25, 35, 45, 55, 65, 75, 85, 95).random()
                SandboxQuestion("$base²", (base * base).toString())
            }
        ),
        MathTrick(
            id = "sub_from_1000",
            category = "Subtraction",
            title = "Subtracting from 1000",
            summary = "Subtract all from 9, and the last from 10.",
            conceptExplanation = "To subtract a 3-digit number from 1000, subtract the first digit from 9, the second from 9, and the third from 10. E.g., 1000 - 356 -> 9-3=6, 9-5=4, 10-6=4 -> 644.",
            walkthroughProblem = "1000 - 482",
            walkthroughSteps = listOf(
                WalkthroughStep("Step 1: Subtract the hundreds digit (4) from 9.", "5"),
                WalkthroughStep("Step 2: Subtract the tens digit (8) from 9.", "1"),
                WalkthroughStep("Step 3: Subtract the units digit (2) from 10.", "8"),
                WalkthroughStep("Step 4: Combine the three digits into a single number.", "518")
            ),
            finalWalkthroughAnswer = "518",
            generateSandboxQuestion = {
                val num = Random.nextInt(101, 999)
                SandboxQuestion("1000 - $num", (1000 - num).toString())
            }
        ),
        MathTrick(
            id = "divide_by_5",
            category = "Division",
            title = "Quick Division by 5",
            summary = "Double the number and divide by 10.",
            conceptExplanation = "To divide any number by 5, first double the number, then divide by 10 (move the decimal point one position to the left). E.g., 145 / 5 -> 145 * 2 = 290 -> 29.0.",
            walkthroughProblem = "84 ÷ 5",
            walkthroughSteps = listOf(
                WalkthroughStep("Step 1: Double the number 84. What do you get?", "168"),
                WalkthroughStep("Step 2: Divide 168 by 10 (use decimal format).", "16.8")
            ),
            finalWalkthroughAnswer = "16.8",
            generateSandboxQuestion = {
                val num = Random.nextInt(15, 199)
                SandboxQuestion("$num ÷ 5", (num / 5.0).toString().removeSuffix(".0"))
            }
        ),
        MathTrick(
            id = "percent_15",
            category = "Percentages",
            title = "Finding 15% Tips Fast",
            summary = "Find 10%, add half of that (5%).",
            conceptExplanation = "To find 15% of a number: find 10% first (divide by 10). Then find 5% (which is exactly half of the 10% value). Finally, add them. E.g., 15% of 80 -> 10% is 8 -> 5% is 4 -> 8 + 4 = 12.",
            walkthroughProblem = "15% of 160",
            walkthroughSteps = listOf(
                WalkthroughStep("Step 1: Find 10% of 160 (divide by 10).", "16"),
                WalkthroughStep("Step 2: Find 5% (halve the 10% result of 16).", "8"),
                WalkthroughStep("Step 3: Sum the 10% and 5% values.", "24")
            ),
            finalWalkthroughAnswer = "24",
            generateSandboxQuestion = {
                val num = listOf(40, 60, 80, 120, 140, 180, 200, 240, 300).random()
                SandboxQuestion("15% of $num", (num * 0.15).toInt().toString())
            }
        )
    )

    fun getTrickById(id: String): MathTrick? {
        return tricks.find { it.id == id }
    }
}
