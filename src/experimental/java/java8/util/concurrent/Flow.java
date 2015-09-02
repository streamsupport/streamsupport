/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java8.util.concurrent;

import java.util.ArrayList;

import java8.util.function.Consumer;
import java8.util.function.Function;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;

/**
 * Interrelated interfaces and static methods for establishing
 * flow-controlled components in which {@link Publisher Publishers}
 * produce items consumed by one or more {@link Subscriber
 * Subscribers}, each managed by a {@link Subscription
 * Subscription}.
 *
 * <p>These interfaces correspond to the <a
 * href="http://www.reactive-streams.org/"> reactive-streams</a>
 * specification.  They apply in both concurrent and distributed
 * asynchronous settings: All (seven) methods are defined in {@code
 * void} "one-way" message style. Communication relies on a simple form
 * of flow control (method {@link Subscription#request}) that can be
 * used to avoid resource management problems that may otherwise occur
 * in "push" based systems.
 *
 * <p><b>Examples.</b> A {@link Publisher} usually defines its own
 * {@link Subscription} implementation; constructing one in method
 * {@code subscribe} and issuing it to the calling {@link
 * Subscriber}. It publishes items to the subscriber asynchronously,
 * normally using an {@link Executor}.  For example, here is a very
 * simple publisher that only issues (when requested) a single {@code
 * TRUE} item to a single subscriber.  Because the subscriber receives
 * only a single item, this class does not use buffering and ordering
 * control required in most implementations (for example {@link
 * SubmissionPublisher}).
 *
 * <pre> {@code
 * class OneShotPublisher implements Publisher<Boolean> {
 *   private final ExecutorService executor = ForkJoinPool.commonPool(); // daemon-based
 *   private boolean subscribed = false; // true after first subscribe
 *   public synchronized void subscribe(Subscriber<? super Boolean> subscriber) {
 *     if (subscribed)
 *        subscriber.onError(new IllegalStateException()); // only one allowed
 *     else {
 *       subscribed = true;
 *       subscriber.onSubscribe(new OneShotSubscription(subscriber, executor));
 *     }
 *   }
 *   static class OneShotSubscription implements Subscription {
 *     private final Subscriber<? super Boolean> subscriber;
 *     private final ExecutorService executor;
 *     private Future<?> future; // to allow cancellation
 *     private boolean completed = false;
 *     OneShotSubscription(Subscriber<? super Boolean> subscriber,
 *                         ExecutorService executor) {
 *       this.subscriber = subscriber;
 *       this.executor = executor;
 *     }
 *     public synchronized void request(long n) {
 *       if (n != 0 && !completed) {
 *         completed = true;
 *         if (n < 0) {
 *           IllegalStateException ex = new IllegalStateException();
 *           executor.execute(() -> subscriber.onError(ex));
 *         }
 *         else {
 *           future = executor.submit(() -> {
 *             subscriber.onNext(Boolean.TRUE);
 *             subscriber.onComplete();
 *           });
 *         }
 *       }
 *     }
 *     public synchronized void cancel() {
 *       completed = true;
 *       if (future != null) future.cancel(false);
 *     }
 *   }
 * }}</pre>
 *
 * <p>A {@link Subscriber} arranges that items be requested and
 * processed.  Items (invocations of {@link Subscriber#onNext}) are
 * not issued unless requested, but multiple items may be requested.
 * Many Subscriber implementations can arrange this in the style of
 * the following example, where a buffer size of 1 single-steps, and
 * larger sizes usually allow for more efficient overlapped processing
 * with less communication; for example with a value of 64, this keeps
 * total outstanding requests between 32 and 64.  (See also {@link
 * #consume(long, Publisher, Consumer)} that automates a common case.)
 * Because Subscriber method invocations for a given {@link
 * Subscription} are strictly ordered, there is no need for these
 * methods to use locks or volatiles unless a Subscriber maintains
 * multiple Subscriptions (in which case it is better to instead
 * define multiple Subscribers, each with its own Subscription).
 *
 * <pre> {@code
 * class SampleSubscriber<T> implements Subscriber<T> {
 *   final Consumer<? super T> consumer;
 *   Subscription subscription;
 *   final long bufferSize;
 *   long count;
 *   SampleSubscriber(long bufferSize, Consumer<? super T> consumer) {
 *     this.bufferSize = bufferSize;
 *     this.consumer = consumer;
 *   }
 *   public void onSubscribe(Subscription subscription) {
 *     long initialRequestSize = bufferSize;
 *     count = bufferSize - bufferSize / 2; // re-request when half consumed
 *     (this.subscription = subscription).request(initialRequestSize);
 *   }
 *   public void onNext(T item) {
 *     if (--count <= 0)
 *       subscription.request(count = bufferSize - bufferSize / 2);
 *     consumer.accept(item);
 *   }
 *   public void onError(Throwable ex) { ex.printStackTrace(); }
 *   public void onComplete() {}
 * }}</pre>
 *
 * <p>The default value of {@link #defaultBufferSize} may provide a
 * useful starting point for choosing request sizes and capacities in
 * Flow components based on expected rates, resources, and usages.
 * Or, when flow control is known to be always inapplicable, a
 * subscriber may initially request an effectively unbounded number of
 * items, as in:
 *
 * <pre> {@code
 * class UnboundedSubscriber<T> implements Subscriber<T> {
 *   public void onSubscribe(Subscription subscription) {
 *     subscription.request(Long.MAX_VALUE); // effectively unbounded
 *   }
 *   public void onNext(T item) { use(item); }
 *   public void onError(Throwable ex) { ex.printStackTrace(); }
 *   public void onComplete() {}
 *   void use(T item) { ... }
 * }}</pre>
 *
 * @author Doug Lea
 * @since 1.9
 */
