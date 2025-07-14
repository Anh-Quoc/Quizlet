package com.prm.quizlet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.prm.quizlet.entity.Flashcards;
import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {
    private List<Flashcards> flashcards;

    public FlashcardAdapter(List<Flashcards> flashcards) {
        this.flashcards = flashcards;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Flashcards flashcard = flashcards.get(position);
        holder.tvTitle.setText(flashcard.front_text);
        holder.tvDesc.setText(flashcard.back_text);
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc;
        FlashcardViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvFlashcardTitle);
            tvDesc = itemView.findViewById(R.id.tvFlashcardDesc);
        }
    }
}