@file:OptIn(ExperimentalCoroutinesApi::class)

package inc.dna.coroutines.dispatchers

import inc.dna.coroutines.dispatchers.test.DispatcherId
import inc.dna.coroutines.dispatchers.test.createTestContext
import inc.dna.coroutines.dispatchers.test.set
import inc.dna.coroutines.dispatchers.test.setAll
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.withContext

class RunTes4tTest {

  @Test
  fun `default runTest method`() =
      kotlinx.coroutines.test.runTest {
        assertFalse(dispatchers.io is TestDispatcher)
        assertSame(dispatchers.io, Dispatchers.IO)
      }

  @Test
  fun `custom runTest method`() =
      inc.dna.coroutines.dispatchers.test.runTest { assertIs<TestDispatcher>(dispatchers.io) }

  @Test
  fun `default runTest method with custom context`() =
      inc.dna.coroutines.dispatchers.test.runTest(createTestContext()) {
        assertIs<TestDispatcher>(dispatchers.io)
      }

  @Test
  fun `default behavior for standard dispatchers`() =
      inc.dna.coroutines.dispatchers.test.runTest {
        val jobs =
            listOf(
                launch(dispatchers.io) {},
                launch(dispatchers.default) {},
            )
        jobs.forEach { assertFalse(it.isCompleted) }

        runCurrent()

        jobs.forEach { assertTrue(it.isCompleted) }
      }

  @Test
  fun `adjusted behavior for standard dispatchers`() =
      inc.dna.coroutines.dispatchers.test.runTest {
        dispatchers.set(DispatcherId.IO, ::UnconfinedTestDispatcher)
        dispatchers.set(DispatcherId.Default, ::UnconfinedTestDispatcher)
        dispatchers.set(DispatcherId.Main, ::UnconfinedTestDispatcher)
        val jobs =
            listOf(
                launch(dispatchers.io) {},
                launch(dispatchers.default) {},
                launch(dispatchers.main) {},
            )
        jobs.forEach { assertTrue(it.isCompleted) }
      }

  @Test
  fun `default behavior for unconfined dispatchers`() =
      inc.dna.coroutines.dispatchers.test.runTest {
        val jobs =
            listOf(
                launch(dispatchers.main) {},
                launch(dispatchers.mainImmediate) {},
                launch(dispatchers.unconfined) {},
            )
        jobs.forEach { assertTrue(it.isCompleted) }

        runCurrent()
      }

  @Test
  fun `adjusted behavior for unconfined dispatchers`() =
      inc.dna.coroutines.dispatchers.test.runTest {
        dispatchers.set(DispatcherId.Main, ::StandardTestDispatcher)
        dispatchers.set(DispatcherId.MainImmediate, ::StandardTestDispatcher)
        dispatchers.set(DispatcherId.Unconfined, ::StandardTestDispatcher)
        val jobs =
            listOf(
                launch(dispatchers.main) {},
                launch(dispatchers.mainImmediate) {},
                launch(dispatchers.unconfined) {},
            )
        jobs.forEach { assertFalse(it.isCompleted) }

        runCurrent()
      }

  @Test
  fun `set different io dispatcher`() =
      inc.dna.coroutines.dispatchers.test.runTest {
        dispatchers.set(DispatcherId.IO, ::UnconfinedTestDispatcher)
        var executed = false
        launch(dispatchers.io) { executed = true }
        assertTrue(executed)
      }

  @Test
  fun `setAll can make all dispatchers unconfined`() =
      inc.dna.coroutines.dispatchers.test.runTest {
        dispatchers.setAll(::UnconfinedTestDispatcher)
        val allJobs =
            listOf(
                launch(dispatchers.io) {},
                launch(dispatchers.default) {},
                launch(dispatchers.main) {},
                launch(dispatchers.mainImmediate) {},
                launch(dispatchers.unconfined) {},
            )
        allJobs.forEach { assertTrue(it.isCompleted) }
      }

  @Test
  fun `setAll can make all dispatchers starndar`() =
      inc.dna.coroutines.dispatchers.test.runTest {
        dispatchers.setAll(::StandardTestDispatcher)
        val allJobs =
            listOf(
                launch(dispatchers.io) {},
                launch(dispatchers.default) {},
                launch(dispatchers.main) {},
                launch(dispatchers.mainImmediate) {},
                launch(dispatchers.unconfined) {},
            )
        allJobs.forEach { assertFalse(it.isCompleted) }

        runCurrent()

        allJobs.forEach { assertTrue(it.isCompleted) }
      }

  @Test
  fun `run blocking gives real dispatchers`() = runBlocking {
    assertFalse(dispatchers.io is TestDispatcher)
    assertSame(dispatchers.io, Dispatchers.IO)
  }

  @Test
  fun `delays on injected dispatcher follow the time of the TestScope`() {
    inc.dna.coroutines.dispatchers.test.runTest {
      var didStart = false
      val job = launch {
        didStart = true
        withContext(currentDispatchers().io) { delay(10.seconds) }
      }
      assertFalse(didStart)
      runCurrent()
      assertTrue(didStart)
      assertFalse(job.isCompleted)
      advanceTimeBy(10.seconds)
      runCurrent()
      assertTrue(job.isCompleted)
    }
  }
}