// revision 1.23 from 2015-07-24
// http://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/src/main/java/util/concurrent/Flow.java?revision=1.23
public final class Flow {

    private Flow() {} // uninstantiable

    /**
     * A producer of items (and related control messages) received by
     * Subscribers.  Each current {@link Subscriber} receives the same
     * items (via method {@code onNext}) in the same order, unless
     * drops or errors are encountered. If a Publisher encounters an
     * error that does not allow items to be issued to a Subscriber,
     * that Subscriber receives {@code onError}, and then receives no
     * further messages.  Otherwise, when it is known that no further
     * messages will be issued to it, a subscriber receives {@code
     * onComplete}.  Publishers ensure that Subscriber method
     * invocations for each subscription are strictly ordered in <a
     * href="package-summary.html#MemoryVisibility"><i>happens-before</i></a>
     * order.
     *
     * <p>Publishers may vary in policy about whether drops (failures
     * to issue an item because of resource limitations) are treated
     * as unrecoverable errors.  Publishers may also vary about
     * whether Subscribers receive items that were produced or
     * available before they subscribed.
     *
     * @param <T> the published item type
     */
    public static interface Publisher<T> {
        /**
         * Adds the given Subscriber if possible.  If already
         * subscribed, or the attempt to subscribe fails due to policy
         * violations or errors, the Subscriber's {@code onError}
         * method is invoked with an {@link IllegalStateException}.
         * Otherwise, the Subscriber's {@code onSubscribe} method is
         * invoked with a new {@link Subscription}.  Subscribers may
         * enable receiving items by invoking the {@code request}
         * method of this Subscription, and may unsubscribe by
         * invoking its {@code cancel} method.
         *
         * @param subscriber the subscriber
         * @throws NullPointerException if subscriber is null
         */
        public void subscribe(Subscriber<? super T> subscriber);
    }

    /**
     * A receiver of messages.  The methods in this interface are
     * invoked in strict sequential order for each {@link
     * Subscription}.
     *
     * @param <T> the subscribed item type
     */
    public static interface Subscriber<T> {
        /**
         * Method invoked prior to invoking any other Subscriber
         * methods for the given Subscription. If this method throws
         * an exception, resulting behavior is not guaranteed, but may
         * cause the Subscription to be cancelled.
         *
         * <p>Typically, implementations of this method invoke {@code
         * subscription.request} to enable receiving items.
         *
         * @param subscription a new subscription
         */
        public void onSubscribe(Subscription subscription);

        /**
         * Method invoked with a Subscription's next item.  If this
         * method throws an exception, resulting behavior is not
         * guaranteed, but may cause the Subscription to be cancelled.
         *
         * @param item the item
         */
        public void onNext(T item);

