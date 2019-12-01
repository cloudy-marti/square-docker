package fr.umlv.square.utils;

import javax.enterprise.context.ApplicationScoped;

/**
 * This class in a threadSafe counter.
 * It counts two things : 
 * 	- current number 
 *  - total incrementation 
 * @author FAU
 *
 */
@ApplicationScoped
public class SynchronizedCounter {
	private int count;
	private int currentNumber;
	private final Object lock = new Object();
	
	public SynchronizedCounter(int count) {
		synchronized (this.lock) {
			this.count = count;
		}
	}

	/**
	 * ApplicationScoped needed constructor
	 */
	public SynchronizedCounter(){}

	public SynchronizedCounter inc() {
		synchronized (this.lock) {
			this.count++;
			return this;
		}
	}
	
	public void add(int c) {
		synchronized (this.lock) {
			this.count+=c;
		}
	}
	
	public void incCurrentNumber() {
		synchronized (this.lock) {
			this.currentNumber++;
		}
	}	
	
	public void decCurrentNumber() {
		synchronized (this.lock) {
			this.currentNumber--;
		}
	}	
	
	public int getCurrentNumber() {
		synchronized (this.lock) {
			return this.currentNumber;
		}
	}
	public int getCount() {
		synchronized (this.lock) {
			return this.count;
		}
	}
}
