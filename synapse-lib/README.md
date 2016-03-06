# synapse

Synapse is the smart way to connect and configure docker containers.
It uses a number of strategies to retrieve configuration and discover
dependencies such as: environment variables, docker standard variables
(to come: Kubernetes discovery API, Consul discovery API, Consul
configuration API, etcd configuration API).

## Usage

Please see doc [here](../README.md)

## How to build it

Build the core first with:

  cd ../synapse-core
  lein do clean, midje, install

Then you can build this project.

  cd ../synapse-lib
  lein do clena, midje, install

When releasing make sure all project versions have been updated accordingly.

## License

Copyright © 2016 Bruno Bonacci

Distributed under the Apache 2 License.
