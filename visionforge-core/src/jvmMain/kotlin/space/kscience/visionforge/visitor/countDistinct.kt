package space.kscience.visionforge.visitor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.util.concurrent.atomic.AtomicInteger

public suspend fun <T, K> Flow<T>.countDistinctBy(selector: (T) -> K): Map<K, Int> {
    val counter = LinkedHashMap<K, AtomicInteger>()
    collect {
        val key = selector(it)
        counter.getOrPut(key) { AtomicInteger() }.incrementAndGet()
    }
    return counter.mapValues { it.value.toInt() }
}

public suspend fun <T> Flow<T>.countDistinct(): Map<T, Int> = countDistinctBy { it }