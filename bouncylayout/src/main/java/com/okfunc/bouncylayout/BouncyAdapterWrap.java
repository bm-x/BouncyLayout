package com.okfunc.bouncylayout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class BouncyAdapterWrap extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_HEADER = 9910;
    private static final int VIEW_TYPE_FOOTER = 9911;

    private Context mContext;
    private RecyclerView.Adapter mAdapter;

    private BouncyGapLayout mHeadeLayout;
    private BouncyGapLayout mFooterLayout;

    public BouncyAdapterWrap(Context context, RecyclerView.Adapter originAdapter, BouncyConfig config) {
        if (originAdapter == null)
            throw new RuntimeException(("null adapter"));

        mAdapter = originAdapter;

        mHeadeLayout = config.createHeaderView(context);
        mFooterLayout = config.createFooterView(context);
    }

    public BouncyGapLayout getHeaderView() {
        return mHeadeLayout;
    }

    public BouncyGapLayout getFooterView() {
        return mFooterLayout;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            return new WrapHolder(mHeadeLayout);
        }
        if (viewType == VIEW_TYPE_FOOTER) {
            return new WrapHolder(mFooterLayout);
        }
        return mAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position != 0 && position != getItemCount() - 1) {
            mAdapter.onBindViewHolder(holder, position - 1);
        }
    }

    @Override
    public int getItemCount() {
        return mAdapter.getItemCount() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_TYPE_HEADER;

        if (position == getItemCount() - 1)
            return VIEW_TYPE_FOOTER;

        return mAdapter.getItemViewType(position - 1);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter.onDetachedFromRecyclerView(recyclerView);
    }

    private class WrapHolder extends RecyclerView.ViewHolder {
        public WrapHolder(View v) {
            super(v);
        }
    }
}
