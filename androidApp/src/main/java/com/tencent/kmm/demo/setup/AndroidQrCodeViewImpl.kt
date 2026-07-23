package com.tencent.kmm.demo.setup

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.ImageView
import com.tencent.news.core.compose.AndroidQrCodeView
import kotlin.math.abs

internal class AndroidQrCodeViewImpl(context: Context) : AndroidQrCodeView(context) {

    private var content: String = ""
    private var qrForegroundColor: Int = Color.BLACK
    private var qrBackgroundColor: Int = Color.WHITE

    init {
        scaleType = ImageView.ScaleType.FIT_CENTER
        setBackgroundColor(qrBackgroundColor)
    }

    override fun setContent(content: String) {
        this.content = content
        renderQrCode()
    }

    override fun setColor(color: String) {
        qrForegroundColor = parseColor(color, Color.BLACK)
        renderQrCode()
    }

    override fun setQrBackgroundColor(color: String) {
        qrBackgroundColor = parseColor(color, Color.WHITE)
        setBackgroundColor(qrBackgroundColor)
        renderQrCode()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        renderQrCode()
    }

    private fun renderQrCode() {
        if (content.isBlank()) {
            setImageBitmap(null)
            return
        }
        val modules = runCatching {
            SimpleQrCodeEncoder.encode(content)
        }.getOrNull()
        if (modules == null) {
            setImageBitmap(null)
            return
        }
        val viewSize = maxOf(width, height)
        val minQrSize = modules.size + QUIET_ZONE_MODULES * 2
        val bitmapSize = if (viewSize > 0) maxOf(viewSize, minQrSize) else MIN_BITMAP_SIZE
        setImageBitmap(modules.toBitmap(bitmapSize, qrForegroundColor, qrBackgroundColor))
    }

    private fun parseColor(color: String, fallback: Int): Int {
        if (color.isBlank()) return fallback
        return runCatching {
            Color.parseColor(color)
        }.getOrElse {
            fallback
        }
    }
}

private object SimpleQrCodeEncoder {
    private val DATA_CODEWORDS = intArrayOf(19, 34, 55, 80, 108)
    private val ECC_CODEWORDS = intArrayOf(7, 10, 15, 20, 26)
    private const val MASK_PATTERN = 0

    fun encode(content: String): Array<BooleanArray> {
        val data = content.toByteArray(Charsets.UTF_8)
        val versionIndex = DATA_CODEWORDS.indexOfFirst { capacity ->
            BYTE_MODE_BITS + BYTE_COUNT_BITS + data.size * BITS_PER_BYTE <= capacity * BITS_PER_BYTE
        }
        require(versionIndex >= 0) { "QR content is too long" }

        val version = versionIndex + 1
        val dataCodewords = buildDataCodewords(data, DATA_CODEWORDS[versionIndex])
        val eccCodewords = ReedSolomon.computeRemainder(dataCodewords, ECC_CODEWORDS[versionIndex])
        val matrix = QrMatrix(version)
        matrix.drawFunctionPatterns()
        matrix.drawFormatBits(MASK_PATTERN)
        matrix.drawCodewords(dataCodewords + eccCodewords)
        matrix.applyMask(MASK_PATTERN)
        return matrix.modules
    }

    private fun buildDataCodewords(data: ByteArray, capacity: Int): IntArray {
        val buffer = BitBuffer()
        buffer.appendBits(0b0100, BYTE_MODE_BITS)
        buffer.appendBits(data.size, BYTE_COUNT_BITS)
        data.forEach { byte ->
            buffer.appendBits(byte.toInt() and 0xFF, BITS_PER_BYTE)
        }

        val capacityBits = capacity * BITS_PER_BYTE
        buffer.appendBits(0, minOf(TERMINATOR_BITS, capacityBits - buffer.size))
        while (buffer.size % BITS_PER_BYTE != 0) {
            buffer.appendBits(0, 1)
        }

        val result = buffer.toCodewords().toMutableList()
        var padByte = 0xEC
        while (result.size < capacity) {
            result += padByte
            padByte = if (padByte == 0xEC) 0x11 else 0xEC
        }
        return result.toIntArray()
    }
}

