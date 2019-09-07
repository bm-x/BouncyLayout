package com.okfunc.bouncylayout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class BouncyLayout extends RecyclerView {

    private BouncyAdapterWrap mBouncyAdapter;
    private Adapter mOriginalAdapter;
    private BouncyEdge mBouncyEffect;

    public BouncyLayout(Context context) {
        super(context);
    }

    public BouncyLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BouncyLayout(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAdapter(Adapter adapter, BouncyConfig config) {
        if (mOriginalAdapter != null) {
            mOriginalAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
        }
        if (mBouncyEffect != null) {
            mBouncyEffect.clear();
        }

        mOriginalAdapter = adapter;
        mBouncyAdapter = new BouncyAdapterWrap(getContext(), adapter, config);

        super.setAdapter(mBouncyAdapter);
        adapter.registerAdapterDataObserver(mAdapterDataObserver);

        mBouncyEffect = new BouncyEdge(getContext(), this, mBouncyAdapter, mBouncyAdapter, config);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (getAdapter() == null) {
            setAdapter(new ScrollViewAdapter());
        }

        if (mOriginalAdapter instanceof ScrollViewAdapter) {
            ScrollViewAdapter adapter = (ScrollViewAdapter) mOriginalAdapter;
            child.setLayoutParams(generateLayoutParams(params));
            adapter.addView(child);
            return;
        }

        super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        setAdapter(adapter, BouncyConfig.createDefault(getContext()));
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (getLayoutManager() == null) {
            setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        }
        return super.generateLayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        if (getLayoutManager() == null) {
            setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        }
        return super.generateLayoutParams(attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        if (getLayoutManager() == null) {
            setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        }
        return super.generateDefaultLayoutParams();
    }

    @Override
    public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
        setAdapter(adapter);
    }

    @Override
    public void scrollToPosition(int position) {
        super.scrollToPosition(position + 1);
    }

    @Override
    public void smoothScrollToPosition(int position) {
        super.smoothScrollToPosition(position + 1);
    }

    private void notifyDataSetChange() {
        if (mBouncyEffect != null) {
            mBouncyEffect.onDataSetChange();
        }
    }

    private final AdapterDataObserver mAdapterDataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChange();
            mBouncyAdapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyDataSetChange();
            mBouncyAdapter.notifyItemRangeChanged(positionStart + 1, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            notifyDataSetChange();
            mBouncyAdapter.notifyItemRangeChanged(positionStart + 1, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyDataSetChange();
            mBouncyAdapter.notifyItemRangeInserted(positionStart + 1, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyDataSetChange();
            mBouncyAdapter.notifyItemRangeRemoved(positionStart + 1, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            notifyDataSetChange();
            mBouncyAdapter.notifyItemMoved(fromPosition + 1, toPosition + 1);
        }
    };
}
