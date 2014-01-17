package com.greenisland.taxi.test;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-24上午12:09:43
 */
public class Test2 {
	public static void main(String[] args) {
		new Test2().init();
	}

	private void init() {
		final Sweethearts s = new Sweethearts();
		new Thread(new java.lang.Runnable() {

			public void run() {
				try {
					Thread.sleep(10000);
					for (int i = 1; i <= 50; i++) {
						s.boy(i);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}).start();
		for (int i = 1; i <= 50; i++) {
			s.girl(i);
		}
	}
}

class Sweethearts {
	boolean flag = false;

	synchronized void boy(int i) {
		// 判断是否被女孩唤醒
		while (!flag) {
			try {
				// 继续等待女孩发问
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 女孩问了，男孩回答
		System.out.println("男孩说：我叫马随！");
		// 回答完设置等待女孩发文标记
		flag = false;
		// 唤醒女孩发问
		notify();
	}

	synchronized void girl(int i) {
		// 判断是否被男孩唤醒
		while (flag) {
			try {
				// 等待男孩回答
				wait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 男孩回答完了，女孩又问男孩
		System.out.println("女孩问道：小子你叫什么？");
		// 设置等待男孩回答标记
		flag = true;
		notify();
	}
}