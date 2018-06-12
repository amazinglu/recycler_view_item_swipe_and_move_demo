package com.example.amazinglu.recycler_view_function_demo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MotionEvent;
import android.view.View;

import java.util.Collections;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE;
import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;

enum ButtonsState {
    GONE,
    LEFT_VISIBLE,
    RIGHT_VISIBLE
}

class SwipeController extends ItemTouchHelper.Callback {

    private boolean swipeBack = false;
    private ButtonsState buttonShowedState = ButtonsState.GONE;
    private static final float buttonWidth = 300;
    private RectF buttonInstance = null;
    private RecyclerView.ViewHolder curItemViewHolder = null;
    private SwipeControllerActions swipeControllerActions;
    private ListFragment fragment;

    public SwipeController(ListFragment fragment, SwipeControllerActions swipeControllerActions) {
        this.fragment = fragment;
        this.swipeControllerActions = swipeControllerActions;
    }

    /**
     * define what action we can do to items in recycler view
     * */
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(UP | DOWN, LEFT | RIGHT);
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = buttonShowedState != ButtonsState.GONE;
            /**
             * set the flag to 0, 这样子item就会回到原来的direction，就是会有一个swipe back的效果
             * */
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    /**
     * onChildDraw():在RecyclerView调用onDraw方法的时候，调用此方法
     原文解释：Called by ItemTouchHelper on RecyclerView’s onDraw callback
     可以在这个方法中，对itemView进行设置。如透明度，背景的变化等
     ** 这里的用处是根据dx和dy来调整item的位置
     * */
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ACTION_STATE_SWIPE) {
            if (buttonShowedState != ButtonsState.GONE) {
                /**
                 * 当item被向左或者向右swipe的时候，因为convertToAbsoluteDirection() 会return 0 的关系
                 * item会swipe back，但是因为有下面两句的关系，当dx的绝对值小于buttonWidth的时候，就会停止
                 * swipe back了，因为此时的dx都会被设定为button width
                 *
                 * dx, dy是针对于item默认的位置的偏移量
                 * */
                if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
                    dX = Math.max(dX, buttonWidth);
                }
                if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
                    dX = Math.min(dX, -buttonWidth);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
            else {
                setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }

        if (buttonShowedState == ButtonsState.GONE) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
        curItemViewHolder = viewHolder;
    }

    /**
     * ACTION_DOWN: you are touching the screen
     * ACTION_UP: you remove the finger from the screen
     * */
    private void setTouchListener(final Canvas c, final RecyclerView recyclerView,
                                  final RecyclerView.ViewHolder viewHolder,
                                  final float dX, final float dY, final int actionState,
                                  final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean isSwipe = false;
                /**
                 * 现在只有像左swipe的时候才会swipe back 且定住
                 * */
                isSwipe = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
                if (isSwipe && dX < -buttonWidth) { // means now I remove my finger from the screen
                    swipeBack = true;
                    buttonShowedState = ButtonsState.RIGHT_VISIBLE;

                    /**
                     * 到这个位置item已经完成了swipe然后swipe back 然后定住的操作
                     * 下面就是再点击一下这个item然后item就回到原来的位置
                     * 通过override OnTouchListener 来模拟点击的操作 (setTouchDownListener, setTouchUpListener)
                     * */
                    if (buttonShowedState != ButtonsState.GONE) {
                        setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        /**
                         * 因为我们现在模拟click操作，所以item的click listener得先disable，不然会conflict
                         * */
                        setItemsClickable(recyclerView, false);
                    }
                }
                return false;
            }
        });
    }

    private void setTouchDownListener(final Canvas c, final RecyclerView recyclerView,
                                      final RecyclerView.ViewHolder viewHolder,
                                      final float dX, final float dY, final int actionState,
                                      final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    /**
                     * 现在是finger touch item的时候
                     * */
                    setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
                return false;
            }
        });
    }

    private void setTouchUpListener(final Canvas c, final RecyclerView recyclerView,
                                    final RecyclerView.ViewHolder viewHolder, final float dX,
                                    final float dY, final int actionState,
                                    final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /**
                 * if 条件成立的话，finger 就离开item了
                 * 此时将dx设置成0，buttonShowedState 设置成GONE，onChildDraw就会吧item设置回默认的位置
                 * */
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    SwipeController.super.onChildDraw(c, recyclerView, viewHolder,
                            0F, dY, actionState, isCurrentlyActive);
                    recyclerView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return false;
                        }
                    });
                    /**
                     * 记得从新enable click listener
                     * */
                    setItemsClickable(recyclerView, true);
                    swipeBack = false;

                    /**
                     * 到底是如何激活这个if条件然后执行相应事件的呢
                     * 首先这个setTouchUpListener在swipe完了以后再点击的时候才会被调用
                     * 然后buttonInstance.contains(event.getX(), event.getY()) 如果是true的话，
                     * 说明点击的区域在buttonInstance里面
                     * 说明在点击这个button ！！！
                     * 所以就激活if 条件了
                     * 这或许就是button onclick listener的真正的原理
                     * */
                    if (swipeControllerActions != null && buttonInstance != null
                            && buttonInstance.contains(event.getX(), event.getY())) {
                        if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
                            swipeControllerActions.onLeftClicked(viewHolder.getAdapterPosition());
                        } else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
                            swipeControllerActions.onRightClicked(viewHolder.getAdapterPosition());
                        }
                    }

                    buttonShowedState = ButtonsState.GONE;
                    curItemViewHolder = null;
                }
                return false;
            }
        });
    }

    private void setItemsClickable(RecyclerView recyclerView, boolean isClickable) {
        for (int i = 0; i < recyclerView.getChildCount(); ++i) {
            recyclerView.getChildAt(i).setClickable(isClickable);
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; ++i) {
                fragment.swapItem(i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; --i) {
                fragment.swapItem(i, i - 1);
            }
        }
        fragment.getAdapter().notifyItemMoved(fromPosition, toPosition);
        /**
         * return true means 执行拖动
         * */
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        /**
         * when swipe right, delete the item
         * */
        if (direction == RIGHT) {
            fragment.deleteItem(viewHolder.getAdapterPosition());
        }
    }

    /**
     * 画swipe button后显示的button，这些button在第一次调用SwipeController时就已经画出来了
     * 要和card view 配合人使用，因为cardview可以挡住button
     * 例如说向左swipe时只有左边的button可以看到，右边的被card view挡住了
     * */
    private void drawButtons(Canvas c, RecyclerView.ViewHolder viewHolder) {
        float buttonWidthWithoutPadding = buttonWidth - 20;
        float corners = 16;
        View itemView = viewHolder.itemView;
        Paint paint = new Paint();

        RectF rightButton = new RectF(itemView.getRight() - buttonWidthWithoutPadding, itemView.getTop(),
                itemView.getRight(), itemView.getBottom());
        paint.setColor(Color.BLUE);
        c.drawRoundRect(rightButton, corners, corners, paint);
        drawText("EDIT", c, rightButton, paint);

        buttonInstance = null;
        if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
            buttonInstance = rightButton;
        }
    }

    private void drawText(String text, Canvas c, RectF button, Paint paint) {
        float textSize = 60;
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setTextSize(textSize);

        float textWidth = paint.measureText(text);
        c.drawText(text, button.centerX()-(textWidth/2), button.centerY()+(textSize/2), paint);
    }

    public void onDraw(Canvas c) {
        if (curItemViewHolder != null) {
            drawButtons(c, curItemViewHolder);
        }
    }
}