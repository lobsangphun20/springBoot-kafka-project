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

