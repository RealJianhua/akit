package wenjh.akit.common.receiver;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsMessageReceiver implements Comparable<AbsMessageReceiver> {

	private List<String> actions = new ArrayList<String>(4);
	private int priority;
	
	public AbsMessageReceiver(String... actions) {
		for (String action : actions) {
			this.actions.add(action);
		}
	}
	
	/**
	 * 
	 * @param priority 权重。值越大将越早收到消息
	 * @param actions
	 */
	public AbsMessageReceiver(int priority, String... actions) {
		for (String action : actions) {
			this.actions.add(action);
		}
		this.priority = priority;
	}
	
	/**
	 * 设置权重。值越大将越早收到消息
	 * @param priority
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void addAction(String action) {
		this.actions.add(action);
	}
	
	public List<String> getActions() {
		return actions;
	}
	
	public abstract boolean onReceive(Bundle bundle, String action);
	
	/**
	 * 相同权值，按照注册顺序排序
	 */
	@Override
	public int compareTo(AbsMessageReceiver another) {
		if (another == null) {
			throw new NullPointerException("the message receiver shouldn't be null!");
		}		
		if(equals(another)) {
			return 0;
		} else {
			return this.priority > another.priority ? -1 : 1;
		}
	}

	@Override
	public String toString() {
		return "AbsMessageReceiver [actions=" + actions + ", priority="
				+ priority + "]";
	}
}
