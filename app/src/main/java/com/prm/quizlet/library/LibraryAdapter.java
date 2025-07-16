package com.prm.quizlet.library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.prm.quizlet.R;
import com.prm.quizlet.entity.Sets;

import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<LibraryItem> items;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public interface OnSetClickListener {
        void onSetClick(Sets set);
    }
    private OnSetClickListener listener;

    public LibraryAdapter(List<LibraryItem> items, OnSetClickListener listener) {
        this.items = items;
        this.listener = listener;
    }
    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof MonthHeader) ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_month_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_set, parent, false);
            return new ItemViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).monthText.setText(
                    ((MonthHeader) items.get(position)).monthText
            );
        } else if (holder instanceof ItemViewHolder) {
            SetItem setItem = (SetItem) items.get(position);
            Sets set = setItem.set;
            ((ItemViewHolder) holder).tvTitle.setText(set.title);
            ((ItemViewHolder) holder).tvTerms.setText(setItem.flashcardCount + " terms");
            ((ItemViewHolder) holder).tvOwner.setText(set.description != null ? set.description : "");
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSetClick(set);
            });
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView monthText;
        HeaderViewHolder(View v) {
            super(v);
            monthText = v.findViewById(R.id.tvMonthHeader);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTerms, tvOwner;
        ItemViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvTerms = v.findViewById(R.id.tvTerms);
            tvOwner = v.findViewById(R.id.tvOwner);
        }
    }
}