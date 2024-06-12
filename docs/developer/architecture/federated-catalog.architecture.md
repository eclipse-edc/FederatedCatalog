# Federated Catalog Architecture

## General thoughts

- The Federated Catalog (FC) is the one that does the crawling (Federated Catalog Crawler, FCC) and that goes to
  Target Catalog Nodes (TCN) to get their catalog. The Federated Catalog exposes the Catalog API to client apps such
  as web UIs
- The Target Catalog Node (TCN) hosts the catalog and it responds to DSP catalog requests. Typically, TCNs are EDC
  connectors.
- Both the FCC and TCN are thought of as logical components (similar to the EDC) rather than physical things. They can
  be embedded in a runtime via extensions, deployed alongside it as standalone runtimes or be deployed completely
  separate (even in a different environment). It's also possible to deploy a runtime that hosts both an TCN and an FCC.
- we distinguish between a catalog _query_ and a catalog _update request_: the former is coming from a client
  application through the Catalog API, e.g. from a UI application that wants to get the entire catalog, whereas the
  latter (_update request_) is a DSP request issued by the crawler to other TCNs

## TCN endpoint resolution

The Federated Catalog requires a list of Target Catalog Nodes, so it knows _which endpoints to crawl_. This list is
provided by the `TargetNodeDirectory`. During the preparation of a crawl run, the `ExecutionManager` queries that
directory and obtains the list of TCNs.

In the simplest of cases, the `TargetNodeDirectory` is backed by a static file, but more complex implementations such as
a centralized participant registry are conceivable.

## Terminology

- TCN: Target Catalog Node - serves DSP catalog requests.
- FC: Federated Catalog - maintains a snapshot of the catalogs of all participants in a dataspace.

## Important classes and objects

- `Crawler`: a piece of software within the FC that periodically issues `update-requests` to other TCNs. Receives
  a `WorkItem` and executes the DSP catalog request.
- `WorkItem`: a unit of work (= "crawl target") for the crawler
- `UpdateRequest`: a DSP catalog request from the Crawler to a TCN to get that TCN's catalog
- `UpdateResponse`: the response to an `UpdateRequest`
- `ExecutionPlan`: defines how the crawlers should run, i.e. periodically, based on an event, etc. By default, FC runs
  on a periodic schedule.
- `ExecutionManager`: this is the central component that instantiates `Crawlers` and schedules/distributes the work
  among them.
- `QueryEngine`: a service that interprets and executes a catalog query

## Use cases for the TCN

**as the TCN I want to:**

- support multiple query protocols such as IDS
- handle queries (i.e. limit/reduce the query result) based on policies
- use the `AssetIndex` to resolve queries
- the `AssetIndex` support pluggable catalog backends such as databases

## Use cases for the FCC

**as the `Crawler` I want to:**

- run periodically
- support multiple update protocols like IDS and "EDC native" (which is yet to be defined)
- issue a catalog update request that goes out to all my protocol adapters
- present my `VerifiableCredential` in every catalog `update-request` (cf. ION Demo)
- put the catalog update response into a queue/ringbuffer
- emit events (e.g. through an `Observable`)

**as the `LoaderManager` I want to:**

- take out a batch of `update-responses` from the queue and feed them to the `Loader`
- emit events
- run independently from the crawler = we should not block each other
- have 1...n `Loaders` that I can delegate out to

**as the `Loader` I want to:**

- insert data into the database
- perform any sort of data sanitization (e.g. no white spaces...)
- perform any sort of data transformation

**as the `QueryEngine` I want to:**

- receive a query in a standardized format (e.g. as a `Query` object) with filters,...
- have 1...n Query adapters
- ask my query adapters whether they can handle an incoming query (`canHandle`) or map query type to a particular
  QueryAdpater
- forward the query to my query adapter(s)
- wait for their result asynchronously, e.g. using a `CompletableFuture` or similar

**as the `QueryAdapter` I want to:**

- implement the concrete persistence backend, e.g. CosmosDB and maintain its credentials
- registers with the QueryEngine based on a query type
- receive the query in a typed format
- transform the query into a specific format (e.g. into SQL, gremlin statements, etc.)
- execute the query against my persistence backend
- return the result asynchronously

## Architectural and deployment considerations

The design of the Cache's crawler aims at being flexible, scalable and low-maintenance. The ultimate goal is
to crawl a dataspace as fast and efficient as possible. We therefore envision some sort of segmentation of the
dataspace.
We call these segments "Partitions". Many dataspaces will want partitioning based on geographical regions.

Each partition can have several crawlers, the concrete amount is determined by the deployment, e.g. a K8S cluster
with 10 crawlers. They are relatively dumb pieces of software: when there's work, they run off and crawl.

Whether there's work is determined by the "ExecutionPlan", which is maintained by the "PartitionManager".
When the ExecutionPlan requires it, the PartitionManager populates a queue shared by all crawlers.

The ExecutionPlan contains a list of target TCNs (or rather: their URLs) and what protocol to use to query them.
It also contains essentially a crontab spec, which determines when the aforementioned shared queue gets populated.

So in summary, a likely deployment will be a K8S cluster with 1 PartitionManager and N Crawlers.