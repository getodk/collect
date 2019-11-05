# State of the union

The purpose of this document is to give anyone who reads it a quick overview
of both the current state and the direction of the code. The community should try
and update this document as the code evolves.

## How we got here

* App originally built in Java for Android 1.0/T-Mobile G1
* Written at Google by graduate student interns and then University of Washington
* Designed as a survey application backed by [JavaRosa](https://github.com/opendatakit/javarosa/) communicating with [OpenRosa](https://docs.opendatakit.org/openrosa/) servers
* Many different contributors/styles/eras over 10 year lifetime
* App wasn't built with a TDD workflow or with automated testing
* Lots of work in the last two years to add more tests and clean up code using coverage measurement and static checks

## Where we are now

* App has mixture of unit tests (JUnit), Robolectric tests (Junit + Robolectric) and Espresso tests but coverage is far from complete
* Test style, reasoning and layering is inconsistent
* App still written in Java with min API at 16 so basically targeting Java 7 source
* UI has is "iconic" (old) but with a lot of inconsistencies and quirks and is best adapted to small screens
* A lot of code lives in between one "god" Activity (FormEntryActivity) and a process singleton (FormController)
* Core form entry flow uses custom side-to-side swipe view (in FormEntryActivity made up of ODKView)
* Async/reactivity handled with a mixture of callbacks, LiveData and Rx
* App stores data in flat files indexed in SQLite
* Preferences for the app use Android's Preferences abstraction (for UI also)
* Material Components styles are used in some places but app still uses AppCompat theme
* Dagger is used to inject "black box" objects such as Activity and in some other places but isn't set up in a particularly advanced way
* Http is handled using OkHttp3 and https client abstractions are generally wrapped in Android's AsyncTask (and some Rx)
* Geo activities use three engines (Mapbox, osmdroid, Google Maps) depending on the selected basemap even though Mapbox could do everything osmdroid does
* Code goes through static analysis using CheckStyle, PMD, SpotBugs and Android Lint

## Where we're going

* Trying to adopt Material Design's language to make design decisions and conversations easier in the absence of designers and to make the UI more consistent for enumerators ([“Typography rework” discussion](https://forum.opendatakit.org/t/reworking-collects-typography/20634))
* Moving non UI testing away from Espresso to cut down on long test startup times
* Slowly moving responsibilities out of FormEntryActivity
* Talk of moving to Kotlin but not real plans as of yet ([“Using Kotlin for ODK Android apps” discussion](https://forum.opendatakit.org/t/using-kotlin-for-odk-android-apps/18367))
* General effort to increase test coverage and quality while working on anything and pushing more for tests in PR review
