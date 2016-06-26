package org.spat.utility.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 顺序敏感的HashMap
 * get(int idx)/remove(int idx) 为按加入hashMap的顺序号 (index) 来取得/删除 数据
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class IndexHashMap extends HashMap {

	private static final long serialVersionUID = 1L;

	private List list = new ArrayList();

	public Object put(Object key, Object value) {
		if (!containsKey(key)) {
			list.add(key);
		}
		return super.put(key, value);
	}

	public Object get(int idx) {
		return super.get(getKey(idx));
	}

	public int getIndex(Object key) {
		return list.indexOf(key);
	}

	public Object getKey(int idx) {
		if (idx >= list.size())
			return null;
		return list.get(idx);
	}

	public void remove(int idx) {
		Object key = getKey(idx);
		removeFromList(getIndex(key));
		super.remove(key);
	}

	public Object remove(Object key) {
		removeFromList(getIndex(key));
		return super.remove(key);
	}

	public void clear() {
		this.list = new ArrayList();
		super.clear();
	}

	private void removeFromList(int idx) {
		if (idx < list.size() && idx >= 0) {
			list.remove(idx);
		}
	}
}