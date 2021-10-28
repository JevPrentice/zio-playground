This project is a sample for interacting with kafka using zio is based on https://blog.rockthejvm.com/zio-kafka/

###Start up environment:
```
docker compose up

```

###Destroy environment:
```
docker compose down --remove-orphans
```

### Create bash session inside kafka
```
docker exec -it broker bash
```

### Create topic
```
codekafka-topics --bootstrap-server localhost:9092 --topic updates --create
```

### Produce element to kafka

```
kafka-console-producer --topic updates --broker-list localhost:9092 --property parse.key=true --property key.separator=,
cce715dc-e818-4b02-a802-f31ee12c2b8b,{"players": [{"name": "ITA", "score": 0},{"name":"ENG", "score": 1}]}
```