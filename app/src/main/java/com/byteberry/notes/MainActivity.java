package com.byteberry.notes;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * BUG FIXES applied here:
 *  1. Registers a click listener on the adapter so tapping opens AddNote in
 *     edit mode, passing the note's ID via EXTRA_NOTE_ID.
 *  2. Attaches an ItemTouchHelper for left-swipe-to-delete with a red
 *     background + delete icon drawn during the swipe.
 */
public class MainActivity extends AppCompatActivity {

    private NoteViewModel noteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final NoteAdapter adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, adapter::submitList);

        // ── Tap to edit ──────────────────────────────────────────────────────
        adapter.setOnNoteClickListener(note -> {
            Intent intent = new Intent(MainActivity.this, AddNote.class);
            intent.putExtra(AddNote.EXTRA_NOTE_ID, note.getId());
            startActivity(intent);
        });

        // ── Swipe left to delete ─────────────────────────────────────────────
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final ColorDrawable background = new ColorDrawable(Color.parseColor("#F44336"));
            private final Drawable deleteIcon = ContextCompat.getDrawable(
                    MainActivity.this, android.R.drawable.ic_menu_delete);

            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false; // drag-and-drop not needed
            }

            // MainActivity.java (around line 80)
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Change adapter.getItem to adapter.getNoteAt
                Note note = adapter.getNoteAt(viewHolder.getAdapterPosition());
                noteViewModel.delete(note);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;
                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int iconTop    = itemView.getTop()  + iconMargin;
                int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                int iconRight  = itemView.getRight() - iconMargin;
                int iconLeft   = iconRight - deleteIcon.getIntrinsicWidth();

                background.setBounds(
                        itemView.getRight() + (int) dX,
                        itemView.getTop(),
                        itemView.getRight(),
                        itemView.getBottom());
                background.draw(c);

                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                deleteIcon.setTint(Color.WHITE);
                deleteIcon.draw(c);
            }
        }).attachToRecyclerView(recyclerView);

        // ── FAB — add new note ───────────────────────────────────────────────
        FloatingActionButton buttonAddNote = findViewById(R.id.button_add_note);
        buttonAddNote.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, AddNote.class)));
    }
}
