# Web3j Demo API Application

## Introduction

This application is made to showcase the [Web3j API](https://github.com/web3j/web3j) that is used to interact with the Ethereum network 
by connecting to Ethereum nodes using JSON-RPC over HTTP, WebSockets, IPC. 

## Requirements

* Java 11
* Maven 3.5
* Docker (optional)
* Docker Compose (optional)

## Build

```  
mvn clean package  
```

## Run the application

If you don't have an Ethereum node you can use the `docker-compose.yml` file. Open your terminal and
navigate to the `web3j.api.demo` source directory then run the following command:

```
cd <PROJECT_DIRECTORY>
// Build Ganache-Cli image and Run it
docker-compose up -d 
```

The `Ganache-Cli` will be running on the http port 8545. Note that once the image is built, if you
want to start/stop the container you can run ,for instance, the container through the following: 

```
docker container start <CONTAINER_ID>
```

###  IDE

Run the main class `com.sy.web3j.api.demo.Bootstrap` if you want to use a regular HTTP protocol to
connect the Ethereum node. 

###  Maven

Open your terminal, navigate to the `web3j.api.demo` source directory then run the following command
```
mvn spring-boot:run
```

### Java

Open your terminal, navigate to the `web3j.api.demo` source directory then run the following command 

```
java -jar target/web3j-api-demo-1.0-0-SNAPSHOT.jar
```


### Note
If you have an Ethereum node that supports WebSockets protocol you can add the VM options 
`-Dspring.profiles.active=rpc-ws` while running the application. For instance:
```
mvn spring-boot:run -Dspring.profiles.active=rpc-ws
```

## Technical Environment

Web3j API Demo is a self contained application based on **Spring Boot** that runs an embedded servlet 
container running by default on port 8080 that expose a **REST API**.  It connects to an Ethereum
node through HTTP or WebSockets. It default to HTTP, but you can fallback to WebSockets by setting
the spring profile to `rpc-ws` if you use an Ethereum node that support WebSockets.

The following list the main frameworks and libraries used to implement this application:

- [Spring boot](https://spring.io/projects/spring-boot): Simple and rapid framework to create simple and web based applications.
- [Ganache-Cli](https://github.com/trufflesuite/ganache-cli): Lightweight Ethereum client. 
- [Web3j](https://github.com/web3j/web3j): Java lightweight library to interact with Ethereum network.
- [lombok](https://projectlombok.org/) : Framework auto generating code for java (getter, setter, ...).
- [vavr](http://www.vavr.io): Functional library for java.
- [Junit 5](https://junit.org/junit5/): The next generation of testing framework for java.
- [AssertionsJ](http://joel-costigliola.github.io/assertj/): Fluent assertions for java.


## Configuration

The following lists the available configuration keys and their default value in 
`yaml format`

- Spring boot application (application.yml)
```
web3j:
  http:
    client-address: http://localhost:8545
  ws:
    client-address: ws://localhost:8546

```

- Provided Ganache-Cli image (docker-compose.yml)
```
# The block gas limit in wei. (0xfffffffffff -> 17592186044415)
gasLimit 0xfffffffffff

# Specify blockTime in seconds for automatic mining
blockTime 20

# Amount of ether to assign each of the generated test account
defaultBalanceEther 3000000

```


## Features

This applications serves a REST API to interact with the Ethereum network along with subscription to
newly created/mined block and transaction. **Whenever, a block is mined the application receives
a message that is printed in the console.** 

**Note**: The example below uses curl command line to interact with the application but you can pick up any
web client you want.

### Get the transaction count for a given account

```
curl -i -X GET 'http://localhost:8080/transactions/0x2ba1ebe992d461d6d90ebcbdfee8ec7b4db509b2/count'

HTTP/1.1 200
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Wed, 13 Mar 2019 12:21:23 GMT

1

```

### Get th balance for a given account

```
curl -i -X GET 'http://localhost:8080/accounts/0x2ba1ebe992d461d6d90ebcbdfee8ec7b4db509b2/balance'

HTTP/1.1 200
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Wed, 13 Mar 2019 12:21:23 GMT

3000000

```

### Get the unlocked accounts owned by the Ethereum client

```
curl -i -X GET 'http://localhost:8080/accounts/'

HTTP/1.1 200
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Wed, 13 Mar 2019 12:20:46 GMT

["0x2ba1ebe992d461d6d90ebcbdfee8ec7b4db509b2","0xaf35ffdf903be76164468264360d8034c9c93c5d","0x07bfa3abc6dade3b7d9bdcc621cb30087350dae1","0x35a52937136f7e0196449b7ff99534f6dc633f99","0x524803faa8abfb017a8e8a35539773eeb41fae06","0x4b96e6334fd028c8ec9088c1941f8c644a981d27","0xbe361cb454ff8d3c4cb9081db0971bc249275049","0xf84799df8574e23c54443b8121f80b109153ea0b","0x9fc54b824426062d8bc0f41870baeb29e5d1a52f","0xde3d3e2627f2fe3df482721114d77be17f441b21"]
```

### Transfer Ether

```
curl 'http://localhost:8080/transactions' -i -X POST -H 'Content-Type: application/json;charset=UTF-8' -d '{"fromAccountAddress": "0x2ba1ebe992d461d6d90ebcbdfee8ec7b4db509b2", "toAccountAddress": "0xaf35ffdf903be76164468264360d8034c9c93c5d", "amountInEther": 1 }'

HTTP/1.1 202
Content-Length: 0
Date: Wed, 13 Mar 2019 12:45:04 GMT


```

The transaction is confirmed once the block is mined. The logs down below show the notifications sent by the Ethereum network.
```
2019-03-13 17:54:40.734  INFO 51702 --- [pool-2-thread-1] c.s.w.a.d.l.BlockchainEventConsumer      : A new block with hash 0x693980f3ed4c8fe7aedc9e9fd86be8ad3b9ea816643c4cbbae4c07b1e7611094 has been mined
2019-03-13 17:54:40.742  INFO 51702 --- [pool-2-thread-5] c.s.w.a.d.l.BlockchainEventConsumer      : A new transaction with hash 0x8e5200c7ea78a2eb82a83652a5864ddd4bb228f842a945fb593d0075dcb2f442 has been confirmed

```
