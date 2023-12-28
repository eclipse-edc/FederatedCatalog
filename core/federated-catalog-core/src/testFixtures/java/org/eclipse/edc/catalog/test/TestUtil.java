/*
 *  Copyright (c) 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - Initial implementation
 *
 */

package org.eclipse.edc.catalog.test;

import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.DataService;
import org.eclipse.edc.catalog.spi.Dataset;
import org.eclipse.edc.catalog.spi.Distribution;
import org.eclipse.edc.crawler.spi.TargetNode;
import org.eclipse.edc.crawler.spi.WorkItem;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.types.domain.offer.ContractOffer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TestUtil {

    public static final String TEST_PROTOCOL = "test-protocol";

    public static WorkItem createWorkItem() {
        return new WorkItem("test-url", "test-protocol");
    }

    @NotNull
    public static ContractOffer createOffer(String id) {
        return ContractOffer.Builder.newInstance()
                .id(id)
                .assetId(id)
                .policy(Policy.Builder.newInstance().build())
                .build();
    }

    public static Catalog createCatalog(String id) {
        var dataService = DataService.Builder.newInstance().build();
        return Catalog.Builder.newInstance()
                .id(id)
                .contractOffers(List.of(createOffer("test-offer")))
                .dataServices(List.of(dataService))
                .datasets(List.of(Dataset.Builder.newInstance().distributions(List.of(Distribution.Builder.newInstance().dataService(dataService).format("test-format").build())).build()))
                .properties(new HashMap<>())
                .build();
    }

    @NotNull
    public static TargetNode createNode() {
        return new TargetNode("testnode" + UUID.randomUUID(), "http://test.com", List.of(TEST_PROTOCOL));
    }
}
