package org.drools.cloudevents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonFormat;
import org.kie.api.runtime.KieRuntimeBuilder;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.drools.cloudevents.JsonUtil.createEvent;
import static org.drools.cloudevents.JsonUtil.readValueAsMapOfStringAndObject;

@Path("/drools")
@Consumes({MediaType.APPLICATION_JSON, JsonFormat.CONTENT_TYPE})
@Produces(MediaType.APPLICATION_JSON)
public class RuleEngineResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleEngineResource.class);

    @Inject
    KieRuntimeBuilder runtimeBuilder;

    @POST
    @Path("evaluate")
    public Response evaluate(CloudEvent event) {
        if (event == null || event.getData() == null) {
            throw new BadRequestException("Invalid data received. Null or empty event");
        }

        KieSession ksession = runtimeBuilder.newKieSession();
        List<String> results = new ArrayList<>();
        ksession.setGlobal("results", results);

        Map<String, Object> map = readValueAsMapOfStringAndObject(new String(event.getData().toBytes()));
        ksession.insert(map);
        ksession.fireAllRules();

        return Response.ok(createEvent(results)).build();
    }
}
