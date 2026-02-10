package inc.dna.coroutines.test

import inc.dna.coroutines.DispatchersContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest as realRunTest
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** typealias to allow linters to flag all imports of [kotlinx.coroutines.test.TestScope]. */
typealias TestScope = kotlinx.coroutines.test.TestScope

/**
 * Wrapper around [kotlinx.coroutines.test.TestScope] factory method that ensures that
 * [inc.dna.coroutines.DispatcherProvider] is a [TestDispatchers] instance.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope(context: CoroutineContext = EmptyCoroutineContext): TestScope {
  return kotlinx.coroutines.test.TestScope(createTestContext(context))
}

/**
 * Wrapper around the [kotlinx.coroutines.test.runTest] extension method of [TestScope]
 */
fun TestScope.runTest(
  timeout: Duration = 10.seconds,
  testBody: suspend kotlinx.coroutines.test.TestScope.() -> Unit
) {
  check(coroutineContext[DispatchersContextElement] != null) {
    "TestScope does not contain a DispatchersContextElement which is required to use dispatcher" +
      " injection."
  }
  this.realRunTest(timeout, testBody)
}
