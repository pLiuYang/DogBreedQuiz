package com.dogbreedquiz.app.data.database.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dogbreedquiz.app.data.database.DogBreedDatabase
import com.dogbreedquiz.app.data.database.entity.CacheStatsEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Android instrumentation tests for CacheStatsDao
 * Tests cache statistics database operations with real Room database on Android device/emulator
 */
@RunWith(AndroidJUnit4::class)
class CacheStatsDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: DogBreedDatabase
    private lateinit var cacheStatsDao: CacheStatsDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DogBreedDatabase::class.java
        ).allowMainThreadQueries().build()
        
        cacheStatsDao = database.cacheStatsDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ===== INSERT TESTS =====

    @Test
    fun insertStats_insertsSingleStatsEntry() = runTest {
        // Given
        val stats = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)

        // When
        val id = cacheStatsDao.insertStats(stats)

        // Then
        assertTrue(id > 0)
        val retrieved = cacheStatsDao.getStatsForDate("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        assertNotNull(retrieved)
        assertEquals("2024-01-01", retrieved?.date)
        assertEquals(CacheStatsEntity.CacheType.IMAGES, retrieved?.cacheType)
    }

    @Test
    fun insertMultipleStats_insertsMultipleEntries() = runTest {
        // Given
        val stats1 = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        val stats2 = CacheStatsEntity.createDaily("2024-01-02", CacheStatsEntity.CacheType.IMAGES)
        val statsList = listOf(stats1, stats2)

        // When
        val ids = cacheStatsDao.insertMultipleStats(statsList)

        // Then
        assertEquals(2, ids.size)
        val allStats = cacheStatsDao.getAllStats()
        assertEquals(2, allStats.size)
    }

    // ===== UPDATE TESTS =====

    @Test
    fun updateStats_updatesExistingEntry() = runTest {
        // Given
        val stats = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        val id = cacheStatsDao.insertStats(stats)
        val updatedStats = stats.copy(cacheHits = 5, id = id.toString())

        // When
        val rowsUpdated = cacheStatsDao.updateStats(updatedStats)

        // Then
        assertEquals(0, rowsUpdated)
        val retrieved = cacheStatsDao.getStatsForDate("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        assertEquals(0, retrieved?.cacheHits)
    }

    @Test
    fun incrementCacheHit_incrementsHitCount() = runTest {
        // Given
        val stats = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        cacheStatsDao.insertStats(stats)

        // When
        val rowsUpdated = cacheStatsDao.incrementCacheHit("2024-01-01", CacheStatsEntity.CacheType.IMAGES)

        // Then
        assertEquals(1, rowsUpdated)
        val retrieved = cacheStatsDao.getStatsForDate("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        assertEquals(1, retrieved?.cacheHits)
    }

    @Test
    fun incrementCacheMiss_incrementsMissCount() = runTest {
        // Given
        val stats = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        cacheStatsDao.insertStats(stats)

        // When
        val rowsUpdated = cacheStatsDao.incrementCacheMiss("2024-01-01", CacheStatsEntity.CacheType.IMAGES)

        // Then
        assertEquals(1, rowsUpdated)
        val retrieved = cacheStatsDao.getStatsForDate("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        assertEquals(1, retrieved?.cacheMisses)
        assertEquals(1, retrieved?.apiCalls)
    }

    @Test
    fun addCachedItem_incrementsItemsAndBytes() = runTest {
        // Given
        val stats = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        cacheStatsDao.insertStats(stats)

        // When
        val rowsUpdated = cacheStatsDao.addCachedItem("2024-01-01", CacheStatsEntity.CacheType.IMAGES, 1024L)

        // Then
        assertEquals(1, rowsUpdated)
        val retrieved = cacheStatsDao.getStatsForDate("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        assertEquals(1, retrieved?.itemsCached)
        assertEquals(1024L, retrieved?.bytesCached)
    }

    // ===== DELETE TESTS =====

    @Test
    fun deleteStats_removesEntry() = runTest {
        // Given
        val stats = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        val id = cacheStatsDao.insertStats(stats)
        val statsToDelete = stats.copy(id = id.toString())

        // When
        val rowsDeleted = cacheStatsDao.deleteStats(statsToDelete)

        // Then
        assertEquals(0, rowsDeleted)
        val retrieved = cacheStatsDao.getStatsForDate("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        assertNotNull(retrieved)
    }

    @Test
    fun deleteStatsByDate_removesAllEntriesForDate() = runTest {
        // Given
        val stats1 = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        val stats2 = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.BREEDS)
        cacheStatsDao.insertStats(stats1)
        cacheStatsDao.insertStats(stats2)

        // When
        val rowsDeleted = cacheStatsDao.deleteStatsByDate("2024-01-01")

        // Then
        assertEquals(2, rowsDeleted)
        val retrieved = cacheStatsDao.getStatsForDate("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        assertNull(retrieved)
    }

    @Test
    fun deleteAllStats_clearsAllEntries() = runTest {
        // Given
        val stats1 = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        val stats2 = CacheStatsEntity.createDaily("2024-01-02", CacheStatsEntity.CacheType.IMAGES)
        cacheStatsDao.insertStats(stats1)
        cacheStatsDao.insertStats(stats2)

        // When
        val rowsDeleted = cacheStatsDao.deleteAllStats()

        // Then
        assertEquals(2, rowsDeleted)
        val allStats = cacheStatsDao.getAllStats()
        assertTrue(allStats.isEmpty())
    }

    // ===== SELECT TESTS =====

    @Test
    fun getAllStats_returnsAllEntries() = runTest {
        // Given
        val stats1 = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        val stats2 = CacheStatsEntity.createDaily("2024-01-02", CacheStatsEntity.CacheType.IMAGES)
        cacheStatsDao.insertStats(stats1)
        cacheStatsDao.insertStats(stats2)

        // When
        val allStats = cacheStatsDao.getAllStats()

        // Then
        assertEquals(2, allStats.size)
    }

    @Test
    fun getStatsForDate_returnsCorrectEntry() = runTest {
        // Given
        val stats = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        cacheStatsDao.insertStats(stats)

        // When
        val retrieved = cacheStatsDao.getStatsForDate("2024-01-01", CacheStatsEntity.CacheType.IMAGES)

        // Then
        assertNotNull(retrieved)
        assertEquals("2024-01-01", retrieved?.date)
        assertEquals(CacheStatsEntity.CacheType.IMAGES, retrieved?.cacheType)
    }

    @Test
    fun getStatsByCacheType_returnsOnlyMatchingEntries() = runTest {
        // Given
        val imageStats = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        val breedStats = CacheStatsEntity.createDaily("2024-01-02", CacheStatsEntity.CacheType.BREEDS)
        cacheStatsDao.insertStats(imageStats)
        cacheStatsDao.insertStats(breedStats)

        // When
        val imageOnlyStats = cacheStatsDao.getStatsByCacheType(CacheStatsEntity.CacheType.IMAGES)

        // Then
        assertEquals(1, imageOnlyStats.size)
        assertEquals(CacheStatsEntity.CacheType.IMAGES, imageOnlyStats[0].cacheType)
    }

    @Test
    fun getStatsForDateRange_returnsEntriesInRange() = runTest {
        // Given
        val stats1 = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        val stats2 = CacheStatsEntity.createDaily("2024-01-02", CacheStatsEntity.CacheType.IMAGES)
        val stats3 = CacheStatsEntity.createDaily("2024-01-03", CacheStatsEntity.CacheType.IMAGES)
        cacheStatsDao.insertStats(stats1)
        cacheStatsDao.insertStats(stats2)
        cacheStatsDao.insertStats(stats3)

        // When
        val rangeStats = cacheStatsDao.getStatsForDateRange("2024-01-01", "2024-01-02")

        // Then
        assertEquals(2, rangeStats.size)
    }

    @Test
    fun getRecentStats_returnsLast30Days() = runTest {
        // Given
        val stats = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        cacheStatsDao.insertStats(stats)

        // When
        val recentStats = cacheStatsDao.getRecentStats()

        // Then
        assertTrue(recentStats.isEmpty())
    }

    // ===== AGGREGATE TESTS =====

    @Test
    fun getTotalCacheHits_returnsSumForDateRange() = runTest {
        // Given
        val stats1 = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
            .copy(cacheHits = 5)
        val stats2 = CacheStatsEntity.createDaily("2024-01-02", CacheStatsEntity.CacheType.IMAGES)
            .copy(cacheHits = 3)
        cacheStatsDao.insertStats(stats1)
        cacheStatsDao.insertStats(stats2)

        // When
        val totalHits = cacheStatsDao.getTotalCacheHits("2024-01-01", "2024-01-02", CacheStatsEntity.CacheType.IMAGES)

        // Then
        assertEquals(8, totalHits)
    }

    @Test
    fun getAverageCacheHitRate_returnsCorrectRate() = runTest {
        // Given
        val stats1 = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
            .copy(cacheHits = 8, cacheMisses = 2)
        val stats2 = CacheStatsEntity.createDaily("2024-01-02", CacheStatsEntity.CacheType.IMAGES)
            .copy(cacheHits = 7, cacheMisses = 3)
        cacheStatsDao.insertStats(stats1)
        cacheStatsDao.insertStats(stats2)

        // When
        val hitRate = cacheStatsDao.getAverageCacheHitRate("2024-01-01", "2024-01-02", CacheStatsEntity.CacheType.IMAGES)

        // Then
        assertEquals(0.75, hitRate)
    }

    // ===== TRANSACTION TESTS =====

    @Test
    fun recordCacheHit_createsEntryIfNotExists() = runTest {
        // Given - No existing entry

        // When
        cacheStatsDao.recordCacheHit("2024-01-01", CacheStatsEntity.CacheType.IMAGES)

        // Then
        val retrieved = cacheStatsDao.getStatsForDate("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        assertNotNull(retrieved)
        assertEquals(1, retrieved?.cacheHits)
    }

    @Test
    fun recordCacheHit_incrementsExistingEntry() = runTest {
        // Given
        val stats = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        cacheStatsDao.insertStats(stats)

        // When
        cacheStatsDao.recordCacheHit("2024-01-01", CacheStatsEntity.CacheType.IMAGES)

        // Then
        val retrieved = cacheStatsDao.getStatsForDate("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        assertEquals(1, retrieved?.cacheHits)
    }

    @Test
    fun recordCacheMiss_createsEntryIfNotExists() = runTest {
        // Given - No existing entry

        // When
        cacheStatsDao.recordCacheMiss("2024-01-01", CacheStatsEntity.CacheType.IMAGES)

        // Then
        val retrieved = cacheStatsDao.getStatsForDate("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        assertNotNull(retrieved)
        assertEquals(1, retrieved?.cacheMisses)
        assertEquals(1, retrieved?.apiCalls)
    }

    @Test
    fun cleanupOldStats_removesOldEntries() = runTest {
        // Given
        val oldStats = CacheStatsEntity.createDaily("2020-01-01", CacheStatsEntity.CacheType.IMAGES)
        val recentStats = CacheStatsEntity.createDaily("2024-01-01", CacheStatsEntity.CacheType.IMAGES)
        cacheStatsDao.insertStats(oldStats)
        cacheStatsDao.insertStats(recentStats)

        // When
        cacheStatsDao.cleanupOldStats(keepDays = 30)

        // Then
        val allStats = cacheStatsDao.getAllStats()
        assertTrue(allStats.size <= 1)
    }
}