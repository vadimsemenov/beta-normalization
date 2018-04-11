/**
 * @author  Vadim Semenov (semenov@rain.ifmo.ru)
 */
sealed class Lambda {
    val freeVariables: Set<String> by lazy { this.freeVars() }

    fun normalized(): Lambda =  normalForm(this)

    protected abstract fun freeVars(): Set<String>
}

data class Variable(val name: String) : Lambda() {
    override fun toString(): String = name

    override fun freeVars(): Set<String> = setOf(name)
}

data class Abstraction(val over: String, val body: Lambda) : Lambda() {
    override fun toString(): String = "\\$over.$body"

    override fun freeVars(): Set<String> = body.freeVariables - over
}

data class Application(val lhs: Lambda, val rhs: Lambda) : Lambda() {
    override fun toString(): String {
        val left = if (lhs is Abstraction) "($lhs)" else lhs.toString()
        val right = if (rhs is Abstraction || rhs is Application) "($rhs)" else rhs.toString()
        return "$left $right"
    }

    override fun freeVars(): Set<String> = lhs.freeVariables + rhs.freeVariables
}


fun normalForm(lambda: Lambda): Lambda = when (lambda) {
    is Variable    -> lambda
    is Abstraction -> Abstraction(over = lambda.over, body = normalForm(lambda = lambda.body))
    is Application -> {
        val hnf = headNormalForm(lambda = lambda.lhs)
        when (hnf) {
            is Abstraction -> normalForm(lambda = reduce(hnf, lambda.rhs))
            else           -> Application(lhs = normalForm(hnf), rhs = normalForm(lambda.rhs))
//                            normalized, because we cannot reduce on normalized hnf
        }
    }
}

fun headNormalForm(lambda: Lambda): Lambda = when (lambda) {
    is Variable    -> lambda
    is Abstraction -> Abstraction(over = lambda.over, body = headNormalForm(lambda.body))
    is Application -> {
        val hnf = headNormalForm(lambda = lambda.lhs)
        when (hnf) {
            is Abstraction -> headNormalForm(lambda = reduce(hnf, lambda.rhs))
            else           -> Application(lhs = hnf, rhs = lambda.rhs)
        }
    }
}

internal fun reduce(abstraction: Abstraction, substitution: Lambda): Lambda {
    return substitute(
            lambda = abstraction.body,
            instead = abstraction.over,
            substitution = substitution,
            substitutionFreeVars = substitution.freeVariables,
            nameMapping = emptyMap()
    )
}

internal fun substitute(lambda: Lambda, instead: String, substitution: Lambda, substitutionFreeVars: Set<String>,
                        nameMapping: Map<String, String>): Lambda = when(lambda) {
    is Variable    -> if (lambda.name == instead) substitution else Variable(
            name = nameMapping.getOrDefault(lambda.name, lambda.name)
    )
    is Application -> Application(
            lhs = substitute(lambda.lhs, instead, substitution, substitutionFreeVars, nameMapping),
            rhs = substitute(lambda.rhs, instead, substitution, substitutionFreeVars, nameMapping)
    )
    is Abstraction -> {
        if (lambda.over == instead) {
            substituteFreeVariables(lambda, nameMapping)
        } else {
            val newName = if (!substitutionFreeVars.contains(lambda.over)) lambda.over else
                newName(lambda.over.prime(), substitutionFreeVars + lambda.body.freeVariables + nameMapping.values)
            val newMapping = nameMapping + (lambda.over to newName)
            Abstraction(
                    over = newName,
                    body = substitute(lambda.body, instead, substitution, substitutionFreeVars, newMapping)
            )
        }
    }
}

internal fun substituteFreeVariables(lambda: Lambda, nameMapping: Map<String, String>): Lambda = when (lambda) {
    is Variable    -> Variable(
            name = nameMapping.getOrDefault(lambda.name, lambda.name)
    )
    is Abstraction -> Abstraction(
            over = lambda.over,
            body = substituteFreeVariables(lambda.body, nameMapping - lambda.over)
    )
    is Application -> Application(
            lhs = substituteFreeVariables(lambda.lhs, nameMapping),
            rhs = substituteFreeVariables(lambda.rhs, nameMapping)
    )
}

private fun newName(name: String, bounds: Set<String>): String =
        if (bounds.contains(name)) newName(name.prime(), bounds) else name

private fun String.prime() = "$this'"