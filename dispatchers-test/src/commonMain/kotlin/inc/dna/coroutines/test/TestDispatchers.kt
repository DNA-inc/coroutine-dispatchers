package inc.dna.coroutines.test

import inc.dna.coroutines.DispatcherProvider
import inc.dna.coroutines.DispatchersContextElement
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Wrapper around [kotlinx.coroutines.test.runTest] that ensures that the [DispatcherProvider] is a
 * [TestDispatchers] instance.
 */
fun runTest(
    context: CoroutineContext = EmptyCoroutineContext,
    timeout: Duration = 10.seconds,
    testBody: suspend TestScope.() -> Unit,
) = kotlinx.coroutines.test.runTest(createTestContext(context), timeout) { testBody() }

/**
 * Creates a [CoroutineContext] with a [TestDispatchers] instance and the [TestCoroutineScheduler]
 * which that [TestDispatchers] instance uses.
 */
fun createTestContext(context: CoroutineContext = EmptyCoroutineContext): CoroutineContext {
  val testDispatchers =
      (context[DispatchersContextElement] as? TestDispatchers)
          ?: run {
            val scheduler =
                context[TestCoroutineScheduler]
                    ?: (context[CoroutineDispatcher] as? TestDispatcher)?.scheduler
                    ?: TestCoroutineScheduler()
            TestDispatchers(scheduler)
          }
  return context + testDispatchers.scheduler + testDispatchers
}

class TestDispatchers(
    internal val scheduler: TestCoroutineScheduler,
) :
    AbstractCoroutineContextElement(key = DispatchersContextElement),
    DispatcherProvider,
    DispatchersContextElement {

  internal val dispatchersMap = mutableMapOf<DispatcherId, CoroutineDispatcher>()

  override val provider: DispatcherProvider = this

  override val default: CoroutineDispatcher
    get() = get(DispatcherId.Default)

  override val io: CoroutineDispatcher
    get() = get(DispatcherId.IO)

  override val main: CoroutineDispatcher
    get() = get(DispatcherId.Main)

  override val mainImmediate: CoroutineDispatcher
    get() = get(DispatcherId.MainImmediate)

  override val unconfined: CoroutineDispatcher
    get() = get(DispatcherId.Unconfined)

  @OptIn(ExperimentalCoroutinesApi::class)
  fun get(id: DispatcherId): CoroutineDispatcher =
      dispatchersMap.getOrPut(id) {
        if (id.isUnconfinedByDefault) UnconfinedTestDispatcher(scheduler)
        else StandardTestDispatcher(scheduler)
      }
}

sealed class DispatcherId(open val isUnconfinedByDefault: Boolean) {
  data object IO : DispatcherId(isUnconfinedByDefault = false)

  data object Default : DispatcherId(isUnconfinedByDefault = false)

  data object Main : DispatcherId(isUnconfinedByDefault = true)

  data object MainImmediate : DispatcherId(isUnconfinedByDefault = true)

  data object Unconfined : DispatcherId(isUnconfinedByDefault = true)
}

fun DispatcherProvider.set(
    id: DispatcherId,
    dispatcher: (TestCoroutineScheduler) -> CoroutineDispatcher,
) {
  check(this is TestDispatchers) { "Dispatchers must be a TestDispatchers instance" }
  dispatchersMap[id] = dispatcher(scheduler) as TestDispatcher
}

fun DispatcherProvider.setAll(create: (TestCoroutineScheduler) -> CoroutineDispatcher) {
  check(this is TestDispatchers) { "Dispatchers must be a TestDispatchers instance" }
  val dispatcher = create(scheduler)
  dispatchersMap[DispatcherId.IO] = dispatcher
  dispatchersMap[DispatcherId.Default] = dispatcher
  dispatchersMap[DispatcherId.Main] = dispatcher
  dispatchersMap[DispatcherId.MainImmediate] = dispatcher
  dispatchersMap[DispatcherId.Unconfined] = dispatcher
}
