package com.greenisland.taxi.test;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-26上午10:31:23
 */
public class Thread1 extends Thread {

	private SynTest test;
	private int result;

	public void sendData(SynTest test, int value) {
		this.test = test;
		this.result = value;
	}

	@Override
	public void run() {
		while (true) {
			synchronized (test) {
				test.setValue(result);
			}
		}
	}

	public static void main(String[] args) {
		SynTest test = new SynTest();
		Thread1 ti = new Thread1();
		ti.start();
		int value = test.request(ti);
		System.out.println(value);
	}
}

class SynTest {
	private int value;

	public void setValue(int value) {
		this.value = value;
	}

	public synchronized int request(Thread1 thread1) {
		thread1.sendData(this, 6);
		return value;
	}
}