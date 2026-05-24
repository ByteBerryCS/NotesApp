package com.byteberry.notes;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class Repo {
    private final DAO dao;
    private final LiveData<List<Note>> allNotes;

    public Repo(Application application) {
        Database database = Database.getInstance(application);
        dao = database.dao();
        allNotes = dao.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void insert(Note note) {
        // BUG FIX: reuse the shared single-thread executor so DB writes are
        // always serialised and we avoid creating a second thread pool.
        Database.databaseWriteExecutor.execute(() -> dao.insert(note));
    }

    public void update(Note note) {
        Database.databaseWriteExecutor.execute(() -> dao.update(note));
    }

    public void delete(Note note) {
        Database.databaseWriteExecutor.execute(() -> dao.delete(note));
    }
}