package org.drools.cloudevents;

import java.util.HashMap;
import java.util.Map;

import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonFormat;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.kie.api.prototype.PrototypeFactInstance;
import org.kie.api.runtime.KieRuntimeBuilder;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.drools.cloudevents.JsonUtil.cloudEventToPrototypeFact;
import static org.drools.cloudevents.JsonUtil.createEvent;

@Path("drools")
public class RuleEngineResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleEngineResource.class);

    @Inject
    KieRuntimeBuilder runtimeBuilder;

    @POST
    @Path("evaluate")
    @Consumes({MediaType.APPLICATION_JSON, JsonFormat.CONTENT_TYPE})
    @Produces(MediaType.APPLICATION_JSON)
    public Response evaluate(CloudEvent event) {
        if (event == null || event.getData() == null) {
            throw new BadRequestException("Invalid data received. Null or empty event");
        }

        KieSession ksession = runtimeBuilder.newKieSession();
        Map<String, Object> results = new HashMap<>();
        ksession.setGlobal("results", results);

        PrototypeFactInstance fact = cloudEventToPrototypeFact(event);

        ksession.insert(fact);
        ksession.fireAllRules();

        System.out.println(results);

        return Response.ok(createEvent(results)).build();
    }
}
