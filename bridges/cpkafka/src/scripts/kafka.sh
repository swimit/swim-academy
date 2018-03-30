#!/usr/bin/env bash

zkServer start
kafka-server-start /usr/local/etc/kafka/server.properties

kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic swim-test

kafka-topics --list --zookeeper localhost:2181

kafka-console-producer --broker-list localhost:9092 --topic swim-test

kafka-console-consumer --bootstrap-server localhost:9092 --topic swim-test --from-beginning