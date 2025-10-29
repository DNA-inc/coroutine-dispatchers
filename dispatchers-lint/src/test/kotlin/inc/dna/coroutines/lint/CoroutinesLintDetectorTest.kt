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
            @@ -3 +3 @@
            -import kotlinx.coroutines.test.runTest
            +import inc.dna.coroutines.test.runTest
            """
                .trimIndent())
  }

  fun testFrameworkMethodInvocationWithoutImport() {
    runTest(
            """
            package inc.dna.coroutines.test

            class MyTest {
                @Test
                fun test() = kotlinx.coroutines.test.runTest {}
            }
            """)
        .expect(
            """
            src/inc/dna/coroutines/test/MyTest.kt:5: Error: Use inc.dna.coroutines.test.runTest or pass a TestDispatchersContext to runTest to ensure that dispatchers are properly replaced. [UseTestContextIssue]
                fun test() = kotlinx.coroutines.test.runTest {}
                                                     ~~~~~~~
            1 error
            """
                .trimIndent())
        .expectFixDiffs(
            """
            Fix for src/inc/dna/coroutines/test/MyTest.kt line 5: Replace with inc.dna.coroutines.test.runTest:
            @@ -5 +5 @@
            -    fun test() = kotlinx.coroutines.test.runTest {}
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
            @@ -3 +3 @@
            -import kotlinx.coroutines.test.TestScope
            +import inc.dna.coroutines.test.TestScope
            """
                .trimIndent())
  }

  fun testTestScopeCreationWithoutImport() {
    runTest(
            """
            package inc.dna.coroutines.test

            class MyTest {
                val scope = kotlinx.coroutines.test.TestScope()
            }
            """)
        .expect(
            """
            src/inc/dna/coroutines/test/MyTest.kt:4: Error: Use inc.dna.coroutines.test.TestScope() to ensure test dispatcher replacement. [UseTestContextIssue]
                val scope = kotlinx.coroutines.test.TestScope()
                                                    ~~~~~~~~~
            1 error
            """
                .trimIndent())
        .expectFixDiffs(
            """
            Fix for src/inc/dna/coroutines/test/MyTest.kt line 4: Replace with inc.dna.coroutines.test.TestScope:
            @@ -4 +4 @@
            -    val scope = kotlinx.coroutines.test.TestScope()
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
