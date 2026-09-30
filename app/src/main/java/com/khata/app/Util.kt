package com.khata.app

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

const val TYPE_RECEIVED = "RECEIVED"
const val TYPE_GIVEN = "GIVEN"

private val dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
private val timeFmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

/** Formats paise as ₹ with Indian digit grouping, e.g. 25000050 -> ₹2,50,000.50 */
fun formatMoney(paise: Long): String {
    val negative = paise < 0
    val v = abs(paise)
    val rupees = (v / 100).toString()
    val p = (v % 100).toInt()
    val grouped = if (rupees.length <= 3) rupees else {
        val last3 = rupees.takeLast(3)
        val rest = rupees.dropLast(3)
        rest.reversed().chunked(2).joinToString(",").reversed() + "," + last3
    }
    val fraction = if (p != 0) "." + p.toString().padStart(2, '0') else ""
    return (if (negative) "-" else "") + "₹" + grouped + fraction
}

/** Amount for text fields: 250000 -> "2500", 250050 -> "2500.50" */
fun plainAmount(paise: Long): String =
    if (paise % 100 == 0L) (paise / 100).toString()
    else "${paise / 100}.${(paise % 100).toString().padStart(2, '0')}"

/** Parses user input like "1,250.50" into paise. Returns null if invalid. */
fun parseAmount(input: String): Long? {
    val t = input.trim().replace(",", "")
    if (t.isEmpty()) return null
    return try {
        val bd = BigDecimal(t)
        if (bd.signum() < 0) null
        else bd.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact()
    } catch (e: Exception) {
        null
    }
}

fun statusOf(balance: Long): String = when {
    balance > 0 -> "Receivable"
    balance < 0 -> "Payable"
    else -> "Settled"
}

fun fullDate(iso: String): String =
    try { LocalDate.parse(iso).format(dateFmt) } catch (e: Exception) { iso }

fun dayLabel(iso: String): String {
    val d = try { LocalDate.parse(iso) } catch (e: Exception) { return iso }
    val today = LocalDate.now()
    return when (d) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> d.format(dateFmt)
    }
}

fun prettyTime(t: String): String =
    try { LocalTime.parse(t).format(timeFmt) } catch (e: Exception) { t }
