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

package org.eclipse.edc.catalog.store;

import org.eclipse.edc.catalog.spi.CatalogConstants;
import org.eclipse.edc.catalog.spi.FederatedCacheStore;
import org.eclipse.edc.connector.controlplane.catalog.spi.Catalog;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.query.CriterionOperatorRegistry;
import org.eclipse.edc.util.concurrency.LockManager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * An ephemeral in-memory cache store.
 */
public class InMemoryFederatedCacheStore implements FederatedCacheStore {

    private final Map<String, MarkableEntry<Catalog>> cache = new ConcurrentHashMap<>();
    private final CriterionOperatorRegistry criterionOperatorRegistry;
    private final LockManager lockManager;

    public InMemoryFederatedCacheStore(LockManager lockManager, CriterionOperatorRegistry criterionOperatorRegistry) {
        this.criterionOperatorRegistry = criterionOperatorRegistry;
        this.lockManager = lockManager;
    }

    @Override
    public void save(Catalog catalog) {
        lockManager.writeLock(() -> {
            var id = ofNullable(catalog.getProperties().get(CatalogConstants.PROPERTY_ORIGINATOR))
                    .map(Object::toString)
                    .orElse(catalog.getId());
            return cache.put(id, new MarkableEntry<>(false, catalog));
        });
    }

    @Override
    public Collection<Catalog> query(List<Criterion> query) {
        //AND all predicates
        var rootPredicate = query.stream()
                .map(criterionOperatorRegistry::toPredicate)
                .reduce(x -> true, Predicate::and);
        return lockManager.readLock(() -> cache.values().stream().map(MarkableEntry::getEntry).filter(rootPredicate).collect(Collectors.toList()));
    }

    @Override
    public void deleteExpired() {
        lockManager.writeLock(() -> {
            cache.values().removeIf(MarkableEntry::isMarked);
            return null;
        });
    }

    @Override
    public void expireAll() {
        cache.replaceAll((k, v) -> new MarkableEntry<>(true, v.getEntry()));
    }

    private static class MarkableEntry<B> {
        private final B entry;
        private final boolean mark;

        MarkableEntry(boolean isMarked, B catalog) {
            entry = catalog;
            mark = isMarked;
        }

        public boolean isMarked() {
            return mark;
        }

        public B getEntry() {
            return entry;
        }

    }
}
