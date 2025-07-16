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
    private List<Flashcards> Flashcards;

    public FlashcardAdapter(List<Flashcards> Flashcards) {
        this.Flashcards = Flashcards;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcards, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Flashcards flashcards = Flashcards.get(position);
        holder.tvTitle.setText(flashcards.back_text);
        holder.tvDesc.setText(flashcards.front_text);
    }

    @Override
    public int getItemCount() {
        return Flashcards.size();
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