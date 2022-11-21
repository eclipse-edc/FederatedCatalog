package org.eclipse.edc.catalog.node.directory.filesystem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.catalog.spi.FederatedCacheNode;
import org.eclipse.edc.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.util.concurrency.LockManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileBasedNodeDirectory implements FederatedCacheNodeDirectory {

    private final List<FederatedCacheNode> nodes = new ArrayList<>();
    private final LockManager lockManager;
    private final ObjectMapper objectMapper;

    public FileBasedNodeDirectory(File nodeFile, LockManager lockManager, ObjectMapper objectMapper) {

        this.lockManager = lockManager;
        this.objectMapper = objectMapper;
        readAll(nodeFile);
    }

    @Override
    public List<FederatedCacheNode> getAll() {
        return lockManager.readLock(() -> nodes);
    }

    @Override
    public void insert(FederatedCacheNode node) {
        lockManager.writeLock(() -> nodes.add(node));
    }

    private void readAll(File nodeFile) {
        try {
            var content = Files.readString(nodeFile.toPath());
            var n = objectMapper.readValue(content, new TypeReference<List<FederatedCacheNode>>() {
            });
            nodes.addAll(n);
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }
}
