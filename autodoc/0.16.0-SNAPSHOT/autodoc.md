Module `connector-runtime`
--------------------------
**Artifact:** org.eclipse.edc:connector-runtime:0.16.0-SNAPSHOT

**Categories:** _None_

### Extension points
_None_

### Extensions
#### Class: `org.eclipse.edc.federatedcatalog.end2end.DataplaneInstanceRegistrationExtension`
**Name:** "DataplaneInstanceRegistrationExtension"

### Configuration_None_

#### Provided services
- `org.eclipse.edc.connector.dataplane.selector.spi.client.DataPlaneClientFactory`

#### Referenced (injected) services
- `org.eclipse.edc.connector.dataplane.selector.spi.store.DataPlaneInstanceStore` (required)

Module `crawler-spi`
--------------------
**Name:** Crawler services
**Artifact:** org.eclipse.edc:crawler-spi:0.16.0-SNAPSHOT

**Categories:** _None_

### Extension points
  - `org.eclipse.edc.crawler.spi.TargetNodeFilter`
  - `org.eclipse.edc.crawler.spi.TargetNodeDirectory`

### Extensions
Module `federated-catalog-api`
------------------------------
**Artifact:** org.eclipse.edc:federated-catalog-api:0.16.0-SNAPSHOT

**Categories:** _None_

### Extension points
_None_

### Extensions
#### Class: `org.eclipse.edc.catalog.api.query.FederatedCatalogApiExtension`
**Name:** "Cache Query API Extension"

**Overview:** No overview provided.


### Configuration

| Key                     | Required | Type     | Default        | Pattern | Min | Max | Description                  |
| ----------------------- | -------- | -------- | -------------- | ------- | --- | --- | ---------------------------- |
| `web.http.catalog.port` | `*`      | `string` | `17171`        |         |     |     | Port for catalog api context |
| `web.http.catalog.path` | `*`      | `string` | `/api/catalog` |         |     |     | Path for catalog api context |

#### Provided services
_None_

#### Referenced (injected) services
- `org.eclipse.edc.web.spi.WebService` (required)
- `org.eclipse.edc.catalog.spi.QueryService` (required)
- `org.eclipse.edc.spi.system.health.HealthCheckService` (optional)
- `org.eclipse.edc.jsonld.spi.JsonLd` (required)
- `org.eclipse.edc.spi.types.TypeManager` (required)
- `org.eclipse.edc.transform.spi.TypeTransformerRegistry` (required)
- `org.eclipse.edc.spi.system.apiversion.ApiVersionService` (required)
- `org.eclipse.edc.web.spi.configuration.PortMappingRegistry` (required)

Module `federated-catalog-cache-sql`
------------------------------------
**Artifact:** org.eclipse.edc:federated-catalog-cache-sql:0.16.0-SNAPSHOT

**Categories:** _None_

### Extension points
_None_

### Extensions
#### Class: `org.eclipse.edc.catalog.cache.sql.SqlFederatedCatalogCacheExtension`
**Name:** "SQL federated catalog cache"

**Overview:** No overview provided.


### Configuration

| Key                                         | Required | Type     | Default   | Pattern | Min | Max | Description               |
| ------------------------------------------- | -------- | -------- | --------- | ------- | --- | --- | ------------------------- |
| `edc.sql.store.federatedcatalog.datasource` | `*`      | `string` | `default` |         |     |     | The datasource to be used |

#### Provided services
- `org.eclipse.edc.catalog.spi.FederatedCatalogCache`

#### Referenced (injected) services
- `org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry` (required)
- `org.eclipse.edc.transaction.spi.TransactionContext` (required)
- `org.eclipse.edc.catalog.cache.sql.FederatedCatalogCacheStatements` (optional)
- `org.eclipse.edc.spi.types.TypeManager` (required)
- `org.eclipse.edc.sql.QueryExecutor` (required)
- `org.eclipse.edc.sql.bootstrapper.SqlSchemaBootstrapper` (required)

Module `federated-catalog-core`
-------------------------------
**Artifact:** org.eclipse.edc:federated-catalog-core:0.16.0-SNAPSHOT

**Categories:** _None_

### Extension points
_None_

### Extensions
#### Class: `org.eclipse.edc.catalog.cache.FederatedCatalogDefaultServicesExtension`
**Name:** "FederatedCatalogDefaultServicesExtension"

**Overview:** No overview provided.


### Configuration

| Key                                          | Required | Type     | Default | Pattern | Min | Max | Description                                                                                                     |
| -------------------------------------------- | -------- | -------- | ------- | ------- | --- | --- | --------------------------------------------------------------------------------------------------------------- |
| `edc.catalog.cache.execution.enabled`        | `*`      | `string` | `true`  |         |     |     | Determines whether catalog crawling is globally enabled or disabled                                             |
| `edc.catalog.cache.partition.num.crawlers`   | `*`      | `string` | `2`     |         |     |     | The number of crawlers (execution threads) that should be used. The engine will re-use crawlers when necessary. |
| `edc.catalog.cache.execution.period.seconds` | `*`      | `string` | `60`    |         |     |     | The time to elapse between two crawl runs                                                                       |
| `edc.catalog.cache.execution.delay.seconds`  |          | `string` | `0`     |         |     |     | The initial delay for the cache crawler engine                                                                  |
| `edc.catalog.cache.retry.retries.max`        | `*`      | `string` | `5`     |         |     |     | How many retries will be executed in case of crawler failure                                                    |
| `edc.catalog.cache.retry.delay.seconds`      | `*`      | `string` | `10`    |         |     |     | How many seconds the crawler will await after a failure before retry                                            |

