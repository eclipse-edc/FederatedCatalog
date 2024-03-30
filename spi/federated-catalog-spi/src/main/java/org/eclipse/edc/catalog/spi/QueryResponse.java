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
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.catalog.spi;

import org.eclipse.edc.connector.controlplane.catalog.spi.Catalog;

import java.util.ArrayList;
import java.util.List;

public class QueryResponse {
    private Status status;
    private List<String> errors = new ArrayList<>();
    private List<Catalog> catalogs = new ArrayList<>();

    private QueryResponse(Status status) {
        this.status = status;
        errors = new ArrayList<>();
    }

    public QueryResponse() {

    }

    public static QueryResponse ok(List<Catalog> result) {
        return Builder.newInstance()
                .status(Status.ACCEPTED)
                .catalogs(result)
                .build();
    }

    public List<Catalog> getCatalogs() {
        return catalogs;
    }

    public Status getStatus() {
        return status;
    }

    public List<String> getErrors() {
        return errors;
    }

    public enum Status {
        ACCEPTED,
        NO_ADAPTER_FOUND
    }

    public static final class Builder {

        private final QueryResponse response;

        private Builder() {
            response = new QueryResponse();
            response.status = Status.ACCEPTED; //that's the default
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder catalogs(List<Catalog> catalogs) {
            response.catalogs = catalogs;
            return this;
        }

        public Builder status(Status status) {
            response.status = status;
            return this;
        }

        public QueryResponse build() {
            return response;
        }

        public Builder error(String error) {
            response.errors.add(error);
            return this;
        }
    }
}
