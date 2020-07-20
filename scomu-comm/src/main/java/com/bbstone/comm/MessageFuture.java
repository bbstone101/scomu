package com.bbstone.comm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

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

	private static final AtomicReferenceFieldUpdater<MessageFuture, CmdResult> RESULT_UPDATER = AtomicReferenceFieldUpdater
			.newUpdater(MessageFuture.class, CmdResult.class, "cmdResult");

	public void setResult(CmdResult cmdResult) {
		if (RESULT_UPDATER.compareAndSet(this, null, cmdResult)) {
			log.info("result is set, do wake up other threads.........");
			checkNotifyWaiters();
//			if (checkNotifyWaiters()) {
//                notifyListeners();
//            }
			if (listners != null && listners.size() > 0) {
				for (ResultListner l : listners) {
					l.resultReady(cmdResult);
				}
			}
			return;
		}
		throw new IllegalStateException("complete already: " + this);
	}

	public CmdResult getResult() throws InterruptedException {
		if (isDone())
			return this.cmdResult;

		if (Thread.interrupted()) {
			throw new InterruptedException("Thread has been interrupted.");
		}

		synchronized (this) {
			while (!isDone()) {
				incWaiters();
				try {
					wait();
				} finally {
					decWaiters();
				}
			}
		}

		return cmdResult;
	}

	public CmdResult getResult(long timeout, TimeUnit unit) throws TimeoutException {
		if (!isDone()) {
			try {
				if (!await(timeout, unit)) {
					throw new TimeoutException();
				}
			} catch (InterruptedException e) {
				return null;
			}
		}
		return cmdResult;
	}

	public void addListener(ResultListner listener) {
		this.listners.add(listener);
	}

	private synchronized boolean checkNotifyWaiters() {
		if (waiters > 0) {
			notifyAll();
		}
		return true;
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

	public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
		return await0(unit.toNanos(timeout), true);
	}

	private boolean await0(long timeoutNanos, boolean interruptable) throws InterruptedException, InterruptedException {
		if (isDone()) {
			return true;
		}

		if (timeoutNanos <= 0) {
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
				Thread.currentThread().interrupt();
			}
		}
	}
}
