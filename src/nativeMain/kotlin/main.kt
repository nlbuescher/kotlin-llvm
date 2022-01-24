import TokenType.*
import platform.posix.*

/*
# Compute the nth fibonacci number
def fib(n)
	if x < 3 then
		1
	else
		fib(x - 1) + fib (x - 2)

# this expression will compute the 40th number
fib(40)
*/
/*
extern sin(arg);
extern cos(arg);
extern atan2(arg1 arg2);

atan2(sin(.4), cos(42))
*/

enum class TokenType {
	// commands
	Def,
	Extern,

	// primary
	Identifier,
	Number
}

data class Token(
	val type: TokenType,
	val column: Int,
	val value: String? = null,
)

fun lexLine(line: String): List<Token> = buildList {
	var index = 0

	while (index < line.length) {
		// skip whitespace
		while (line.getOrNull(index)?.isWhitespace() == true)
			index += 1

		if (line.getOrNull(index)?.isLetter() == true) {
			val column = index + 1

			val string = buildString {
				append(line[index])
				while (line.getOrNull(++index)?.isLetterOrDigit() == true)
					append(line[index])
			}

			when (string) {
				"def" -> add(Token(Def, column))
				"extern" -> add(Token(Extern, column))
				else -> add(Token(Identifier, column, value = string))
			}

			continue
		}

		if (line.getOrNull(index)?.isDigit() == true || line.getOrNull(index) == '.') {
			val column = index + 1

			var foundDot = false
			val number = buildString {
				do {
					if (foundDot && line[index] == '.') {
						compileError(column, "Invalid number. Numbers may have at most one decimal")
					}

					foundDot = foundDot || line[index] == '.'
					append(line[index++])
				}
				while (line.getOrNull(index)?.isDigit() == true || line.getOrNull(index) == '.')
			}

			add(Token(Number, column, value = number))
			continue
		}

		if (line.getOrNull(index) == '#') {
			// comment until end of line
			break
		}
	}
}

fun compileError(column: Int, message: Any?) {
	fprintf(stderr, "ERROR at column $column: $message\n")
	exit(1)
}

fun main() {
	val tokens = lexLine(readln())
	tokens.forEach {
		println(it)
	}
}
