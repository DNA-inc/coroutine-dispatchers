package inc.dna.coroutines.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.junit.jupiter.api.Test

class CoroutinesLintDetectorTest : LintDetectorTest() {
  override fun getDetector() = CoroutinesLintDetector()

  override fun getIssues() = listOf(CoroutinesLintDetector.UseTestContextIssue)

  @Test
  fun `invoking framework runTest without context reports`() {
    lint()
        .files(
            *stubs.toTypedArray(),
            kotlin(
                """
                package inc.dna.coroutines.test

                import kotlinx.coroutines.test.runTest

                class MyTest {
                    @Test
                    fun test() = runTest {}
                }
                """
                    .trimIndent()),
        )
        .run()
        .expect(
            """
            src/inc/dna/coroutines/test/MyTest.kt:7: Error: Use inc.dna.coroutines.test.runTest or pass a TestDispatchersContext to runTest to ensure that dispatchers are properly replaced. [UseTestContextIssue]
                fun test() = runTest {}
                             ~~~~~~~
            1 error
            """
                .trimIndent())
        .expectFixDiffs(
            """
            Fix for src/inc/dna/coroutines/test/MyTest.kt line 7: Replace with inc.dna.coroutines.test.runTest:
            @@ -3,0 +4 @@
            +import org.jetbrains.kotlinx.coroutines.test.runTest
            @@ -7 +8 @@
            -    fun test() = runTest {}
            +    fun test() = inc.dna.coroutines.test.runTest {}
            """
                .trimIndent())
  }

  @Test
  fun `invoking framework runTest with context is clean`() {
    // although this is technically incorrect, we don't want to flag it because we can't reliable
    // track where the
    // custom context is coming from.
    lint()
        .files(
            *stubs.toTypedArray(),
            kotlin(
                """
                package inc.dna.coroutines.test

                import kotlinx.coroutines.EmptyCoroutineContext
                import kotlinx.coroutines.test.runTest

                class MyTest {
                    val context = EmptyCoroutineContext
                    @Test
                    fun test() = runTest(context) {}
                }
                """
                    .trimIndent()),
        )
        .run()
        .expectClean()
  }

  @Test
  fun `invoking custom runTest with context is clean`() {
    lint()
        .files(
            *stubs.toTypedArray(),
            kotlin(
                """
                package inc.dna.coroutines.test

                import inc.dna.coroutines.test.runTest
                import kotlinx.coroutines.EmptyCoroutineContext

                class MyTest {
                    @Test
                    fun test() = runTest {}
                }
                """
                    .trimIndent()),
        )
        .run()
        .expectClean()
  }

  companion object {
    val coroutineStubs =
        kotlin(
            """
            package kotlinx.coroutines
            
            interface CoroutineContext
            
            object EmptyCoroutineContext : CoroutineContext
            """
                .trimIndent())

    val coroutineTestStubs =
        kotlin(
            """
            package kotlinx.coroutines.test

            import kotlinx.coroutines.CoroutineContext
            import kotlinx.coroutines.EmptyCoroutineContext

            fun runTest(context: CoroutineContext = EmptyCoroutineContext, block: suspend () -> Unit) {
                block()
            }   
            """
                .trimIndent())
    val dnaRunTestStub =
        kotlin(
            """
            package inc.dna.coroutines.test

            fun runTest(block: suspend () -> Unit) {
                block()
            }   
            """
                .trimIndent())
    val stubs = listOf(coroutineStubs, coroutineTestStubs, dnaRunTestStub)
  }
}
