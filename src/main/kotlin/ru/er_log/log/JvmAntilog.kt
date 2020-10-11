package ru.er_log

import com.github.aakira.napier.Antilog
import com.github.aakira.napier.Napier
import ru.er_log.log.JvmFormatter
import java.io.PrintWriter
import java.io.StringWriter
import java.util.logging.*
import java.util.regex.Pattern

class JvmAntilog(
        private val defaultTag: String = "app",
        private val handler: List<Handler> = listOf()
) : Antilog() {

    companion object {
        private const val CALL_STACK_INDEX = 8
    }

    val consoleHandler: ConsoleHandler = ConsoleHandler().apply {
        level = Level.ALL
        formatter = JvmFormatter()
    }

    private val logger: Logger = Logger.getLogger(JvmAntilog::class.java.name).apply {
        level = Level.ALL
        useParentHandlers = false

        if (handler.isEmpty()) {
            addHandler(consoleHandler)
            return@apply
        }
        handler.forEach {
            addHandler(it)
        }
    }

    private val anonymousClass = Pattern.compile("(\\$\\d+)+$")

    private val tagMap: HashMap<Napier.Level, String> = hashMapOf(
            Napier.Level.VERBOSE to "[V]",
            Napier.Level.DEBUG to "[D]",
            Napier.Level.INFO to "[I]",
            Napier.Level.WARNING to "[W]",
            Napier.Level.ERROR to "[E]",
            Napier.Level.ASSERT to "[A]"
    )

    override fun performLog(priority: Napier.Level, tag: String?, throwable: Throwable?, message: String?) {

        val debugTag = tag ?: performTag(defaultTag)

        val fullMessage = if (message != null) {
            if (throwable != null) {
                "$message\n${throwable.stackTraceString}"
            } else {
                message
            }
        } else throwable?.stackTraceString ?: return

        when (priority) {
            Napier.Level.VERBOSE -> logger.finest(buildLog(priority, debugTag, fullMessage))
            Napier.Level.DEBUG -> logger.fine(buildLog(priority, debugTag, fullMessage))
            Napier.Level.INFO -> logger.info(buildLog(priority, debugTag, fullMessage))
            Napier.Level.WARNING -> logger.warning(buildLog(priority, debugTag, fullMessage))
            Napier.Level.ERROR -> logger.severe(buildLog(priority, debugTag, fullMessage))
            Napier.Level.ASSERT -> logger.severe(buildLog(priority, debugTag, fullMessage))
        }
    }

    internal fun buildLog(priority: Napier.Level, tag: String?, message: String?): String {
        val builder = StringBuilder()
        builder.append(tagMap[priority])
        val tag = tag ?: performTag(defaultTag)
        if (tag.isNotEmpty()) {
            builder.append(" ")
            builder.append(tag)
        }
        builder.append(" :: ")
        builder.append(message)
        return builder.toString()
    }

    private fun performTag(defaultTag: String): String {
        return String()
//        val thread = Thread.currentThread().stackTrace
//
//        return if (thread.size >= CALL_STACK_INDEX) {
//            thread[CALL_STACK_INDEX].run {
//                "${createStackElementTag(className)}\$$methodName"
//            }
//        } else {
//            defaultTag
//        }
    }

    internal fun createStackElementTag(className: String): String {
        var tag = className
        val m = anonymousClass.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        return tag.substring(tag.lastIndexOf('.') + 1)
    }

    private val Throwable.stackTraceString
        get(): String {
            // DO NOT replace this with Log.getStackTraceString() - it hides UnknownHostException, which is
            // not what we want.
            val sw = StringWriter(256)
            val pw = PrintWriter(sw, false)
            printStackTrace(pw)
            pw.flush()
            return sw.toString()
        }
}
