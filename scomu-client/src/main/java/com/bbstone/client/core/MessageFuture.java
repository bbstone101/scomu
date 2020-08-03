package com.bbstone.client.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.bbstone.comm.ResultListner;
import com.bbstone.comm.enums.RetCode;
import com.bbstone.comm.model.CmdResult;

import lombok.extern.slf4j.Slf4j;

/**
 * TODO support attach listener to MsgFuture(priority: LOW)
 *
 * @author bbstone
 *
 */
@Slf4j
public class MessageFuture {

	private volatile CmdResult cmdResult = null;
	private List<ResultListner> listners = new ArrayList<>();

	private short waiters;
	// request startTime
	private long startTime;

	private static final AtomicReferenceFieldUpdater<MessageFuture, CmdResult> RESULT_UPDATER = AtomicReferenceFieldUpdater
			.newUpdater(MessageFuture.class, CmdResult.class, "cmdResult");

	public void addListener(ResultListner listener) {
		this.listners.add(listener);
	}

	private void incWaiters() {
		if (waiters == Short.MAX_VALUE) {
			throw new IllegalStateException("too many waiters: " + this);
		}
		++waiters;
	}

	private void decWaiters() {
		--waiters;
	}

	private boolean isDone() {
		return this.cmdResult != null;
	}

	private synchronized boolean checkNotifyWaiters() {
		if (waiters > 0) {
			notifyAll();
		}
		return true;
	}

	/**
	 * set result, and notify all waiting threads
	 * 
	 * @param cmdResult - this parameter should never null
	 */
	public void setResult(CmdResult cmdResult) {
		if (RESULT_UPDATER.compareAndSet(this, null, cmdResult)) {
			log.info("result is set, do wake up other threads.........");
			// req cost time
			log.info("request cost time: {} ms.", (System.currentTimeMillis() - startTime));
			if (checkNotifyWaiters()) {
//              notifyListeners();
				if (listners != null && listners.size() > 0) {
					for (ResultListner l : listners) {
						l.resultReady(cmdResult);
					}
				}
			}
			return;
		}
		// repeat filling the result
		throw new IllegalStateException("repeat filling the result: " + this);
	}

	/**
	 * get result, if result not ready, wait for it
	 * 
	 * @return
	 */
	public CmdResult getResult() {
		this.startTime = System.currentTimeMillis();
		return this.getResult(ClientConfig.reqTimeout, TimeUnit.MILLISECONDS);
	}

	
	/**
	 * get result, if result not ready, wait for it
	 * 
	 * @return
	 */
	public CmdResult getResultWithoutTimeout() {
		if (isDone()) {
			return this.cmdResult;
		}
		// handle thread interrupted situation
		if (Thread.interrupted()) {
			log.error("thread is interupted");
			return CmdResult.from(RetCode.THREAD_INTERRUPTED.code(), RetCode.THREAD_INTERRUPTED.descp());
//			throw new InterruptedException("Thread has been interrupted.");
		}
		// wait for result ready
		synchronized (this) {
			while (!isDone()) {
				incWaiters();
				try {
					wait();
				} catch (InterruptedException e) {
					if (isDone()) {
						return this.cmdResult;
					} else {
						log.error("thread is interupted");
						return CmdResult.from(RetCode.THREAD_INTERRUPTED.code(), RetCode.THREAD_INTERRUPTED.descp());
					}
				} finally {
					decWaiters();
				}
			}
		}
		//
		return cmdResult;
	}

	/**
	 * get result, if result not ready, wait for it at specified time
	 * 
	 * @param timeout - max time to wait the result ready
	 * @param unit    - time unit
	 * @return - the result
	 * @throws TimeoutException
	 */
	public CmdResult getResult(long timeout, TimeUnit unit) {
		if (isDone()) {
			return this.cmdResult;
		}
		// handle thread interrupted situation
		if (Thread.interrupted()) {
			log.error("thread is interupted");
			return CmdResult.from(RetCode.THREAD_INTERRUPTED.code(), RetCode.THREAD_INTERRUPTED.descp());
		}
		if (!await(timeout, unit)) {
			// handle thread interrupted situation
			if (Thread.interrupted()) {
				log.error("thread is interrupted");
				return CmdResult.from(RetCode.THREAD_INTERRUPTED.code(), RetCode.THREAD_INTERRUPTED.descp());
			}
			// req timeout
			log.warn("request timeout.");
			return CmdResult.from(RetCode.REQ_TIMEOUT.code(), RetCode.REQ_TIMEOUT.descp());
		}
		return cmdResult;
	}

	private boolean await(long timeout, TimeUnit unit) {
		if (isDone()) {
			return true;
		}
		long timeoutNanos = unit.toNanos(timeout);
		if (timeoutNanos <= 0) {
			log.warn("timeout value illegal, it should larger than 0. now timeoutNanos: {}", timeoutNanos);
			return isDone();
		}

		long startTime = System.nanoTime();
		long waitTime = timeoutNanos;
		for (;;) {
			synchronized (this) {
				if (isDone()) {
					return true;
				}
				incWaiters();
				try {
					wait(waitTime / 1000000, (int) (waitTime % 1000000));
				} catch (InterruptedException e) {
					log.warn("request waiting thread is interrupted.");
//						interrupted = true;
					// Interrupts this thread. set interrupt flag
					Thread.currentThread().interrupt();
					return isDone();
				} finally {
					decWaiters();
				}
			}
			if (isDone()) {
				return true;
			} else {
				waitTime = timeoutNanos - (System.nanoTime() - startTime);
				// wait time is up/exceed
				if (waitTime <= 0) {
					log.info("wait time is up/exceed: {} ns.", waitTime);
					return isDone();
				}
			}
		}
	}

	/**
	private boolean await0(long timeoutNanos, boolean interruptable) throws InterruptedException, InterruptedException {
		if (isDone()) {
			return true;
		}

		if (timeoutNanos <= 0) {
			log.warn("timeout should larger than 0. timeoutNanos: {}", timeoutNanos);
			return isDone();
		}

		if (interruptable && Thread.interrupted()) {
			throw new InterruptedException("Thread has been interrupted.");
		}

		long startTime = System.nanoTime();
		long waitTime = timeoutNanos;
		boolean interrupted = false;
		try {
			for (;;) {
				synchronized (this) {
					if (isDone()) {
						return true;
					}
					try {
						wait(waitTime / 1000000, (int) (waitTime % 1000000));
					} catch (InterruptedException e) {
						if (interruptable) {
							throw e;
						} else {
							interrupted = true;
						}
					}
				}
				if (isDone()) {
					return true;
				} else {
					waitTime = timeoutNanos - (System.nanoTime() - startTime);
					if (waitTime <= 0) {
						return isDone();
					}
				}
			}
		} finally {
			if (interrupted) {
				// set interrupt flag
				Thread.currentThread().interrupt();
			}
		}
	} */
	
}
