package com.willbroadbelt.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.willbroadbelt.db.AthleteDatabase;
import org.openapitools.client.ApiClient;
import org.openapitools.client.api.ActivitiesApi;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Flux;
import com.willbroadbelt.server.actions.EventAction;
import com.willbroadbelt.strava.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Used to process new events sent from the Strava webhooks
 * Can register any number of EventActions that will be called when new events are processed
 *
 * TODO: Rework this with mult-threads and an event queue so the processing is non-blocking
 */
public class WebhookProcessor {

    private AthleteDatabase athleteDb;

    private List<EventAction> actions = new ArrayList();

    private final Logger logger = Logger.getLogger(WebhookProcessor.class.getName());


    public WebhookProcessor(final AthleteDatabase athleteDb) {
        this.athleteDb = athleteDb;
    }

    public WebhookProcessor registerEventAction(EventAction action) {
        logger.fine("Registered: " + action.toString());

        actions.add(action);
        return this;
    }

    public void processEvent(final Event event) {
        // Not dealing with athlete CRUD events, for now
        if (event.objectType().equals(Event.ObjectType.ATHLETE)) return;
        Flux.fromIterable(actions)
                .filter(a -> a.getRequiredAspectTypes().contains(event.aspectType()))
                .subscribe(a -> {
                    logger.info("Event will be processed");
                    var record = athleteDb.getAthleteById(event.ownerId());

                    var apiClient = configuredClient();
                    apiClient.setAccessToken(record.accessToken());

                    var api = new ActivitiesApi(apiClient);
                    var changedItem = api.getActivityById(event.objectId(), false);

                    logger.info(changedItem.toString());

                    a.setApiClient(apiClient);
                    a.doAction(event, changedItem);
                });
    }

    /**
     * Have to custom configure the ApiClient ObjectMapper not to send null values
     */
    private ApiClient configuredClient() {
        var objectMapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());

        var httpMessageConverter = new MappingJackson2HttpMessageConverter(objectMapper);

        RestTemplate restTemplate = new RestTemplate();

        // disable default URL encoding
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        restTemplate.setUriTemplateHandler(uriBuilderFactory);

        restTemplate.getMessageConverters().add(0, httpMessageConverter);

        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(restTemplate.getRequestFactory()));

        return new ApiClient(restTemplate);
    }


}
