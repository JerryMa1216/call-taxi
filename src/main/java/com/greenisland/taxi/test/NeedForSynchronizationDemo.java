package com.greenisland.taxi.test;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-10-24上午12:33:26
 */
public class NeedForSynchronizationDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FinTrans ft = new FinTrans();
		TransThread tt1 = new TransThread(ft, "Deposit Thread");
		TransThread tt2 = new TransThread(ft, "Withdrawal Thread");
		tt1.start();
		tt2.start();
	}

}
