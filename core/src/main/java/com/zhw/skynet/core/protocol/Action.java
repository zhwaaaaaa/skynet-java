package com.zhw.skynet.core.protocol;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.function.BiConsumer;

/**
 * 用于异步通知结果。只能通知一次。
 * {@link #notifyError(Throwable)} 和 {@link #notifyResp(Object)}} 返回true表示成功通知。
 * 否则需要通知着自己释放资源.
 * <p>
 * notify的时候必须要要
 *
 * @param <T>
 */
public class Action<T> {
    private static final int STATE_GIVE_UP = -2;
    private static final int STATE_NOTIFY_EMPTY = -1;
    private static final int STATE_NOTIFY = 0;
    private static final int STATE_WAITING = 1;
    private static final int STATE_NOT_WAITING = 2;

    private static final class Sync extends AbstractQueuedSynchronizer {
        Sync(int initState) {
            setState(initState);
        }

        /**
         * @param u 要更新的值
         * @param a 当前状态
         * @param b 失败状态
         * @return 是当前状态并且更新成功
         */
        boolean trySetIfIsANotB(int u, int a, int b) {
            for (; ; ) {
                int c = getState();
                if (c == a) {
                    if (compareAndSetState(a, u)) {
                        return true;
                    }
                } else if (c == b) {
                    return false;
                } else {
                    throw new IllegalStateException("[BUG]illegal state " + c);
                }
            }
        }

        protected boolean tryAcquire(int acquires) {
            //STATE_NOTIFY ||  STATE_NOTIFY_EMPTY||STATE_WAITING_TIMEOUT
            return getState() <= 0;
        }

        protected boolean tryRelease(int releases) {
            int u;
            for (; ; ) {
                int c = getState();
                //STATE_NOTIFY ||STATE_NOTIFY_EMPTY||STATE_WAITING_TIMEOUT
                if (c <= 0) {
                    return false;
                } else if (c == STATE_WAITING) {
                    u = STATE_NOTIFY;
                } else {
                    u = STATE_NOTIFY_EMPTY;
                }

                if (compareAndSetState(c, u)) {
                    return true;
                }
            }
        }
    }

    private Throwable err;
    private T result;
    private final BiConsumer<T, Throwable> consumer;

    private Sync sync;

    public Action() {
        sync = new Sync(STATE_NOT_WAITING);
        consumer = null;
    }

    public Action(BiConsumer<T, Throwable> consumer) {
        this.consumer = consumer;
        sync = new Sync(STATE_WAITING);
    }

    public final boolean notifyResp(T result) {

        if (consumer == null) {
            this.result = result;
            return sync.release(1);
        } else {
            if (sync.release(1)) {
                consumer.accept(result, null);
                return true;
            }
            return false;
        }
    }

    public final boolean notifyError(Throwable err) {
        if (consumer == null) {
            this.err = err;
            return sync.release(1);
        } else {
            if (sync.release(1)) {
                consumer.accept(null, err);
                return true;
            }
            return false;
        }
    }

    public final T waitResponse(long timeout) throws Throwable {
        if (consumer != null) {
            throw new IllegalStateException("[BUG]has consumer cannot wait");
        }

        if (!sync.trySetIfIsANotB(STATE_WAITING, STATE_NOT_WAITING, STATE_NOTIFY_EMPTY)) {
            // 已经STATE_NOTIFY_EMPTY
            if (err != null) {
                throw err;
            }
            return result;
        }
        boolean b = false;
        try {
            b = sync.tryAcquireNanos(1, timeout * 1000000);
        } catch (InterruptedException e) {
            // 打断
            if (sync.trySetIfIsANotB(STATE_GIVE_UP, STATE_WAITING, STATE_NOTIFY)) {
                throw e;
            }
        }
        // 超时 且 放弃失败
        if (!b && sync.trySetIfIsANotB(STATE_GIVE_UP, STATE_WAITING, STATE_NOTIFY)) {
            return null;
        }

        if (err != null) {
            throw err;
        }
        return result;
    }

    public final T waitResponse() throws Throwable {
        if (consumer != null) {
            throw new IllegalStateException("[BUG]has consumer cannot wait");
        }

        if (!sync.trySetIfIsANotB(STATE_WAITING, STATE_NOT_WAITING, STATE_NOTIFY_EMPTY)) {
            // 已经STATE_NOTIFY_EMPTY
            if (err != null) {
                throw err;
            }
            return result;
        }

        try {
            sync.acquireInterruptibly(1);
        } catch (InterruptedException e) {
            //
            if (sync.trySetIfIsANotB(STATE_GIVE_UP, STATE_WAITING, STATE_NOTIFY)) {
                throw e;
            }
        }
        if (err != null) {
            throw err;
        }
        return result;
    }
}
