package com.prm.quizlet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.prm.quizlet.entity.Sets;
import java.util.List;

public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.SetViewHolder> {
    private List<Sets> Sets;

    public SetsAdapter(List<Sets> Sets) {
        this.Sets = Sets;
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        Sets sets = Sets.get(position);
        holder.tvTitle.setText(sets.title);
        holder.tvDesc.setText(sets.description);
    }

    @Override
    public int getItemCount() {
        return Sets.size();
    }

    static class SetViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc;
        SetViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSetTitle);
            tvDesc = itemView.findViewById(R.id.tvSetDesc);
        }
    }
}