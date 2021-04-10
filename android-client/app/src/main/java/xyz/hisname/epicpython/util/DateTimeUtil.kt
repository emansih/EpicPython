package xyz.hisname.epicpython.util

import java.lang.Long.parseLong
import java.time.Instant
import java.time.OffsetDateTime

object DateTimeUtil {

    fun getCalToString(date: String): String{
        return Instant.ofEpochMilli(parseLong(date))
            .atOffset(OffsetDateTime.now().offset)
            .toLocalDate()
            .toString()
    }
}