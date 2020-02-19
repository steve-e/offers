# Offers
An example RESTful offers microservice.

Create, query and cancel offers. 
The service calculates when offers have expired.

## Getting started
`git clone git@github.com:steve-e/offers.git`

### Running the app
`sbt run`

### Create an offer
```
curl -X POST -H 'Content-Type:application/json'  http://localhost:9000/v1/offers -d '{
  "price": "9.99",
  "currency": "GBP",
  "description": "iPhone",
  "start": "2020-02-20T00:00:00",
  "duration": "10 days"
}'
```

## API

### Create
`POST /vi/offers`

Request:

```
{
  "price": "9.99",
  "currency": "GBP",
  "description": "iPhone",
  "start": "2020-02-20T00:00:00",
  "duration": "10 days"
}
```

Successful Response Body: 

`{ "id" : "<offerid>" }`

Use the returned <offerid> to query or cancel

Status:
* 201 - offer created
* 400 - bad request format or validation error

### Find
`GET /vi/offers/<offerid>`

Successful Response Body: 
```
{
  "id": "6042a12b-950b-4129-982a-c82aabab33c8",
  "price": 9.99,
  "currency": "GBP",
  "description": "iPhone",
  "start": "2020-02-20T00:00:00",
  "end": "2020-03-01T00:00:00"
}
```

Status:
* 200 - offer found
* 404 - offer not found
* 410 - offer expired or cancelled

### Cancel
`DELETE /vi/offers/<offerid>`
Status:
* 200 - offer cancelled
* 404 - offer not found

## Tests
* [Acceptance tests](test/acceptance/OfferAcceptanceSpec.scala)
* [Service tests](test/com/github/stevee/offers/service/OfferServiceSpec.scala)
* [Controller tests](test/com/github/stevee/offers/controller/OfferControllerSpec.scala)

## Limitations
1. There is no authentication, authorization or TLS. 
There would need to be authorization to create and modify offers
2. The persistence layer is for demo purposes only. 
It is in memory and not thread safe.
3. The REST api only provides the minimum requested operations. 
There could be a search api, an update api, or a way of extending offers.
4. There is no Open API documentation
5. There is no monitoring and not much logging. 
6. There are no build steps to create and publish a docker image


## Implementation Notes
This project is implemented with Play framework, as requested.
It was initiated with the the scala seed template using

`sbt new playframework/play-scala-seed.g8`

Effect type is Future, test framework Specs2, json is Play Json.
Without hints otherwise I would have used http4s, cats-effect, circe, and scala test
