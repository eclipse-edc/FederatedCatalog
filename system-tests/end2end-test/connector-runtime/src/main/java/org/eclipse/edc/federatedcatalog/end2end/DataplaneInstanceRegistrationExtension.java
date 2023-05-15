package org.eclipse.edc.federatedcatalog.end2end;


import org.eclipse.edc.connector.dataplane.selector.spi.instance.DataPlaneInstance;
import org.eclipse.edc.connector.dataplane.selector.spi.store.DataPlaneInstanceStore;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

public class DataplaneInstanceRegistrationExtension implements ServiceExtension {

    @Inject
    private DataPlaneInstanceStore dataPlaneInstanceStore;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var dpi = DataPlaneInstance.Builder.newInstance()
                .id("test-instance")
                .allowedDestType("test-dest-type")
                .allowedSourceType("test-src-type")
                .url("http://test.local")
                .build();
        dataPlaneInstanceStore.create(dpi).orElseThrow(f -> new EdcException(f.getFailureDetail()));
    }
}
