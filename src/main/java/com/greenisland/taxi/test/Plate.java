package com.greenisland.taxi.test;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-29下午5:05:28
 */
public class Plate {
	List<Object> eggs = new ArrayList<Object>();

	public synchronized Object getEgg() {
		while (eggs.size() == 0) {
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Object egg = eggs.get(0);
		eggs.clear();
		notify();
		System.out.println("拿到鸡蛋了！"+egg.toString());
		return egg;
	}

	public synchronized void putEgg(Object egg) {
		while (eggs.size() > 0) {
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		eggs.add(egg);
		notify();
		System.out.println("放入鸡蛋"+egg.toString());
	}

	static class AddThread extends Thread {
		private Plate plate;
		private Object egg = "鸡蛋1";

		public AddThread(Plate plate) {
			this.plate = plate;
		}

		public void run() {
			plate.putEgg(egg);
		}
	}

	static class GetThread extends Thread {
		private Plate plate;

		public GetThread(Plate plate) {
			this.plate = plate;
		}

		public void run() {
			plate.getEgg();
		}
	}

	public static void main(String[] args) {
		Plate plate = new Plate();
		for (int i = 0; i < 10; i++) {
			new Thread(new GetThread(plate)).start();
			new Thread(new AddThread(plate)).start();
			new Thread(new AddThread(plate)).start();
			new Thread(new GetThread(plate)).start();
		}
	}
}
