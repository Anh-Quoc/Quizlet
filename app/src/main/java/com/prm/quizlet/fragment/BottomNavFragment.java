package com.prm.quizlet.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.prm.quizlet.R;

public class BottomNavFragment extends Fragment {

    public BottomNavFragment() {
        super(R.layout.fragment_bottom_nav);
    }

    public interface OnBottomNavClickListener {
        void onHomeClick();
        void onCreateClick();
        void onLibraryClick();
        void onFreeTrialClick();
    }

    private OnBottomNavClickListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnBottomNavClickListener) {
            listener = (OnBottomNavClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBottomNavClickListener");
        }
    }

    private int selectedNavId = R.id.btn_home_nav; // mặc định là home

    public void setSelectedNavId(int id) {
        this.selectedNavId = id;
        if (getView() != null) {
            updateSelectedNav(selectedNavId);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_home_nav).setOnClickListener(v -> {
            if (listener != null) listener.onHomeClick();
        });
        view.findViewById(R.id.btn_create_nav).setOnClickListener(v -> {
            if (listener != null) listener.onCreateClick();
        });
        view.findViewById(R.id.btn_library_nav).setOnClickListener(v -> {
            if (listener != null) listener.onLibraryClick();
        });
        view.findViewById(R.id.btn_free_trial_nav).setOnClickListener(v -> {
            if (listener != null) listener.onFreeTrialClick();
        });

        updateSelectedNav(selectedNavId);
    }

    private void updateSelectedNav(int selectedId) {
        int activeColor = Color.parseColor("#4F6DF9");
        int inactiveColor = Color.parseColor("#8A92A6");

        int[] navIds = {
                R.id.btn_home_nav,
                R.id.btn_create_nav,
                R.id.btn_library_nav,
                R.id.btn_free_trial_nav
        };

        for (int id : navIds) {
            LinearLayout layout = requireView().findViewById(id);
            ImageView icon = (ImageView) layout.getChildAt(0);
            TextView label = (TextView) layout.getChildAt(1);

            boolean isSelected = id == selectedId;
            icon.setColorFilter(isSelected ? activeColor : inactiveColor);
            label.setTextColor(isSelected ? activeColor : inactiveColor);
        }
    }
}
