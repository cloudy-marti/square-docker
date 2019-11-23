package fr.umlv.square.utils;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Counter {
	private int count;
	private int currentNumber;
	private final Object lock = new Object();
	
	public Counter(int count) {
		synchronized (this.lock) {
			this.count = count;
		}
	}
	
	
	public Counter(){}

	public Counter inc() {
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
	
	public void UpdateCurrentCounter(int number) {
		synchronized (this.lock) {
			this.currentNumber = number;
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
