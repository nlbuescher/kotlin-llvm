package kotlin_llvm

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.system.*


data class Location(val file: String, val line: Int = 0, val column: Int = 0) {
	override fun toString() = "$file:$line:$column"
}

val errorString: String get() = strerror_l(errno, uselocale(null))?.toKString() ?: ""

fun compilerError(message: Any?): Nothing {
	fputs("ERROR: $message\n", stderr)
	exitProcess(1)
}

fun compilerError(message: Any?, location: Location): Nothing {
	fputs("$location: ERROR: $message\n", stderr)
	exitProcess(1)
}

fun readFile(fileName: String): String {
	val file = fopen(fileName, "r") ?: compilerError("Could not open file '$fileName': $errorString")

	fseek(file, 0, SEEK_END)
	val size = ftell(file)

	rewind(file)
	val contents = memScoped {
		val buffer = allocArray<ByteVar>(size + 1)
		fread(buffer, size.convert(), 1, file)
		buffer.toKString()
	}

	fclose(file)

	return contents
}
