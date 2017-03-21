[![JitPack](https://img.shields.io/github/tag/streamsupport/streamsupport.svg?label=JitPack)](https://jitpack.io/#streamsupport/streamsupport)
[![Maven Central](https://img.shields.io/maven-central/v/net.sourceforge.streamsupport/streamsupport.svg)](http://mvnrepository.com/artifact/net.sourceforge.streamsupport/streamsupport)
[![Build Status](https://travis-ci.org/streamsupport/streamsupport.svg)](https://travis-ci.org/streamsupport/streamsupport)
[![Coverage Status](https://coveralls.io/repos/github/streamsupport/streamsupport/badge.svg)](https://coveralls.io/github/streamsupport/streamsupport)
[![javadoc.io](https://javadocio-badges.herokuapp.com/net.sourceforge.streamsupport/streamsupport/badge.svg)](http://www.javadoc.io/doc/net.sourceforge.streamsupport/streamsupport/)
[![javadoc](https://img.shields.io/github/tag/streamsupport/streamsupport.svg?label=javadoc)](https://jitpack.io/com/github/streamsupport/streamsupport/-SNAPSHOT/javadoc/)
<!--[![Download](https://api.bintray.com/packages/streamsupport/maven/streamsupport/images/download.svg) ](https://bintray.com/streamsupport/maven/streamsupport/_latestVersion)-->
<!--[![Join the chat at https://gitter.im/streamsupport/streamsupport](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/streamsupport/streamsupport?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)-->

![](art/streamsupport.png)

Import from http://hg.code.sf.net/p/streamsupport/code

streamsupport is a backport of the Java 8 java.util.function (functional interfaces) and java.util.stream (streams) API for users of Java 6 or 7 supplemented with selected additions from java.util.concurrent which didn't exist back in Java 6.

Due to the lack of default interface methods and static interface methods in pre-Java 8 the API had to be slightly adjusted in these areas but still covers the full functionality scope of Java 8. In detail, static and default interface methods have been moved to companion classes in the same package that bear the identical name as the interface but with an "s" appended (e.g. Comparator -> Comparators).

For ease of use, the default methods for most of the functional interfaces were NOT retained as abstract methods in the redefined interfaces (keeping them single method interfaces) - the missing default (and static) methods can always be found in the corresponding companion class.

Want also lambdas? https://github.com/orfjackal/retrolambda

[![](art/streamsupport-sf.png)](http://streamsupport.sourceforge.net/)

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

## Usage

After:

```java
RefStreams.of("one", "two", "three", "four")
    .filter(e -> e.length() > 3)
    .peek(e -> System.out.println("Filtered value: " + e))
    .map(String::toUpperCase)
    .peek(e -> System.out.println("Mapped value: " + e))
    .collect(Collectors.toList());
```

After:

```java
public static List<String> getNames(List<User> users) {
    return StreamSupport.stream(users).map(user -> user.name()).collect(Collectors.toList());
}
```

After:

```java
public static String[] getNames(User[] users) {
    return J8Arrays.stream(users).map(user -> user.name()).toArray(length -> new String[length]);
}
```

## Installation

build.gradle:

```gradle
dependencies {
    compile 'net.sourceforge.streamsupport:streamsupport:1.5.4'
    compile 'net.sourceforge.streamsupport:streamsupport-cfuture:1.5.4'
    compile 'net.sourceforge.streamsupport:streamsupport-atomic:1.5.4'
    compile 'net.sourceforge.streamsupport:streamsupport-flow:1.5.4'
    compile 'net.sourceforge.streamsupport:streamsupport-literal:1.5.4'
}
```

or via jitpack (comming soon):

```gradle
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    compile 'com.github.streamsupport.streamsupport:streamsupport:-SNAPSHOT'
    compile 'com.github.streamsupport.streamsupport:streamsupport-cfuture:-SNAPSHOT'
    compile 'com.github.streamsupport.streamsupport:streamsupport-atomic:-SNAPSHOT'
    compile 'com.github.streamsupport.streamsupport:streamsupport-flow:-SNAPSHOT'
    compile 'com.github.streamsupport.streamsupport:streamsupport-literal:-SNAPSHOT'
}
```

## Build

### maven

```sh
./mvnw clean install
```

### gradle

```sh
./gradlew clean assemble
```

## Release Notes

[Release Notes](Readme.txt)

## LICENSE

GPL2, CE (GNU General Public License, version 2, with the Classpath Exception)
