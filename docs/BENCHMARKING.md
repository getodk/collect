# Benchmarking

## Setting up Central

Download and extract the forms folder [from Google Drive](https://drive.google.com/drive/folders/1A0LZdQaY2y204hc4Yu3HVDi2U1I26WFs?usp=sharing).

Install `uv`:

```bash
curl -LsSf https://astral.sh/uv/install.sh | sh
```

`setup.py` reads the ODK Central connection settings from a config TOML file. This
script expects it to be named `.pyodk_config.toml` and to live in the same
directory as the forms. The file looks like:

```toml
[central]
base_url = "https://your-central-server.example.com"
username = "you@example.com"
password = "your-password"
```

To run the setup do:

```bash
uv run benchmarking/setup.py /path/to/benchmark-forms
```

## Running benchmarks

```bash
benchmarking/benchmark.sh
```

### Devices

These devices can be used to run benchmarks:

- Fairphone 3