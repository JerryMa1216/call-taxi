package com.greenisland.taxi.test;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-25下午9:27:41
 */
public class Sample {
	private int number;

	public synchronized void increase() {
		while (0 != number) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		number++;
		System.out.println(number);
		notify();
	}

	public synchronized void decrease() {
		while (0 == number) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		number--;
		System.out.println(number);
		notify();
	}
}

class IncreaseThread extends Thread {
	private Sample sample;

	public IncreaseThread(Sample sample) {
		this.sample = sample;
	}

	@Override
	public void run() {
		for (int i = 0; i < 20; i++) {
			try {
				Thread.sleep((long) (Math.random() * 100));
			} catch (Exception e) {
				e.printStackTrace();
			}
			sample.increase();
		}
	}
}

class DecreaseThread extends Thread {
	private Sample sample;

	public DecreaseThread(Sample sample) {
		this.sample = sample;
	}

	@Override
	public void run() {
		for (int i = 0; i < 20; i++) {
			try {
				Thread.sleep((long) (Math.random() * 100));
			} catch (Exception e) {
				e.printStackTrace();
			}
			sample.decrease();
		}
	}
}