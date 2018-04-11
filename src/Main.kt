import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author  Vadim Semenov (semenov@rain.ifmo.ru)
 */
fun main(args: Array<String>) {
    val input = if (args.size > 0) Files.newInputStream(Paths.get(args[0])) else System.`in`
    val output = if (args.size > 1) Files.newOutputStream(Paths.get(args[1])) else System.out
    BufferedReader(InputStreamReader(input)).use { reader ->
        PrintWriter(output).use { writer ->
            val parser = LambdaParser()
            while (true) {
                val line = reader.readLine() ?: return
                if (line.isNotBlank()) {
                    val startParsing = System.currentTimeMillis()
                    val lambda = parser.parse(line)
                    System.err.println("Parsed in ${System.currentTimeMillis() - startParsing} ms:".padEnd(24) + lambda)
                    val startNormalization = System.currentTimeMillis()
                    val normalized = lambda.normalized()
                    writer.println(normalized)
                    System.err.println("Normalized in ${System.currentTimeMillis() - startNormalization} ms:".padEnd(24) + normalized)
                }
            }
        }
    }
}