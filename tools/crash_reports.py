#!/usr/bin/env -S uv run --script
# /// script
# requires-python = ">=3.10"
# dependencies = ["google-cloud-bigquery"]
# ///

import argparse
import sys
from google.cloud import bigquery
from google.auth.exceptions import DefaultCredentialsError

QUERY_TEMPLATE = """
    SELECT t.issue_id,
           COUNT(DISTINCT t.installation_uuid) as users
    FROM `api-project-322300403941.firebase_crashlytics.org_odk_collect_android_ANDROID` as t,
         UNNEST(exceptions) as e
    WHERE is_fatal = true
      AND t.issue_title != "java.lang.OutOfMemoryError"
      AND t.issue_subtitle != "java.lang.OutOfMemoryError"
      AND t.issue_subtitle != "com.android.internal.os.BinderInternal$GcWatcher.finalize"
      AND t.issue_subtitle !=
          "java.util.concurrent.TimeoutException - com.android.internal.os.BinderInternal$GcWatcher.finalize() timed out after 10 seconds"
      AND t.issue_subtitle != "android.os.ThreadLocalWorkSource.setUid"
      AND t.issue_subtitle != "java.lang.Object.wait"
      AND e.type != "java.lang.OutOfMemoryError"
      AND STARTS_WITH(t.application.display_version, "{version}.")
    GROUP BY t.issue_id
    ORDER BY users DESC
        LIMIT 10
"""

def main() -> int:
    parser = argparse.ArgumentParser(
        description="Query Firebase Crashlytics via BigQuery for top crashes by users affected"
    )
    parser.add_argument(
        "version",
        help="App version to query (e.g., v2026.3)"
    )
    args = parser.parse_args()

    try:
        client = bigquery.Client()
    except DefaultCredentialsError:
        parser.error(
            "Not authenticated with Google Cloud! Run: gcloud auth application-default login"
        )

    version = args.version
    query = QUERY_TEMPLATE.format(version=version)
    query_job = client.query(query)
    results = query_job.result()

    print("Top 10 crashes by users affected:\n")
    for row in results:
        print(f"https://console.firebase.google.com/project/api-project-322300403941/crashlytics/app/android:org.odk.collect.android/issues/{row.issue_id}\n")

    return 0

if __name__ == "__main__":
    sys.exit(main())