# Money Transfer API

This project implements a RESTful API (including data model and the backing implementation) for money transfers between accounts.

Table of contents
=================

<!--ts-->
   * [Summary](#summary)
   * [Goals](#goals)
   * [Dependencies](#dependencies)
   * [Data Model](#data-model)
   * [Usage](#usage)
   * [Limitations](#limitations)
   * [TODOs](#todos)
<!--te-->
## Summary

- Money transfer API with simple and extensible data model.
- Supports transfer in one currency.
- Transfer is idempotent even in the case of concurrent requests.
- 3 main APIs: a) Create Account: ``POST /api/1.0/account``, b) Get Account: `GET /api/1.0/account?id=<account_id>`, and c) Transfer: `POST /api/1.0/transfer` 
- Minimal use frameworks/libraries.

## Goals
- Keep it simple and to the point (e.g. no authentication support).
- The datastore should run in-memory for the sake of this test.
- The transfer API should be able to handle concurrency and be idempotent.
- The final result should be executable as a standalone program (should not require a
pre-installed container/server). So, I have not used jetty/tomcat server, simply implemented with basic [jdk11 httpserver](https://docs.oracle.com/en/java/javase/11/docs/api/jdk.httpserver/com/sun/net/httpserver/package-summary.html) module.
- Demonstrate with tests that the API works as expected.

## Dependencies

- [jdk11](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)
- [Jackson](https://github.com/FasterXML/jackson) for JSON (de)serialization
- [Lombok](https://projectlombok.org/) to simplify POJO classes
- [vavr](https://www.vavr.io/) for easy functional programming

## Data Model
There are only two models: `Account` and `Transaction`
```java
@Value
@Builder(toBuilder = true)
public class Account
{
    String id;
    String name;
    Double balance;
    String currency;
    Boolean state;
    String createdAt;
    String updatedAt;
}

@Value
@Builder(toBuilder = true)
public class Transaction
{
    String id;
    String type;
    String requestID;
    TransactionStatus status;
    String createdAt;
    String updatedAt;
    String completedAt;
    String reference;
    Handshake[] handshakes;

    @Value
    @Builder
    public static class Handshake
    {
        String id;
        String accountID;
        Double amount;
        String currency;
    }
}
```
Simple `ConcurrentHashMap` is used to store the data and access concurrently. The transfer is designed as a **handshake** between two account; one handhsake being a debit from one account, and the other being a credit to another account. 


## Usage

1. Ensure environment has jdk11 setup. The project has been tested with jdk 11.0.5 on macOS version 10.13.6. But it should work on Linux or Windows environment too.
2. Clone the repo.
3. Fire the main class (`com.revolut.backend.App`), which should start the `httpserver`. 
4. APIs:
- ```curl -X POST localhost:8000/api/1.0/account -d '{"name": "sagar" , "balance" : 200.0,  "currency" : "EUR"}'```
- Note that balance parameter is optional and if no balance is provided then account starts with a minimum balance of 100.
- Response will be a json containing the account id, e.g. `{"id": "f0462e0d-ecba-4391-b4ca-971b102a0922"}`
- To get an account: 
- `curl -X GET localhost:8000/api/1.0/account?id=f0462e0d-ecba-4391-b4ca-971b102a0922`
- Get Account response will be like:
```$xslt
{
    "id": "f0462e0d-ecba-4391-b4ca-971b102a0922",
    "name": "sagar",
    "balance": 150.5,
    "currency": "EUR"
}
```
- Transfer Request:
```$xslt
curl -X POST localhost:8000/api/1.0/transfer -d 
'{
    "requestID": "req1" , 
    "sourceAccountID" : "f0462e0d-ecba-4391-b4ca-971b102a0922", 
    "targetAccountID" : "57278ec7-8587-4554-ac76-c285adeb640c", 
    "amount" : 50.50,  
    "currency" : "EUR", 
    "reference" : "test transfer"
}'
```                        
`requestID` is a string parameter to ensure that the request is idempotent.
``sourceAccountID`` is the UUID of the account from which the money is to be transferred.
``targetAccountID`` is the UUID of the account to which the money is to be transferred.
``amount`` is a `Double` value, upto two decimal places, indicating the amount to transfer.
``currency`` is the 3-letter ISO code of the currency in which transfer is to happen. Currently the API does not support cross-currency transfer, however the logic can be easily extended to support that without making any change in the data model.
``reference`` is the string description of the transfer.

## Limitations

- As mentioned before that the API currently supports transfer in one currency. But the logic can be easily extended.
- Tried to package jar with dependencies using ``maven-assembly-plugin`` and `maven-shade-plugin` but still the creation of fat jar was failing. Hence, there is no executable. But, the application runs from the main class.

## TODOs

- Build an executable jar with all the dependencies.
- Add more logging.