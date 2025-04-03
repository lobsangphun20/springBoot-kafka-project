** This is a springBoot microservice project. 
	 Language : Java
	 build tool : Gradle groovy
	 Framework : Spring, spring cloud
	 Messaging server : Kafka broker with help of docker image
	 requirement : Docker engine running kafka messaging server.
** 

***************************************

File Structure is as follows

-cashcard-root
		-cashcard-traction-domain
		-cashcard-traction-source
		-cashcard-traction-enricher
		-cashcard-traction-sink
		
		
1. cashcard-root should have settings.gradle file which include all the project(sub-modules) 
2. It's important to keep all sub-modules project right under root project as seen above Directory tree structure.
***************************************



1) For source
docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic approvalRequest-out-0

./gradlew cashcard-transaction-sink:bootRun --args="--spring.cloud.function.definition=sinkToConsole;cashCardTransactionFileSink --spring.cloud.stream.bindings.cashCardTransactionFileSink-in-0.destination=enrichTransaction-out-0 --spring.cloud.stream.bindings.sinkToConsole-in-0.destination=enrichTransaction-out-0"

./gradlew cashcard-transaction-enricher:bootRun --args="--spring.cloud.stream.bindings.enrichTransaction-in-0.destination=approvalRequest-out-0"

./gradlew cashcard-transaction-sink:bootRun --args="--spring.cloud.stream.bindings.sinkToConsole-in-0.destination=enrichTransaction-out-0"

