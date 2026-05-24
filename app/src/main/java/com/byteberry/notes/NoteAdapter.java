package com.byteberry.notes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

/**
 * BUG FIX: The original adapter had no click listener, so tapping a note did
 * nothing.  This version exposes an OnNoteClickListener interface that
 * MainActivity sets to open AddNote in edit mode.
 */
public class NoteAdapter extends ListAdapter<Note, NoteAdapter.NoteHolder> {

    // ── Click callback ───────────────────────────────────────────────────────
    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }
    // Inside NoteAdapter.java
    public Note getNoteAt(int position) {
        return getItem(position);
    }

    private OnNoteClickListener listener;

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.listener = listener;
    }

    // ── DiffUtil ─────────────────────────────────────────────────────────────
    private static final DiffUtil.ItemCallback<Note> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Note>() {
                @Override
                public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                    return oldItem.getTitle().equals(newItem.getTitle()) &&
                            oldItem.getDescription().equals(newItem.getDescription());
                }
            };

    public NoteAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Note currentNote = getItem(position);
        holder.textViewTitle.setText(currentNote.getTitle());
        holder.textViewDescription.setText(currentNote.getDescription());

        // Wire click to the listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(currentNote);
            }
        });
    }

    // ── ViewHolder ───────────────────────────────────────────────────────────
    static class NoteHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final TextView textViewDescription;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
        }
    }
}