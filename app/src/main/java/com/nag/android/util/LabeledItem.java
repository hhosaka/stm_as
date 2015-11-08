package com.nag.android.util;

public class LabeledItem<T> implements CharSequence{
	private String label;
	private T value;

	public LabeledItem(String label, T value){
		this.label = label;
		this.value = value;
	}

	public T getValue(){
		return value;
	}

	@Override
	public String toString(){
		return label;
	}

	@Override
	public char charAt(int index) {
		return label.charAt(index);
	}

	@Override
	public int length() {
		return label.length();
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return label.subSequence(start, end);
	}

	public static int indexOf(LabeledItem<?>[]items, Object value){
		for(int i=0; i<items.length; ++i){
			if(value.equals(items[i].getValue())){
				return i;
			}
		}
		return -1;
	}
}
