package org.eclipse.edc.catalog.api.query;

import org.eclipse.edc.catalog.spi.QueryEngine;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.health.HealthCheckResult;
import org.eclipse.edc.spi.system.health.HealthCheckService;
import org.eclipse.edc.web.spi.WebService;

@Extension(value = FederatedCatalogCacheQueryApiExtension.NAME)
public class FederatedCatalogCacheQueryApiExtension implements ServiceExtension {

    public static final String NAME = "Cache Query API Extension";

    @Inject
    private WebService webService;

    @Inject
    private QueryEngine queryEngine;

    @Inject(required = false)
    private HealthCheckService healthCheckService;

    @Override
    public String name() {
        return NAME;
    }


    @Override
    public void initialize(ServiceExtensionContext context) {
        var catalogController = new FederatedCatalogApiController(queryEngine);
        webService.registerResource(catalogController);

        // contribute to the liveness probe
        if (healthCheckService != null) {
            healthCheckService.addReadinessProvider(() -> HealthCheckResult.Builder.newInstance().component("FCC Query API").build());
        }
    }

}
