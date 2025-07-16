package com.prm.quizlet.details;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm.quizlet.FlashcardAdapter;
import com.prm.quizlet.R;
import com.prm.quizlet.entity.Flashcards;

import java.util.List;

public class FlashcardDetailsAdapter extends RecyclerView.Adapter<FlashcardDetailsAdapter.FlashcardViewHolder>{

    private List<Flashcards> flashcards;

    public FlashcardDetailsAdapter(List<Flashcards> flashcards) {
        this.flashcards = flashcards;
    }

    @NonNull
    @Override
    public FlashcardDetailsAdapter.FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard_details, parent, false);
        return new FlashcardDetailsAdapter.FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Flashcards card = flashcards.get(position);
        holder.tvTerm.setText(card.front_text);
        holder.tvDefinition.setText(card.back_text);

        // Có thể set sự kiện cho ivSound và ivStar nếu muốn
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    public static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        TextView tvTerm, tvDefinition;
        ImageView ivSound, ivStar;

        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTerm = itemView.findViewById(R.id.tvTerm);
            tvDefinition = itemView.findViewById(R.id.tvDefinition);
            ivSound = itemView.findViewById(R.id.ivSound);
            ivStar = itemView.findViewById(R.id.ivStar);
        }
    }
}
