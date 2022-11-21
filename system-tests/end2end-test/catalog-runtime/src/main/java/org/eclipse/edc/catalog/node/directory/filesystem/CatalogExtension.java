package org.eclipse.edc.catalog.node.directory.filesystem;

import org.eclipse.edc.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.util.concurrency.LockManager;

import java.io.File;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

public class CatalogExtension implements ServiceExtension {

    private static final String FILE_LOCATION_SETTING = "fcc.directory.file";

    @Inject
    private RemoteMessageDispatcherRegistry registry;

    @Inject
    private TypeManager typeManager;
    private File nodeFile;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var setting = ofNullable(context.getSetting(FILE_LOCATION_SETTING, null))
                .orElseThrow(() -> new EdcException(format("Config property [%s] not found, will ABORT!", FILE_LOCATION_SETTING)));

        nodeFile = new File(setting);

    }

    @Provider
    public FederatedCacheNodeDirectory createFileSystemDirectory() {
        return new FileBasedNodeDirectory(nodeFile, new LockManager(new ReentrantReadWriteLock()), typeManager.getMapper());
    }

}
