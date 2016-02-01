# synapse

A Clojure library designed to ... well, that part is up to you.

## Usage

```

%%HOME%% --> /home/ubuntu
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
