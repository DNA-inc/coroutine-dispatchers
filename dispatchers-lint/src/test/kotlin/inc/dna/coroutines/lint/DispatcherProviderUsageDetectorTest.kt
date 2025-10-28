package inc.dna.coroutines.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import org.intellij.lang.annotations.Language

class DispatcherProviderUsageDetectorTest : LintDetectorTest() {
  override fun getDetector() = TestContextLintDetector()

  override fun getIssues() = listOf(DispatcherProviderUsageDetector.UseDispatcherProviderIssue)

  fun testRealDispatcherUsage() {
    runTest(
            """
            package inc.dna.coroutines.test

            import kotlinx.coroutines.Dispatchers
            import kotlinx.coroutines.withContext

            suspend fun myMethod() {
              withContext(Dispatchers.IO) {}
              withContext(Dispatchers.Default) {}
              withContext(Dispatchers.Main) {}
              withContext(Dispatchers.Main.immediate) {}
            }
            """)
        .expect(
            """
            src/inc/dna/coroutines/test/test.kt:7: Error: Dispatchers should not be used directly. Use the DispatcherProvider instead. [UseDispatcherProvider]
              withContext(Dispatchers.IO) {}
                          ~~~~~~~~~~~~~~
            src/inc/dna/coroutines/test/test.kt:8: Error: Dispatchers should not be used directly. Use the DispatcherProvider instead. [UseDispatcherProvider]
              withContext(Dispatchers.Default) {}
                          ~~~~~~~~~~~~~~~~~~~
            src/inc/dna/coroutines/test/test.kt:9: Error: Dispatchers should not be used directly. Use the DispatcherProvider instead. [UseDispatcherProvider]
              withContext(Dispatchers.Main) {}
                          ~~~~~~~~~~~~~~~~
            src/inc/dna/coroutines/test/test.kt:10: Error: Dispatchers should not be used directly. Use the DispatcherProvider instead. [UseDispatcherProvider]
              withContext(Dispatchers.Main.immediate) {}
                          ~~~~~~~~~~~~~~~~~~~~~~~~~~
            4 errors
            """
                .trimIndent())
        .expectFixDiffs(
            """
            Fix for src/inc/dna/coroutines/test/test.kt line 7: Use currentDispatchers():
            @@ -2,0 +3 @@
            +import inc.dna.coroutines.currentDispatchers
            @@ -7 +8 @@
            -  withContext(Dispatchers.IO) {}
            +  withContext(currentDispatchers().io) {}
            Fix for src/inc/dna/coroutines/test/test.kt line 8: Use currentDispatchers():
            @@ -2,0 +3 @@
            +import inc.dna.coroutines.currentDispatchers
            @@ -8 +9 @@
            -  withContext(Dispatchers.Default) {}
            +  withContext(currentDispatchers().default) {}
            Fix for src/inc/dna/coroutines/test/test.kt line 9: Use currentDispatchers():
            @@ -2,0 +3 @@
            +import inc.dna.coroutines.currentDispatchers
            @@ -9 +10 @@
            -  withContext(Dispatchers.Main) {}
            +  withContext(currentDispatchers().main) {}
            Fix for src/inc/dna/coroutines/test/test.kt line 10: Use currentDispatchers():
            @@ -2,0 +3 @@
            +import inc.dna.coroutines.currentDispatchers
            @@ -10 +11 @@
            -  withContext(Dispatchers.Main.immediate) {}
            +  withContext(currentDispatchers().mainImmediate) {}
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

                  interface CoroutineDispatcher
                  interface MainDispatcher : CoroutineDispatcher {
                    val immediate: CoroutineDispatcher
                  }

                  private object DummyCoroutineDispatcher : MainDispatcher {
                    override val immediate: CoroutineDispatcher get() = this
                  }

                  suspend fun withContext(context: CoroutineDispatcher, block: suspend () -> Unit) {}

                  object Dispatchers {
                      val Main: MainDispatcher = DummyCoroutineDispatcher
                      val IO: CoroutineDispatcher = DummyCoroutineDispatcher
                      val Default: CoroutineDispatcher = DummyCoroutineDispatcher
                  }
                  """
                      .trimIndent()),
          )
          .run()
}
