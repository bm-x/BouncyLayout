package com.okfunc.bouncylayout;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

public class BouncyEdge {

    private BouncyLayout mBouncyLayout;
    private BouncyAdapterWrap mAdapter;
    private RecyclerView.Adapter mOriginAdapter;
    private BouncyConfig mConfig;
    private Context mContext;

    private LinearLayoutManager mLinearLayoutManager;
    private GridLayoutManager mGridLayoutManager;

    private boolean isGridLayout;
    private boolean isReverseLayout;
    private boolean isVertical;
    private boolean handleTouchEvent;
    private boolean inTouchEvent;
    private boolean dontIntecpterScrollerEvent;

    private int lastX = 0;
    private int lastY = 0;

    private int mGapLimitPx;

    private boolean isFooterFixLengthChecked;
    private int mFooterFixLength = 0;

    private BouncyGapLayout mHeaderView;
    private BouncyGapLayout mFooterView;

    private OverScroller mScroller;

    public BouncyEdge(Context context, BouncyLayout bouncyLayout, BouncyAdapterWrap adapter,
                      RecyclerView.Adapter originAdapter, BouncyConfig config) {
        mBouncyLayout = bouncyLayout;
        mContext = context;
        mAdapter = adapter;
        mOriginAdapter = originAdapter;
        mConfig = config;

        mGapLimitPx = config.gapLimit;

        mHeaderView = mAdapter.getHeaderView();
        mFooterView = mAdapter.getFooterView();

        RecyclerView.LayoutManager layoutManager = mBouncyLayout.getLayoutManager();

        if (!(layoutManager instanceof LinearLayoutManager)) {
            throw new RuntimeException("RecyclerView must use LinearLayoutManager or GridLayoutManager");
        }

        if (layoutManager instanceof GridLayoutManager) {
            isGridLayout = true;
            mGridLayoutManager = (GridLayoutManager) layoutManager;
        }

        mLinearLayoutManager = (LinearLayoutManager) layoutManager;
        isReverseLayout = mLinearLayoutManager.getReverseLayout();
        isVertical = mLinearLayoutManager.getOrientation() == LinearLayoutManager.VERTICAL;

        initGapView();

        mScroller = new OverScroller(context);

        mBouncyLayout.scrollToPosition(0);
        mBouncyLayout.addOnScrollListener(mScrollListener);
        mBouncyLayout.addOnItemTouchListener(mItemTouchListener);
        mBouncyLayout.setOnFlingListener(mFlingListener);
    }

    public void onDataSetChange() {
        isFooterFixLengthChecked = false;
        mFooterFixLength = 0;
    }

    private Runnable mScrollUpdate = new Runnable() {
        @Override
        public void run() {
            if (mScroller.computeScrollOffset()) {
                final int x = mScroller.getCurrX();
                final int y = mScroller.getCurrY();
                final int detallX = x - lastX;
                final int deltalY = y - lastY;

                lastX = x;
                lastY = y;

                mBouncyLayout.scrollBy(detallX, deltalY);
                mBouncyLayout.postOnAnimation(this);
            } else if (isGapViewVisibale()) {
                springBack();
            }
        }
    };

    private void springBack() {
        abortScroll();

        dontIntecpterScrollerEvent = true;

        int headerVisibale = getHeaderVisibleLength();
        int footerVisibale = getFooterVisibleLength();

        if (headerVisibale > 0) {
            mScroller.springBack(0, 0, 0, 0, headerVisibale, headerVisibale);
            mBouncyLayout.postOnAnimation(mScrollUpdate);
        } else if (footerVisibale > 0) {
            mScroller.springBack(0, 0, 0, 0, -footerVisibale, -footerVisibale);
            mBouncyLayout.postOnAnimation(mScrollUpdate);
        }
    }

    private boolean handOverScrollBack = false;

    private final RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (!isGapViewVisibale()) {
                handOverScrollBack = false;
                return;
            }

