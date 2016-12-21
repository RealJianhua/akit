package wenjh.akit.activity.base;

import java.util.ArrayList;
import java.util.Collection;

public class TipsList extends ArrayList<TipsMessage> {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean add(TipsMessage object) {
		boolean r = super.add(object);
		sort();
		return r;
	}

	public TipsMessage peek() {
		if (size() > 0) {
			return get(0);
		} else {
			return null;
		}
	}

	@Override
	public void add(int index, TipsMessage object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends TipsMessage> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends TipsMessage> collection) {
		boolean r = super.addAll(collection);
		sort();
		return r;
	}

	/**
	 * 对接收器按照权重排序，权重越高，将越先接收到消息
	 */
	protected synchronized void sort() {
		TipsMessage[] array = toArray(new TipsMessage[size()]);

		TipsMessage temp;
		int in;
		for (int i = 1; i < array.length; i++) {
			temp = array[i];
			in = i;
			while (in > 0 && (array[in - 1].priority < temp.priority)) {
				array[in] = array[in - 1];
				--in;
			}
			array[in] = temp;
		}

		clear();
		for (TipsMessage absMessageReceiver : array) {
			super.add(absMessageReceiver);
		}
	}
}