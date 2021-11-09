package com.can;

import com.can.data.structure.sds.Sds;
import com.can.data.structure.sds.SdsHdr8;
import com.can.data.structure.skiplist.SkipList;

/**
 * Hello world!
 */
public class App {

	public static void main(String[] args) {

		SkipList skipList = new SkipList();

		char[] sdsContent = new char[2];
		sdsContent[0] = '1';
		sdsContent[1] = '2';
		Sds sds = new SdsHdr8(sdsContent);

		for (int i = 0; i < 10; i += 2) {
			skipList.insert(i, sds);
		}

		skipList.insert(7, sds);
		skipList.insert(9, sds);

	}
}
