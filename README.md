# Cloudera Manager HipChat Chatbot

## Introduction
This chatbot forwards information provided by a Cloudera Manager Server on the managed clusters. The information can be requested via messages in a HipChat chatroom. There is a list of valid messages the chatbot can process. It is necessary for the hipchat.com service to be able to access the endpoint the chatbot is running on. As well as the chatbot needs to be able to access the Cloudera Manager Server.

## Setup

### How to Build
* make sure java (at least 1.7) is installed on your machine
* make sure gradle is installed on your machine
* clone the project
* optional: you are free to now already edit the properties file `/src/main/resources/application.properties` to match your setup. Be aware, that you cannot edit this file after building the project. Though as this is a Spring Boot application it is possible to add an `application.properties` file to a `/config` subdirectory after building as well (section 'How to Deploy'). This new file will hide the one in the .jar. Have a look at http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html `§ 24.3 Application property files` for more information.
* open bash
* go to containing folder
* run `./gradlew assemble`

### Creating a Cloudera Manager User
To access the information the manager provides the chatbot can log in as any user already existing. However it is recommended to create an extra account only used by the application. To create a new user
* open Cloudera Manager UI
* log in with administrative rights
* go to tab `Administration`
* select `Add User`
* choose a user name and password
* select role `Read-Only` and press `ok`.

### Setting up a HipChat Chat Room
In addition to many already build in integrations Hipchat provides the opportunity to build your own integration. That is creating a room that delivers messages starting with a slash command of your choice directly to the chatbot application. Have a look at the following link for more information on how to build your own integration.

Link: https://blog.hipchat.com/2015/02/11/build-your-own-integration-with-hipchat/

### How to Deploy

* copy `build/libs/cloudera-manager-hipchat-chatbot-[version].jar` to wherever you want the program be executed
	* make sure java is installed on this machine as well
* if you didn't edit the initial properties file before the build or if you want to use different properties
	* create a subdirectory `/config` next to your .jar file
	* create a file named `application.properties` within `/config`
	* copy the following skeleton into the file an insert the values you configured in the sections 'Creating a Cloudera Manager User' and 'Setting up a HipChat Chat Room'

```
#application.properties
cm.password=<cm login password>
cm.user=<cm user name>
cm.hostname=<Cloudera Manager hostname>
cm.port=<Cloudera Manager server port>
server.port=<Hipchat server port>
webhook.room.id=<room id>
webhook.notificationUrl =<your notification url>
```

* start application by running `path/to/java-version/bin/java -jar cloudera-manager-hipchat-chatbot-[version].jar` from your bash

## Communicate with the Chatbot
Creating the chatroom you chose a slash command for the communication. For example `/cm`. This implies that you need to precede each command with `/cm`. If you are not sure which commands are available start with `/cm help`. The chatbot will respond listing all available commands.

### Available Commands
* `current load`: current cpu load across hosts
* `current storage`: current DFS capacity
* `current io network`: current byte rates across network interfaces
* `current io hdfs`: current byte rates across datanodes
* `current io disk`: current byte rates across disks
* `current pool cores`: 
* `current pool memory`: 
* `hosts`: list of all hosts
* `hosts decommisioned`: list of all hosts with status decommissioned
* `hosts decommissioning`: list of all hosts with status decommissioning
* `hosts commisioned`: list of all hosts with status commissioned
* `hosts bad`: list of all hosts with status bad health
* `hosts good`: list of all hosts with status good health
* `hosts concerning`: list of all hosts with status concerning health
* `services`: list of all services
* `services bad`: list of all services with status bad health
* `services good`: list of all services with status good health
* `services concerning`: list of all services with status concerning health
* `status`: information about the general health of the cluster
* `status service <service>`: health status of the given service <service> (e.g. hdfs, yarn, hue...)