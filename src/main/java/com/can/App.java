package com.can;

/**
 * Hello world!
 */
public class App {

	public static void main(String[] args) {

		int a = (0xc0 | 2 << 4);

		System.out.println(a + " " + Integer.toBinaryString(a ));
	}

	private static char[] createCharArr(char... args) {

		int length = args.length;
		char[] arr = new char[length];

		for (int i = 0; i < args.length; i++) {
			arr[i] = args[i];
		}
		return arr;
	}

}
