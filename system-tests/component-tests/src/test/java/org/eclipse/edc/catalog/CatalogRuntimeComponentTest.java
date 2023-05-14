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

package org.eclipse.edc.catalog;

import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.CatalogConstants;
import org.eclipse.edc.catalog.spi.CatalogRequestMessage;
import org.eclipse.edc.catalog.spi.FederatedCacheNode;
import org.eclipse.edc.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.junit.annotations.ComponentTest;
import org.eclipse.edc.junit.extensions.EdcExtension;
import org.eclipse.edc.spi.message.RemoteMessageDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.time.Duration.ofSeconds;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.catalog.TestFunctions.catalogBuilder;
import static org.eclipse.edc.catalog.TestFunctions.catalogOf;
import static org.eclipse.edc.catalog.TestFunctions.createOffer;
import static org.eclipse.edc.catalog.TestFunctions.emptyCatalog;
import static org.eclipse.edc.catalog.TestFunctions.insertSingle;
import static org.eclipse.edc.catalog.TestFunctions.queryCatalogApi;
import static org.eclipse.edc.catalog.TestFunctions.randomCatalog;
import static org.eclipse.edc.catalog.matchers.CatalogRequestMatcher.sentTo;
import static org.eclipse.edc.catalog.spi.CatalogConstants.PROPERTY_ORIGINATOR;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ComponentTest
@ExtendWith(EdcExtension.class)
public class CatalogRuntimeComponentTest {
    public static final String TEST_CATALOG_ID = "test-catalog-id";
    private static final Duration TEST_TIMEOUT = ofSeconds(10);

    @BeforeEach
    void setup(EdcExtension extension) {
        extension.setConfiguration(Map.of(
                // make sure only one crawl-run is performed
                "edc.catalog.cache.execution.period.seconds", "2",
                // number of crawlers will be limited by the number of crawl-targets
                "edc.catalog.cache.partition.num.crawlers", "10",
                // give the runtime time to set up everything
                "edc.catalog.cache.execution.delay.seconds", "1",
                "web.http.port", valueOf(TestFunctions.PORT),
                "web.http.path", TestFunctions.BASE_PATH
        ));
    }

    @Test
    @DisplayName("Crawl a single target, yields no results")
    void crawlSingle_noResults(RemoteMessageDispatcher dispatcher, FederatedCacheNodeDirectory directory) {
        // prepare node directory
        insertSingle(directory);
        // intercept request egress
        when(dispatcher.send(eq(Catalog.class), isA(CatalogRequestMessage.class)))
                .thenReturn(emptyCatalog());

        await().pollDelay(ofSeconds(1))
                .atMost(TEST_TIMEOUT)
                .untilAsserted(() -> {
                    var response = queryCatalogApi();
                    assertThat(response).hasSize(1);
                    assertThat(response).allSatisfy(c -> assertThat(c.getContractOffers()).isEmpty());
                });
    }

    @Test
    @DisplayName("Crawl a single target, yields some results")
    void crawlSingle_withResults(RemoteMessageDispatcher dispatcher, FederatedCacheNodeDirectory directory) {
        // prepare node directory
        insertSingle(directory);
        // intercept request egress
        when(dispatcher.send(eq(Catalog.class), isA(CatalogRequestMessage.class)))
                .thenReturn(randomCatalog(TEST_CATALOG_ID, 5))
                .thenReturn(emptyCatalog()); // this is important, otherwise there is an endless loop!

        await().pollDelay(ofSeconds(1))
                .atMost(TEST_TIMEOUT)
                .untilAsserted(() -> {
                    var catalogs = queryCatalogApi();
                    assertThat(catalogs).allSatisfy(c -> assertThat(c.getContractOffers()).hasSize(5));
                });
    }

    @Test
    @DisplayName("Crawl a single targets, > 100 results, needs paging")
    void crawlSingle_withPagedResults(RemoteMessageDispatcher dispatcher, FederatedCacheNodeDirectory directory) {
        // prepare node directory
        insertSingle(directory);

        // intercept request egress
        when(dispatcher.send(eq(Catalog.class), isA(CatalogRequestMessage.class)))
                .thenReturn(randomCatalog(TEST_CATALOG_ID, 100))
                .thenReturn(randomCatalog(TEST_CATALOG_ID, 100))
                .thenReturn(randomCatalog(TEST_CATALOG_ID, 50));

        await().pollDelay(ofSeconds(1))
                .atMost(TEST_TIMEOUT)
                .untilAsserted(() -> {
                    var catalogs = queryCatalogApi();
                    assertThat(catalogs.size()).isEqualTo(1);
                    assertThat(catalogs.get(0).getContractOffers()).hasSize(250);
                });
        verify(dispatcher, atLeast(3)).send(eq(Catalog.class), isA(CatalogRequestMessage.class));

    }

