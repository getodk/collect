ls -R collect_app/src/test/java/ | grep Test.java > .circleci/collect_app_test_files.txt
ls -R collect_app/src/test/java/ | grep Test.kt >> .circleci/collect_app_test_files.txt
cat .circleci/collect_app_test_files.txt | sed "s/\.java//" | sed "s/\.kt//" > .circleci/collect_app_test_classes.txt
