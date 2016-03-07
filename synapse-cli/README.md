# synapse

Synapse is the smart way to connect and configure docker containers.
It uses a number of strategies to retrieve configuration and discover
dependencies such as: environment variables, docker standard variables
(to come: Kubernetes discovery API, Consul discovery API, Consul
configuration API, etcd configuration API).

## Usage

Please see doc [here](../README.md)

## How to build it

This project uses ClojureScript and Nodejs to build a executable.
So make sure you have `npm` installed and `nexe` as well as the platform
building tools `gc` etc.

    brew install node
    brew install npm
    npm install nexe -g

Then build the executable with:

    lein exe

Which is just an alias for:

    lein clean
    lein cljsbuild once
    nexe -f -i ./target/synapse.js -o ./target/synapse

## To release

Start with the java version:

    export VER=0.3.2
    rm -fr mkdir -p /tmp/synapse && mkdir -p /tmp/synapse
    lein do clean, midje, bin
    mv ./target/synapse /tmp/synapse/synapse-$VER-java8

Then build the Platform version (Mac OSX) with:

    lein exe
    mv ./target/exe/synapse-$VER /tmp/synapse/synapse-$VER-`uname -s`-`uname -m`

Finally build the linux version with:

    lein do clean, cljsbuild once, docker-latest
    # it will terminate immediately
    docker run brunobonacci/synapse bash
    docker cp `docker ps -a | grep brunobonacci/synapse | head -1| awk '{print $1}'`:/opt/exe/synapse-Linux-x86_64 /tmp/synapselinux
    mv /tmp/synapselinux/* /tmp/synapse/synapse-$VER-Linux-x86_64


## License

Copyright © 2016 Bruno Bonacci

Distributed under the Apache 2 License.
