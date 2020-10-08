package ru.er_log.util

import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions

/**
 * Невозможно сделать runtime проверку на data-класс и использовать все его
 * преимущества. Поэтому существует данная функция, реализующая одно из преимуществ
 * data-классов - копирование объектов.
 */
fun <T : Any> clone(obj: T): T {
    if (!obj::class.isData) {
        println(obj)
        throw Error("clone is only supported for data classes")
    }

    val copy = obj::class.memberFunctions.first { it.name == "copy" }
    val instanceParam = copy.instanceParameter!!

    return copy.callBy(mapOf(instanceParam to obj)) as T
}