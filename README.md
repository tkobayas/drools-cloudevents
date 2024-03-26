# Rule based CloudEvents processing

This is a sample project demonstrating how Drools can process CloudEvents, evaluating them against a set of rules expressed in a simple YAML format.

In order to implement your own business logic rewrite the file `rules.drl.yaml` under the `src/main/resources/org/drools/cloudevents` folder (or add any other file with `.drl.yaml` extension in that folder) and update the provided integration test accordingly.

## Running the example

To start the rule service:
```sh
mvn clean compile quarkus:dev
```

To test the service, open a new terminal and run:
```sh
curl -v http://localhost:8080/drools/evaluate \
  -H "Ce-Specversion: 1.0" \
  -H "Ce-Type: fact.Measurement" \
  -H "Ce-Source: io.cloudevents.examples/user" \
  -H "Ce-Id: 536808d3-88be-4077-9d7a-a3f162705f78" \
  -H "Content-Type: application/json" \
  -H "Ce-Subject: SUBJ-0001" \
  -d '{"id":"color","val":"red"}'
```

You should get a response:
```
...
{"color":"red"}
```

## Push the image to a private container registry

1. Start a private registry locally. For example, using https://hub.docker.com/_/registry
```sh
docker run -d -p 5000:5000 --name registry registry:2
```
For simplicity, the steps run the registry without any authentication. For production, you should secure the registry.

2. Edit application.properties. For example:
```properties
quarkus.container-image.build=true
quarkus.container-image.push=false
quarkus.container-image.registry=localhost:5000
quarkus.container-image.insecure=true
quarkus.container-image.group=my-private-group
quarkus.container-image.name=drools-cloudevents
quarkus.container-image.tag=latest

drools.prototypes=allowed
```
You may change `quarkus.container-image.group` and `quarkus.container-image.name` as you like.

3. Build and push the image
```sh
mvn clean package -Dquarkus.container-image.push=true
```

4. Run the registered image
```sh
docker run -d -p 8080:8080 localhost:5000/my-private-group/drools-cloudevents:latest
```

5. Test the service with the same curl command as described in "Running the example".

## Push the image to a public container registry (Docker Hub for example)

1. Create an account and confirm that you can log in.
```sh
docker login -u your_username -p your_password
```
It will store the credentials in `~/.docker/config.json` so that the maven build can use it.

2. Edit application.properties. For example:
```properties
quarkus.container-image.build=true
quarkus.container-image.push=true
quarkus.container-image.registry=docker.io
quarkus.container-image.group=your_username
quarkus.container-image.name=drools-cloudevents
quarkus.container-image.tag=latest

drools.prototypes=allowed
```
`quarkus.container-image.group` has to be the same as your docker hub username. You may change `quarkus.container-image.name` as you like.

3. Build and push the image
```sh
mvn clean package -Dquarkus.container-image.push=true
```

4. Run the registered image
```sh
docker run -d -p 8080:8080 your_username/drools-cloudevents:latest
```

5. Test the service with the same curl command as described in "Running the example".

**Note that the rule is built in the immutable image, so you will need to rebuild and push the image if you update the rule.**
