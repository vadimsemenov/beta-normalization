import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * @author Vadim Semenov (semenov@rain.ifmo.ru)
 */
internal class NormalizationTest {
    private val parser = LambdaParser()

    @Test fun normalForm() {
        assertNormalization("f x ((\\x.f x) (\\x.x))", "f x (f (\\x.x))")
        assertNormalization("f x ((\\x.\\y.y x) (\\z.z y))", "f x (\\y'.y' (\\z.z y))")
        assertNormalization("y' x ((\\x.\\y.y x) (\\z.z y)) y''", "y' x (\\y'.y' (\\z.z y)) y''")
        assertNormalization("f x ((\\x.\\z.\\y.y x) (\\z.z y))", "f x (\\z.\\y'.y' (\\z.z y))")
        assertNormalization("(\\f.f \\y. \\x.f x) (x)", "(x \\y.\\x'.x x')")
        assertNormalization("(\\f.f x) \\x.x", "x")
        assertNormalization("(\\f.f \\x.x) \\x.x", "\\x.x")
        assertNormalization("(\\f.f x) (x)", "x x")
        assertNormalization("(\\y.\\f.f y x) (f)", "\\f'.f' f x")
        assertNormalization("(\\x.\\x.\\x.\\x.x) (\\x.x) ((\\x.x) (\\x.x)) x (\\y.x)", "\\y.x")
        assertNormalization("(\\x.\\x.x (\\x.\\x.x)) ((\\x.x) (\\x.x)) (\\y.x) x", "x x")
        assertNormalization("(\\a.\\x.\\x.a) x", "\\x'.\\x''.x")
        assertNormalization("(\\x.\\y.z y x) (y z)", "\\y'.z y' (y z)")
        assertNormalization("(\\x.\\y.x y) (\\x. x x) (\\y.x)", "x")
        assertNormalization("(\\f.f (f x)) (\\x.g (f x))", "g (f (g (f x)))")
        assertNormalization("(\\x. x ((\\x.x) y)) y", "(y y)")
        assertNormalization( "(\\x. x ((\\y.(\\x.x y)) (x x))) (\\f.f t)", "(t (t t))")
        assertNormalization("(\\a.\\x.\\x.x a) (x x)", "\\x'.\\x''.x'' (x x)")
        assertNormalization("(\\a.\\x.\\x.x a) (x x) $OMEGA $ID", "(x x)")
    }

    private fun assertNormalization(unnormalized: String, normalized: String) {
        val lambda = parser.parse(unnormalized)
        val normalizedLambda = parser.parse(normalized)
        assertEquals(normalizedLambda, lambda.normalized(), unnormalized)
    }

    companion object {
        private val OMEGA = "((\\x.x x) (\\x.x x))"
        private val ID = "(\\x.x)"
    }
}