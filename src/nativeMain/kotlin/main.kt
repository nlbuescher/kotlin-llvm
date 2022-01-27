package kotlin_llvm


sealed class Node

sealed class Expression : Node()

data class NumberExpression(val number: Number) : Expression()

data class VariableExpression(val name: String) : Expression()

data class BinaryExpression(val op: String, val lhs: Expression, val rhs: Expression) : Expression()

data class CallExpression(val callee: String, val args: List<String>) : Expression()

data class Prototype(val name: String, val args: List<String>) : Node()

data class Function(val prototype: Prototype, val body: Expression) : Node()


fun parseTokens(tokens: List<Token>): List<Node> {
	val nodes = mutableListOf<Node>()

	var index = 0
	while (index < tokens.size) {
		// top ::= definition | external | expression
		when (tokens[index]) {
			is DefToken -> {
				val def = tokens[index++]

				val funName = tokens.getOrNull(index++)
					?: compilerError("Expected function name in prototype", def.loc)

				if (funName !is WordToken)
					compilerError("Expected function name in prototype, got '$funName'", funName.loc)

				val openParen = tokens.getOrNull(index++)
					?: compilerError("Expected opening parenthesis in prototype", funName.loc)

				if (openParen !is WordToken || openParen.value != "(")
					compilerError("Expected opening parenthesis in prototype, got '$openParen'", openParen.loc)

				val argNames = mutableListOf<String>()
				while (true) {
					val token = tokens.getOrNull(index++)
						?: compilerError("Expected argument name or closing parenthesis in prototype", funName.loc)

					if (token as? WordToken == null
						|| (token.value != ")" && token.value.any { !it.isLetterOrDigit() })
					) {
						compilerError("Expected argument name or closing parenthesis in prototype, got '$token'", token.loc)
					}

					if (token.value == ")") {
						// finish prototype
						nodes.add(Prototype(funName.value, argNames))
						break
					}
					else {
						argNames.add(token.value)
					}
				}
			}
			is ExternToken -> {
				TODO("parsing extern declarations is not implemented yet")
			}
			else -> {
				TODO("parsing expressions is not implemented yet")
			}
		}
	}
	return nodes
}

/*******************
 * MAIN            *
 *******************/

fun main() {
	val tokens = lexFile("test.kaleidoscope")
	tokens.forEach(::println)

	val nodes = parseTokens(tokens)
	nodes.forEach(::println)
}
