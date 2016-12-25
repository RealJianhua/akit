package wenjh.akit.common.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class BaseListAdapter<T> extends BaseAdapter {

	protected List<T> items;
	private boolean notifyOnChange = true;
	protected Context context = null;
	protected LayoutInflater inflater = null;
	private OnSizeChangedListener listener = null;

	public BaseListAdapter(Context context) {
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		items = new ArrayList<T>();
	}

	public View inflate(int layoutId) {
		return inflater.inflate(layoutId, null);
	}
	
	public BaseListAdapter(Context context, List<T> objects) {
		this(context);
		this.items = objects;
	}
	
	public List<T> getItems() {
		return items;
	}
	
	/**
	 * 监听适配器数据数量变化事件。
	 * @param listener
	 */
	public void setListener(OnSizeChangedListener listener) {
		this.listener = listener;
	}

	/**
	 * 在列表末尾添加一个元素
	 * 
	 * @param object
	 */
	public void add(T object) {
		items.add(object);
		
		if(listener != null)
			listener.onSizeChanged(items, items.size());
		
		if (notifyOnChange) {
			notifyDataSetChanged();
		}
	}
	
	public void replaceItem(int position, T t) {
		items.remove(position);
		items.add(position, t);
		if(notifyOnChange) {
			notifyDataSetChanged();
		}
	}
	
	public void replace(Collection<? extends T> collection) {
		items.clear();
		addAll(collection);
	}
	
	/**
	 * 向adapter添加一个元素。不刷新界面
	 * @param object
	 */
	public void addTo(T object) {
		items.add(object);
		if(listener != null)
			listener.onSizeChanged(items, items.size());
	}
	
	public void addTo(int p, T object) {
		items.add(p, object);
		if(listener != null)
			listener.onSizeChanged(items, items.size());
	}
	
	/**
	 * 在列表指定位置添加一个元素
	 * 
	 * @param object
	 */
	public void add(int prosition,T object) {
		items.add(prosition,object);
		if(listener != null)
			listener.onSizeChanged(items, items.size());
		
		if (notifyOnChange) {
			notifyDataSetChanged();
		}
	}
	

	/**
	 * 添加集合中的元素到列表末尾
	 * 
	 * @param collection
	 */
	public void addAll(Collection<? extends T> collection) {
		addAll(collection, notifyOnChange);
	}
	
	public void addAll(Collection<? extends T> collection,boolean notify) {
		items.addAll(collection);
		
		if(listener != null)
			listener.onSizeChanged(items, items.size());
		
		if (notify)
			notifyDataSetChanged();
	}

	/**
	 * 添加集合中的元素到列表指定位置
	 * 
	 * @param collection
	 */
	public void addAll(int porsition,Collection<? extends T> collection) {
		items.addAll(porsition,collection);
		
		if(listener != null)
			listener.onSizeChanged(items, items.size());
		
		if (notifyOnChange)
			notifyDataSetChanged();
	}
	
	/**
	 * 添加若干个中的元素到列表末尾
	 * 
	 * @param
	 */
	public void addAll(T... items) {
		for (T item : items) {
			this.items.add(item);
		}
		
		if(listener != null)
			listener.onSizeChanged(this.items, this.items.size());
		
		if (notifyOnChange)
			notifyDataSetChanged();
	}

	/**
	 * 插入元素到指定位置
	 * 
	 * @param object
	 * @param index
	 */
	public void insert(T object, int index) {
		items.add(index, object);
		if (notifyOnChange)
			notifyDataSetChanged();
	}

	/**
	 * 移除一个元素
	 * 
	 * @param object
	 */
	public boolean remove(T object) {
		boolean b = items.remove(object);
		
		if(listener != null)
			listener.onSizeChanged(items, items.size());
		
		if (notifyOnChange)
			notifyDataSetChanged();
		
		return b;
	}

	/**
	 * 移除指定位置的元素
	 * 
	 * @param index
	 */
	public T remove(int position) {
		T t = items.remove(position);
		
		if(listener != null)
			listener.onSizeChanged(items, items.size());
		
		if (notifyOnChange)
			notifyDataSetChanged();
		return t;
	}

	public T delete(int position) {
		T t = items.remove(position);
		
		if(listener != null)
			listener.onSizeChanged(items, items.size());
		
		return t;
	}
	
	public void delete(T t) {
		items.remove(t);
		
		if(listener != null)
			listener.onSizeChanged(items, items.size());
	}
	
	/**
	 * 对列表进行排序
	 * 
	 * @param comparator
	 */
	public void sort(Comparator<? super T> comparator) {
		Collections.sort(items, comparator);
		if (notifyOnChange)
			notifyDataSetChanged();
	}

	/**
	 * 清空列表
	 */
	public void clear() {
		clear(notifyOnChange);
	}
	
	public void clear(boolean notify) {
		items.clear();
		
		if(listener != null)
			listener.onSizeChanged(items, items.size());
		
		if (notify)
			notifyDataSetChanged();
	}

	public void notifyDataSetChanged() {
		// in main thread
		if (Looper.myLooper() == getContext().getMainLooper()) {
			super.notifyDataSetChanged();
		} else {
			new Handler(getContext().getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					BaseListAdapter.this.notifyDataSetChanged();
				}
			});
		}
		notifyOnChange = true;
	}

	/**
	 * 设置为true时，那么当{@link #add(Object)}, {@link #insert(Object, int)},
	 * {@link #remove(Object)}等等添加/修改数据集合等操作时， 将调用
	 * {@link #notifyDataSetChanged()}方法更新界面。如果设置为false，则只有当手动调用
	 * {@link #notifyDataSetChanged()}方法更新界面。
	 * 
	 * @param notifyOnChange
	 */
	public void setNotifyOnChange(boolean notifyOnChange) {
		this.notifyOnChange = notifyOnChange;
	}

	public int getCount() {
		return items.size();
	}

	public T getItem(int position) {
		return items.get(position);
	}

	public int getItemPosition(T item) {
		return items.indexOf(item);
	}
	
	public int getPosition(T item) {
		return items.indexOf(item);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public abstract View getView(int position, View convertView,
			ViewGroup parent);

	public Context getContext() {
		return context;
	}

	public interface OnSizeChangedListener {
		void onSizeChanged(@SuppressWarnings("rawtypes") List items, int size);
	}
	
}
