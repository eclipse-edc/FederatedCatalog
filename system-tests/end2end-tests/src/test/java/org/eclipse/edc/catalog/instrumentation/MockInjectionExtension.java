package org.eclipse.edc.catalog.instrumentation;

import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.message.RemoteMessageDispatcher;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockInjectionExtension implements ServiceExtension {

    @Inject
    private RemoteMessageDispatcherRegistry registry;
    private RemoteMessageDispatcher dispatcher;

    @Override
    public void initialize(ServiceExtensionContext context) {
        registry.register(createDispatcher());
    }

    @Provider
    public RemoteMessageDispatcher createDispatcher() {
        if (dispatcher == null) {
            dispatcher = mock(RemoteMessageDispatcher.class);
            when(dispatcher.protocol()).thenReturn("ids-multipart");
        }

        return dispatcher;
    }
}
