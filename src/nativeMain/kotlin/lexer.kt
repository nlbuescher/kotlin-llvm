package kotlin_llvm

sealed class Token {
	abstract val loc: Location
}

data class DefToken(override val loc: Location) : Token()

data class ExternToken(override val loc: Location) : Token()

data class WordToken(override val loc: Location, val value: String) : Token()

data class NumberToken(override val loc: Location, val value: Number) : Token()


val charTokens = charArrayOf(
	'(', ',', ')',

	// operators
	'+', '-',
	'<',
)

fun lexLine(line: String, location: Location): List<Token> = buildList {
	var index = 0

	while (index < line.length) {
		// skip whitespace
		while (line.getOrNull(index)?.isWhitespace() == true)
			index += 1

		if (index == line.length)
			break

		val loc = location.copy(column = index + 1)

		// comments
		if (line.getOrNull(index) == '#') {
			// comment until end of line
			break
		}

		// single character tokens
		else if (line[index] in charTokens) {
			add(WordToken(loc, line[index].toString()))
			index++
		}

		// words
		else if (line[index].isLetter()) {
			val string = buildString {
				append(line[index])
				while (line.getOrNull(++index)?.isLetterOrDigit() == true)
					append(line[index])
			}

			when (string) {
				"def" -> add(DefToken(loc))
				"extern" -> add(ExternToken(loc))
				else -> add(WordToken(loc, value = string))
			}
		}

		// numbers
		else if (line[index].isDigit() || line[index] == '.') {
			val number = buildString {
				var gotDot = false
				do {
					if (gotDot && line[index] == '.') {
						compilerError("Invalid number. Numbers may have at most one decimal point", loc)
					}

					gotDot = gotDot || line[index] == '.'
					append(line[index++])
				}
				while (line.getOrNull(index)?.isDigit() == true || line.getOrNull(index) == '.')
			}

			add(NumberToken(loc, value = number.toDouble()))
		}

		// unrecognized token
		else {
			compilerError("Unexpected character '${line[index]}'", loc)
		}
	}
}

fun lexString(string: String, loc: Location): List<Token> {
	return string.split('\n').flatMapIndexed { index, line ->
		lexLine(line, loc.copy(line = index + 1))
	}
}

fun lexFile(fileName: String): List<Token> {
	val contents = readFile(fileName)
	return lexString(contents, Location(fileName))
}
