package inc.dna.coroutines.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.intellij.lang.annotations.Language

class CoroutinesLintDetectorTest : LintDetectorTest() {
  override fun getDetector() = TestContextLintDetector()

  override fun getIssues() = listOf(TestContextLintDetector.UseTestContextIssue)

  fun testFrameworkMethodInvocation() {
    runTest(
            """
            package inc.dna.coroutines.test

            import kotlinx.coroutines.test.runTest

            class MyTest {
                @Test
                fun test() = runTest {}
            }
            """)
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
            @@ -7 +7 @@
            -    fun test() = runTest {}
            +    fun test() = inc.dna.coroutines.test.runTest {}
            """
                .trimIndent())
  }

  fun testFrameworkMethodInvocationWithContext() {
    // although this is technically incorrect, we don't want to flag it because we can't reliable
    // track where the
    // custom context is coming from.
    runTest(
            """
            package inc.dna.coroutines.test

            import kotlinx.coroutines.EmptyCoroutineContext
            import kotlinx.coroutines.test.runTest

            class MyTest {
                val context = EmptyCoroutineContext
                @Test
                fun test() = runTest(context) {}
            }
            """)
        .expectClean()
  }

  fun testLibraryMethodInvocation() {
    runTest(
            """
            package inc.dna.coroutines.test

            import inc.dna.coroutines.test.runTest
            import kotlinx.coroutines.EmptyCoroutineContext

            class MyTest {
                @Test
                fun test() = runTest {}
            }
            """)
        .expectClean()
  }

  fun testTestScopeCreation() {
    runTest(
            """
            package inc.dna.coroutines.test

            import kotlinx.coroutines.test.TestScope

            class MyTest {
                val scope = TestScope()
            }
            """)
        .expect(
            """
            src/inc/dna/coroutines/test/MyTest.kt:6: Error: Use inc.dna.coroutines.test.TestScope() to ensure test dispatcher replacement. [UseTestContextIssue]
                val scope = TestScope()
                            ~~~~~~~~~
            1 error
            """
                .trimIndent())
        .expectFixDiffs(
            """
            Fix for src/inc/dna/coroutines/test/MyTest.kt line 6: Replace with inc.dna.coroutines.test.TestScope:
            @@ -6 +6 @@
            -    val scope = TestScope()
            +    val scope = inc.dna.coroutines.test.TestScope()
            """
                .trimIndent())
  }

  private fun runTest(@Language("kotlin") code: String) =
      lint()
          .files(
              kotlin(code.trimIndent()),
              kotlin(
                  """
                  package kotlinx.coroutines

                  interface CoroutineContext

                  object EmptyCoroutineContext : CoroutineContext
                  """
                      .trimIndent()),
              kotlin(
                  """
                  package kotlinx.coroutines.test

                  import kotlinx.coroutines.CoroutineContext
                  import kotlinx.coroutines.EmptyCoroutineContext

                  interface TestScope

                  fun TestScope(context: CoroutineContext = EmptyCoroutineCOntext): TestScope = TODO()

                  fun runTest(context: CoroutineContext = EmptyCoroutineContext, block: suspend () -> Unit) {
                    block()
                  }
                  """
                      .trimIndent()),
              kotlin(
                  """
                  package inc.dna.coroutines.test

                  fun runTest(block: suspend () -> Unit) {
                      block()
                  }
                  """
                      .trimIndent()),
          )
          .run()
}
