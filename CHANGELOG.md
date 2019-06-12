# Synapse Changelog

## Release v0.4.0 (2019-06-12)
  * added prefix resolver

## Release v0.4.0 (2019-04-28)
  * Build native binary with GraalVM

## Release v0.3.4 (2016-04-22)
  * CLI - compile with alpine linux 3.3 + musl in fully static to
    avoid libc++ compat warning.

## Release v0.3.3 (2016-03-09)
  * CORE: re-quote-pattern requires different patterns Java/Javascript :-(
  * CORE: improved performance

## Release v0.3.2 (2016-03-07)
  * CORE: re-quote-pattern missing special char

## Release v0.3.1 (2016-03-07)
  * LIB: fix: load-config-file! without env-map returning `[config nil]` instead of just config

## Release v0.3.0 (2016-03-06)
  * Split between core, lib and cli.
  * release synapse as a lib.

## Release v0.2.0 (2016-02-28)
  * Initial release
  * Support for templating files
  * Support for Docker environment variables
  * Native executables
