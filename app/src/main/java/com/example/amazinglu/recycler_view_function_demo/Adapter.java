package com.example.amazinglu.recycler_view_function_demo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.amazinglu.recycler_view_function_demo.model.Element;

import java.util.List;

public class Adapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_ITEM = 2;

    private List<Element> data;
    private Context context;

    public Adapter(List<Element> data) {
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.header_view_recycler_view,
                    parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view  = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_recycler_view, parent, false);
            return new ListViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_ITEM) {
            ListViewHolder viewHolder = (ListViewHolder) holder;
            viewHolder.text.setText(data.get(position - 1).text);
        }
    }

    @Override
    public int getItemCount() {
        return data.size() + 1;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }
}
