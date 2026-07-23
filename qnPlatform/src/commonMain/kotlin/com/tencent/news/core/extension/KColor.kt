package com.tencent.news.core.extension

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class KColor(colorString: String) {

    private val colorInt: Int = toColorInt(colorString)

    fun alpha(): Int {
        return Companion.alpha(colorInt)
    }

    fun red(): Int {
        return Companion.red(colorInt)
    }

    fun green(): Int {
        return Companion.green(colorInt)
    }

    fun blue(): Int {
        return Companion.blue(colorInt)
    }

    companion object {

        fun isValidColor(colorStr: String?): Boolean {
            if (colorStr.isNullOrEmpty()) {
                return false
            }

            if (colorStr[0] != '#') {
                return false
            }

            // 6 或 8 位色值
            return colorStr.length == 7 || colorStr.length == 9
        }

        fun toColorInt(colorStr: String?): Int {
            colorStr ?: return 0

            if (!isValidColor(colorStr)) {
                return 0
            }

            // Use a long to avoid rollovers on #ffXXXXXX
            var color = colorStr.substring(1).toLong(16)
            if (colorStr.length == 7) {
                color = color or 0x00000000ff000000L // Set the alpha value
            }

            return color.toInt()
        }

        /**
         * 将int色值转换为十六进制
         */
        fun toColorHex(color: Int, needAlpha: Boolean = true): String {
            val a = if (needAlpha) alpha(color).toHexStr() else ""
            val r = red(color).toHexStr()
            val g = green(color).toHexStr()
            val b = blue(color).toHexStr()
            return "#${a}${r}${g}${b}"
        }

        fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int {
            return alpha shl 24 or (red shl 16) or (green shl 8) or blue
        }

        fun rgb(red: Int, green: Int, blue: Int): Int {
            return -0x1000000 or (red shl 16) or (green shl 8) or blue
        }

        fun blendARGB(color1: Int, color2: Int, ratio: Float): Int {
            val inverseRatio = 1 - ratio
            val a = alpha(color1) * inverseRatio + alpha(color2) * ratio
            val r = red(color1) * inverseRatio + red(color2) * ratio
            val g = green(color1) * inverseRatio + green(color2) * ratio
            val b = blue(color1) * inverseRatio + blue(color2) * ratio
            return argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
        }

        fun blendARGB(color1: String, color2: String, ratio: Float): String {
            val newColor = blendARGB(toColorInt(color1), toColorInt(color2), ratio)
            return toColorHex(newColor)
        }

        // 给色值混合透明度，alphaPercent取值为 [0.0-1.0]
        fun blendAlpha(color: String, alphaPercent: Float): String {
            val newColorInt = blendAlpha(toColorInt(color), alphaPercent)
            return toColorHex(newColorInt)
        }

        fun blendAlpha(color: Int, alphaPercent: Float): Int {
            val newAlpha = (alpha(color) * alphaPercent).toInt()
            return argb(newAlpha, red(color), green(color), blue(color))
        }

        fun alpha(colorInt: Int): Int {
            return colorInt ushr 24
        }

        fun red(colorInt: Int): Int {
            return colorInt shr 16 and 0xFF
        }

        fun green(colorInt: Int): Int {
            return colorInt shr 8 and 0xFF
        }

        fun blue(colorInt: Int): Int {
            return colorInt and 0xFF
        }

        fun hsbToRgb(hsb: FloatArray): Int {
            return hsbToRgb(hsb[0], hsb[1], hsb[2])
        }

        fun rgbToHsb(
            r: Int,
            g: Int,
            b: Int,
            outHsb: FloatArray,
        ) {
            val rNorm = r / 255f
            val gNorm = g / 255f
            val bNorm = b / 255f
            val max = maxOf(rNorm, gNorm, bNorm)
            val min = minOf(rNorm, gNorm, bNorm)
            val delta = max - min
            val brightness = max
            val saturation = if (max == 0f) 0f else delta / max
            val hue = when {
                delta == 0f -> 0f
                max == rNorm -> (60 * ((gNorm - bNorm) / delta) + 360) % 360
                max == gNorm -> (60 * ((bNorm - rNorm) / delta) + 120) % 360
                max == bNorm -> (60 * ((rNorm - gNorm) / delta) + 240) % 360
                else -> 0f
            }
            outHsb[0] = hue
            outHsb[1] = saturation
            outHsb[2] = brightness
        }

        fun rgbToHsl(
            r: Int,
            g: Int,
            b: Int,
            outHsl: FloatArray,
        ) {
            val rf = r / 255f
            val gf = g / 255f
            val bf = b / 255f
            val max: Float = max(rf, max(gf, bf))
            val min: Float = min(rf, min(gf, bf))
            val deltaMaxMin = max - min
            var h: Float
            val s: Float
            val l = (max + min) / 2f
            if (max == min) {
                // Monochromatic
                s = 0f
                h = s
            } else {
                h = if (max == rf) {
                    (gf - bf) / deltaMaxMin % 6f
                } else if (max == gf) {
                    (bf - rf) / deltaMaxMin + 2f
                } else {
                    (rf - gf) / deltaMaxMin + 4f
                }
                s = deltaMaxMin / (1f - abs(2f * l - 1f))
            }
            h = h * 60f % 360f
            if (h < 0) {
                h += 360f
            }
            outHsl[0] = constrain(h, 0f, 360f)
            outHsl[1] = constrain(s, 0f, 1f)
            outHsl[2] = constrain(l, 0f, 1f)
        }

        private fun hsbToRgb(hue: Float, saturation: Float, brightness: Float): Int {
            if (saturation == 0f) {
                val value = (brightness * 255).toInt()
                return rgb(value, value, value)
            }
            val h = hue / 60
            val i = h.toInt()
            val f = h - i
            val p = (brightness * (1 - saturation) * 255).toInt()
            val q = (brightness * (1 - f * saturation) * 255).toInt()
            val t = (brightness * (1 - (1 - f) * saturation) * 255).toInt()
            val v = (brightness * 255).toInt()
            return when (i % 6) {
                0 -> rgb(v, t, p)
                1 -> rgb(q, v, p)
                2 -> rgb(p, v, t)
                3 -> rgb(p, q, v)
                4 -> rgb(t, p, v)
                5 -> rgb(v, p, q)
                else -> rgb(0, 0, 0)
            }
        }


        private fun constrain(amount: Float, low: Float, high: Float): Float {
            return if (amount < low) low else min(amount, high)
        }

        private fun Int.toHexStr(): String {
            val result = this.toString(radix = 16)

            // 判断获取到的A,R,G,B值的长度 如果长度等于1 给A,R,G,B值前添0
            return if (result.length == 1) "0$result" else result
        }

    }

}