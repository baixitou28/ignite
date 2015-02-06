package org.apache.ignite.internal.processors.cache.local;

import org.apache.ignite.*;

import javax.cache.processor.*;

/**
 * Local atomic cache metrics test with tck specific.
 */
public class GridCacheAtomicLocalTckMetricsSelfTestImpl extends GridCacheAtomicLocalMetricsSelfTest {
    /**
     * @throws Exception If failed.
     */
    public void testEntryProcessorRemove() throws Exception {
        IgniteCache<Integer, Integer> cache = grid(0).jcache(null);

        cache.put(1, 20);

        int result = cache.invoke(1, new EntryProcessor<Integer, Integer, Integer>() {
            @Override public Integer process(MutableEntry<Integer, Integer> entry, Object... arguments)
                    throws EntryProcessorException {
                Integer result = entry.getValue();

                entry.remove();

                return result;
            }
        });

        assertEquals(1L, cache.metrics().getCachePuts());

        assertEquals(20, result);
        assertEquals(1L, cache.metrics().getCacheHits());
        assertEquals(100.0f, cache.metrics().getCacheHitPercentage());
        assertEquals(0L, cache.metrics().getCacheMisses());
        assertEquals(0f, cache.metrics().getCacheMissPercentage());
        assertEquals(1L, cache.metrics().getCachePuts());
        assertEquals(1L, cache.metrics().getCacheRemovals());
        assertEquals(0L, cache.metrics().getCacheEvictions());
        assert cache.metrics().getAveragePutTime() >= 0;
        assert cache.metrics().getAverageGetTime() >= 0;
        assert cache.metrics().getAverageRemoveTime() >= 0;
    }

    /**
     * @throws Exception If failed.
     */
    public void testCacheStatistics() throws Exception {
        IgniteCache<Integer, Integer> cache = grid(0).jcache(null);

        cache.put(1, 10);

        assertEquals(0, cache.metrics().getCacheRemovals());
        assertEquals(1, cache.metrics().getCachePuts());

        cache.remove(1);

        assertEquals(0, cache.metrics().getCacheHits());
        assertEquals(1, cache.metrics().getCacheRemovals());
        assertEquals(1, cache.metrics().getCachePuts());

        cache.remove(1);

        assertEquals(0, cache.metrics().getCacheHits());
        assertEquals(0, cache.metrics().getCacheMisses());
        assertEquals(1, cache.metrics().getCacheRemovals());
        assertEquals(1, cache.metrics().getCachePuts());

        cache.put(1, 10);
        assertTrue(cache.remove(1, 10));

        assertEquals(1, cache.metrics().getCacheHits());
        assertEquals(0, cache.metrics().getCacheMisses());
        assertEquals(2, cache.metrics().getCacheRemovals());
        assertEquals(2, cache.metrics().getCachePuts());

        assertFalse(cache.remove(1, 10));

        assertEquals(1, cache.metrics().getCacheHits());
        assertEquals(1, cache.metrics().getCacheMisses());
        assertEquals(2, cache.metrics().getCacheRemovals());
        assertEquals(2, cache.metrics().getCachePuts());
    }

    /**
     * @throws Exception If failed.
     */
    public void testConditionReplace() throws Exception {
        IgniteCache<Integer, Integer> cache = grid(0).jcache(null);

        long hitCount = 0;
        long missCount = 0;
        long putCount = 0;

        boolean result = cache.replace(1, 0, 10);

        ++missCount;
        assertFalse(result);

        assertEquals(missCount, cache.metrics().getCacheMisses());
        assertEquals(hitCount, cache.metrics().getCacheHits());
        assertEquals(putCount, cache.metrics().getCachePuts());

        assertFalse(cache.containsKey(1));

        cache.put(1, 10);
        ++putCount;

        assertEquals(missCount, cache.metrics().getCacheMisses());
        assertEquals(hitCount, cache.metrics().getCacheHits());
        assertEquals(putCount, cache.metrics().getCachePuts());

        assertTrue(cache.containsKey(1));

        result = cache.replace(1, 10, 20);

        assertTrue(result);
        ++hitCount;
        ++putCount;

        assertEquals(missCount, cache.metrics().getCacheMisses());
        assertEquals(hitCount, cache.metrics().getCacheHits());
        assertEquals(putCount, cache.metrics().getCachePuts());

        result = cache.replace(1, 40, 50);

        assertFalse(result);
        ++hitCount;

        assertEquals(hitCount, cache.metrics().getCacheHits());
        assertEquals(putCount, cache.metrics().getCachePuts());
        assertEquals(missCount, cache.metrics().getCacheMisses());
    }

    /**
     * @throws Exception If failed.
     */
    public void testPutIfAbsent() throws Exception {
        IgniteCache<Integer, Integer> cache = grid(0).jcache(null);

        long hitCount = 0;
        long missCount = 0;
        long putCount = 0;

        boolean result = cache.putIfAbsent(1, 1);

        ++putCount;
        assertTrue(result);

        assertEquals(missCount, cache.metrics().getCacheMisses());
        assertEquals(hitCount, cache.metrics().getCacheHits());
        assertEquals(putCount, cache.metrics().getCachePuts());

        result = cache.putIfAbsent(1, 1);

        assertFalse(result);
        assertEquals(hitCount, cache.metrics().getCacheHits());
        assertEquals(putCount, cache.metrics().getCachePuts());
        assertEquals(missCount, cache.metrics().getCacheMisses());
    }
}
