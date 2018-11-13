/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.client.consumer;

import eu.arrowhead.common.Message;
import eu.arrowhead.common.api.ArrowheadApplication;
import eu.arrowhead.common.api.ArrowheadSecurityContext;
import eu.arrowhead.common.api.clients.HttpClient;
import eu.arrowhead.common.api.clients.StaticHttpClient;
import eu.arrowhead.common.api.clients.core.OrchestrationClient;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.OrchestrationFlags;
import eu.arrowhead.common.model.ServiceRequestForm;
import org.joda.time.DateTime;

public class EnergyForecastConsumer extends ArrowheadApplication {
    public static void main(String[] args) {
        new EnergyForecastConsumer(args).start();
    }

    private EnergyForecastConsumer(String[] args) {
        super(args);
    }

    @Override
    protected void onStart() {
        final ArrowheadSecurityContext securityContext = ArrowheadSecurityContext.createFromProperties(true);
        final ArrowheadSystem me = ArrowheadSystem.createFromProperties();
        final OrchestrationClient orchestrationClient = OrchestrationClient.createFromProperties(securityContext);
        final ServiceRequestForm serviceRequestForm = new ServiceRequestForm.Builder(me)
                .requestedService("energy", "json", getProps().isSecure())
                .flag(OrchestrationFlags.Flags.OVERRIDE_STORE, true)
                .flag(OrchestrationFlags.Flags.PING_PROVIDERS, false)
                .flag(OrchestrationFlags.Flags.METADATA_SEARCH, true)
                .flag(OrchestrationFlags.Flags.ENABLE_INTER_CLOUD, false)
                .build();
        final HttpClient energyClient = orchestrationClient.buildClient(serviceRequestForm, new StaticHttpClient.Builder());

        final DateTime ts = DateTime.now().plusHours(1);
        final Message message = energyClient.get()
                .queryParam("building", 1)
                .queryParam("timestamp", ts.getMillis() / 1000)
                .send()
                .readEntity(Message.class);
        log.info("Got " + message.getEntry().size() + " entries.");
    }

    @Override
    protected void onStop() {

    }

}