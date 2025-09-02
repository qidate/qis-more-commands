package com.qi.qismorecommands.command.StringVar

import java.util.Locale
import kotlin.random.Random

object StringMaker {
    // 合并字符串
    fun joinStrings(str1: String?, str2: String): String {
        return (str1 ?: "") + str2
    }

    // 转换为大写
    fun toUpperCase(str: String): String {
        return str.uppercase(Locale.getDefault())
    }

    // 转换为小写
    fun toLowerCase(str: String): String {
        return str.lowercase(Locale.getDefault())
    }

    // 生成随机字母字符串
    fun randomLetters(length: Int): String {
        return buildString {
            repeat(length) {
                val charCode = if (Random.Default.nextBoolean()) {
                    Random.Default.nextInt(26) + 'a'.code
                } else {
                    Random.Default.nextInt(26) + 'A'.code
                }
                append(charCode.toChar())
            }
        }
    }

    // 生成随机数字字符串
    fun randomNumbers(length: Int): String {
        return buildString {
            repeat(length) {
                append(Random.Default.nextInt(10))
            }
        }
    }

    // 生成随机混合字符串
    fun randomMixed(length: Int): String {
        return buildString {
            repeat(length) {
                when (Random.Default.nextInt(3)) {
                    0 -> append((Random.Default.nextInt(26) + 'a'.code).toChar())
                    1 -> append((Random.Default.nextInt(26) + 'A'.code).toChar())
                    else -> append(Random.Default.nextInt(10))
                }
            }
        }
    }

    // 生成自定义字符集随机字符串
    fun randomChars(charset: String?, length: Int): String {
        if (charset.isNullOrEmpty()) return ""

        return buildString {
            repeat(length) {
                val index = Random.Default.nextInt(charset.length)
                append(charset[index])
            }
        }
    }

    // 阿拉伯数字转简单中文数字 (12345 → 一二三四五)
    fun numberToChineseSimple(numberStr: String?): String {
        if (numberStr.isNullOrEmpty()) return ""

        return buildString {
            numberStr.forEach { char ->
                if (char.isDigit()) {
                    append("零一二三四五六七八九"[char.digitToInt()])
                } else {
                    append(char)
                }
            }
        }
    }

    // 阿拉伯数字转大写中文数字 (12345 → 壹贰叁肆伍)
    fun numberToChineseCapital(numberStr: String?): String {
        if (numberStr.isNullOrEmpty()) return ""

        return buildString {
            numberStr.forEach { char ->
                if (char.isDigit()) {
                    append("零壹贰叁肆伍陆柒捌玖"[char.digitToInt()])
                } else {
                    append(char)
                }
            }
        }
    }

    // 阿拉伯数字转标准中文数字 (12345 → 一万二千三百四十五)
    fun numberToChineseNormal(numberStr: String): String {
        return numberStr.toLongOrNull()?.let { number ->
            convertWithUnits(number, false)
        } ?: "无效数字"
    }

    // 阿拉伯数字转财务中文数字 (12345 → 壹万贰仟叁佰肆拾伍)
    fun numberToChineseFinancial(numberStr: String): String {
        return numberStr.toLongOrNull()?.let { number ->
            convertWithUnits(number, true)
        } ?: "无效数字"
    }

    // 带单位的数字转换核心方法
    private fun convertWithUnits(number: Long, financial: Boolean): String {
        if (number == 0L) return "零"
        if (number < 0) return "负" + convertWithUnits(-number, financial)

        val units = if (financial) {
            arrayOf("", "拾", "佰", "仟", "万", "亿", "兆", "京")
        } else {
            arrayOf("", "十", "百", "千", "万", "亿", "兆", "京")
        }

        val digits = if (financial) {
            arrayOf("零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖")
        } else {
            arrayOf("零", "一", "二", "三", "四", "五", "六", "七", "八", "九")
        }

        val unitValues = longArrayOf(1, 10, 100, 1000, 10000, 100000000L, 1000000000000L, 10000000000000000L)
        var remaining = number
        val result = StringBuilder()

        for (i in unitValues.indices.reversed()) {
            if (remaining >= unitValues[i]) {
                val quotient = remaining / unitValues[i]
                if (quotient > 0) {
                    if (quotient < 10) {
                        result.append(digits[quotient.toInt()]).append(units[i])
                    } else {
                        result.append(convertWithUnits(quotient, financial)).append(units[i])
                    }
                }
                remaining %= unitValues[i]

                if (remaining > 0 && remaining < unitValues[i] / 10) {
                    result.append(digits[0])
                }
            }
        }

        val resultStr = result.toString()
        return when {
            resultStr.startsWith("一十") -> resultStr.substring(1)
            resultStr.startsWith("壹拾") -> resultStr.substring(1)
            else -> resultStr
        }
    }
}