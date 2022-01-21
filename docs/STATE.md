# State of the union

The purpose of this document is to give anyone who reads it a quick overview
of both the current state and the direction of the code. The community should try
and update this document as the code evolves.

## How we got here

* App originally built in Java for Android 1.0/T-Mobile G1
* Written at Google by graduate student interns and then University of Washington
* Designed as a survey application backed by [JavaRosa](https://github.com/getodk/javarosa/) (which deals with XForm forms) communicating with [OpenRosa](https://docs.getodk.org/openrosa/) servers
* Many different contributors/styles/eras over 10+ year lifetime
* App wasn't built with a TDD workflow or with automated testing
* Lots of work in the last few years to add more tests and clean up code using coverage measurement and static checks

## Where we are now

* App written in a mixture of Java and Kotlin
* App has mixture of unit tests, ["Local tests" and "Instrumented tests"](https://developer.android.com/training/testing/fundamentals#instrumented-vs-local) but coverage is far from complete
* Local tests are a mixture of classic Robolectric and AndroidX Test/Espresso (backed by Robolectric)
* UI is "iconic" (old) but with a lot of inconsistencies and quirks and is best adapted to small screens (although often used on tablets)
* A lot of code lives in between one "god" Activity (`FormEntryActivity`) and a process singleton (`FormController`)
* Core form entry flow uses custom side-to-side swipe view (in `FormEntryActivity` made up of `ODKView`)
* Questions are rendered using a view "framework" of implementations inheriting from `QuestionWidget` (which is documented at in [WIDGETS.MD](WIDGETS.md))
* Async/reactivity handled with a mixture of callbacks, LiveData and Rx
* App mostly stores data in flat files indexed in SQLite
* Access to data in SQLite happens through repository objects which deal in data/domain objects (`FormsRepository` and `Form` for example)
* Settings UIs for the app use Android's Preferences abstraction
* App uses [Material Theming](https://material.io/develop/android/theming/theming-overview) so [Material components](https://material.io/components?platform=android) are preferred over custom or platform ones.
* Dagger2 is used to inject "black box" objects such as Activity and just uses a very basic setup
* Http is handled using OkHttp3 and https client abstractions are generally wrapped in Android's AsyncTask (and some Rx)
* Geo activities use three engines (Mapbox, osmdroid, Google Maps) depending on the selected basemap even though Mapbox could do everything osmdroid does
* Code goes through static analysis using CheckStyle, PMD, ktlint and Android Lint
* Forms get into the app from three different sources (Open Rosa servers, Google Drive and disk) but the logic for this is disparate and they don't sit behind a common interface
* Instances are linked to the forms they are instances of through formid and version. However, the same formid and version combination could represent multiple forms in storage
* `SharedPreferences` is wrapped in app's own `Settings` abstraction

## Where we're going

* General effort to increase test coverage and quality while working on anything and pushing more for tests in PR review
* Slowly moving responsibilities out of `FormEntryActivity`
* Writing pretty much all new code in Kotlin
* Writing new code using a [multi-module approach](CODE-GUIDELINES.md#gradle-sub-modules) (feature modules, mini frameworks etc) and breaking old code out into modules when opportunities come up
* Trying to remove technical debt flagged with `@Deprecated`
* Replacing Rx (and other async work such as `AsyncTask`) with `LiveData` + `Scheduler` abstraction
* Gradually removing use of `CursorLoader` (all remaining uses are in `CursorLoaderFactory`)
* Using AndroidX Test in new local tests and migrating other local tests as we touch them (from classic Robolectric)
