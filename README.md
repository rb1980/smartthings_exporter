# SmartThings Exporter

A Prometheus exporter for SmartThings devices and sensors. This exporter allows you to monitor your SmartThings devices using Prometheus and visualize the data using tools like Grafana.

## Features

- Exports SmartThings device metrics in Prometheus format
- Supports OAuth2 authentication with SmartThings API
- Provides device status and sensor readings
- Easy to set up and configure

## Prerequisites

- Go 1.24.1 or later
- SmartThings OAuth client credentials
- SmartThings-compatible devices

## Installation

```bash
go install github.com/rb1980/smartthings_exporter@latest
```

## Configuration

1. First, register your SmartThings OAuth client:

```bash
smartthings_exporter register --smartthings.oauth-client=YOUR_CLIENT_ID
```

2. Follow the prompts to complete OAuth registration and save the token.

## Usage

Start the exporter:

```bash
smartthings_exporter start \
  --smartthings.oauth-client=YOUR_CLIENT_ID \
  --smartthings.oauth-token.file=/path/to/token.json \
  --web.listen-address=:9499
```

### Flags

- `--web.listen-address`: The address to listen on for web interface and telemetry (default: ":9499")
- `--web.telemetry-path`: Path under which to expose metrics (default: "/metrics")
- `--smartthings.oauth-client`: SmartThings OAuth client ID (required)
- `--smartthings.oauth-token.file`: File containing the SmartThings OAuth token (required)

## Metrics

The exporter provides various metrics from your SmartThings devices, including:

- Device status
- Sensor readings (temperature, humidity, etc.)
- Battery levels
- Connection status

All metrics are prefixed with `smartthings_` and include device ID and name labels.

## Building from Source

```bash
git clone https://github.com/rb1980/smartthings_exporter.git
cd smartthings_exporter
go build
```

## License

Licensed under the Apache License, Version 2.0. See LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
