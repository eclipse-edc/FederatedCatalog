/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.catalog.api.query;

import org.eclipse.edc.catalog.spi.QueryEngine;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.health.HealthCheckResult;
import org.eclipse.edc.spi.system.health.HealthCheckService;
import org.eclipse.edc.web.spi.WebService;
import org.jetbrains.annotations.NotNull;

@Extension(value = FederatedCatalogCacheQueryApiExtension.NAME)
public class FederatedCatalogCacheQueryApiExtension implements ServiceExtension {

    public static final String NAME = "Cache Query API Extension";
    @Setting(value = "Provide a context alias in which the FCC API is registered. Defaults to \"management\".")
    public static final String SETTING_API_CONTEXT_ALIAS = "edc.federated-catalog.api.context";
    private static final String DEFAULT_CONTEXT = "default";
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
        webService.registerResource(getContextAlias(context), catalogController);

        // contribute to the liveness probe
        if (healthCheckService != null) {
            var successResult = HealthCheckResult.Builder.newInstance().component("FCC Query API").build();
            healthCheckService.addReadinessProvider(() -> successResult);
            healthCheckService.addLivenessProvider(() -> successResult);
        }
    }

    @NotNull
    private String getContextAlias(ServiceExtensionContext context) {
        return context.getSetting(SETTING_API_CONTEXT_ALIAS, DEFAULT_CONTEXT);
    }

}
