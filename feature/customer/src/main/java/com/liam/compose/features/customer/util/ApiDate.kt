package com.liam.compose.features.customer.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Wire format for every date the customer API takes or returns. */
const val API_DATE_PATTERN = "yyyy-MM-dd"

/**
 * `DatePicker` works in UTC-midnight millis, so the formatter is pinned to UTC — using the device
 * zone here would shift a picked day by one for anyone east or west of GMT.
 */
private fun apiDateFormat(): SimpleDateFormat =
    SimpleDateFormat(API_DATE_PATTERN, Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

fun Long.toApiDate(): String = apiDateFormat().format(Date(this))

fun String.toUtcMillisOrNull(): Long? = runCatching { apiDateFormat().parse(this)?.time }.getOrNull()

/**
 * Today in the *device* zone. A salesperson's "today's visits" means their local day, so this
 * deliberately does not use the UTC formatter above; the picker still round-trips the resulting
 * string to the same calendar day because it formats and parses with the same UTC clock.
 */
fun todayApiDate(): String =
    SimpleDateFormat(API_DATE_PATTERN, Locale.US).format(Date())
