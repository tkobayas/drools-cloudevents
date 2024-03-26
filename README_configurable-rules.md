## Configurable Rules
This drools-cloudevents "configurable-rules" version has a capability to load rules by mounting a rule directory to a container.

As an example, this project has `config-rules` directory which contains `*.drl.yaml` files.

To run the container with the configurable rules, you need to mount the `config-rules` directory to the container.

```sh
docker run -v ./config-rules/:/opt/rules/:ro -d -p 8080:8080 my-registry/my-group/drools-cloudevents:1.0-SNAPSHOT
```
(Confirm your image name with `docker images` command)

This `-v ./config-rules/:/opt/rules/:ro` option mounts the `config-rules` directory to the `/opt/rules` directory in the container, which is automatically loaded by the application at start-up. You can change the rule files when you restart the container.

If you don't provide the `-v` option, the container loads the default rules placed in the `src/main/jib/opt/rules` directory.

The same curl test command in `README.md` will get the different response by the configured rule.
```
...
{"configured rule result":"red!!"}
```
