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

# The pyODK config TOML is expected to live in the same directory as the forms.
CONFIG_FILE_NAME = ".pyodk_config.toml"


def find_form_definition(directory: Path, form_name: str) -> Path:
    form_file_name = f"{form_name}.xlsx"
    candidate = directory / form_file_name
    if not candidate.is_file():
        raise FileNotFoundError(
            f"Could not find the form file {form_file_name!r} in {directory}."
        )
    return candidate


def find_entity_csv(directory: Path, entity_list_csv_name: str) -> Path:
    candidate = directory / entity_list_csv_name
    if not candidate.is_file():
        raise FileNotFoundError(
            f"Could not find the entity list CSV {entity_list_csv_name!r} in {directory}"
        )
    return candidate


def read_entities(csv_path: Path) -> list[dict]:
    with csv_path.open(newline="", encoding="utf-8-sig") as csv_file:
        reader = csv.DictReader(csv_file)
        if reader.fieldnames is None:
            raise ValueError(f"The CSV {csv_path} appears to be empty")
        if "label" not in reader.fieldnames:
            raise ValueError(
                f"The CSV {csv_path} does not contain the \"label\" column "
                f"Available columns: {reader.fieldnames}"
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


def create_entity_list(client: Client, entity_list_name: str) -> None:
    client.entity_lists.create(
        entity_list_name=entity_list_name,
    )
    print(f"Created entity list {entity_list_name!r}")


def populate_entity_list(
    client: Client,
    entities: list[dict],
    entity_list_name: str,
    entity_batch_size: int,
) -> None:
    # Property columns are every CSV column except the label column.
    property_names = [key for key in entities[0] if key != "label"]
    for name in property_names:
        client.entity_lists.add_property(
            name=name,
            entity_list_name=entity_list_name,
        )
    print(f"Created {len(property_names)} entity list properties in list {entity_list_name}")

    total = len(entities)
    for start in range(0, total, entity_batch_size):
        batch = entities[start:start + entity_batch_size]
        client.entities.create_many(
            data=batch,
            entity_list_name=entity_list_name,
        )
        print(
            f"Populated {entity_list_name!r} with "
            f"{min(start + len(batch), total)}/{total} entities"
        )


def publish_form(client: Client, form_definition: Path):
    form = client.forms.create(
        definition=str(form_definition),
    )
    print(f"Published form {form.xmlFormId!r} (version {form.version!r})")
    return form


def create_app_user(client: Client, form_id: str, app_user_name: str):
    app_users = client.projects.create_app_users(
        display_names=[app_user_name],
        forms=[form_id],
    )
    if not app_users:
        raise RuntimeError(
            f"ODK Central did not create the app user {app_user_name!r}"
        )
    return app_users[0]


def print_access_urls(base_url: str, project_id: int, app_user, form_id: str) -> None:
    project_url = f"{base_url}/#/projects/{project_id}"

    print()
    print(f"Central project URL: {project_url}")
    app_user_url = f"{base_url}/v1/key/{app_user.token}/projects/{project_id}"
    print(f"App user URL: {app_user_url}")


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "directory",
        type=Path,
        help=(
            "Directory containing the '100k Entities Filter' form and the "
            "'entities_100k.csv' entity list CSV"
        ),
    )
    args = parser.parse_args(argv)

    directory: Path = args.directory
    if not directory.is_dir():
        parser.error(f"{directory} is not a directory")

    config_path = directory / CONFIG_FILE_NAME
    if not config_path.is_file():
        parser.error(
            f"Could not find the pyODK config {CONFIG_FILE_NAME!r} in {directory}"
        )

    form_definition = find_form_definition(directory, "100k Entities Filter")
    entity_csv = find_entity_csv(directory, "entities_100k.csv")
    entities = read_entities(entity_csv)
    print(f"Read {len(entities)} entities from {entity_csv}")

    with Client(config_path=str(config_path)) as client:
        base_url = client.config.central.base_url.rstrip("/")

        project_name = "Collect benchmarking"
        project_id = create_project(client, project_name)
        print(f"Created project {project_name!r} (id={project_id})")

    with Client(config_path=str(config_path), project_id=project_id) as client:
        create_entity_list(client, "entities_100k")
        populate_entity_list(
            client,
            entities,
            "entities_100k",
            1000,
        )
        form = publish_form(client, form_definition)
        app_user = create_app_user(client, form.xmlFormId, "100k Entities Filter")

        print_access_urls(base_url, project_id, app_user, form.xmlFormId)

    print("Done.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
