# synapse

**WORK IN PROGRESS**

Synapse is the smart way to connect and configure docker containers.
It uses a number of strategies to retrieve configuration and discover
dependencies such as: environment variables, docker standard variables
(to come: Kubernetes discovery API, Consul discovery API, Consul
configuration API, etcd configuration API).


## Usage

You can create your configuration files and put resolvable tags in
the place where you expect values passed down to the container.
Synapse will then replace the tag (or placeholder) with the actual value.

Here some example of resolvable tags (more to come):

```
%%HOME%%                             /home/ubuntu
%%docker>zookeeper%%                 ZOOKEEPER_PORT_2181_TCP_ADDR ZOOKEEPER_PORT_2181_TCP_PORT ZOOKEEPER_PORT_2181_TCP
%%>zookeeper%%                       single container link with lowest port
%%>zookeeper:2181%%                  single container link with specific port
%%>>zookeeper.*:2181%%               multiple containers link (csl)

%%[addr]>>zookeeper.*:2181%%         multiple containers link but output just the address
%%[port,sep=|]>>zookeeper.*:2181%%   multiple containers link but output just the port and concatenate with |

resolvers: env, docker, kubernetes, consul

```


## License

Copyright © 2015 Bruno Bonacci

Distributed under the Apache 2 License.
