package inc.dna.coroutines.test

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.ExperimentalCoroutinesApi

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