    @Test
    @DisplayName("Crawl a single target twice, emulate deletion of assets")
    void crawlSingle_withDeletions_shouldRemove(RemoteMessageDispatcher dispatcher, FederatedCacheNodeDirectory directory) {
        // prepare node directory
        insertSingle(directory);

        // intercept request egress
        when(dispatcher.send(eq(Catalog.class), isA(CatalogRequestMessage.class)))
                .thenReturn(completedFuture(catalogBuilder().id(TEST_CATALOG_ID).contractOffers(new ArrayList<>(List.of(
                        createOffer("offer1"), createOffer("offer2"), createOffer("offer3")
                ))).build()))
                .thenReturn(emptyCatalog(TEST_CATALOG_ID))
                .thenReturn(completedFuture(catalogBuilder().id(TEST_CATALOG_ID).contractOffers(new ArrayList<>(List.of(
                        createOffer("offer1"), createOffer("offer2")/* this one is "deleted": createOffer("offer3") */
                ))).build()));

        await().pollDelay(ofSeconds(1))
                .atMost(TEST_TIMEOUT)
                .untilAsserted(() -> {
                    var catalogs = queryCatalogApi();
                    assertThat(catalogs).hasSize(1);
                    assertThat(catalogs.get(0).getContractOffers()).hasSize(2)
                            .noneMatch(offer -> offer.getId().equals("offer3"));
                    verify(dispatcher, atLeast(4)).send(eq(Catalog.class), isA(CatalogRequestMessage.class));
                });

    }

    @Test
    @DisplayName("Crawl a single target twice, emulate deleting and re-adding of assets with same ID")
    void crawlSingle_withUpdates_shouldReplace(RemoteMessageDispatcher dispatcher, FederatedCacheNodeDirectory directory) {
        // prepare node directory
        insertSingle(directory);

        // intercept request egress
        when(dispatcher.send(eq(Catalog.class), isA(CatalogRequestMessage.class)))
                .thenReturn(completedFuture(catalogBuilder().id(TEST_CATALOG_ID).contractOffers(new ArrayList<>(List.of(
                        createOffer("offer1"), createOffer("offer2"), createOffer("offer3")
                ))).build()))
                .thenReturn(emptyCatalog(TEST_CATALOG_ID))
                .thenReturn(completedFuture(catalogBuilder().id(TEST_CATALOG_ID).contractOffers(new ArrayList<>(List.of(
                        createOffer("offer1"), createOffer("offer2"), createOffer("offer3")
                ))).build()));

        await().pollDelay(ofSeconds(1))
                .atMost(TEST_TIMEOUT)
                .untilAsserted(() -> {
                    var catalogs = queryCatalogApi();
                    assertThat(catalogs).hasSize(1);
                    assertThat(catalogs.get(0).getContractOffers()).hasSize(3);
                    verify(dispatcher, atLeast(4)).send(eq(Catalog.class), isA(CatalogRequestMessage.class));
                });

    }

    @Test
    @DisplayName("Crawl a single target twice, emulate addition of assets")
    void crawlSingle_withAdditions_shouldAdd(RemoteMessageDispatcher dispatcher, FederatedCacheNodeDirectory directory) {
        // prepare node directory
        insertSingle(directory);

        // intercept request egress
        when(dispatcher.send(eq(Catalog.class), isA(CatalogRequestMessage.class)))
                .thenReturn(completedFuture(catalogBuilder().id("test-cat").contractOffers(new ArrayList<>(List.of(
                        createOffer("offer1"), createOffer("offer2"), createOffer("offer3")
                ))).build()))
                .thenReturn(completedFuture(catalogBuilder().id("test-cat").contractOffers(new ArrayList<>(List.of(
                        createOffer("offer1"), createOffer("offer2"), createOffer("offer3"), createOffer("offer4"), createOffer("offer5")
                ))).build()));

        await().pollDelay(ofSeconds(1))
                .atMost(TEST_TIMEOUT)
                .untilAsserted(() -> {
                    var catalogs = queryCatalogApi();
                    assertThat(catalogs).hasSize(1);
                    assertThat(catalogs)
                            .allSatisfy(cat -> assertThat(cat.getContractOffers()).hasSize(5))
                            .allSatisfy(co -> assertThat(Integer.parseInt(co.getContractOffers().get(0).getId().replace("offer", ""))).isIn(1, 2, 3, 4, 5));
                    verify(dispatcher, atLeast(2)).send(eq(Catalog.class), isA(CatalogRequestMessage.class));
                });

    }

