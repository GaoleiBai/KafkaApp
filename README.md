# kafka-example-app

Steps:

1. start zookeeper instance

bin/zookeeper-server-start.sh config/zookeeper.properties

2. start 3 instances of kafka broker

bin/kafka-server-start.sh config/server.properties

bin/kafka-server-start.sh config/server-1.properties

bin/kafka-server-start.sh config/server-2.properties

3. create topic if not already exists

bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 2 --partitions 3 --topic numbers-topic

list topics command:

bin/kafka-topics.sh --list --zookeeper localhost:2181

4. start MessageConsumerDemo with arguments

localhost:2181 abc numbers-topic 3

5. start MessageProducerDemo with arguments

500000