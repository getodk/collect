# Benchmarking

## `create_100k_entities_project.py`

Creates and populates an ODK Central project for the "100k Entities Filter"
benchmark using [pyODK](https://github.com/getodk/pyodk).

Given a directory that contains:

* a form named `100k Entities Filter.xlsx` (an XLSForm), and
* an entity list CSV named `entities_100k.csv`

the script will:

1. create a new project on the configured ODK Central server,
2. create an entity list named `entities_100k` (adding a property for each non-`label` CSV column),
3. bulk-create the entities from `entities_100k.csv` into the list,
4. publish the `100k Entities Filter` form, and
5. create an app user that has access to the published form.

When it finishes, the script prints the Central project URL and the app user
URL that can be entered into ODK Collect to configure the server.

### Setup

Install `uv`:

```bash
curl -LsSf https://astral.sh/uv/install.sh | sh
```

pyODK reads the ODK Central connection settings from a config TOML file. This
script expects it to be named `.pyodk_config.toml` and to live in the same
directory as the form and CSV. The file looks like:

```toml
[central]
base_url = "https://your-central-server.example.com"
username = "you@example.com"
password = "your-password"
```

### Usage

```bash
uv run create_100k_entities_project.py /path/to/benchmark-forms
```
