# synapse

**WORK IN PROGRESS**

Synapse is the smart way to connect and configure docker containers.
It uses a number of strategies to retrieve configuration and discover
dependencies such as: environment variables, docker standard variables
(to come: Kubernetes discovery API, Consul discovery API, Consul
configuration API, etcd configuration API).


## Usage

At its simplest `synapse` takes environment variables and replaces them
into configuration files, but there is more.
You can create your configuration files and put resolvable tags in
the place where you expect values passed down to the container.
Synapse will then replace the tag (or placeholder) with the actual value.

Let's see the anatomy of a resolvable tag:

```
               target pattern
     resolver       |           delimiter
         |          |           |
      %%docker>>zookeeper.*:2181%%
       |      |              |
delimiter     |            port
           single or    (optional)
            multiple

```


Here some examples of resolvable tags (more to come):


* `%%HOME%% %%env>HOME%% %%DATA_DIR%%`
It will look for a matching environment variable,
when found it will replace the tag with its value.

* `%%env>>SERVICE.*%%`
A double angular bracket (`>>`) means that you expect more
than one result. It will look for environment variables
like: `$SERVICE1`, `$SERVICE2`, `$SERVICE3` (matching
`SERVICE.*` regex) and replace the tag with the values
as a comma-separated list.

* `%%docker>zookeeper%%`
It will look for Docker's standard environment variables
to link the container. For example it in this case it will
look for: `$ZOOKEEPER_PORT_2181_TCP_ADDR`,
`$ZOOKEEPER_PORT_2181_TCP_PORT` and `$ZOOKEEPER_PORT_2181_TCP`
The docker resolution is the most commonly used so you can
omit the `docker` resolver name.

* `%%>zookeeper%%`
When the port is not specified it will look for all available
port names and take the lowest. A service might have more
than one service port (such as admin port or peers port)
but typically the client port is the lowest one.

* `%%>zookeeper:2181%%`
Otherwise you can specify the port you wish. This will be
resolved and replaced with the actual container's port.

* `%%>>zookeeper.*:2181%%`
If you expect more than one container with a given name
then you can specify a pattern and add a double angular
bracket (`>>`). In this case it will look for all zookeeper
containers and replace the tag with a comma-separated list.


### The resolvers

The resolver decides how the rest of the tag is interpreted.

### `env` resolver

The simplest resolver is the `env` resolver which takes the target and
tries to find a matching environment variable to resolve.

For example the tag `%%env>DATA_DIR%%` will be replaced with the
content of the environment variable `$DATA_DIR` if it is defined. If
the environment variable is not found then the tag will be left
unchanged and an error will be reported.

When trying to resolve an environment variable directly the `env>`
part can be omitted so the previous tag can be written as
`%%DATA_DIR%%`.



## License

Copyright © 2015 Bruno Bonacci

Distributed under the Apache 2 License.
