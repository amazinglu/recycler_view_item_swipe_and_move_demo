package com.example.amazinglu.recycler_view_function_demo;

import android.content.ClipData;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.amazinglu.recycler_view_function_demo.model.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListFragment extends Fragment {

//    @BindView(R.id.main_recycler_view) RecyclerView recyclerView;

    private List<Element> data;
    private Adapter adapter;
    private RecyclerView recyclerView;

    /**
     * the call back object of swipe or move item in recycler view
     */
    private SwipeController swipeController;
    private ItemTouchHelper itemTouchHelper;

    public static ListFragment newInstance() {
        return new ListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        loadData();
        recyclerView = (RecyclerView) view.findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        adapter = new Adapter(data);
        recyclerView.setAdapter(adapter);

        setUpItemTouchHelper();

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }

    private void setUpItemTouchHelper() {
        swipeController = new SwipeController(this, new SwipeControllerActions() {
            @Override
            public void onLeftClicked(int position) {
                // TODO: edit the item
            }

            @Override
            public void onRightClicked(int position) {
               Toast.makeText(getContext(), "item " + position + " edit button clicked",
                       Toast.LENGTH_LONG).show();
            }
        });
        itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void loadData() {
        data = new ArrayList<>();
        for (int i = 0; i < 20; ++i) {
            Element element = new Element();
            element.text = "item" + i;
            data.add(element);
        }
    }

    public void swapItem(int i, int j) {
        Collections.swap(data, i - 1, j - 1);
    }

    public void deleteItem(int position) {
        data.remove(position - 1);
        adapter.notifyItemRemoved(position);
    }

    public Adapter getAdapter() {
        return adapter;
    }
}
