package com.greenisland.taxi.test;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-24上午12:00:37
 */
public class Test {
	public static void main(String[] args) {
		new Test().init();
	}

	private void init() {
		final Sweetheart s = new Sweetheart();
		new Thread(new java.lang.Runnable() {

			public void run() {
				for (int i = 1; i <= 50; i++) {
					s.boy(i);
				}
			}
		}).start();
		for (int i = 1; i <= 50; i++) {
			s.girl(i);
		}
	}
}

class Sweetheart {
	synchronized void boy(int i) {
		System.out.println("我好想见到一个女孩呀,怎么没有呢!,次数: " + i);
	}

	synchronized void girl(int i) {
		System.out.println("怎么这条路上男孩子都没有一个呢!死哪了...,次数: " + i);
	}
}
