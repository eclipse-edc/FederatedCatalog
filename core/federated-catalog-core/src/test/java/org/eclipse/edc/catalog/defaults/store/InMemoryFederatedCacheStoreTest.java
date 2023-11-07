/*
 *  Copyright (c) 2020 - 2022 Microsoft Corporation
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

package org.eclipse.edc.catalog.defaults.store;


import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.CatalogConstants;
import org.eclipse.edc.catalog.store.InMemoryFederatedCacheStore;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.types.domain.asset.Asset;
import org.eclipse.edc.spi.types.domain.offer.ContractOffer;
import org.eclipse.edc.util.concurrency.LockManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryFederatedCacheStoreTest {

    private InMemoryFederatedCacheStore store;

    @BeforeEach
    public void setUp() {
        store = new InMemoryFederatedCacheStore(new LockManager(new ReentrantReadWriteLock()));
    }

    @Test
    void queryCacheContainingOneElementWithNoCriterion_shouldReturnUniqueElement() {
        var contractOfferId = UUID.randomUUID().toString();
        var assetId = UUID.randomUUID().toString();
        var catalogEntry = createEntry(contractOfferId, createAsset(assetId));

        store.save(catalogEntry);

        var result = store.query(Collections.emptyList());

        assertThat(result)
                .hasSize(1)
                .allSatisfy(co -> assertThat(co.getContractOffers().get(0).getAssetId()).isEqualTo(assetId));
    }

    @Test
    void queryCacheAfterInsertingSameAssetTwice_shouldReturnLastInsertedContractOfferOnly() {
        var contractOfferId1 = UUID.randomUUID().toString();
        var assetId = UUID.randomUUID().toString();
        var entry1 = createEntry(contractOfferId1, createAsset(assetId));
        var entry2 = createEntry(contractOfferId1, createAsset(assetId));

        store.save(entry1);
        store.save(entry2);

        var result = store.query(Collections.emptyList());

        assertThat(result)
                .hasSize(1)
                .allSatisfy(co -> {
                    assertThat(co.getId()).isEqualTo(contractOfferId1);
                    assertThat(co.getContractOffers().get(0).getAssetId()).isEqualTo(assetId);
                });
    }

    @Test
    void queryCacheContainingTwoDistinctAssets_shouldReturnBothContractOffers() {
        var contractOfferId1 = UUID.randomUUID().toString();
        var contractOfferId2 = UUID.randomUUID().toString();
        var assetId1 = UUID.randomUUID().toString();
        var assetId2 = UUID.randomUUID().toString();
        var entry1 = createEntry(contractOfferId1, createAsset(assetId1));
        var entry2 = createEntry(contractOfferId2, createAsset(assetId2));

        store.save(entry1);
        store.save(entry2);

        var result = store.query(Collections.emptyList());

        assertThat(result)
                .hasSize(2)
                .anySatisfy(co -> assertThat(co.getContractOffers().get(0).getAssetId()).isEqualTo(assetId1))
                .anySatisfy(co -> assertThat(co.getContractOffers().get(0).getAssetId()).isEqualTo(assetId2));
    }

    @Test
    void removedMarked_noneMarked() {
        var contractOfferId1 = UUID.randomUUID().toString();
        var contractOfferId2 = UUID.randomUUID().toString();
        var assetId1 = UUID.randomUUID().toString();
        var assetId2 = UUID.randomUUID().toString();
        var entry1 = createEntry(contractOfferId1, createAsset(assetId1));
        var entry2 = createEntry(contractOfferId2, createAsset(assetId2));

        store.save(entry1);
        store.save(entry2);

        assertThat(store.query(List.of())).hasSize(2);

        store.deleteExpired(); // none of them is marked, d
        assertThat(store.query(List.of())).containsExactlyInAnyOrder(entry1, entry2);

    }

    @Test
    void removedMarked_shouldDeleteMarked() {
        var contractOfferId1 = UUID.randomUUID().toString();
        var contractOfferId2 = UUID.randomUUID().toString();
        var assetId1 = UUID.randomUUID().toString();
        var assetId2 = UUID.randomUUID().toString();
        var entry1 = createEntry(contractOfferId1, createAsset(assetId1));
        var entry2 = createEntry(contractOfferId2, createAsset(assetId2));

        store.save(entry1);
        store.save(entry2);

        assertThat(store.query(List.of())).hasSize(2);

        store.expireAll(); // two items marked
        store.save(createEntry(UUID.randomUUID().toString(), createAsset(UUID.randomUUID().toString())));
        store.deleteExpired(); // should delete only marked items
        assertThat(store.query(List.of())).hasSize(1)
                .doesNotContain(entry1, entry2);

    }

    private Asset createAsset(String id) {
        return Asset.Builder.newInstance()
                .id(id)
                .build();
    }

    private ContractOffer createContractOffer(String id, Asset asset) {
        return ContractOffer.Builder.newInstance()
                .id(id)
                .assetId(asset.getId())
                .policy(Policy.Builder.newInstance().build())
                .build();
    }

    private Catalog createEntry(String id, Asset asset) {
        var offer = createContractOffer("offer-" + id, asset);
        return Catalog.Builder.newInstance().contractOffers(List.of(offer)).id(id)
                .property(CatalogConstants.PROPERTY_ORIGINATOR, "http://test.source/" + id).build();
    }

}
