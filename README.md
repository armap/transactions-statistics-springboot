# Transactions Statistics

A RESTful API with the following use cases: 

- Accepts new transactions and stores them in memory concurrenly, but deleting them after 60 seconds.
- Calculates Statistics for the last 60 secons of transactions.


*For more information, see the Backend_Engineer_Coding_Challenge.pdf file.

*All the code for "Integration tests" inside the it/ folder was not coded by me, it was received with the coding challenge.

## Installation
- Compile:    
```bash
mvn clean install 
```
- Run:        
```bash
mvn spring-boot:run
```

## API usage:
Post transactions
```bash
POST http://localhost:8080/transactions
Body:
{
  "amount": "23.6",
  "timestamp": "2018-07-17T09:59:51.312Z"
}
```
Get statistics
```bash
GET http://localhost:8080/statistics
```
Delete transactions
```bash
DELETE http://localhost:8080/transactions
```
