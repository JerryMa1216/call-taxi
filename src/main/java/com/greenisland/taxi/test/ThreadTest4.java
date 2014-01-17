package com.greenisland.taxi.test;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-25上午12:37:46
 */
public class ThreadTest4 {
	public static void main(String[] args) {
		Example example = new Example();
		Thread t1 = new TheThread(example);
		Thread t2 = new TheThread2(example);
		t1.start();
		t2.start();
	}
}

class Example {
	public synchronized void execute() {
		for (int i = 0; i < 20; i++) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("hello: " + i);
		}
	}

	public synchronized void execute2() {
		for (int i = 0; i < 20; i++) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("world: " + i);
		}
	}
}

class TheThread extends Thread {
	private Example example;

	public TheThread(Example example) {
		this.example = example;
	}

	@Override
	public void run() {
		super.run();
		this.example.execute();
	}
}

class TheThread2 extends Thread {
	private Example example;

	public TheThread2(Example example) {
		this.example = example;
	}

	@Override
	public void run() {
		super.run();
		this.example.execute2();
	}
}