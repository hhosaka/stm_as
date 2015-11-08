package com.nag.android.stm;

import android.graphics.Point;

import com.nag.android.util.AbstractCharSequence;

public class SizeItem extends AbstractCharSequence {
	private final static String SEPARATOR = "x"; 
	private Point size;

	SizeItem(String label){
		int index = label.indexOf(SEPARATOR);
		if(index>0){
			size = new Point(
			Integer.parseInt(label.substring(index+1)),
			Integer.parseInt(label.substring(0,index)));
		}
	}

	SizeItem(int width, int height){
		size = new Point( width, height);
	}

	public String toString(){
		return size.y + SEPARATOR + size.x;
	}

	public Point getSize(){
		return size;
	}
}
