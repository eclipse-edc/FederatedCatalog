/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.catalog.cache.query;

import org.eclipse.edc.connector.controlplane.catalog.spi.Catalog;
import org.eclipse.edc.connector.controlplane.catalog.spi.Dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.Optional.ofNullable;

class CatalogUtil {
    public static Catalog merge(Catalog destination, Catalog source) {
        destination.getDatasets().addAll(source.getDatasets());
        destination.getDataServices().addAll(source.getDataServices());
        destination.getProperties().putAll(source.getProperties());
        destination.getDistributions().addAll(source.getDistributions());
        return destination;
    }

    public static Catalog.Builder copyCatalog(Catalog catalog) {
        return Catalog.Builder.newInstance().id(catalog.getId())
                .participantId(catalog.getParticipantId())
                .properties(ofNullable(catalog.getProperties()).orElseGet(HashMap::new))
                .dataServices(ofNullable(catalog.getDataServices()).orElseGet(ArrayList::new))
                .datasets(ofNullable(catalog.getDatasets()).orElseGet(ArrayList::new));
    }

    public static Catalog.Builder copyCatalog(Catalog catalog, List<Dataset> datasets) {
        return Catalog.Builder.newInstance().id(catalog.getId())
                .participantId(catalog.getParticipantId())
                .properties(ofNullable(catalog.getProperties()).orElseGet(HashMap::new))
                .dataServices(ofNullable(catalog.getDataServices()).orElseGet(ArrayList::new))
                .datasets(datasets);
    }
}
