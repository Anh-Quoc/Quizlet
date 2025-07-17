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
    private final OnSetClickListener listener;

    public interface OnSetClickListener {
        void onSetClick(Sets set);
    }
    public SetsAdapter(List<Sets> Sets, OnSetClickListener listener) {
        this.Sets = Sets;
        this.listener = listener;
    }


    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sets, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        Sets set = Sets.get(position);
        holder.tvTitle.setText(set.title);
        holder.tvDesc.setText(set.description);
        holder.bind(set, listener); // ✅ truyền listener
    }


    @Override
    public int getItemCount() {
        return Sets.size();
    }

    static class SetViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, setName;
        SetViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSetTitle);
            tvDesc = itemView.findViewById(R.id.tvSetDesc);
        }

        void bind(Sets set, OnSetClickListener listener) {
            itemView.setOnClickListener(v -> listener.onSetClick(set));
        }

    }
}