private class BitBuffer {
    private val bits = mutableListOf<Int>()
    val size: Int get() = bits.size

    fun appendBits(value: Int, length: Int) {
        require(length >= 0)
        for (i in length - 1 downTo 0) {
            bits += (value ushr i) and 1
        }
    }

    fun toCodewords(): IntArray {
        val result = IntArray((bits.size + BITS_PER_BYTE - 1) / BITS_PER_BYTE)
        bits.forEachIndexed { index, bit ->
            result[index / BITS_PER_BYTE] =
                result[index / BITS_PER_BYTE] or (bit shl (BITS_PER_BYTE - 1 - index % BITS_PER_BYTE))
        }
        return result
    }
}

private class QrMatrix(private val version: Int) {
    val size: Int = 17 + 4 * version
    val modules: Array<BooleanArray> = Array(size) { BooleanArray(size) }
    private val reserved: Array<BooleanArray> = Array(size) { BooleanArray(size) }

    fun drawFunctionPatterns() {
        drawFinderPattern(3, 3)
        drawFinderPattern(size - 4, 3)
        drawFinderPattern(3, size - 4)
        drawTimingPatterns()
        drawAlignmentPattern()
        setFunctionModule(8, size - 8, true)
    }

    fun drawFormatBits(mask: Int) {
        val data = (FORMAT_ECC_LOW_BITS shl 3) or mask
        var remainder = data
        repeat(10) {
            remainder = (remainder shl 1) xor ((remainder ushr 9) * FORMAT_GENERATOR)
        }
        val bits = ((data shl 10) or remainder) xor FORMAT_MASK

        for (i in 0..5) setFunctionModule(8, i, getBit(bits, i))
        setFunctionModule(8, 7, getBit(bits, 6))
        setFunctionModule(8, 8, getBit(bits, 7))
        setFunctionModule(7, 8, getBit(bits, 8))
        for (i in 9 until 15) setFunctionModule(14 - i, 8, getBit(bits, i))
        for (i in 0 until 8) setFunctionModule(size - 1 - i, 8, getBit(bits, i))
        for (i in 8 until 15) setFunctionModule(8, size - 15 + i, getBit(bits, i))
        setFunctionModule(8, size - 8, true)
    }

    fun drawCodewords(codewords: IntArray) {
        var bitIndex = 0
        var upward = true
        var x = size - 1
        while (x > 0) {
            if (x == 6) x--
            for (i in 0 until size) {
                val y = if (upward) size - 1 - i else i
                for (dx in 0..1) {
                    val xx = x - dx
                    if (!reserved[y][xx]) {
                        val byteIndex = bitIndex / BITS_PER_BYTE
                        val bit = if (byteIndex < codewords.size) {
                            ((codewords[byteIndex] ushr (BITS_PER_BYTE - 1 - bitIndex % BITS_PER_BYTE)) and 1) != 0
                        } else {
                            false
                        }
                        modules[y][xx] = bit
                        bitIndex++
                    }
                }
            }
            upward = !upward
            x -= 2
        }
    }

    fun applyMask(mask: Int) {
        for (y in 0 until size) {
            for (x in 0 until size) {
                if (!reserved[y][x] && shouldMask(mask, x, y)) {
                    modules[y][x] = !modules[y][x]
                }
            }
        }
    }

    private fun drawFinderPattern(centerX: Int, centerY: Int) {
        for (dy in -4..4) {
            for (dx in -4..4) {
                val x = centerX + dx
                val y = centerY + dy
                if (x !in 0 until size || y !in 0 until size) continue
                val distance = maxOf(abs(dx), abs(dy))
                setFunctionModule(x, y, distance != 2 && distance != 4)
            }
        }
    }

