package com.example.domain_impl.local_database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.domain_api.local_database.entities.WatchLaterDatabaseMovie


class WatchLaterMoviesDatabaseMigrations {

    companion object {

        val migration_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """ALTER TABLE ${WatchLaterDatabaseMovie.TABLE_NAME} 
                        | ADD COLUMN ${WatchLaterDatabaseMovie.COLUMN_NOTIFICATION_DATE}
                        | INTEGER DEFAULT null""".trimMargin())
            }
        }
    }
}