        /**
         * Method invoked upon an unrecoverable error encountered by a
         * Publisher or Subscription, after which no other Subscriber
         * methods are invoked by the Subscription.  If this method
         * itself throws an exception, resulting behavior is
         * undefined.
         *
         * @param throwable the exception
         */
        public void onError(Throwable throwable);

        /**
         * Method invoked when it is known that no additional
         * Subscriber method invocations will occur for a Subscription
         * that is not already terminated by error, after which no
         * other Subscriber methods are invoked by the Subscription.
         * If this method throws an exception, resulting behavior is
         * undefined.
         */
        public void onComplete();
    }

    /**
     * Message control linking a {@link Publisher} and {@link
     * Subscriber}.  Subscribers receive items only when requested,
     * and may cancel at any time. The methods in this interface are
     * intended to be invoked only by their Subscribers; usages in
     * other contexts have undefined effects.
     */
    public static interface Subscription {
        /**
         * Adds the given number {@code n} of items to the current
         * unfulfilled demand for this subscription.  If {@code n} is
         * negative, the Subscriber will receive an {@code onError}
         * signal with an {@link IllegalArgumentException} argument.
         * Otherwise, the Subscriber will receive up to {@code n}
         * additional {@code onNext} invocations (or fewer if
         * terminated).
         *
         * @param n the increment of demand; a value of {@code
         * Long.MAX_VALUE} may be considered as effectively unbounded
         */
        public void request(long n);

        /**
         * Causes the Subscriber to (eventually) stop receiving
         * messages.  Implementation is best-effort -- additional
         * messages may be received after invoking this method.  A
         * cancelled subscription need not ever receive an {@code
         * onComplete} signal.
         */
        public void cancel();
    }

    /**
     * A component that acts as both a Subscriber and Publisher.
     *
     * @param <T> the subscribed item type
     * @param <R> the published item type
     */
    public static interface Processor<T,R> extends Subscriber<T>, Publisher<R> {
    }

    // Support for static methods

    static final int DEFAULT_BUFFER_SIZE = 256;

    /**
     * Returns a default value for Publisher or Subscriber buffering,
     * that may be used in the absence of other constraints.
     *
     * @implNote
     * The current value returned is 256.
     *
     * @return the buffer size value
     */
    public static int defaultBufferSize() {
        return DEFAULT_BUFFER_SIZE;
    }

    abstract static class CompletableSubscriber<T,U>
        implements Subscriber<T>, Consumer<T>
    {
        final CompletableFuture<U> status;
        Subscription subscription;
        long requestSize;
        long count;
        CompletableSubscriber(long bufferSize, CompletableFuture<U> status) {
            this.status = status;
            this.requestSize = bufferSize;
        }
        public final void onSubscribe(Subscription subscription) {
            long rs = requestSize;
            count = requestSize -= (rs >>> 1);
            (this.subscription = subscription).request(rs);
        }
        public final void onError(Throwable ex) {
            status.completeExceptionally(ex);
        }
        public void onNext(T item) {
            try {
                if (--count <= 0)
                    subscription.request(count = requestSize);
                accept(item);
            } catch (Throwable ex) {
                subscription.cancel();
                status.completeExceptionally(ex);
            }
        }
    }

    static final class ConsumeSubscriber<T> extends CompletableSubscriber<T,Void> {
        final Consumer<? super T> consumer;
        ConsumeSubscriber(long bufferSize,
                          CompletableFuture<Void> status,
                          Consumer<? super T> consumer) {
            super(bufferSize, status);
            this.consumer = consumer;
        }
        public void accept(T item) { consumer.accept(item); }
        public void onComplete() { status.complete(null); }
    }

