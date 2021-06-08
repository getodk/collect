# State of the union

The purpose of this document is to give anyone who reads it a quick overview
of both the current state and the direction of the code. The community should try
and update this document as the code evolves.

## How we got here

* App originally built in Java for Android 1.0/T-Mobile G1
* Written at Google by graduate student interns and then University of Washington
* Designed as a survey application backed by [JavaRosa](https://github.com/getodk/javarosa/) (which deals with XForm forms) communicating with [OpenRosa](https://docs.getodk.org/openrosa/) servers
* Many different contributors/styles/eras over 10 year lifetime
* App wasn't built with a TDD workflow or with automated testing
* Lots of work in the last two years to add more tests and clean up code using coverage measurement and static checks

## Where we are now

* App has mixture of unit tests (JUnit), Robolectric tests (Junit + Robolectric) and Espresso tests but coverage is far from complete
* UI is "iconic" (old) but with a lot of inconsistencies and quirks and is best adapted to small screens
* A lot of code lives in between one "god" Activity (FormEntryActivity) and a process singleton (FormController)
* Core form entry flow uses custom side-to-side swipe view (in FormEntryActivity made up of ODKView)
* Questions are rendered using a view "framework" of implementations inheriting from `QuestionWidget`
* Async/reactivity handled with a mixture of callbacks, LiveData and Rx
* App stores data in flat files indexed in SQLite
* Access to data in SQLite is done inconsistently through a mix of provider, helper and DAO objects
* Raw access to database rows is favored over the use of domain objects
* Settings for the app use Android's Preferences abstraction
* Material Components styles are used in some places but app still uses AppCompat theme
* Dagger is used to inject "black box" objects such as Activity and in some other places but isn't set up in a particularly advanced way
* Http is handled using OkHttp3 and https client abstractions are generally wrapped in Android's AsyncTask (and some Rx)
* Geo activities use three engines (Mapbox, osmdroid, Google Maps) depending on the selected basemap even though Mapbox could do everything osmdroid does
* Code goes through static analysis using CheckStyle, PMD, SpotBugs and Android Lint
* Code is mostly organized into packages based around what kind of object they are which has become unwieldy
* Forms get into the app from three different sources (Open Rosa servers, Google Drive and disk) but the logic for this is disparate and they don't sit behind a common interface
* Instances are linked to the forms they are instances of through formid and version. However, the same formid and version combination could represent multiple forms in storage

## Where we're going

* Trying to adopt Material Design's language to make design decisions and conversations easier in the absence of designers and to make the UI more consistent for enumerators ([“Typography rework” discussion](https://forum.getodk.org/t/reworking-collects-typography/20634))
* Moving non UI testing away from Espresso to cut down on long test startup times
* Slowly moving responsibilities out of FormEntryActivity
* Some Kotlin being introduced in code pulled into Gradle submodules
* General effort to increase test coverage and quality while working on anything and pushing more for tests in PR review
* Trying to remove technical debt flagged with `@Deprecated`
* Favoring domain objects (instance, form) with related logic where possible to more explicitly link data and logic
* Moving code to packages based on domain slices (`audio` or `formentry` for instance) to make it easier to work on isolated features and navigate code
* `QuestionWiget` implementations are moving from defining their "answer" view programmatically to [implementing `onCreateAnswerView`](WIDGETS.md)
* Replacing Rx (and other async work such as `AsyncTask`) with LiveData + Scheduler abstraction
* Moving away from custom `SharedPreferences` abstractions (`GeneralSharedPreferences` and `AdminSharedPreferences`) to just using `SharedPreferences` interface
* Replacing `..Factory` and `..Provider` objects with the new Java `Supplier` interface as much as possible
