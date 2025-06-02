# State of the union

The purpose of this document is to give anyone who reads it a quick overview  of both the current state and the direction of the code. The community should try and update this document as the code evolves.

## How we got here

* App originally built in Java for Android 1.0/T-Mobile G1
* Written at Google by graduate student interns and then University of Washington
* Designed as a survey application backed by [JavaRosa](https://github.com/getodk/javarosa/) (which deals with XForm forms) communicating with [OpenRosa](https://docs.getodk.org/openrosa/) servers
* Many different contributors/styles/eras over 15+ year lifetime
* App wasn't built with a TDD workflow or with automated testing
* Lots of work in the last few years to add more tests and clean up code using coverage measurement and static checks

## Where we are now

* App written in a mixture of Java and Kotlin
* App has mixture of unit tests, ["Local tests" and "Instrumented tests"](https://developer.android.com/training/testing/fundamentals#instrumented-vs-local) but coverage is far from complete
* Local tests are a mixture of classic Robolectric and AndroidX Test/Espresso (backed by Robolectric)
* UI is "iconic" (old) but with a lot of inconsistencies and quirks and is best adapted to small screens (although often used on tablets)
* A lot of code lives in between one "god" Activity (`FormFillingActivity`) and a process singleton (`FormController`)
* Core form entry flow uses custom side-to-side swipe view (in `FormFillingActivity` made up of `ODKView`)
* Questions are rendered using a view "framework" of implementations inheriting from `QuestionWidget` (which is documented in [WIDGETS.MD](WIDGETS.md)) which are also used to store UI state during form entry
* Almost all app data is stored in Android's user-accessible "external" rather than "internal" storage
* App mostly stores state in multiple SQLite databases - this means there's no DB level integrity guarantees on some relationships (like between forms and instances for example)
* Access to state in SQLite happens through repository objects which deal in data/domain objects (`FormsRepository` and `Form` for example)
* Settings UIs for the app use Android's Preferences abstraction
* App uses [Material 3 Theming](https://m3.material.io/foundations/customization) so [Material components](https://material.io/components?platform=android) are preferred over custom or platform ones.
* Dagger2 is used to inject "black box" objects such as Activity and just uses a very basic setup
* Http is handled using OkHttp3 and https client abstractions are generally wrapped in Android's AsyncTask
* Geo activities use three engines (Mapbox, osmdroid, Google Maps) depending on the selected basemap
* Code goes through static analysis using CheckStyle, PMD, ktlint and Android Lint
* Forms get into the app from two different sources (Open Rosa servers and disk) but the logic for this is disparate and they don't sit behind a common interface
* Instances are linked to the forms they are instances of through formid and version. However, the same formid and version combination could represent multiple forms in storage
* `SharedPreferences` is wrapped in app's own `Settings` abstraction
* The form hierarchy is rendered using `FormHierarchyFragment`, which hasn't been seriously touched (at a code or design) level for a few years

## Where we're going

* General effort to increase test coverage and quality while working on anything and enforcing tests for new code in PR review
* Moving responsibilities out of `FormFillingActivity` into other components (like Fragments, ViewModels, use cases etc)
* Writing all new code in Kotlin
* Writing new code using a [multi-module approach](CODE-GUIDELINES.md#gradle-sub-modules) (feature modules, mini frameworks etc) and breaking old code out into modules when opportunities come up
* Trying to remove technical debt flagged with `@Deprecated`
* Replacing async work such as `AsyncTask` with `Flow` (converted to `LiveData` in UI code) + `Scheduler` abstraction
* Gradually removing use of `CursorLoader` (all remaining uses are in `CursorLoaderFactory`)
* Using AndroidX Test in new local tests and migrating other local tests as we touch them (from classic Robolectric)
* Moving towards a ["data services"](data_services_architecture.pdf) oriented architecture that has emerged over time that uses AndroidX Architecture Components for the core of the UI (Fragment, View, ViewModel etc.)
* Improving the `MapFragment` abstraction so more logic can be shared between the map engines
