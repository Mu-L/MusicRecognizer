package com.mrsep.musicrecognizer.core.database

import android.util.Log
import androidx.room3.AutoMigration
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.useReaderConnection
import androidx.room3.useWriterConnection
import com.mrsep.musicrecognizer.core.database.enqueued.EnqueuedRecognitionDao
import com.mrsep.musicrecognizer.core.database.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.core.database.migration.AutoMigrationSpec3To4
import com.mrsep.musicrecognizer.core.database.track.TrackDao
import com.mrsep.musicrecognizer.core.database.track.TrackEntity
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "ApplicationDatabase"

@Database(
    entities = [
        TrackEntity::class,
        EnqueuedRecognitionEntity::class,
    ],
    version = 11,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = AutoMigrationSpec3To4::class),
        AutoMigration(from = 4, to = 5),
    ]
)
@ColumnTypeConverters(
    value = [
        FileRoomConverter::class,
        InstantRoomConverter::class,
        DurationRoomConverter::class,
        LocalDateRoomConverter::class,
    ]
)
abstract class ApplicationDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao

    abstract fun enqueuedRecognitionDao(): EnqueuedRecognitionDao

    suspend fun getDataSize(): Long {
        val q = "SELECT page_count * page_size as size FROM pragma_page_count(), pragma_page_size()"
        return useReaderConnection { connection ->
            connection.usePrepared(q) { statement ->
                statement.step()
                statement.getLong(0)
            }
        }
    }

    suspend fun checkoutWithRetry(): Boolean {
        var attemptCount = 1
        while (attemptCount <= 3) {
            if (checkout()) return true
            Log.i(TAG, "Database checkpoint was blocked, retry")
            delay(500.milliseconds * attemptCount)
            attemptCount++
        }
        return false
    }

    // https://www.sqlite.org/pragma.html#pragma_wal_checkpoint
    private suspend fun checkout(): Boolean {
        return useWriterConnection { connection ->
            connection.usePrepared("PRAGMA wal_checkpoint(FULL)") { statement ->
                statement.step()
                if (statement.getLong(0) == 0L) {
                    if (statement.getLong(1) == -1L && statement.getLong(2) == -1L) {
                        Log.w(TAG, "There is no write-ahead log for database")
                    }
                    true
                } else {
                    false
                }
            }
        }
    }
}
