# Dispatchers Context Element

Lightweight library to support test `CoroutineDispatcher` injection from unit tests through the `CoroutineContext`.

## Usage

```kotlin
// without this library
suspend fun doSomething(dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    withContext(dispatcher) { ... }
}

// with this library
suspend fun doSomething() {
    withContext(currentDispatchers().io) { ... }
}
```

## Dependencies

```kotlin
dependencies {
    implementation("inc.dna.coroutines:dispatchers:<latest-version>")
    testImplementation("inc.dna.coroutines:dispatchers-test:<latest-version>")
}
```