    private fun drawTimingPatterns() {
        for (i in 0 until size) {
            if (!reserved[6][i]) setFunctionModule(i, 6, i % 2 == 0)
            if (!reserved[i][6]) setFunctionModule(6, i, i % 2 == 0)
        }
    }

    private fun drawAlignmentPattern() {
        if (version == 1) return
        val center = version * 4 + 10
        for (dy in -2..2) {
            for (dx in -2..2) {
                val distance = maxOf(abs(dx), abs(dy))
                setFunctionModule(center + dx, center + dy, distance != 1)
            }
        }
    }

    private fun setFunctionModule(x: Int, y: Int, isBlack: Boolean) {
        modules[y][x] = isBlack
        reserved[y][x] = true
    }

    private fun shouldMask(mask: Int, x: Int, y: Int): Boolean {
        return when (mask) {
            0 -> (x + y) % 2 == 0
            else -> false
        }
    }

    private fun getBit(value: Int, index: Int): Boolean {
        return ((value ushr index) and 1) != 0
    }
}

private object ReedSolomon {
    private val exp = IntArray(512)
    private val log = IntArray(256)

    init {
        var x = 1
        for (i in 0 until 255) {
            exp[i] = x
            log[x] = i
            x = x shl 1
            if (x and 0x100 != 0) {
                x = x xor 0x11D
            }
        }
        for (i in 255 until exp.size) {
            exp[i] = exp[i - 255]
        }
    }

    fun computeRemainder(data: IntArray, degree: Int): IntArray {
        val generator = generatorPolynomial(degree)
        val result = IntArray(degree)
        data.forEach { value ->
            val factor = value xor result[0]
            for (i in 0 until degree - 1) {
                result[i] = result[i + 1]
            }
            result[degree - 1] = 0
            for (i in 0 until degree) {
                result[i] = result[i] xor multiply(generator[i], factor)
            }
        }
        return result
    }

    private fun generatorPolynomial(degree: Int): IntArray {
        val result = IntArray(degree)
        result[degree - 1] = 1
        var root = 1
        for (i in 0 until degree) {
            for (j in 0 until degree) {
                result[j] = multiply(result[j], root)
                if (j + 1 < degree) {
                    result[j] = result[j] xor result[j + 1]
                }
            }
            root = multiply(root, 0x02)
        }
        return result
    }

    private fun multiply(x: Int, y: Int): Int {
        return if (x == 0 || y == 0) {
            0
        } else {
            exp[log[x] + log[y]]
        }
    }
}

private fun Array<BooleanArray>.toBitmap(size: Int, foregroundColor: Int, backgroundColor: Int): Bitmap {
    val moduleCount = this.size + QUIET_ZONE_MODULES * 2
    val scale = maxOf(1, size / moduleCount)
    val drawSize = moduleCount * scale
    val offset = (size - drawSize) / 2
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        isAntiAlias = false
        style = Paint.Style.FILL
    }

    canvas.drawColor(backgroundColor)
    paint.color = foregroundColor
    forEachIndexed { y, row ->
        row.forEachIndexed { x, isBlack ->
            if (isBlack) {
                val left = offset + (x + QUIET_ZONE_MODULES) * scale
                val top = offset + (y + QUIET_ZONE_MODULES) * scale
                canvas.drawRect(
                    left.toFloat(),
                    top.toFloat(),
                    (left + scale).toFloat(),
                    (top + scale).toFloat(),
                    paint,
                )
            }
        }
    }
    return bitmap
}

private const val MIN_BITMAP_SIZE = 256
private const val QUIET_ZONE_MODULES = 4
private const val BITS_PER_BYTE = 8
private const val BYTE_MODE_BITS = 4
private const val BYTE_COUNT_BITS = 8
private const val TERMINATOR_BITS = 4
private const val FORMAT_ECC_LOW_BITS = 1
private const val FORMAT_GENERATOR = 0x537
private const val FORMAT_MASK = 0x5412
