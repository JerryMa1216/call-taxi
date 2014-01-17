package com.greenisland.taxi.test;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-24上午12:31:39
 */
public class TransThread extends Thread {
	private FinTrans ft;

	TransThread(FinTrans ft, String name) {
		super(name); // Save thread's name
		this.ft = ft; // Save reference to financial transaction object
	}

	@Override
	public void run() {
		super.run();
		for (int i = 0; i < 100; i++) {
			if (getName().equals("Deposit Thread")) {
				// Start of deposit thread's critical code section
				// 存款操作（每次存2000元）
				synchronized (ft) {
					ft.transName = "Deposit";
					try {
						Thread.sleep((int) (Math.random() * 1000));
					} catch (InterruptedException e) {
					}
					ft.amount = 2000.0;
					System.out.println(ft.transName + " " + ft.amount);
				}
			} else {
				// Start of withdrawal thread's critical code section
				// 取款操作（每次取250元）
				synchronized (ft) {
					ft.transName = "Withdrawal";
					try {
						Thread.sleep((int) (Math.random() * 1000));
					} catch (InterruptedException e) {
					}
					ft.amount = 250.0;
					System.out.println(ft.transName + " " + ft.amount);
				}
			}
		}
	}

}