#### Provided services
- `org.eclipse.edc.catalog.spi.FederatedCatalogCache`
- `org.eclipse.edc.crawler.spi.TargetNodeDirectory`
- `org.eclipse.edc.catalog.spi.QueryService`
- `org.eclipse.edc.crawler.spi.CrawlerActionRegistry`
- `org.eclipse.edc.crawler.spi.model.ExecutionPlan`

#### Referenced (injected) services
- `org.eclipse.edc.catalog.spi.FederatedCatalogCache` (required)

#### Class: `org.eclipse.edc.catalog.cache.FederatedCatalogCoreServicesExtension`
**Name:** "Federated Catalog Core"

**Overview:** No overview provided.


### Configuration

| Key                                          | Required | Type     | Default | Pattern | Min | Max | Description                                                                                                     |
| -------------------------------------------- | -------- | -------- | ------- | ------- | --- | --- | --------------------------------------------------------------------------------------------------------------- |
| `edc.catalog.cache.execution.enabled`        | `*`      | `string` | `true`  |         |     |     | Determines whether catalog crawling is globally enabled or disabled                                             |
| `edc.catalog.cache.partition.num.crawlers`   | `*`      | `string` | `2`     |         |     |     | The number of crawlers (execution threads) that should be used. The engine will re-use crawlers when necessary. |
| `edc.catalog.cache.execution.period.seconds` | `*`      | `string` | `60`    |         |     |     | The time to elapse between two crawl runs                                                                       |
| `edc.catalog.cache.execution.delay.seconds`  |          | `string` | `0`     |         |     |     | The initial delay for the cache crawler engine                                                                  |
| `edc.catalog.cache.retry.retries.max`        | `*`      | `string` | `5`     |         |     |     | How many retries will be executed in case of crawler failure                                                    |
| `edc.catalog.cache.retry.delay.seconds`      | `*`      | `string` | `10`    |         |     |     | How many seconds the crawler will await after a failure before retry                                            |

#### Provided services
_None_

#### Referenced (injected) services
- `org.eclipse.edc.crawler.spi.model.ExecutionPlan` (optional)
- `org.eclipse.edc.catalog.spi.FederatedCatalogCache` (required)
- `org.eclipse.edc.crawler.spi.CrawlerActionRegistry` (required)
- `org.eclipse.edc.crawler.spi.TargetNodeDirectory` (required)
- `org.eclipse.edc.crawler.spi.TargetNodeFilter` (optional)
- `org.eclipse.edc.spi.system.health.HealthCheckService` (optional)

Module `federated-catalog-core-2025`
------------------------------------
**Artifact:** org.eclipse.edc:federated-catalog-core-2025:0.16.0-SNAPSHOT

**Categories:** _None_

### Extension points
_None_

### Extensions
#### Class: `org.eclipse.edc.catalog.cache.FederatedCatalogCacheExtension`
**Name:** "Federated Catalog Cache DSP 2025/1"

**Overview:** No overview provided.


### Configuration_None_

#### Provided services
_None_

#### Referenced (injected) services
- `org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry` (required)
- `org.eclipse.edc.crawler.spi.CrawlerActionRegistry` (required)
- `org.eclipse.edc.spi.types.TypeManager` (required)
- `org.eclipse.edc.participant.spi.ParticipantIdMapper` (required)
- `org.eclipse.edc.transform.spi.TypeTransformerRegistry` (required)
- `org.eclipse.edc.jsonld.spi.JsonLd` (required)
- `org.eclipse.edc.transform.spi.TypeTransformerRegistry` (required)
- `org.eclipse.edc.participantcontext.single.spi.SingleParticipantContextSupplier` (required)

Module `federated-catalog-spi`
------------------------------
**Name:** Catalog services
**Artifact:** org.eclipse.edc:federated-catalog-spi:0.16.0-SNAPSHOT

**Categories:** _None_

### Extension points
  - `org.eclipse.edc.catalog.spi.FederatedCatalogCache`

### Extensions
Module `target-node-directory-sql`
----------------------------------
**Artifact:** org.eclipse.edc:target-node-directory-sql:0.16.0-SNAPSHOT

**Categories:** _None_

### Extension points
_None_

### Extensions
#### Class: `org.eclipse.edc.catalog.directory.sql.SqlTargetNodeDirectoryExtension`
**Name:** "SQL target node directory"

**Overview:** No overview provided.


### Configuration

| Key                                            | Required | Type     | Default   | Pattern | Min | Max | Description               |
| ---------------------------------------------- | -------- | -------- | --------- | ------- | --- | --- | ------------------------- |
| `edc.sql.store.targetnodedirectory.datasource` | `*`      | `string` | `default` |         |     |     | The datasource to be used |

#### Provided services
- `org.eclipse.edc.crawler.spi.TargetNodeDirectory`

#### Referenced (injected) services
- `org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry` (required)
- `org.eclipse.edc.transaction.spi.TransactionContext` (required)
- `org.eclipse.edc.catalog.directory.sql.TargetNodeStatements` (optional)
- `org.eclipse.edc.spi.types.TypeManager` (required)
- `org.eclipse.edc.sql.QueryExecutor` (required)
- `org.eclipse.edc.sql.bootstrapper.SqlSchemaBootstrapper` (required)

