wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/6.0.1_r3-robolectric-r1/android-all-6.0.1_r3-robolectric-r1.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/7.0.0_r1-robolectric-r1/android-all-7.0.0_r1-robolectric-r1.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/9-robolectric-4913185-2/android-all-9-robolectric-4913185-2.jar -P robolectric-deps
wget -nc https://repo1.maven.org/maven2/org/robolectric/android-all/10-robolectric-5803371/android-all-10-robolectric-5803371.jar -P robolectric-deps

mvn install:install-file -DgroupId=org.robolectric -DartifactId=android-all -Dversion=6.0.1_r3-robolectric-r1 -Dpackaging=jar -Dfile=robolectric-deps/android-all-6.0.1_r3-robolectric-r1.jar
mvn install:install-file -DgroupId=org.robolectric -DartifactId=android-all -Dversion=7.0.0_r1-robolectric-r1 -Dpackaging=jar -Dfile=robolectric-deps/android-all-7.0.0_r1-robolectric-r1.jar
mvn install:install-file -DgroupId=org.robolectric -DartifactId=android-all -Dversion=9-robolectric-4913185-2 -Dpackaging=jar -Dfile=robolectric-deps/android-all-9-robolectric-4913185-2.jar
mvn install:install-file -DgroupId=org.robolectric -DartifactId=android-all -Dversion=10-robolectric-5803371 -Dpackaging=jar -Dfile=robolectric-deps/android-all-10-robolectric-5803371.jar
