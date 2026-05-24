package com.byteberry.notes;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@androidx.room.Database(entities = {Note.class}, version = 1, exportSchema = false)
public abstract class Database extends RoomDatabase {
    private static Database INSTANCE;

    // Shared executor used by Repo AND the seed callback
    static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    public abstract DAO dao();

    public static synchronized Database getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            Database.class,
                            "notes_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return INSTANCE;
    }

    /**
     * BUG FIX: The original callback called INSTANCE.dao() inside onCreate(),
     * but INSTANCE is still null at that point because Room's builder hasn't
     * returned yet.  The fix: schedule the seed work on the shared executor
     * AFTER the instance is assigned, so INSTANCE is guaranteed non-null.
     */
    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Post to executor; by the time this runs, getInstance() has returned
            // and INSTANCE is fully initialised.
            databaseWriteExecutor.execute(() -> {
                DAO dao = INSTANCE.dao(); // safe — INSTANCE is set before any executor task runs
                dao.insert(new Note("Welcome!", "Tap a note to edit it, or swipe left to delete."));
                dao.insert(new Note("Grocery List", "Milk, eggs, bread, butter"));
                dao.insert(new Note("Meeting Notes", "Discuss Q3 roadmap and assign owners."));
            });
        }
    };
}