Import from http://hg.code.sf.net/p/streamsupport/code

streamsupport is a backport of the Java 8 java.util.function (functional interfaces) and java.util.stream (streams) API for users of Java 6 or 7 supplemented with selected additions from java.util.concurrent which didn't exist back in Java 6.

Due to the lack of default interface methods and static interface methods in pre-Java 8 the API had to be slightly adjusted in these areas but still covers the full functionality scope of Java 8. In detail, static and default interface methods have been moved to companion classes in the same package that bear the identical name as the interface but with an "s" appended (e.g. Comparator -> Comparators).

For ease of use, the default methods for most of the functional interfaces were NOT retained as abstract methods in the redefined interfaces (keeping them single method interfaces) - the missing default (and static) methods can always be found in the corresponding companion class.

Want also lambdas? https://github.com/orfjackal/retrolambda

[streamsupport Web Site](http://streamsupport.sourceforge.net/)

## Categories

[Libraries](http://sourceforge.net/directory/development/softdevlibraries/)

## Features

* Java 8 Streams library backport
* Java 8 CompletableFuture backport
* Java 8 Parallel array operations backport
* Java 8 Functional interfaces backport
* Further java.util.concurrent enhancements from Java 7/8 backported to Java 6
* Includes miscellaneous Java 8 goodies (Optional, StringJoiner, ...)
* Supports Android

## Installation

build.gradle:

```gradle
dependencies {
    compile 'net.sourceforge.streamsupport:streamsupport:1.4'
}
```

or via jitpack:

```gradle
repositories {
    // ...
    maven { url "https://jitpack.io" }
}

dependencies {
    compile 'com.github.yongjhih:streamsupport:-SNAPSHOT'
}
```

## RELEASE NOTES

 - "1.4-stable" is the current stable release


## GENERAL

 - To create a Stream from a java.util Collection use the static j8.u.s.StreamSupport
   methods j8.u.s.StreamSupport#stream(Collection), or (for a parallel Stream)
   j8.u.s.StreamSupport#parallelStream(Collection)

 - The static methods from interface j.u.s.Stream are located in j8.u.s.RefStreams
   which also contains the new Java 9 j.u.s.Stream default methods. The equivalent
   methods in j8.u.s.StreamSupport are deprecated since 1.3.2 and will be removed
   in a future release.

 - As of release 1.4, the former single streamsupport.jar has been partitioned into
   a core streamsupport.jar and 3 additional optional components:

   * streamsupport-cfuture (CompletableFuture API)
   * streamsupport-atomic  (j8.u.c.atomic package)
   * streamsupport-flow    (Flow API)

   All of them have a dependency on the core streamsupport.jar

 - The provided Jar files are compiled for Java 6 (Bytecode version 50). You'll need
   Retrolambda (https://github.com/orfjackal/retrolambda) to build the core
   streamsupport.jar from its sources

 - It is possible to turn off the automatic use of native specializations
   with the boolean system property

   java8.util.Spliterators.assume.oracle.collections.impl=false

   The property must have been set not later than the java8.util.Spliterators class
   is loaded. This switch is provided for users on non-OpenJDK based JREs (e.g. IBM
   Java 6/7) to increase the odds that streamsupport can be used on their platform.
   This switch has no effect on Android.

 - Release 1.2 improves upon the ForkJoin threadpool implementation and is
   the first release that begins to expand the API into the Java 9 realm.

 - Release 1.3 adds the new Java 9 Stream methods takeWhile() and dropWhile().
   If anyone longs for the default method implementations, they are available
   in j8.u.s.RefStreams (but that shouldn't be necessary as all Streams created
   via StreamSupport#stream()/#parallelStream() already implement these methods).

 - As of 1.3.1, on Android, it is no longer necessary to set the system property
   "java8.util.Spliterators.assume.oracle.collections.impl" to "false".

 - As of 1.3.2, the static methods from interface j.u.s.Stream that are located
   in j8.u.s.StreamSupport are deprecated. Please use the equivalent methods in
   j8.u.s.RefStreams.

 - Release 1.4 completes the Java 9 "More Concurrency Updates" JEP 266.
   See http://openjdk.java.net/jeps/266
   This covers, among other things, the introduction of the Java 9 Flow API
   (the preliminary implementation of reactive streams) and Java 9 enhancements
   to j.u.c.CompletableFuture and fork/join as well as improvements to their
   implementation.



## VERSION HISTORY

1.4-stable (2015-11-15)
 - JDK-8134852: Java 9 fork/join with API enhancements (JEP 266)
 - JDK-8134851: CompletableFuture with API enhancements (JEP 266)
 - JDK-8134850: integrate the Java 9 Flow API (JEP 266)
 - moved CompletableFuture to streamsupport-cfuture jar (Ticket#120)
 - moved j8.u.c.atomic package to streamsupport-atomic jar (Ticket#121)
 - moved the new Java 9 Flow API to streamsupport-flow jar (Ticket#119)
 - Optional.or() can use a covariant Supplier as parameter (Ticket#125)
 - JDK-8135248: add Objects utility methods to check indexes / ranges
 - JDK-8142493: check indexes/ranges behavior when oobe produces null
 - JDK-8138963: new Objects methods to default to non-null
 - JDK-8141652: rename methods nonNullElse* to requireNonNullElse*
 - this release also got tested on Android 6.0 (API 23)

1.3.2-stable (2015-10-04)
 - JDK-8080418: add Optional.or()
 - JDK-8136686: Collectors.counting reduce boxing
 - JDK-8134853: update j.u.concurrent and related (JEP 266)
 - JDK-8134854: update j.u.concurrent.atomic classes (JEP 266)
 - LinkedListSpliterator bugfix (Ticket#103)
 - The static methods from interface j.u.s.Stream that are located
   in j8.u.s.StreamSupport are now deprecated. Please use the
   equivalent methods in j8.u.s.RefStreams.

1.3.1-stable (2015-09-03)
 - this is primarily a maintenance and code cleansing release
 - on Android it is no longer required to set the
   "java8.util.Spliterators.assume.oracle.collections.impl"
   system property (Ticket#93)
 - reduced ConcurrentHashMap resizing for parallel Stream#distinct()
   operations and account for the known level of parallelism (#82)
 - backwards-compatible generalization of the signatures of the static
   #generate() and #iterate() methods in j8.u.s.RefStreams and
   j8.u.s.StreamSupport (JDK-8132097, Ticket#79)
 - cleaned up the source code (#84), got rid of the useless J8Builder
   classes in Double/Long/IntStreams (#87), removed the stupid static
   #add(Stream.Builder<T> builder, T t) method from j8.u.s.RefStreams
   and j8.u.s.StreamSupport (#88)
 - added the missing Builder#add() method to the nested Builder
   interfaces in j8.u.s.Double/Int/LongStream (#86), added the
   missing (former default) methods in the nested OfDouble/OfInt/OfLong
   interfaces in j8.u.PrimitiveIterator (#90)
 - a couple of Javadoc tweaks / clarifications from the OpenJDK
   bugtracking system

1.3-stable (2015-07-14)
 - JDK-8071597 : "Add Stream dropWhile and takeWhile operations"
   is the pre-eminent new (Java 9) feature
   https://bugs.openjdk.java.net/browse/JDK-8071597
   If anyone longs for the default method implementations, they
   are available in j8.u.s.RefStreams. But that should rarely be
   useful as all Streams created via StreamSupport#stream() or
   StreamSupport#parallelStream() already implement these methods.
 - added the missing Spliterator default method implementations
   to all AbstractSpliterator base classes in j8.u.Spliterators
   https://sourceforge.net/p/streamsupport/discussion/general/thread/b65852d5/

1.2.2-stable (2015-06-26)
 - JDK-8129120 is the outstanding new feature: the properties of
   terminal operations don't get back-propagated upstream anymore
   See https://bugs.openjdk.java.net/browse/JDK-8129120
 - backported other recent improvements from the Java 9 repo
   * JDK-8080623 (CPU overhead in FJ due to spinning in awaitWork)
   * JDK-8080945 (Improve performance of primitive Arrays.sort)
 - added #replaceAll() and #spliterator() to j8.util.Lists
   (Ticket#61)
 - added #removeIf() to j8.lang.Iterables and optimized
   its #spliterator() implementation (Ticket#62)
 - added #asIterator(Enumeration) to j8.util.Iterators
   (JDK-8072726, Ticket#44)
 - added new class j8.util.stream.RefStreams as a new home
   for the static (and upcoming default) methods of j.u.s.Stream

1.2.1-stable (2015-04-23)
 - this is a hotfix release for a serious ForkJoinPool bug
   introduced in OpenJDK 8u40 / 1.2-stable (JDK-8078490)
   * JDK-8078490 (Missed submissions in ForkJoinPool)
   See Ticket#58 and
   https://bugs.openjdk.java.net/browse/JDK-8078490
   http://cs.oswego.edu/pipermail/concurrency-interest/2015-April/014240.html
 - the only other appreciable change is a bugfix in
   CompletableFuture (Ticket#52)
   * JDK-8068432 (Inconsistent exception handling in thenCompose)

1.2-stable (2015-04-09)
 - a new ForkJoinPool implementation (Ticket#27), introduced
   in OpenJDK 8u40. See JDK-8056248 (improve ForkJoin thread
   throttling). This is a major change.
 - a first dive into Java 9 (in the sense of new API methods)
   * JDK-8071600 (Collectors: Add a flat-mapping collector)
   * JDK-8071670 (Optional  : Add method ifPresentOrElse())
   * JDK-8050820 (Optional  : Add method stream())
   * JDK-8050819 (Stream    : Add method ofNullable(T))
 - backported two recent improvements from the Java 9 repo
   * JDK-8067969 (Optimize Stream.count for SIZED Streams)
   * JDK-8075307 (Parallel stateful pipeline flags inconsistent)

1.1.5-stable (2015-03-12)
 - added new "native" specializations for
   * j.u.HashSet (Ticket#32) [NOT available on Android]
   * the Collections returned from the j.u.HashMap
     #entrySet(), #keySet() and #values() methods
     (Ticket#30) [also NOT available on Android]
 - on Android it therefore becomes now mandatory to set the
   "java8.util.Spliterators.assume.oracle.collections.impl"
   system property to false
 - the library now detects when it is running on a stream enabled
   JRE (>= Java 8) and automatically takes advantage of its
   Spliterators for all Collections (Ticket#11)
   This feature can be disabled by setting the system property
   "java8.util.Spliterators.jre.delegation.enabled" to false
   (which has no effect on a Java 6/7 JRE or Android)
 - backported most of the OpenJDK 8u40 changes (and some more
   that will supposedly appear in 8u60).  Notably:
   * JDK-8070099 (ForEachOps.ForEachOrderedTask improvement)
   * JDK-8040892 (Collectors.toMap incorrect message bugfix)
   * JDK-8072909 (TimSort AIOOBE exception bugfix)
   * JDK-8066397 (remove ThreadLocal/SplittableRandom network code)
   * JDK-8056249 (CompletableFuture resource usage improvement)
   In addition, lots of Javadoc tweaks / clarifications from the
   OpenJDK Jira (cf. tickets for more details)

1.1.4-stable (2015-02-09)
 - added a new "native" specialization for
   * j.u.LinkedList (Ticket#10)
 - added the mising static methods from j.u.Map.Entry
   to j8.u.Maps.Entry (Ticket#16)
 - consolidated the OpenJDK test suite and the Java 6 tests:
   The whole test suite can be run on Java 6 now (Ticket#17)
   It even works on Android with some caveats (Ticket#15/#17/#18)
   ART is a good environment for the suite (even on 4.4), Dalvik
   not so much
 - first release with dedicated support for Android (Ticket#15)
   All of the currently available "native" Spliterator implementations
   are also supported on Android
   The "java8.util.Spliterators.assume.oracle.collections.impl"
   property still has to be set to "false", but the library will
   detect that it is running on Android and enables the Android
   implementation automatically
   Parallel streams on Android are also supported since 1.1.4
   Tests on a Galaxy Nexus API 15 AVD seem to indicate that the
   library can even be used on Ice Cream Sandwich devices.
   Running the test suite on real Dalvik VM devices is hard because
   of heap memory constraints, but API 19 & 21 are quite well tested.

1.1.3-stable (2015-01-07)
 - added additional native specializations for
   * j.u.c.LinkedBlockingQueue (Ticket#9)
   * j.u.c.LinkedBlockingDeque (Ticket#9)
 - Ticket#12 & Ticket#13:
   It is now possible to turn off the automatic use of native
   specializations with the boolean system property
   java8.util.Spliterators.assume.oracle.collections.impl=false
   The property must have been set not later than the
   java8.util.Spliterators class is loaded
   This switch is provided for users on non-OpenJDK based
   JREs or for Android developers to increase the odds that
   streamsupport may be running on their platform. For all
   others nothing has changed.

1.1.2-stable (2014-09-22)
 - added j8.u.StreamSupport#parallelStream(Collection c) (Ticket#8)
 - added j8.u.c.ThreadLocalRandom (Ticket#7)
 - reenact fix for OpenJDK Bug ID: JDK-8037857
 - reenact fix for OpenJDK Bug ID: JDK-8042355

1.1.1-stable (2014-08-03)
 - fixed Ticket#6 (Bug in j8.u.Comparators)

1.1-stable (2014-08-01)
 - added the OpenJDK Map-defaults test to Java 8 tests
 - added minor test for COW collections to Java 6 tests
 - Javadoc: emphasized early-binding behavior of COW collections
 - no further changes since 1.1-rc3

1.1-rc3 (2014-07-27)
 - added additional specializations for
   * j.u.c.CopyOnWriteArrayList
   * j.u.c.CopyOnWriteArraySet
 - updated Javadoc for new/changed methods

1.1-rc2 (2014-07-20)
 - added additional specializations for
   * j.u.ArrayDeque
   * j.u.Vector
   * j.u.PriorityQueue
   * j.u.c.PriorityBlockingQueue
 - finished Ticket#4 (ArrayListSpliterator non-interference)
 - conducted the changes from Ticket#5
   * do the dispatch in a new method Spliterators#spliterator(Collection c)
   * use this method in j8.u.s.StreamSupport#stream(Collection c)
   * use this method in j8.u.s.Nodes.CollectionNode#spliterator()
   * make Spliterators.IteratorSpliterator<T> package-private again

1.1-rc1 (2014-07-13)
 - changes in j8.u.s.StreamSupport#stream(Collection<? extends T> c)
   * specialized for LinkedHashSet and ArrayBlockingQueue
   * natively specialized for ArrayList and Arrays$ArrayList
   * uses Java 8 interface defaults for List, Set and SortedSet
 - made Spliterators.IteratorSpliterator<T> public
 - minor Javadoc fixes

1.0-stable (2014-06-22)
 - first "stable release" (1.0)
 - added missing default methods from j.u.(c.Concurrent)Map to j8.u.Maps

rc-3.0 (2014-06-16)
 - fixed Ticket#3 (Stream.distinct() throws NoClassDefFoundError on Google App Engine)
 - added missing setAll() / parallelSetAll() methods to j8.u.J8Arrays
 - added missing parallelPrefix() methods to j8.u.J8Arrays
 - added parallelPrefix(), parallelSort() and (parallel)setAll() tests to Java 8 tests
 - added parallelSort() test to Java 6 tests

rc-2.0 (2014-06-09)
 - added missing j.u.Comparator methods to j8.u.Comparators
 - fixed Ticket#2 (Javadoc errors)
 - added Comparator tests to Java 8 tests

rc-1.5 (2014-06-01)
 - added j.u.c.RecursiveTask / j.u.c.RecursiveAction
 - visibility of Spliterators.OfPrimitive#forEachRemaining is now public
 - fixed most of the worst Javadoc errors (Ticket#2)

rc-1.0 (2014-05-29)
 - OpenJDK stream tests port completed (passes > 10000 tests now)
 - fixed SecurityException in j8.u.c.CompletableFuture
 - added j.u.c.Phaser
 - minor Striped64 performance improvement
 - added a couple of Java 6 tests

beta-1.5 (2014-05-24)
 - added missing OpenJDK tests from beta-1

beta-1 (2014-05-22)
 - Passing approximately 5700 OpenJDK stream tests
 - fixed Ticket#1: "A certain amount of boxing"
 - fixed ClassCastException in j8.u.s.AbstractPipeline#wrapSink
 - added missing method PrimitiveIterator#forEachRemaining
 - Spliterators.OfPrimitive's visibility is now public
 - more Javadoc

alpha-2 (2014-05-17)
 - added j.u.SplittableRandom
 - added j.u.c.atomic.Long/DoubleAdder
 - added j.u.c.atomic.Long/DoubleAccumulator
 - fixed stack overflow in j8.u.Objects#deepEquals
 - license + example code

alpha-1 (2014-05-10)
 - initial drop
