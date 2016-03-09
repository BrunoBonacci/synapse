#!/bin/bash

# compatibility test env

export MORE_PORTS_PORT_9100_TCP='tcp://172.17.2.10:24123'
export MORE_PORTS_PORT_9200_TCP_ADDR='172.17.2.10'
export MORE_PORTS_PORT_9200_TCP_PORT='123'
export MORE_PORTS_PORT_9300_TCP='tcp://172.17.2.10:34123'
export MORE_PORTS_PORT_9300_TCP_ADDR='172.17.2.10'
export MORE_PORTS_PORT_9300_TCP_PORT='34123'
export MULTIPLE2_PORT_9100_TCP='tcp://172.17.3.20:9100'
export MULTIPLE2_PORT_9200_TCP='tcp://172.17.3.20:9200'
export MULTIPLE2_PORT_9300_TCP='tcp://172.17.3.20:9300'
export MULTIPLE_1_PORT_9100_TCP='tcp://172.17.3.10:9100'
export MULTIPLE_1_PORT_9200_TCP='tcp://172.17.3.10:9200'
export MULTIPLE_1_PORT_9300_TCP='tcp://172.17.3.10:9300'
export MULTIPLE_A_PORT_9100_TCP='tcp://172.17.3.30:9100'
export MULTIPLE_A_PORT_9200_TCP='tcp://172.17.3.30:9200'
export MULTIPLE_A_PORT_9300_TCP='tcp://172.17.3.30:9300'
export MULTI_VAR1='multiple'
export MULTI_VAR2='env-var'
export MULTI_VAR3='matched'
export SIMPLE_VAR='simple-value'
export SINGLE_PORT_3306_TCP='tcp://172.17.1.10:24123'