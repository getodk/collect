#!/usr/bin/env -S uv run --script
# /// script
# requires-python = ">=3.10"
# ///

import csv
import sys

def main() -> int:
    input("Run the query at https://console.cloud.google.com/bigquery?sq=322300403941:b378914d4b4e4580a74bca23575fe7cd, download the CSV results to crash_reports.csv and then press enter...\n")

    print("\nFirebase links:\n\n")
    with open("crash_reports.csv") as csv_file:
        reader = csv.DictReader(csv_file)
        for row in reader:
            print(f"- {row['issue_title']}: https://console.firebase.google.com/project/api-project-322300403941/crashlytics/app/android:org.odk.collect.android/issues/{row['issue_id']}\n\n")
    return 0

if __name__ == "__main__":
    sys.exit(main())