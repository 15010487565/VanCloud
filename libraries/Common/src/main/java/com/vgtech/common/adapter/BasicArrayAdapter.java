package com.vgtech.common.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class BasicArrayAdapter<T> extends BaseAdapter {
    private List<T> mObjects;
    private boolean mNotifyOnChange = true;

    protected Context mContext;
    protected LayoutInflater mInflater;

    public BasicArrayAdapter(Context context) {
        init(context, null);
    }

    public BasicArrayAdapter(Context context, T[] objects) {
        init(context, Arrays.asList(objects));
    }

    public BasicArrayAdapter(Context context, Collection<T> objects) {
        init(context, objects);
    }

    public List<T> getmObjects() {
        return mObjects;
    }

    private void init(Context context, Collection<T> objects) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mObjects = new ArrayList<T>();
        if (objects != null) {
            mObjects.addAll(objects);
        }
    }

    public void add(Collection<T> objects) {
        mObjects.addAll(objects);
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public void add(T[] objects) {
        mObjects.addAll(Arrays.asList(objects));
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public void add(T object) {
        mObjects.add(object);
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public void insert(T object, int index) {
        mObjects.add(index, object);
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(T object) {

        mObjects.remove(object);
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public T remove(int position) {
        T t = mObjects.remove(position);
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
        return t;
    }

    public void remove(Collection<T> collection) {

        mObjects.removeAll(collection);
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        mObjects.clear();
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public void sort(Comparator<? super T> comparator) {
        Collections.sort(mObjects, comparator);
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }

    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    public Context getContext() {
        return mContext;
    }

    public int getCount() {
        return mObjects.size();
    }

    public T getItem(int position) {
        return mObjects.get(position);
    }

    public int getPosition(T item) {
        return mObjects.indexOf(item);
    }

    public long getItemId(int position) {
        return position;
    }
}
