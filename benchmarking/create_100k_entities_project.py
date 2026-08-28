# /// script
# requires-python = ">=3.10"
# dependencies = [
#     "pyodk>=1.3.0",
# ]
# ///

import argparse
import csv
import sys
from pathlib import Path

from pyodk.client import Client

# Names expected by the benchmark, as described in the issue.
FORM_NAME = "100k Entities Filter"
ENTITY_LIST_CSV_NAME = "entities_100k.csv"
ENTITY_LIST_NAME = "entities_100k"

# Fixed name for the project created on ODK Central.
PROJECT_NAME = "Collect benchmarking"

# The CSV column used as the entity label.
LABEL_COLUMN = "label"

# Number of entities to send each call
ENTITY_BATCH_SIZE = 1000

# Fixed display name for the app user created on ODK Central.
APP_USER_NAME = "100k Entities Filter"

# The pyODK config TOML is expected to live in the same directory as the forms.
CONFIG_FILE_NAME = ".pyodk_config.toml"


def find_form_definition(directory: Path) -> Path:
    form_file_name = f"{FORM_NAME}.xlsx"
    candidate = directory / form_file_name
    if not candidate.is_file():
        raise FileNotFoundError(
            f"Could not find the form file {form_file_name!r} in {directory}."
        )
    return candidate


def find_entity_csv(directory: Path) -> Path:
    candidate = directory / ENTITY_LIST_CSV_NAME
    if not candidate.is_file():
        raise FileNotFoundError(
            f"Could not find the entity list CSV {ENTITY_LIST_CSV_NAME!r} in {directory}."
        )
    return candidate


def read_entities(csv_path: Path) -> list[dict]:
    with csv_path.open(newline="", encoding="utf-8-sig") as csv_file:
        reader = csv.DictReader(csv_file)
        if reader.fieldnames is None:
            raise ValueError(f"The CSV {csv_path} appears to be empty.")
        if LABEL_COLUMN not in reader.fieldnames:
            raise ValueError(
                f"The CSV {csv_path} does not contain the {LABEL_COLUMN!r} column. "
                f"Available columns: {reader.fieldnames}."
            )

        return list(reader)


def create_project(client: Client, project_name: str) -> int:
    response = client.session.request(
        method="POST",
        url="projects",
        json={"name": project_name},
    )
    response.raise_for_status()
    project = response.json()
    return project["id"]


def create_entity_list(client: Client) -> None:
    client.entity_lists.create(
        entity_list_name=ENTITY_LIST_NAME,
    )
    print(f"Created entity list {ENTITY_LIST_NAME!r}.")


def populate_entity_list(client: Client, entities: list[dict]) -> None:
    # Property columns are every CSV column except the label column.
    property_names = [key for key in entities[0] if key != LABEL_COLUMN]
    for name in property_names:
        client.entity_lists.add_property(
            name=name,
            entity_list_name=ENTITY_LIST_NAME,
        )
    print(f"Created {len(property_names)} entity list properties.")

    total = len(entities)
    for start in range(0, total, ENTITY_BATCH_SIZE):
        batch = entities[start:start + ENTITY_BATCH_SIZE]
        client.entities.create_many(
            data=batch,
            entity_list_name=ENTITY_LIST_NAME,
        )
        print(
            f"Populated {ENTITY_LIST_NAME!r} with "
            f"{min(start + len(batch), total)}/{total} entities."
        )


def publish_form(client: Client, form_definition: Path):
    form = client.forms.create(
        definition=str(form_definition),
    )
    print(f"Published form {form.xmlFormId!r} (version {form.version!r}).")
    return form


def create_app_user(client: Client, form_id: str):
    app_users = client.projects.create_app_users(
        display_names=[APP_USER_NAME],
        forms=[form_id],
    )
    if not app_users:
        raise RuntimeError(
            f"ODK Central did not create the app user {APP_USER_NAME!r}."
        )
    return app_users


def print_access_urls(base_url: str, project_id: int, app_users, form_id: str) -> None:
    project_url = f"{base_url}/#/projects/{project_id}"

    print()
    print(f"Central project URL: {project_url}")
    for app_user in app_users:
        app_user_url = f"{base_url}/v1/key/{app_user.token}/projects/{project_id}"
        print(f"App user URL: {app_user_url}")


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "directory",
        type=Path,
        help=(
            "Directory containing the '100k Entities Filter' form and the "
            "'entities_100k.csv' entity list CSV."
        ),
    )
    args = parser.parse_args(argv)

    directory: Path = args.directory
    if not directory.is_dir():
        parser.error(f"{directory} is not a directory.")

    config_path = directory / CONFIG_FILE_NAME
    if not config_path.is_file():
        parser.error(
            f"Could not find the pyODK config {CONFIG_FILE_NAME!r} in {directory}."
        )

    form_definition = find_form_definition(directory)
    entity_csv = find_entity_csv(directory)
    entities = read_entities(entity_csv)
    print(f"Read {len(entities)} entities from {entity_csv}.")

    with Client(config_path=str(config_path)) as client:
        base_url = client.config.central.base_url.rstrip("/")
        project_id = create_project(client, PROJECT_NAME)
        print(f"Created project {PROJECT_NAME!r} (id={project_id}).")

    with Client(config_path=str(config_path), project_id=project_id) as client:
        create_entity_list(client)
        populate_entity_list(client, entities)
        form = publish_form(client, form_definition)
        app_users = create_app_user(client, form.xmlFormId)

        print_access_urls(base_url, project_id, app_users, form.xmlFormId)

    print("Done.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
