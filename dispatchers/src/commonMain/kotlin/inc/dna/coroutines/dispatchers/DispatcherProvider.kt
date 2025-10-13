package inc.dna.coroutines.dispatchers

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.currentCoroutineContext

val CoroutineScope.dispatchers: DispatcherProvider
  get() = coroutineContext.dispatchers

suspend fun currentDispatchers(): DispatcherProvider = currentCoroutineContext().dispatchers

val CoroutineContext.dispatchers: DispatcherProvider
  get() = this[DispatchersContextElement]?.provider ?: DefaultDispatcherProvider

/**
 * API for getting a reference to the [CoroutineDispatcher]s for a given [CoroutineScope]. In
 * production this will return the values of [kotlinx.coroutines.Dispatchers]. In tests, this will
 * return a [DispatcherProvider] instance that uses the provided
 * [kotlinx.coroutines.test.TestDispatcher].
 */
interface DispatcherProvider {
  /**
   * Dispatcher for CPU-bound work. In production this will return the value of
   * [kotlinx.coroutines.Dispatchers.Default].
   */
  val default: CoroutineDispatcher

  /**
   * Dispatcher for disk/network IO work. In production this will return the value of
   * [kotlinx.coroutines.Dispatchers.IO].
   */
  val io: CoroutineDispatcher

  /**
   * Dispatcher for code that must run on the main thread. In production this will return the value
   * of [kotlinx.coroutines.Dispatchers.Main].
   *
   * When calling code is already running on the main thread, this will result in a dispatch that
   * executes after the current loop.
   */
  val main: CoroutineDispatcher

  /**
   * Dispatcher for code that must run on the main thread. In production this will return the value
   * of [kotlinx.coroutines.Dispatchers.Main].
   *
   * When calling code is already running on the main thread, this will run unconfined meaning that
   * the code is executed immediately.
   */
  val mainImmediate: CoroutineDispatcher

  /**
   * Dispatcher that is not confined to any specific thread. In production this will return the
   * value of [kotlinx.coroutines.Dispatchers.Unconfined].
   */
  val unconfined: CoroutineDispatcher
}

interface DispatchersContextElement : CoroutineContext.Element {
  val provider: DispatcherProvider

  companion object Key : CoroutineContext.Key<DispatchersContextElement>
}

private object DefaultDispatcherProvider : DispatcherProvider {
  override val unconfined: CoroutineDispatcher
    get() = Dispatchers.Unconfined

  override val default: CoroutineDispatcher
    get() = Dispatchers.Default

  override val io: CoroutineDispatcher
    get() = Dispatchers.IO

  override val main: CoroutineDispatcher
    get() = Dispatchers.Main

  override val mainImmediate: CoroutineDispatcher
    get() = Dispatchers.Main.immediate
}
