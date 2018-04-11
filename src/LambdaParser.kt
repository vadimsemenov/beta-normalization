/**
 * @author  Vadim Semenov (semenov@rain.ifmo.ru)
 */
class LambdaParser {
    private lateinit var lexer: Lexer

    fun parse(expression: String): Lambda {
        lexer = Lexer(expression)
        val lambda = parseExpression()
        require(lexer.lexeme == null) { "Cannot read full input" }
        return lambda
    }

    private fun parseExpression(): Lambda = if (lexer.lexeme == Backslash) {
        parseAbstraction()
    } else {
        val application = parseApplication()
        if (lexer.lexeme == Backslash) {
            Application(application, parseAbstraction())
        } else {
            application
        }
    }

    private fun parseAbstraction(): Abstraction {
        lexer.consume(Backslash)
        val bound = lexer.lexeme ?: throw IllegalStateException("Cannot parse bound after backslash")
        lexer.consume(bound)
        lexer.consume(Dot)
        return Abstraction(bound.string, parseExpression())
    }

    private fun parseApplication(): Lambda {
        var result = parseAtom()!!
        while (true) {
            val atom = parseAtom() ?: return result
            result = Application(result, atom)
        }
    }

    private fun parseAtom(): Lambda? = when (lexer.lexeme) {
        is OpenParenthesis -> {
            lexer.consume(OpenParenthesis)
            val expression = parseExpression()
            lexer.consume(ClosedParenthesis)
            expression
        }
        is Literal ->  {
            val name = lexer.lexeme!!.string
            lexer.consume(Literal(name))
            Variable(name)
        }
        else -> null
    }
}

internal class Lexer(private val input: String) {
    private var ptr: Int = 0

    var lexeme: Lexeme? = nextLexeme()

    fun consume(lexeme: Lexeme) {
        require(this.lexeme == lexeme) { "Expected ${this.lexeme} but found $lexeme" }
        this.ptr += lexeme.string.length
        this.lexeme = nextLexeme()
    }

    private fun nextLexeme(): Lexeme? {
        while (ptr < input.length && input[ptr].isWhitespace()) {
            ++ptr
        }
        if (ptr >= input.length) {
            return null
        }
        return when (input[ptr]) {
            '.' -> Dot
            '\\' -> Backslash
            '(' -> OpenParenthesis
            ')' -> ClosedParenthesis
            in 'a'..'z' -> {
                var end = ptr
                while (end < input.length && input[end].validLiteralChar()) {
                    ++end
                }
                return Literal(input.substring(ptr, end))
            }
            else -> throw IllegalStateException("Cannot parse $input at $ptr")
        }
    }
}

private fun Char.validLiteralChar(): Boolean = this in 'a'..'z' || this in '0'..'9' || this == '\''

internal sealed class Lexeme(open val string: String)
internal data class Literal(override val string: String) : Lexeme(string)
internal object Backslash : Lexeme("\\")
internal object Dot : Lexeme(".")
internal object OpenParenthesis : Lexeme("(")
internal object ClosedParenthesis : Lexeme(")")