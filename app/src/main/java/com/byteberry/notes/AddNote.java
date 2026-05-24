package com.byteberry.notes;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

/**
 * BUG FIX: The original AddNote was completely empty — no UI wiring, no
 * ViewModel usage, no save button logic, and no support for editing existing
 * notes.  This version:
 *   • Wires title + description fields and the Save button.
 *   • Validates that neither field is blank before saving.
 *   • Detects an incoming NOTE_ID extra (set by MainActivity when the user
 *     taps a note) and switches into edit mode: pre-fills the fields and
 *     calls update() instead of insert().
 *   • Finishes the activity after a successful save so the user returns to
 *     the list automatically.
 */
public class AddNote extends AppCompatActivity {

    public static final String EXTRA_NOTE_ID = "com.byteberry.notes.EXTRA_NOTE_ID";
    private static final int NO_ID = -1;

    private NoteViewModel noteViewModel;
    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;

    private int noteId = NO_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        Button buttonSave = findViewById(R.id.button_save);

        // ── Edit mode ────────────────────────────────────────────────────────
        noteId = getIntent().getIntExtra(EXTRA_NOTE_ID, NO_ID);
        if (noteId != NO_ID) {
            setTitle("Edit Note");
            // Load the note on a background thread, then populate the UI
            Database.databaseWriteExecutor.execute(() -> {
                Note note = Database.getInstance(getApplication()).dao().getNoteById(noteId);
                if (note != null) {
                    runOnUiThread(() -> {
                        editTextTitle.setText(note.getTitle());
                        editTextDescription.setText(note.getDescription());
                    });
                }
            });
        } else {
            setTitle("Add Note");
        }

        // ── Save button ──────────────────────────────────────────────────────
        buttonSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = editTextTitle.getText() != null
                ? editTextTitle.getText().toString().trim() : "";
        String description = editTextDescription.getText() != null
                ? editTextDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError("Title cannot be empty");
            editTextTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(description)) {
            editTextDescription.setError("Description cannot be empty");
            editTextDescription.requestFocus();
            return;
        }

        if (noteId == NO_ID) {
            // Insert new note
            noteViewModel.insert(new Note(title, description));
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        } else {
            // Update existing note
            Note note = new Note(title, description);
            note.setId(noteId);
            noteViewModel.update(note);
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
        }

        finish(); // Return to the note list
    }
}