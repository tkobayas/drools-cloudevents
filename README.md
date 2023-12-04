# Rule based CloudEvents processing

This is a sample projects demonstrating how Drools can process CloudEvents, evaluating them against a set of rules expressed in a simple YAML format.

In order to implement your own business logic rewrite the file `rules.drl.yaml` under the `src/main/resources/org/drools/cloudevents` folder (or add any other file with `.drl.yaml` extension in that folder) and update the provided integration test accordingly.