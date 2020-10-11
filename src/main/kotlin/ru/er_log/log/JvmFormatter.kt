package ru.er_log.log

import java.util.logging.Formatter
import java.util.logging.LogRecord

class JvmFormatter : Formatter() {
    override fun format(record: LogRecord?): String {
        return formatMessage(record) + '\n'
    }
}