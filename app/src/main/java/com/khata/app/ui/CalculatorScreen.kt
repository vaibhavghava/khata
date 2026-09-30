package com.khata.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

private val MC = MathContext(15, RoundingMode.HALF_UP)
private const val OPS = "+-×÷"

/**
 * Evaluates an expression made of numbers, + - × ÷ and postfix %.
 * Returns the result as plain text, "Error" for division by zero, or null if incomplete.
 * Percent: "200+10%" = 220 (10% of the running total), "50%" = 0.5, "200×10%" = 20.
 */
private fun evaluate(raw: String): String? {
    var s = raw
    while (s.isNotEmpty() && s.last() in OPS) s = s.dropLast(1)
    var negative = false
    if (s.startsWith("-")) { negative = true; s = s.drop(1) }
    if (s.isEmpty()) return null

    val nums = ArrayList<BigDecimal>()
    val pcts = ArrayList<Boolean>()
    val ops = ArrayList<Char>()
    val sb = StringBuilder()
    try {
        for (ch in s) {
            when {
                ch.isDigit() || ch == '.' -> sb.append(ch)
                ch == '%' -> {
                    if (sb.isEmpty()) return null
                    nums.add(BigDecimal(sb.toString())); pcts.add(true); sb.clear()
                }
                ch in OPS -> {
                    if (sb.isNotEmpty()) { nums.add(BigDecimal(sb.toString())); pcts.add(false); sb.clear() }
                    ops.add(ch)
                }
                else -> return null
            }
        }
        if (sb.isNotEmpty()) { nums.add(BigDecimal(sb.toString())); pcts.add(false) }
    } catch (e: NumberFormatException) {
        return null
    }
    if (nums.isEmpty() || nums.size != ops.size + 1) return null
    if (negative) nums[0] = nums[0].negate()

    val hundred = BigDecimal(100)
    // Pass 1: resolve × and ÷ into additive terms.
    val termOps = ArrayList<Char>()
    val termVals = ArrayList<BigDecimal>()
    val termPct = ArrayList<Boolean>()
    var cur = nums[0]
    var curPct = pcts[0]
    var termOp = '+'
    for (k in ops.indices) {
        val op = ops[k]
        val n = nums[k + 1]
        val p = pcts[k + 1]
        if (op == '×' || op == '÷') {
            val a = if (curPct) cur.divide(hundred, MC) else cur
            val b = if (p) n.divide(hundred, MC) else n
            if (op == '÷' && b.signum() == 0) return "Error"
            cur = if (op == '×') a.multiply(b, MC) else a.divide(b, MC)
            curPct = false
        } else {
            termOps.add(termOp); termVals.add(cur); termPct.add(curPct)
            termOp = op; cur = n; curPct = p
        }
    }
    termOps.add(termOp); termVals.add(cur); termPct.add(curPct)

    // Pass 2: add / subtract.
    var result = if (termPct[0]) termVals[0].divide(hundred, MC) else termVals[0]
    for (j in 1 until termVals.size) {
        val add = if (termPct[j]) result.multiply(termVals[j], MC).divide(hundred, MC) else termVals[j]
        result = if (termOps[j] == '+') result.add(add, MC) else result.subtract(add, MC)
    }
    val rounded = result.round(MC)
    return if (rounded.signum() == 0) "0" else rounded.stripTrailingZeros().toPlainString()
}

private val keyRows = listOf(
    listOf("C", "DEL", "%", "÷"),
    listOf("7", "8", "9", "×"),
    listOf("4", "5", "6", "-"),
    listOf("1", "2", "3", "+"),
    listOf("00", "0", ".", "=")
)

@Composable
fun CalculatorScreen() {
    var expr by remember { mutableStateOf("") }
    var history by remember { mutableStateOf("") }
    var justEvaluated by remember { mutableStateOf(false) }

    fun currentNumber() = expr.takeLastWhile { it.isDigit() || it == '.' }

    fun press(k: String) {
        when (k) {
            "C" -> { expr = ""; history = ""; justEvaluated = false }
            "DEL" -> {
                if (justEvaluated) { expr = ""; justEvaluated = false } else expr = expr.dropLast(1)
            }
            "=" -> {
                if (expr.drop(1).any { it in OPS || it == '%' }) {
                    val r = evaluate(expr)
                    if (r == "Error") {
                        history = "Cannot divide by zero"; expr = ""; justEvaluated = false
                    } else if (r != null) {
                        history = "$expr ="; expr = r; justEvaluated = true
                    }
                }
            }
            "+", "-", "×", "÷" -> {
                justEvaluated = false
                if (expr.isEmpty()) {
                    if (k == "-") expr = "-"
                } else if (expr == "-") {
                    // ignore
                } else if (expr.last() in OPS) {
                    expr = expr.dropLast(1) + k
                } else expr += k
            }
            "%" -> {
                justEvaluated = false
                if (expr.isNotEmpty() && expr.last().isDigit()) expr += "%"
            }
            "." -> {
                if (justEvaluated) { expr = ""; justEvaluated = false }
                if (expr.isNotEmpty() && expr.last() == '%') return
                val cn = currentNumber()
                if (cn.contains('.')) return
                expr += if (cn.isEmpty()) "0." else "."
            }
            else -> { // digits: 0-9 and 00
                if (justEvaluated) { expr = ""; justEvaluated = false; history = "" }
                if (expr.isNotEmpty() && expr.last() == '%') return
                val cn = currentNumber()
                if (cn.length >= 15) return
                if (cn == "0") {
                    if (k == "0" || k == "00") return
                    expr = expr.dropLast(1) + k
                } else if (cn.isEmpty() && k == "00") {
                    expr += "0"
                } else expr += k
            }
        }
    }

    val preview = remember(expr, justEvaluated) {
        if (!justEvaluated && expr.drop(1).any { it in OPS || it == '%' }) {
            evaluate(expr)?.takeIf { it != "Error" }
        } else null
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Text(history, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                expr.ifEmpty { "0" }.replace("-", "−"),
                fontSize = if (expr.length > 12) 34.sp else 52.sp,
                fontWeight = FontWeight.Light,
                maxLines = 2,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                if (preview != null) "= $preview" else " ",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }
        Spacer(Modifier.height(8.dp))
        keyRows.forEach { row ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { k -> CalcKey(k, Modifier.weight(1f)) { press(k) } }
            }
        }
    }
}

@Composable
private fun CalcKey(key: String, modifier: Modifier, onClick: () -> Unit) {
    val isOp = key in listOf("÷", "×", "-", "+", "%")
    val isClear = key == "C" || key == "DEL"
    val isEq = key == "="
    val bg = when {
        isEq -> MaterialTheme.colorScheme.primary
        isOp -> MaterialTheme.colorScheme.primaryContainer
        isClear -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val fg = when {
        isEq -> MaterialTheme.colorScheme.onPrimary
        isOp -> MaterialTheme.colorScheme.onPrimaryContainer
        isClear -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    Surface(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(24.dp),
        color = bg,
        tonalElevation = if (!isOp && !isEq && !isClear) 1.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                if (key == "-") "−" else key,
                color = fg,
                fontSize = if (key == "DEL") 18.sp else 28.sp,
                fontWeight = if (isOp || isEq) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
