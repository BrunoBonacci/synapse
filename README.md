# synapse

Synapse is the smart way to connect and configure docker containers.
It uses a number of strategies to retrieve configuration and discover
dependencies such as: environment variables, docker standard variables
(to come: Kubernetes discovery API, Consul discovery API, Consul
configuration API, etcd configuration API).

## Download

It comes in two forms: a command line tool, or a Clojure library:

Download latest command line tool release here:

  - [synapse for Linux x86_64](https://github.com/BrunoBonacci/synapse/releases/download/0.4.0/synapse-Linux-x86_64)
  - [synapse for OS X x86_64](https://github.com/BrunoBonacci/synapse/releases/download/0.4.0/synapse-Darwin-x86_64)
  - [synapse for Java8 (executable jar)](https://github.com/BrunoBonacci/synapse/releases/download/0.4.0/synapse)

Download the latest library version here:

  - [synapse as Clojure library](https://clojars.org/com.brunobonacci/synapse)

## Installation

  * Installation via Homebrew
  ``` bash
  brew tap BrunoBonacci/lazy-tools
  brew install synapse
  ```

  * Install command line tool (Native binary for Mac-OSX and Linux)
  ``` bash
  mkdir -p ~/bin
  wget https://github.com/BrunoBonacci/synapse/releases/download/0.4.0/synapse-$(uname -s)-$(uname -m) -O ~/bin/synapse
  chmod +x ~/bin/synapse
  export PATH=~/bin:$PATH
  ```

  * Install command line tool (for Windows and other platforms, requires Java JRE)
  ``` bash
  mkdir -p ~/bin
  wget https://github.com/BrunoBonacci/synapse/releases/download/0.4.0/synapse -O ~/bin/synapse
  chmod +x ~/bin/synapse
  export PATH=~/bin:$PATH
  ```

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

### Cheatsheet

**TL;DR** A quick example based list of available resolvable tags.

```
  # Environment variable are replace with their value
  %%HOME%%                   => /home/ubuntu
  %%env>HOME%%               => /home/ubuntu

  # A default value can be given with a double pipe (||)
  %%HOME||/home/user1%%      => /home/ubuntu
  %%LOGS_DIR%%               => %%LOGS_DIR%%
  %%LOGS_DIR||/var/logs%%    => /var/logs

  # When multiple environment are expected use a pattern
  # together with a double angle bracket `>>` instead
  # instead of the single angle bracket `>`
  %%ALLOWED_IP1%%            => 172.17.0.21
  %%ALLOWED_IP2%%            => 172.17.1.2
  %%ALLOWED_IP3%%            => 172.17.3.45
  %%env>>ALLOWED_IP.*%%      => 172.17.0.21,172.17.3.45,172.17.1.2
  %%env>ALLOWED_IP.*%%       => 172.17.0.21

  # When translating from a number of environment variables to
  # properties with a common prefix you can use the `prefix` resolver
  # it will find all the environment variable with this prefix and
  # turn them into properties with the corresponding value.
  # For example, if you have the following environment variables
  # CONF_DATABASE_HOST=db.dev.local
  # CONF_DATABASE_PORT=1234
  # they can be automatically replaced into their corresponding
  # properties with:
  %%prefix>CONF_%%           => database.host=db.dev.local
                                database.port=1234

  # you can control the case and the separator with the following options
  %%prefix[case=camel,sep=]>CONF_%%
                             => databaseHost=db.dev.local
                                databasePort=1234

  # for the odd cases you can preserve the case of the variables
  # CONF_DataBase_Host=db.dev.local
  # CONF_DataBase_Port=1234
  %%prefix[case=preserve]>CONF_%%
                             => DataBase_Host=db.dev.local
                                DataBase_Host=1234


  # When resolving docker links use the docker resolver
  %%docker>db:3306%%         => 172.17.12.21:12321
  %%>db:3306%%               => 172.17.12.21:12321
  %%docker>els1:9200%%       => 172.17.15.10:24123
  %%>els1:9200%%             => 172.17.15.10:24123
  %%>>els.*:9200%%           => 172.17.15.20:12433,172.17.15.10:24123,172.17.15.30:9413

  # ELS exposes two ports 9200, 9300, if not set the lowest is choosen
  %%>>els.*%%                => 172.17.15.20:12433,172.17.15.10:24123,172.17.15.30:9413

  # if you want only one address but multiple are matching
  # the first is lexicographic order is returned.
  %%>els.*:9200%%            => 172.17.15.10:24123

  # defaults works in the same ways
  %%>smtp:25||localhost:25%% => localhost:25

  # you can resolve address and ports separately
  %%[addr]>db:3306%%         => 172.17.12.21
  %%[port]>db:3306%%         => 12321

  # you can use also with multiple targets
  %%[addr]>>els.*:9200%%     => 172.17.15.20,172.17.15.10,172.17.15.30

  # you can customize the separtor with
  %%[addr,sep=;]>>els.*:9200%% => 172.17.15.20;172.17.15.10;172.17.15.30
```

### Basics

Here some examples of resolvable tags (more to come):


* `%%HOME%%`, `%%env>HOME%%`, `%%DATA_DIR%%`, `%%data_dir%%`

  It will look for a matching environment variable,
  when found it will replace the tag with its value.
  the var name/pattern is **case insensitive**.

* `%%env>>SERVICE.*%%`
  A double angle bracket (`>>`) means that you expect more
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
  then you can specify a pattern and add a double angle
  bracket (`>>`). In this case it will look for all zookeeper
  containers and replace the tag with a comma-separated list.


### Default values

* `%%DATA_DIR||//mnt/data%%`  or  `%%env>DATA_DIR||//mnt/data%%`, `%%>>zookeeper.*:2181||10.10.10.10:1221%%`

  You can provide a default value which will be used in case
  no matching candidate env vars are found. Just add `||`
  (double pipe) followed by the default value. The default
  value can be also empty which will resolve to an empty
  string (eg: `%%SOME_VAR||%%`). This it can be useful
  in cases when you want to resolve a value if given
  but not fail it isn't set in the environment.


### Partial resolution and other options

* `%%[addr]>zookeeper.*:2181%%` (=> `172.17.0.2`) or `%%[port]>zookeeper:2181%%` (=> `34765`)

  In some cases it might be useful to get just the host address or
  just the port number, for example when the configuration settings
  require two separate entries. In such case just add `[addr]` or
  `[port]` before the single or double angle bracket and the
  resolver will emit just the part you are interested in.
  Omitting this option is the same as `[addr,port]`

* `%%[sep=;]>>zookeeper.*:2181%%`

  You can customize the separator by adding `sep=` followed by the
  separator you wish to add. It can be empty, a single character or a
  string. `\n` and `\t` will be unescaped while the comma (`,`)
  is not supported as it is the default separator.


### The resolvers

The resolver decides how the rest of the tag is interpreted.

#### `env` resolver

The simplest resolver is the `env` resolver which takes the target and
tries to find a matching environment variable to resolve.

For example the tag `%%env>DATA_DIR%%` will be replaced with the
content of the environment variable `$DATA_DIR` if it is defined. If
the environment variable is not found then the tag will be left
unchanged and an error will be reported.

When trying to resolve an environment variable directly the `env>`
part can be omitted so the previous tag can be written as
`%%DATA_DIR%%`.


#### `prefix` resolver

There are many cases where the application you trying to configure
has a large number of properties which can be configured and you
need to provide the same capability via environment variables.
In this case you can prefix all the configuration variables with
a common prefix such as: `CONF_`, `KAFKA_`, `ZK_` etc and
have `synapse` to replace what follows with a property with the same
name and with the value of the variable.

For example, if you want to set a property called `log.flush.interval.ms`
and `log.roll.hours` but you don't want to have an entry for every
single Kafka broker configuration option you can add in your template
file the following resolver entry.

```
# Resolve any environment varible starting with KAFKA_
%%prefix>KAFKA_%%
```

If the following configuration variable are present:

``` bash
export KAFKA_LOG_FLUSH_INTERVAL_MS=300000
export KAFKA_LOG_ROLL_HOURS=200
```

it will be expanded to:

```
# Resolve any environment varible starting with KAFKA_
log.flush.interval.ms=300000
log.roll.hours=200
```

You can control the case with the `case` option and the
separator with `sep` as follow:

```
# change to camel case and remove the separator
%%prefix[case=camel,sep=]>KAFKA_%%

# will resolve to
logFlushIntervalMs=300000
logRollHours=200
```

Here the full option description:

  - `case` : (default: `lower`), one of `lower`, `camel`, `preserve`.
             `preserve` will maintain the same case as the environment
             variable. So if you have and environment variable
             `CONF_Foo_someOtherKey=200` and a resolver of
             `%%prefix[case=preserve]>CONF_%%` will resolve to
             `Foo.someOtherKey=200`
  - `sep`  : (default: `.` dot), the separator used to replace `_`
  - `type` : (default: `properties`), Currently it's the only
             supported option.

#### `docker` resolver

The docker resolver uses the standard environment variable structure
defined in this https://docs.docker.com/v1.9/compose/env/ page.
Basically if you are trying to link the DB with port 5432
to your container it will look for environment variables with
the following names `DB_PORT_5432_TCP_ADDR`, `DB_PORT_5432_TCP_PORT`
and `DB_PORT_5432_TCP` and use then to resolve the address and port.

So if you have to add the location of the DB in your configuration
files you need just to put the following tag `%%docker>DB:5432%%`
and it will be replaced with the actual container ip address and port.
You can also shorten the tag by omitting the `docker` resolver name
so the previous tag could be written as `%%>DB:5432%%`.

Many times you expect more than one db node to be linked to a container
so that if a particular node is down you can still connect to another one
and oftentimes they are named as `DB` plus a number such as: `DB1`, `DB2`,
`DB3` etc. If you want to get a comma-separated list of them you can
just add another angle bracket `>` in the previous tag and give a pattern
for the tag name like: `%%>>DB.*:5432%%`. Note the double angle brackets `>>`
which means you expect more than one and you wish to get a comma-separated
list of their values.


## Synapse library usage

Synapse can be used as a Clojure library to configure your service.
One way to use it is to create your configuration files as EDN files
and place the resolvable tags in the places you wish to configure.
For example you could replace `username` and `password` pairs with
environment variables.

Firstly add the dependency to your project dependencies:

[![Clojars Project](https://img.shields.io/clojars/v/com.brunobonacci/synapse.svg)](https://clojars.org/com.brunobonacci/synapse)

    [com.brunobonacci/synapse "0.4.0"]

Then prepare a configuration file such as: `config.edn`

``` clojure
{:database
  {:host "%%>database:12345||localhost%%"
   :username "%%DB_USER%%"
   :password "%%DB_PASS%%"}}
```

Set in your environment the vars (or add a default):

``` bash
export DB_USER="your-user"
export DB_PASS="secret"
```

In your program add the require:

``` clojure
(ns your.namspace
  (:require [synapse.synapse :refer [load-config-file!]))

```

Now you can load the configuration file directly with:

``` clojure
(def config (load-config-file! "./config.edn"))

;; => {:database {:host "localhost", :username "your-user", :password "secret"}}
```

If any error occur an exception will be thrown. If you don't want the exception
to be thrown you can you use `load-config-file` (without bang `!`) which returns
a vector of the configuration or `nil` and the error (as `[ config error ]`).

``` clojure
(let [[config error] (load-config-file "./config.edn"))]
  config)
;; => {:database {:host "localhost", :username "your-user", :password "secret"}}
```

## License

Copyright © 2016-2019 Bruno Bonacci

Distributed under the Apache 2 License.
