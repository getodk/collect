# fragmentstest

Includes helpers for writing unit tests for Fragments. This includes (and extends) `androidx.fragment:fragment-testing`. `fragmentstest` needs to be included as a `debugImplementation` dependency (because of the underlying Android testing package):

```gradle
debugImplementation project(':fragmentstest')
```
