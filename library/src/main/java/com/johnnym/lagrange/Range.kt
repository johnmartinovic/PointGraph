package com.johnnym.lagrange

data class Range(val from: Long, val to: Long, val count: Long) {

    val middle: Long
        get() = (from + to) / 2

    val range: Long
        get() = to - from
}