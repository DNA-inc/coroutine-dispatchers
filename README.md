# Dispatchers Context Element

Lightweight KMP library to simplify replacing real `CoroutineDispatcher` instances with `TestDispatcher` by passing around a [DispatcherProvider](dispatchers/src/commonMain/kotlin/inc/dna/coroutines/DispatcherProvider.kt) instance through the `CoroutineContext`.

## Examples

<details open>
    <summary><b>Classes without scope reference</b></summary>

Suspending methods can select the dispatcher to use with the top-level `currentDispatchers()` method that the library provides. In test code you just have to replace the `kotlinx.coroutines.test.runTest` import with `inc.dna.coroutines.test.runTest`.

```kotlin
class MyUseCase {
    suspend operator fun invoke() = withContext(currentDispatchers().io) {

    }
}
```

```kotlin
import inc.dna.coroutines.test.runTest

class MyUseCaseTest {
    fun `my test`() = runText {
        // Arrange
        val subject = MyUseCase()

        // Act
        subject.invoke()

        // Assert
    }
}
```

</details>
<details>
    <summary><b>Classes with constructor-injected CoroutineScope</b></summary>

For classes that have a constructor injected scope, like ViewModels you have to make sure that you pass in a scope which is either the `TestScope`, `backgroundScope` or a custom scope that [inherits the `DispatchersContextElement`](#coroutinecontext-inheritance) from the `TestScope.coroutineContext`.

Then you can use the extension property on `val CoroutineScope.dispatchers` to select the dispatcher that you need.

```kotlin
class MyViewModel(
    val scope: CoroutineScope,
) : ViewModel(scope) {

    private val defaultDispatcher = scope.dispatchers.default
}
```

```kotlin
import inc.dna.coroutines.test.runTest

class MyViewModelTest {
    @Test
    fun `my test`() = runTest {
        val subject = MyViewModel(
            scope = backgroundScope
        )
    }
}
```

</details>

<details>
    <summary>Classes that create and manage their own scope</summary>

If a class creates a CoroutineScope internally it should either use a constructor-injected CoroutineContext or a constructor-injected parent scope to build upon to ensure that the scope [inherits the `DispatchersContextElement`](#coroutinecontext-inheritance).

```kotlin
class MySelfContainedClass(
    val context: CoroutineContext = EmptyCoroutineContext,
) {
    val scope = CoroutineScope(context + SupervisorJob(parent = context.job) + context.dispatchers.default)

    fun release() {
        scope.cancel()
    }
}
```

```kotlin
import inc.dna.coroutines.test.runTest

class MyViewModelTest {
    @Test
    fun `my test`() = runTest {
        // Arrange
        val subject = MySelfContainedClass(
            scope = coroutineContext
        )

        subject.release()
    }
}
```

</details>

<details>
    <summary>Early access to the TestScope</summary>

Besides the top-level runTest, the `coroutines-test` artifact also allows early creation of the `TestScope` so that you can use it to instantiate dependencies when the test framework creates your test instance.

For this the library provides the [`inc.dna.coroutines.test.TestScope`](dispatchers-test/src/commonMain/kotlin/inc/dna/coroutines/test/TestScope.kt) top-level factory method which ensures that the `TestScope` is instantiated with the right `CoroutineContext` elements.

```kotlin
import inc.dna.coroutines.test.TestScope

class MyViewModelTest {

    val scope = TestScope()
    val subject = MySelfContainedClass(scope.backgrounScope)

    @Test
    fun `my test`() = scope.runTest {
        ...
    }
}
```

</details>

## CoroutineContext inheritance

The library works by making the `CoroutineContext` of the `runTest` method contain a [DispatcherContextElement](dispatchers/src/commonMain/kotlin/inc/dna/coroutines/DispatcherProvider.kt) which production code will look for. If it can't find the element in the current `CoroutineContext` real dispatchers will be used.

This means that it is important to structure your code in such a way that all scopes that are used or created during test execution inherit their `CoroutineContext` from the test, which should in any case be done because of structure concurrency rules.

## Dispatcher Mapping

The library has uses a default mapping for mapping real dispatchers to either `StandardTestDispatcher` or `UnconfinedTestDispatcher` according to the following mapping table

|                            | Prod                         | Test                       |
|----------------------------|------------------------------|----------------------------|
| `dispatches.default`       | `Dispatchers.Default`        | `StandardTestDispatcher`   |
| `dispatches.io`            | `Dispatchers.IO`             | `StandardTestDispatcher`   |
| `dispatches.main`          | `Dispatchers.Main`           | `UnconfinedTestDispatcher` |
| `dispatches.mainImmediate` | `Dispatchers.Main.immediate` | `UnconfinedTestDispatcher` |
| `dispatches.unconfined`    | `Dispatchers.Unconfined`     | `UnconfinedTestDispatcher` |

This behavior can be overridden using an extension method on the `DispatcherProvider` interface that is part of the `dispatchers-test` artifact.

```kotlin
fun `my test`() = runTest {
    dispatchers.setAll(::UnconfinedTestDispatcher)
    dispatchers.set(DispatcherId.IO, ::UnconfinedTestDispatcher)
    ..
}
```

## Dependencies

```kotlin
dependencies {
    implementation("inc.dna.coroutines:dispatchers:<latest-version>")
    testImplementation("inc.dna.coroutines:dispatchers-test:<latest-version>")
}
```

For KMP projects

```kotlin
kotlin {
    sourceSets {
        commonMain {
            implementation("inc.dna.coroutines:dispatchers:<latest-version>")
        }
        commonTest {
            implementation("inc.dna.coroutines:dispatchers-test:<latest-version>")
        }
    }
}
```
