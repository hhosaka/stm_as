package com.nag.android.stm;

public class EXIFManipurator {

	private int top=0;
	private int bottom=0;

	public int getTop(){
		return top;
	}

	public int getBottom(){
		return bottom;
	}

	private boolean isSOI(byte[]data){
		return data[0]==0xff && data[1]==0xd8;
	}

	private boolean isEOI(byte[]data, int cur){
		return data[cur]==0xff && data[cur+1]==0xd9;
	}

	private boolean isRotate(byte[]data, int cur){
		return data[cur]==0x01 && data[cur+1]==0x12;
	}

	private boolean isEXIFMarker(byte[]data, int cur){
		return data[cur]==0xff && data[cur+1]==0xe1;
	}

//	private int getMarkerLength(byte[]data,int cur){
//		return data[cur+2]<<8|data[cur+3];
//	}

//	private int getDWORD(byte[]data,int cur){
//		return (data[cur]<<24)|(data[cur+1]<<16)|(data[cur+2]<<8)|(data[cur+3]);
//	}

	private static int getWORD(byte[]data,int cur){
		return data[cur]<<8|data[cur];
	}

	private boolean seekOrientation(byte[]data, int cur, int cnt){
		for(int i=0;i<cnt;++i){
			if(isRotate(data,cur)){
				top = cur;
				bottom = cur + 12;
				return true;
			}else{
				cur+=12;
			}
		}
		top = cur;
		bottom = cur;
		return false;
	}

	public boolean parseExif(byte[]data,int cur){
		if(data[cur+2]=='E'
			&&data[cur+3]=='x'
			&&data[cur+4]=='i'
			&&data[cur+5]=='f'
			&&data[cur+10]==0x00
			&&data[cur+11]==0x2a){
//			assert(data[cur+8]=='M'&&data[cur+9]=='M');// only Big endian support
			cur += 12;
			if(!seekOrientation(data, cur + 2, getWORD(data,cur))){
				++data[cur+1];
			}
		}
		return false;
	}

	public boolean parse(byte[]data){
		if(isSOI(data)){
			int cur = 2;
			while(!isEOI(data,cur)){
				if(isEXIFMarker(data,cur)){
					if(parseExif(data,cur + 2)){
						return true;
					}
				}else{
					cur+=2;
					cur += getWORD(data,cur);
				}
			}
		}
		return false;
	}
}