    @Test
    @DisplayName("Crawl a single target, verify that the originator information is properly inserted")
    void crawlSingle_verifyCorrectOriginator(RemoteMessageDispatcher dispatcher, FederatedCacheNodeDirectory directory) {
        // prepare node directory
        insertSingle(directory);
        // intercept request egress
        when(dispatcher.send(eq(Catalog.class), isA(CatalogRequestMessage.class)))
                .thenReturn(randomCatalog(TEST_CATALOG_ID, 5))
                .thenReturn(emptyCatalog()); // this is important, otherwise there is an endless loop!

        await().pollDelay(ofSeconds(1))
                .atMost(TEST_TIMEOUT)
                .untilAsserted(() -> {
                    var catalogs = queryCatalogApi();
                    assertThat(catalogs).hasSize(1);
                    assertThat(catalogs.get(0).getContractOffers()).hasSize(5);
                    assertThat(catalogs).extracting(Catalog::getProperties).allSatisfy(a -> assertThat(a).containsEntry(PROPERTY_ORIGINATOR, "http://test-node.com/api/v1/ids/data"));
                });
    }


    @Test
    @DisplayName("Crawl 1000 targets, verify that all offers are collected")
    void crawlMany_shouldCollectAll(RemoteMessageDispatcher dispatcher, FederatedCacheNodeDirectory directory) {

        var numTotalAssets = new AtomicInteger();
        var rnd = new SecureRandom();

        // create 1000 crawl targets, setup dispatcher mocks for them
        range(0, 1000)
                .forEach(i -> {
                    var nodeUrl = format("http://test-node%s.com", i);
                    var node = new FederatedCacheNode("test-node" + i, nodeUrl, singletonList(CatalogConstants.IDS_MULTIPART_PROTOCOL));
                    directory.insert(node);

                    var numAssets = rnd.nextInt(50);

                    when(dispatcher.send(eq(Catalog.class), argThat(sentTo(nodeUrl + "/api/v1/ids/data"))))
                            .thenReturn(randomCatalog("catalog-" + nodeUrl, numAssets));
                    numTotalAssets.addAndGet(numAssets);
                });

        await().pollDelay(ofSeconds(1))
                .atMost(TEST_TIMEOUT)
                .untilAsserted(() -> {
                    var catalogs = queryCatalogApi();
                    assertThat(catalogs).hasSize(1000);
                    //assert that the total number of offers across all catalogs is corrects
                    assertThat(catalogs.stream().mapToLong(c -> c.getContractOffers().size()).sum()).isEqualTo(numTotalAssets.get());
                });
    }

    @Test
    @DisplayName("Crawl multiple targets with conflicting asset IDs")
    void crawlMultiple_whenConflictingAssetIds_shouldOverwrite(RemoteMessageDispatcher dispatcher, FederatedCacheNodeDirectory directory) {
        var node1 = new FederatedCacheNode("test-node1", "http://test-node1.com", singletonList(CatalogConstants.IDS_MULTIPART_PROTOCOL));
        var node2 = new FederatedCacheNode("test-node2", "http://test-node2.com", singletonList(CatalogConstants.IDS_MULTIPART_PROTOCOL));

        directory.insert(node1);
        directory.insert(node2);

        when(dispatcher.send(eq(Catalog.class), argThat(sentTo("http://test-node1.com/api/v1/ids/data"))))
                .thenReturn(catalogOf("catalog-" + node1.getTargetUrl(), createOffer("offer1"), createOffer("offer2"), createOffer("offer3")))
                .thenReturn(emptyCatalog());

        when(dispatcher.send(eq(Catalog.class), argThat(sentTo("http://test-node2.com/api/v1/ids/data"))))
                .thenReturn(catalogOf("catalog-" + node2.getTargetUrl(), createOffer("offer14"), createOffer("offer32"), /*this one is conflicting:*/createOffer("offer3")))
                .thenReturn(emptyCatalog());

        await().pollDelay(ofSeconds(1))
                .atMost(TEST_TIMEOUT)
                .untilAsserted(() -> {
                    var catalogs = queryCatalogApi();
                    assertThat(catalogs).hasSize(2);
                    assertThat(catalogs).anySatisfy(c -> assertThat(c.getProperties().get(PROPERTY_ORIGINATOR).toString()).startsWith("http://test-node1.com"));
                    assertThat(catalogs).anySatisfy(c -> assertThat(c.getProperties().get(PROPERTY_ORIGINATOR).toString()).startsWith("http://test-node2.com"));
                    assertThat(catalogs.stream().mapToLong(c -> c.getContractOffers().size()).sum()).isEqualTo(6);
                    assertThat(catalogs.stream().flatMap(c -> c.getContractOffers().stream()).map(ContractOffer::getAssetId))
                            .containsExactlyInAnyOrder("offer1", "offer2", "offer3", "offer14", "offer32", "offer3");
                });


    }


}
