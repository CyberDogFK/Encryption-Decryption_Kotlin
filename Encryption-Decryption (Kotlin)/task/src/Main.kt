package encryptdecrypt

import java.nio.file.Files
import java.nio.file.Path

enum class Mode {
    ENCRYPT,
    DECRYPT
}

enum class ArgsStat {
    NONE,
    MODE,
    KEY,
    DATA,
    FILE_IN,
    FILE_OUT,
    ALGORITHM
}

enum class AlgorithmMode {
    SHIFT,
    UNICODE
}

enum class InputMode {
    CONSOLE,
    FILE
}

enum class OutputMode {
    CONSOLE,
    FILE
}

fun main(args: Array<String>) {
    var mode = Mode.ENCRYPT
    var key = 0
    var data = ""
    var fileIn = ""
    var fileOut = ""
    var outputMode = OutputMode.CONSOLE
    var inputMode = InputMode.CONSOLE
    var algorithmMode = AlgorithmMode.SHIFT

    var argsStat = ArgsStat.NONE
    for (arg in args) {
        if (argsStat == ArgsStat.NONE) {
            argsStat = when (arg) {
                "-mode" -> ArgsStat.MODE
                "-key" -> ArgsStat.KEY
                "-data" -> ArgsStat.DATA
                "-in" -> ArgsStat.FILE_IN
                "-out" -> ArgsStat.FILE_OUT
                "-alg" -> ArgsStat.ALGORITHM
                else -> throw IllegalArgumentException("Wrong argument!")
            }
        } else {
            when (argsStat) {
                ArgsStat.MODE -> {
                    when (arg) {
                        "enc" -> mode = Mode.ENCRYPT
                        "dec" -> mode = Mode.DECRYPT
                    }
                }
                ArgsStat.KEY -> { key = arg.toInt(10) }
                ArgsStat.DATA -> { data = arg }
                ArgsStat.FILE_IN -> {
                    inputMode = InputMode.FILE
                    fileIn = arg
                }
                ArgsStat.FILE_OUT -> {
                    outputMode = OutputMode.FILE
                    fileOut = arg
                }
                ArgsStat.ALGORITHM -> {
                    algorithmMode = when (arg) {
                        "shift" -> AlgorithmMode.SHIFT
                        "unicode" -> AlgorithmMode.UNICODE
                        else -> AlgorithmMode.SHIFT
                    }
                }
                else -> {}
            }
            argsStat = ArgsStat.NONE
        }
    }
    if (data.isNotEmpty() && fileIn.isNotEmpty()) {
        inputMode = InputMode.CONSOLE
    }
    val input: String = when (inputMode) {
        InputMode.CONSOLE -> { data }
        InputMode.FILE -> { Files.readAllLines(Path.of(fileIn)).joinToString(" ") }
    }

    val encryptor = Encryptor()
    val decryptor = Decryptor()

    val result = when (mode) {
        Mode.ENCRYPT -> encryptor.encryptMessage(input, key, algorithmMode)
        Mode.DECRYPT -> decryptor.decryptMessage(input, key, algorithmMode)
    }

    when (outputMode) {
        OutputMode.CONSOLE -> { println(result) }
        OutputMode.FILE -> { Files.writeString(Path.of(fileOut), result)}
    }
}

class Decryptor {
    fun decryptMessage(message: String, key: Int, algorithmMode: AlgorithmMode): String {
        return when (algorithmMode) {
            AlgorithmMode.SHIFT -> { message.map { decryptCharShift(it, key) }.joinToString("") }
            AlgorithmMode.UNICODE -> { message.map { decryptCharUnicode(it, key) }.joinToString("") }
        }
    }

    private fun decryptCharShift(ch: Char, key: Int, startChar: Char, endChar: Char): Char {
        if (!Character.isAlphabetic(ch.code)) {
            return ch
        }
        var value = ch - key
        if (value < startChar) {
            value = (endChar + 1) - (startChar - value)
        }
        return value

    }

    private fun decryptCharShift(ch: Char, key: Int): Char {
        return if (Character.isUpperCase(ch)) {
            decryptCharShift(ch, key, 'A', 'Z')
        } else {
            decryptCharShift(ch, key, 'a', 'z')
        }
    }

    private fun decryptCharUnicode(ch: Char, key: Int): Char {
        return ch - key
    }
}

class Encryptor {
    fun encryptMessage(message: String, key: Int, algorithmMode: AlgorithmMode): String {
        return when (algorithmMode) {
            AlgorithmMode.SHIFT -> { message.map { encryptCharShift(it, key ) }.joinToString("") }
            AlgorithmMode.UNICODE -> { message.map { encryptCharUnicode(it, key) }.joinToString("") }
        }
    }

    private fun encryptCharShift(ch: Char, key: Int): Char {
        return if (Character.isUpperCase(ch)) {
            encryptCharShift(ch, key, 'A', 'Z')
        } else {
            encryptCharShift(ch, key, 'a', 'z')
        }
    }

    private fun encryptCharShift(ch: Char, key: Int, charBegin: Char, charEnd: Char): Char {
        if (!Character.isAlphabetic(ch.code)) {
            return ch
        }
        var value = ch + key
        if (value > charEnd) {
            value = (charBegin - 1) + (value - charEnd)
        }
        return value
    }

    private fun encryptCharUnicode(ch: Char, key: Int): Char {
        return ch + key
    }
}
