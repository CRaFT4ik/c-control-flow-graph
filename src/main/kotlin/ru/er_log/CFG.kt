package ru.er_log

import ru.er_log.antlr.ANTLRManager
import ru.er_log.antlr.CListener


class CFG(input: String)
{
    private val listener = CListener()
    private val manager: ANTLRManager = ANTLRManager(input, listener)

    fun calculate(): CFGResult {
        manager.run()
        return CFGResult()
    }
}

class CFGResult
{
    fun toImage(): ByteArray {
        return ByteArray(0)
    }
}