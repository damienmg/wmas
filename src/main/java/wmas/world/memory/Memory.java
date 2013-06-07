package wmas.world.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import wmas.expression.DereferenceableObject;
import wmas.util.Doublet;
import wmas.xml.XMLCrossRef;
import wmas.xml.XMLEntity;

public class Memory implements Iterable<Integer>, XMLEntity {
	private HashMap<Integer, Data> datas = new HashMap<Integer, Data>(); // the
																			// map
																			// of
																			// all
																			// stored
																			// data
	private LinkedList<Integer> dataKeys = new LinkedList<Integer>();
	private long memorySize;

	private final static char[] memoryTypes = { 'b', 'k', 'M', 'G', 'T', 'P' };

	public Memory() {
		memorySize = 1024 * 1024; // 1M default
	}

	public long getTotalSize() {
		return memorySize;
	}

	public long getFreeSize() {
		return memorySize - getSize();
	}

	public String getMemorySize() {
		int index = 0;
		double mSize = memorySize;
		while (mSize >= 1024 && index < memoryTypes.length) {
			index++;
			mSize /= 1024;
		}
		mSize = Math.floor(mSize * 100) / 100;
		if ((long) mSize == mSize) {
			return Long.toString((long) mSize) + memoryTypes[index];
		}
		return Double.toString(mSize) + memoryTypes[index];
	}

	public boolean setMemorySize(String s) {
		s = s.trim();
		if (s.length() == 0)
			return false;
		char lastChar = s.charAt(s.length() - 1);
		try {
			if (Character.isLetter(lastChar)) {
				lastChar = Character.toLowerCase(lastChar);
				long value = Long.parseLong(s.substring(0, s.length() - 1));
				long base = 1;
				int index = 0;
				while (index < memoryTypes.length
						&& Character.toLowerCase(memoryTypes[index]) != lastChar) {
					index++;
					base *= 1024;
				}
				if (index >= memoryTypes.length)
					return false;
				memorySize = value * base;
				return true;
			} else {
				memorySize = Long.parseLong(s);
				return true;
			}
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	public long getSize() {
		long size = 0;
		for (Data d : datas.values())
			size += d.getSize();
		return size;
	}

	private void addIndex(int index) {
		if (dataKeys.contains(index))
			return;
		Integer i;
		for (ListIterator<Integer> it = dataKeys.listIterator(); it.hasNext();) {
			i = it.next();
			if (i > index) {
				it.previous();
				it.add(index);
				return;
			}
		}
		dataKeys.add(index);
	}

	public boolean add(int index, Data d) {
		long ms = d.getSize();
		int[] path = new int[] { index };
		if (datas.containsKey(index)) {
			ms -= datas.get(index).getSize();
		}
		if (getSize() + ms > memorySize)
			return false;
		if (datas.containsKey(index)) {
			datas.get(index).setMemory(null);
		}
		datas.put(index, d);
		d.setParent(null);
		d.setMemory(this);
		addIndex(index);
		changed(path);
		return true;
	}

	public void clear() {
		for (Data d : datas.values())
			d.setMemory(null);
		datas.clear();
		dataKeys.clear();
		changed(new int[] {});
	}

	public void delete(int identifier) {
		if (datas.containsKey(identifier)) {
			datas.get(identifier).setMemory(null);
			datas.remove(identifier);
			dataKeys.remove(identifier);
			changed(new int[] { identifier });
		}
	}

	public void delete(Collection<Integer> toDelete) {
		for (Integer i : toDelete)
			delete(i.intValue());
	}

	public Data getData(int identifier) {
		return datas.get(identifier);
	}

	public Iterator<Integer> iterator() {
		return datas.keySet().iterator();
	}

	public void set(Memory data) {
		this.datas = data.datas;
		for (Data d : data.datas.values()) {
			d.setMemory(this);
			d.setParent(null);
		}
		changed(new int[] {});
	}

	@Override
	public XMLEntity copy() {
		Memory mem = new Memory();
		mem.memorySize = memorySize;
		return mem;
	}

	@Override
	public void fromXML(Element e, XMLCrossRef refs) throws Exception {
		memorySize = Long.parseLong(e.getAttribute("size"));
	}

	@Override
	public Element toXML(Document root, XMLCrossRef refs) throws Exception {
		Element e = root.createElement(this.getClass().getName());
		e.setAttribute("size", Long.toString(memorySize));
		return e;
	}

	static Data createNumberData(Number v) {
		if (v.longValue() == v.doubleValue()) {
			LongData d = new LongData();
			d.setData(v.longValue());
			return d;
		} else {
			DoubleData d = new DoubleData();
			d.setData(v.doubleValue());
			return d;
		}
	}

	public static Data createData(Object v) {
		if (v instanceof Data) {
			return ((Data) v).copy();
		} else if (v instanceof Number) {
			return createNumberData((Number) v);
		} else if (v instanceof DereferenceableObject) {
			return new ArrayData((DereferenceableObject) v);
		} else {
			StringData d = new StringData();
			d.setData(v.toString());
			return d;
		}
	}

	public int getNbData() {
		return dataKeys.size();
	}

	public int getDataIndex(int index) {
		return index < dataKeys.size() ? dataKeys.get(index) : null;
	}

	private Set<MemoryListener> listeners = new HashSet<MemoryListener>();

	public void addListener(MemoryListener listener) {
		listeners.add(listener);
	}

	public void removeListener(MemoryListener listener) {
		listeners.remove(listener);
	}

	private void changed(int[] indexes) {
		for (MemoryListener l : listeners)
			l.changed(indexes);
	}

	public void setData(int[] dataSetDescription, Data[] data) {
		if (dataSetDescription.length > data.length) { // Deletion
			for (int i = data.length; i < dataSetDescription.length; i++) {
				if (dataSetDescription[i] >= 0)
					delete(dataSetDescription[i]);
				else {
					clear();
				}
			}
		}
		for (int i = 0; i < Math.min(dataSetDescription.length, data.length); i++) {
			if (dataSetDescription[i] >= 0) {
				add(dataSetDescription[i], data[i]);
			}
		}
	}

	public Doublet<Data[], int[]> getData(int[] dataSetDescription) {
		Data[] r1;
		int[] r2;
		if (dataSetDescription == null || dataSetDescription.length == 0) { // Only
																			// the
																			// list
																			// of
																			// availables
																			// data
			r1 = new Data[0];
			r2 = new int[datas.size()];
			int i = 0;
			for (int id : datas.keySet()) {
				r2[i] = id;
				i++;
			}
		} else if (dataSetDescription.length == 1 && dataSetDescription[0] < 0) { // The
																					// whole
																					// memory
			r1 = new Data[datas.size()];
			r2 = new int[datas.size()];
			int i = 0;
			for (int id : datas.keySet()) {
				r2[i] = id;
				r1[i] = datas.get(id).copy();
				i++;
			}
		} else {
			r1 = new Data[dataSetDescription.length];
			r2 = dataSetDescription;
			for (int i = 0; i < dataSetDescription.length; i++) {
				r1[i] = datas.containsKey(r2[i]) ? datas.get(r2[i]).copy()
						: null;
			}
		}
		return new Doublet<Data[], int[]>(r1, r2);
	}

}