            if (!handOverScrollBack && (intecpterException() || !dontIntecpterScrollerEvent) && !mScroller.isFinished()) {
                handOverScrollBack = true;
                int currVelocity = (int) mScroller.getCurrVelocity();

                int headerVisible = getHeaderVisibleLength();
                int footerVisible = getFooterVisibleLength();

                if (headerVisible > 0) {
                    abortScroll();
                    startOverScroll(-headerVisible, -currVelocity, headerVisible, headerVisible, mConfig.gapLimit);
                } else if (footerVisible > 0) {
                    abortScroll();
                    startOverScroll(-footerVisible, currVelocity, -footerVisible, -footerVisible, mConfig.gapLimit);
                }
            }
        }

        public boolean intecpterException() {
            if (mScroller.isFinished() || inTouchEvent) {
                return false;
            }

            int curX = mScroller.getCurrX();
            int curY = mScroller.getCurrY();

            if (isVertical) {
                if (curY > 0 && getHeaderVisibleLength() == 0 && getFooterVisibleLength() > 0) {
                    return true;
                } else if (curY < 0 && getFooterVisibleLength() == 0 && getHeaderVisibleLength() > 0) {
                    return true;
                }
            } else {
                if (curX > 0 && getHeaderVisibleLength() == 0 && getFooterVisibleLength() > 0) {
                    return true;
                } else if (curX < 0 && getFooterVisibleLength() == 0 && getHeaderVisibleLength() > 0) {
                    return true;
                }
            }

            return false;
        }
    };

    private void startOverScroll(int visiable, int velocity, int min, int max, int limit) {
        if (isVertical) {
            mScroller.fling(0, visiable, 0, velocity, 0, 0, min, max, 0, limit);
        } else {
            mScroller.fling(visiable, 0, velocity, 0, min, max, 0, 0, limit, 0);
        }
        mBouncyLayout.postOnAnimation(mScrollUpdate);
    }

    private final RecyclerView.OnFlingListener mFlingListener = new RecyclerView.OnFlingListener() {
        @Override
        public boolean onFling(int velocityX, int velocityY) {
            abortScroll();
            inTouchEvent = false;
            mScroller.fling(0, 0, velocityX, velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            mBouncyLayout.postOnAnimation(mScrollUpdate);
            return true;
        }
    };

    private final RecyclerView.SimpleOnItemTouchListener mItemTouchListener = new RecyclerView.SimpleOnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            onTouchEvent(rv, e);

            handleTouchEvent = isGapViewVisibale();

            return handleTouchEvent;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            boolean handle = mGestureDetector.onTouchEvent(e);

            int action = e.getAction();

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                inTouchEvent = false;

                if (!handle) {
                    onActionUp();
                }
            }
        }
    };

    private final GestureDetectorCompat mGestureDetector = new GestureDetectorCompat(mContext, new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            abortScroll();
            inTouchEvent = true;
            handOverScrollBack = false;
            dontIntecpterScrollerEvent = false;
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (handleTouchEvent) {
                final int headerVisible = getHeaderVisibleLength();
                final int footerVisible = getFooterVisibleLength();

                int visible = (headerVisible > 0) ? headerVisible : footerVisible;
                float distance = isVertical ? distanceY : distanceX;

                if (visible > 0) {
                    if ((visible - distance) < mGapLimitPx) {
                        double ratioVisible = (double) visible / mGapLimitPx;
                        double scrollDist = Math.abs(distance - distance * ratioVisible);

                        if (distance < 0) {
                            scrollDist *= -1;
                        }
                        scrollBy((int) scrollDist);
                    }
                } else {
                    mBouncyLayout.scrollBy((int) distanceX, (int) distanceY);
                }
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            abortScroll();
            inTouchEvent = false;
            if (!isGapViewVisibale()) {
                fling(velocityX, velocityY);
            } else {
                final int headerVisible = getHeaderVisibleLength();
                final int footerVisible = getFooterVisibleLength();

                float velocity = isVertical ? velocityY : velocityX;

                dontIntecpterScrollerEvent = true;

                if (headerVisible > 0) {
                    if (velocity > 0) {
                        startScroll(headerVisible, mConfig.scrollDuration);
                    } else {
                        fling(velocityX, velocityY);
                    }
                } else {
                    if (velocity > 0) {
                        fling(velocityX, velocityY);
                    } else {
                        startScroll(-footerVisible, mConfig.scrollDuration);
                    }
                }
            }
            return true;
        }
    });

    private void fling(float velocityX, float velocityY) {
        if (isVertical) {
            mScroller.fling(0, 0, 0, (int) -velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } else {
            mScroller.fling(0, 0, (int) -velocityX, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
        }
        mBouncyLayout.postOnAnimation(mScrollUpdate);
    }

    private void startScroll(int detail, int duration) {
        if (isVertical) {
            mScroller.startScroll(0, 0, 0, detail, duration);
        } else {
            mScroller.startScroll(0, 0, detail, duration);
        }
        mBouncyLayout.postOnAnimation(mScrollUpdate);
    }

    private void abortScroll() {
        mScroller.abortAnimation();
        mBouncyLayout.removeCallbacks(mScrollUpdate);
        lastX = 0;
        lastY = 0;
    }

    private void onActionUp() {
        springBack();
    }

    private int getHeaderVisibleLength() {
        if (!mHeaderView.isAttachedToWindow) {
            return 0;
        }
        if (isVertical) {
            if (isReverseLayout) {
                return getBottomVisible(mHeaderView);
            } else {
                return getTopVisible(mHeaderView);
            }
        } else {
            if (isReverseLayout) {
                return getRightVisible(mHeaderView);
            } else {
                return getLeftVisible(mHeaderView);
            }
        }
    }

    private int getFooterVisibleLength() {
        if (!mFooterView.isAttachedToWindow) {
            return 0;
        }

        if (!isFooterFixLengthChecked) {
            checkFooterFixLength();
            Log.i("clyde", "footerFix " + mFooterFixLength);
        }

        return getFooterVisibleLengthDirect() - mFooterFixLength;
    }

    private int getFooterVisibleLengthDirect() {
        if (isVertical) {
            if (isReverseLayout) {
                return getTopVisible(mFooterView);
            } else {
                return getBottomVisible(mFooterView);
            }
        } else {
            if (isReverseLayout) {
                return getLeftVisible(mFooterView);
            } else {
                return getRightVisible(mFooterView);
            }
        }
    }

    private void checkFooterFixLength() {
        View first = mBouncyLayout.findChildViewUnder(mBouncyLayout.getPaddingLeft(), mBouncyLayout.getPaddingTop());
        int position = mBouncyLayout.getChildAdapterPosition(first);
        if (position == 1) {
            if (mFooterView.isAttachedToWindow) {
                int footerVisiable = getFooterVisibleLengthDirect();
                if (footerVisiable + first.getTop() == 0 || footerVisiable < -first.getTop()) {
                    mFooterFixLength = 0;
                } else {
                    mFooterFixLength = footerVisiable - first.getTop();
                }
            } else {
                mFooterFixLength = 0;
            }
            isFooterFixLengthChecked = true;
        } else if (position == 0 && mFooterView.isAttachedToWindow) {
            mFooterFixLength = getFooterVisibleLengthDirect() + first.getTop();
            isFooterFixLengthChecked = true;
        }
    }

    private int getTopVisible(View view) {
        return Math.max(0, view.getBottom() - mBouncyLayout.getPaddingTop());
    }

    private int getBottomVisible(View view) {
        return Math.max(0, mBouncyLayout.getHeight() - view.getTop() - mBouncyLayout.getPaddingBottom());
    }

    private int getLeftVisible(View view) {
        return Math.max(0, view.getRight() - mBouncyLayout.getPaddingLeft());
    }

    private int getRightVisible(View view) {
        return Math.max(0, mBouncyLayout.getWidth() - view.getLeft() - mBouncyLayout.getPaddingRight());
    }

    private boolean isGapViewVisibale() {
        return mHeaderView.isAttachedToWindow || mFooterView.isAttachedToWindow;
    }

    private void scrollBy(int dist) {
        if (isVertical) {
            mBouncyLayout.scrollBy(0, dist);
        } else {
            mBouncyLayout.scrollBy(dist, 0);
        }
    }

    private void initGapView() {
        configGapView(mHeaderView);
        configGapView(mFooterView);
    }

    private void configGapView(BouncyGapLayout gapLayout) {
        final int width = isVertical ? ViewGroup.LayoutParams.MATCH_PARENT : mConfig.gapHeight;
        final int height = isVertical ? mConfig.gapHeight : ViewGroup.LayoutParams.MATCH_PARENT;
        gapLayout.setLayoutParams(new ViewGroup.LayoutParams(width, height));
    }

    public void clear() {
        mBouncyLayout.removeOnScrollListener(mScrollListener);
        mBouncyLayout.removeOnItemTouchListener(mItemTouchListener);
    }
}
