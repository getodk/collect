# Test guidelines

Automated tests are very important for building software. They let us add, modify and understand the code in our app with confidence and can prevent us from making mistakes that could end up affecting people collecting data in the field. They can also force us to deal with our code's own design which reveals problems we might not have though about if no tests had been written.

Almost any Pull Request for Collect will require a set of tests along with its code changes. Otherwise there will be nothing to ensure that that code continues to work or exist in the future. We recommend when working on Collect code you follow a [TDD](https://en.wikipedia.org/wiki/Test-driven_development) style of development and write a test before each change/addition to the code. Writing tests can often be harder than writing implementations and this is especially true in a code base like Collect that wasn't written with testing in mind from the start. If you feel stuck it's always good to discuss testing strategies as part of a PR or on our [Slack](https://slack.getodk.org/).

## Different kinds of tests

Although testing different components/features will often require different kinds and styles of testing the tests in Collect can broadly be divided into five kinds:

### JUnit tests

Plain old standard JUnit tests. All the tests in Collect use JUnit in someway but these tests don't use Android specific frameworks (like Espresso/Robolectric) These live in `collect_app/src/test/java`. Often they'll be focused on testing one object and will mock/fake/stub its dependencies (referred to as "unit testing") but some will test internal "APIs" (such as our Open Rosa server abstraction). The key to writing these kinds of tests is that they should not involve or depend on the Android framework. JUnit tests are run on the JVM.

### Robolectric tests

Robolectric is a framework that sits on top of JUnit. It allows you to write tests for Android objects or anything that integrates with Android and run the tests on the JVM. Robolectric makes this possible by providing a fully working fake of the Android SDK. These tests will often check Android specific things like the cleanup on lifecycle for Activity/Fragment or detailed view rendering for example. Robolectric tests also live in `collect_app/src/test/java` but are annotated by `RunWith(RobolectricTestRunner.class)` or `RunWith(AndroidJUnit4.class)` (which is an alias for `RobolecricTestRunner` when running on the JVM). Documentation for Robolectric isn't particularly thorough but help can be found on Stack Overflow or on our Slack (#collect-code channel).

### Feature tests

These tests live in `collect_app/src/androidTest/java/feature` and use the Android Espresso testing framework (which is similar to UI web testing frameworks like FluentLenium or Capybara). Espresso lets you write tests that carry out actions a user would like clicking on things, scrolling etc and then make assertions on what is on screen. The intention of these tests it "drive out" whole features in Collect. To make these tests easier to write and read we use a page objects (in the `org.odk.collect.android.support.pages` package) to abstract actions and assertions on different pages.

### Regression tests

These look very similar to Collect's Feature tests but instead of being written by developers as part of development they are written by Collect's QA team with an aim of test cases they repeat frequently. Generally these shouldn't be written as part of a PR.

### Instrumented tests

These tests live in `collect_app/src/androidTest/java/instrumented`. They also run on a device (like the other tests in `androidTest`) but don't actually interact with UI - they're much more like unit or integration tests of objects. As much as possible we'd like to avoid these kinds of tests as they can usually be replaced by Robolectric or even plain JUnit tests which have much faster build and run times.

## Testing practices and approaches

* https://en.wikipedia.org/wiki/Test-driven_development
* https://martinfowler.com/articles/mocksArentStubs.html
* https://www.martinfowler.com/bliki/PageObject.html
* https://www.destroyallsoftware.com/talks/boundaries