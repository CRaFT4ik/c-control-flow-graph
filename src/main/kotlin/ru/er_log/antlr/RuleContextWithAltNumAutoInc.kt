package ru.er_log.antlr

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.atn.ATN


open class RuleContextWithAltNumAutoInc(parent: ParserRuleContext?, invokingStateNumber: Int) : ParserRuleContext(parent, invokingStateNumber)
{
    companion object {
        const val INIT_ALT_NUMBER = 0
        private var contextCounter = INIT_ALT_NUMBER; get() = field++
    }

    private var altNum = ATN.INVALID_ALT_NUMBER

    init { altNum = contextCounter }

    override fun getAltNumber(): Int = altNum
    override fun setAltNumber(altNum: Int) {}
}