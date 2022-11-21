package org.eclipse.edc.matchers;

import org.eclipse.edc.catalog.spi.CatalogRequest;
import org.mockito.ArgumentMatcher;

public abstract class CatalogRequestMatcher implements ArgumentMatcher<CatalogRequest> {

    public static CatalogRequestMatcher sentTo(String recipientUrl) {
        return new CatalogRequestMatcher() {
            @Override
            public boolean matches(CatalogRequest argument) {
                return argument.getConnectorAddress().equals(recipientUrl);
            }
        };
    }
}
