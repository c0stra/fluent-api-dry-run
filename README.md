# Fluent API dry run
[![Released version](https://img.shields.io/maven-central/v/foundation.fluent.api/fluent-api-dry-run.svg)](https://search.maven.org/#search%7Cga%7C1%7Cfluent-api-dry-run)
[![Build Status](https://travis-ci.org/c0stra/fluent-api-dry-run.svg?branch=master)](https://travis-ci.org/c0stra/fluent-api-dry-run)

Reflection proxy based dry run of complex fluent sentences (statements).

It's not really about mocking in terms of testing, but rather allowing to
bypass the real functionality normally provided by existing implementations.

Compared to normal situations, where providing some simple alternative implementation
is about one/two classes, with fluent API this task gets much more complex.

The problem is, that any method, at the end returning `null` introduces a risk
of `NullPointerException` further in the fluent method chain. So with fluent API, we need to avoid
returning `null` even in cases, when we want to "bypass" the implementation.

Imagine following code:
```java
user.opens("http://test-application/")
        .enters().login("user123").password("s3cr3t")
        .andSubmits();
```

When e.g. we just provide simple bypassing implementation (or mock like mockito) of the `user`,
where method `opens()` returns null, we get `NullPointerException` when method `enters()` should be invoked.

Therefore a solution provided in this module, is reflection proxy based, automatically propagating
mechanism, which is avoiding this risk.

### 1. Maven configuration
In order to have this feature available, you need to add following maven dependency:
```xml
<dependency>
    <groupId>foundation.fluent.api</groupId>
    <artifactId>fluent-api-dry-run</artifactId>
    <version>1.0</version>
</dependency>
```

### 2. Usage
You get the dry run "implementation" of some fluent API root by following initialization:
```java
User user = FluentApiHandler.dryRun("My dry run", User.class);
```
In turn you can use the fluent api of `User` and it won't throw any `NullPointerException`:
```java
user.opens("http://test-application/")
        .enters().login("user123").password("s3cr3t")
        .andSubmits();
```

### 3. Default instances
This tool can only propagate itself on interfaces. But it provides simple workaround for non-interface return types.
You can provide set of default values. If a any default value can be assigned to current return type, it will be returned.
Only if none matches, then for proxy the propagation will take place, and for non-proxy an exception is thrown, so the
point of failure is clear, avoiding `NPE` somewhere in complex chain, where it's impossible to find
out for which method it got thrown.

So in fact even interface return types can be instructed to return default value instead of performing
the auto-propagation.

There is implicit set of default values, which is always present for primitive types.
Additional ones can be provided by additional vararg parameters of the `dryRun` method.

### 4. Method invocation callback
The implementation allows to subscribe for individual method calls using a callback.
You can provide additional parameter `FluentApiCallEventHandler` function interface, and this one will receive
all method calls in the chain.

### 5. Reflection and generics
Normally generics are referred as compile time only feature in Java. It's not completely
true. The reflection API contains options to get also generic information about types in some
situations.

E.g.
```java
private final List<T> list;
```
In the code above, the generic information is not available. The `T` is just a parameter.

But in this case:
```java
private final List<String> list;
```
even the Java reflection allows us to get the information. In that case
the `Field.getGenericType()` method returns `ParametrizedType` representation, which contains
actual type parameters.

Having said that, we can overcome the standard limitations of runtime type erasure, and still
get / resolve actual type parameters in generic types, under some circumstances.

The current limitation simply is, that the interface representing our entry point in the fluent API
(the class, provided to the factory, e.g. `User.class`) needs not to take any generic parameters.
But any inherited interfaces can.

Second limitation is of course on generic methods. Those cannot be resolved.