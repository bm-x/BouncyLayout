package com.okfunc.bouncylayout;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class ScrollViewAdapter extends RecyclerView.Adapter {

    private List<View> views = new ArrayList<>();

    public void addView(View view) {
        views.add(view);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScrollHolder(views.get(viewType));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return views.size();
    }

    private class ScrollHolder extends RecyclerView.ViewHolder {

        public ScrollHolder(View itemView) {
            super(itemView);
        }
    }
}