    /**
     * Creates and subscribes a Subscriber that consumes all items
     * from the given publisher using the given Consumer function, and
     * using the given bufferSize for buffering. Returns a
     * CompletableFuture that is completed normally when the publisher
     * signals {@code onComplete}, or completed exceptionally upon any
     * error, including an exception thrown by the Consumer (in which
     * case the subscription is cancelled if not already terminated).
     * Other attempts to cancel the CompletableFuture need not
     * cause the computation to terminate.
     *
     * @param <T> the published item type
     * @param bufferSize the request size for subscriptions
     * @param publisher the publisher
     * @param consumer the function applied to each onNext item
     * @return a CompletableFuture that is completed normally
     * when the publisher signals onComplete, and exceptionally
     * upon any error
     * @throws NullPointerException if publisher or consumer are null
     * @throws IllegalArgumentException if bufferSize not positive
     */
    public static <T> CompletableFuture<Void> consume(
        long bufferSize, Publisher<T> publisher, Consumer<? super T> consumer) {
        if (bufferSize <= 0L)
            throw new IllegalArgumentException("bufferSize must be positive");
        if (publisher == null || consumer == null)
            throw new NullPointerException();
        CompletableFuture<Void> status = new CompletableFuture<>();
        publisher.subscribe(new ConsumeSubscriber<T>(
                                bufferSize, status, consumer));
        return status;
    }

    /**
     * Equivalent to {@link #consume(long, Publisher, Consumer)}
     * with {@link #defaultBufferSize}.
     *
     * @param <T> the published item type
     * @param publisher the publisher
     * @param consumer the function applied to each onNext item
     * @return a CompletableFuture that is completed normally
     * when the publisher signals onComplete, and exceptionally
     * upon any error
     * @throws NullPointerException if publisher or consumer are null
     */
    public static <T> CompletableFuture<Void> consume(
        Publisher<T> publisher, Consumer<? super T> consumer) {
        return consume(defaultBufferSize(), publisher, consumer);
    }

    /**
     * Temporary implementation for Stream, collecting all items
     * and then applying stream operation.
     */
    static final class StreamSubscriber<T,R> extends CompletableSubscriber<T,R> {
        final Function<? super Stream<T>, ? extends R> fn;
        final ArrayList<T> items;
        StreamSubscriber(long bufferSize,
                         CompletableFuture<R> status,
                         Function<? super Stream<T>, ? extends R> fn) {
            super(bufferSize, status);
            this.fn = fn;
            this.items = new ArrayList<T>();
        }
        public void accept(T item) { items.add(item); }
        public void onComplete() { status.complete(fn.apply(StreamSupport.stream(items))); }
    }

    /**
     * Creates and subscribes a Subscriber that applies the given
     * stream operation to items, and uses the given bufferSize for
     * buffering. Returns a CompletableFuture that is completed
     * normally with the result of this function when the publisher
     * signals {@code onComplete}, or is completed exceptionally upon
     * any error. Other attempts to cancel the CompletableFuture need
     * not cause the computation to terminate.
     *
     * <p><b>Preliminary release note:</b> Currently, this method
     * collects all items before executing the stream
     * computation. Improvements are pending Stream integration.
     *
     * @param <T> the published item type
     * @param <R> the result type of the stream function
     * @param bufferSize the request size for subscriptions
     * @param publisher the publisher
     * @param streamFunction the operation on elements
     * @return a CompletableFuture that is completed normally with the
     * result of the given function as result when the publisher signals
     * onComplete, and exceptionally upon any error
     * @throws NullPointerException if publisher or function are null
     * @throws IllegalArgumentException if bufferSize not positive
     */
    public static <T,R> CompletableFuture<R> stream(
        long bufferSize, Publisher<T> publisher,
        Function<? super Stream<T>, ? extends R> streamFunction) {
        if (bufferSize <= 0L)
            throw new IllegalArgumentException("bufferSize must be positive");
        if (publisher == null || streamFunction == null)
            throw new NullPointerException();
        CompletableFuture<R> status = new CompletableFuture<>();
        publisher.subscribe(new StreamSubscriber<T,R>(
                                bufferSize, status, streamFunction));
        return status;
    }

    /**
     * Equivalent to {@link #stream(long, Publisher, Function)}
     * with {@link #defaultBufferSize}.
     *
     * @param <T> the published item type
     * @param <R> the result type of the stream function
     * @param publisher the publisher
     * @param streamFunction the operation on elements
     * @return a CompletableFuture that is completed normally with the
     * result of the given function as result when the publisher signals
     * onComplete, and exceptionally upon any error
     * @throws NullPointerException if publisher or function are null
     */
    public static <T,R> CompletableFuture<R> stream(
        Publisher<T> publisher,
        Function<? super Stream<T>,? extends R> streamFunction) {
        return stream(defaultBufferSize(), publisher, streamFunction);
    }
}
