# rsql-filter-demo

This is the demo application for the [rsql-filter](../rsql-filter) library. It is a JHipster application whose entity
services are wired to `rsql.RsqlQueryService`, so every entity list endpoint accepts an RSQL `filter` query parameter.
Use it to try filter strings against a real database and a real JPA metamodel without writing an application of your own.

The demo ships only with the 0.6.x line (branch `release-3`). On `master`, which carries the 0.7.x line, this module is
commented out of the reactor, so there is no 0.7.x build of the demo.

## What is wired to rsql-filter

This module is the third module of the `rsql-filter-parent` reactor ([../pom.xml](../pom.xml)) and depends on
`com.nomendi6:rsql-filter` at the parent version, so it always builds against the library sitting next to it.

Three entities carry RSQL filtering. What the filter parser sees is the JPA entity, so the lists below are taken from
[src/main/java/com/nomendi6/rsql/demo/domain](src/main/java/com/nomendi6/rsql/demo/domain). The generator snapshots in
[.jhipster/](.jhipster/) are only a starting point and are behind for `AppObject`: `AppObject.json` stops at
`validUntil` and knows nothing of `uuidField`, `isValid`, `creationDate` or the `product` relation.

| Entity        | Fields                                                                                                                                             | Many-to-one relations                |
| ------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------ |
| `AppObject`   | `code`, `name`, `description`, `objectType`, `lastChange`, `seq`, `status`, `quantity`, `validFrom`, `validUntil`, and three fields added by hand: `uuidField`, `isValid`, `creationDate` | `parent` (AppObject), `product` (Product) |
| `Product`     | `code`, `name`, `description`, `seq`, `status`, `validFrom`, `validUntil`                                                                           | `tproduct` (ProductType), `parent` (Product) |
| `ProductType` | `code`, `name`, `description`, `seq`, `status`, `validFrom`, `validUntil`                                                                           | none                                 |

`status` is the `StandardRecordStatus` enum (`ACTIVE`, `NOT_ACTIVE`) and `objectType` is the `AppObjectType` enum
(`FUNCTIONAL_MODULE`, `FORM`, `REPORT`, `ENTITY`). All three entities extend `AbstractAuditingEntity`, so `createdBy`,
`createdDate`, `lastModifiedBy` and `lastModifiedDate` can be filtered on as well.

Those four hand-added members of `AppObject` exist in Java only. The Liquibase changelog
([20250429060119_added_entity_AppObject.xml](src/main/resources/config/liquibase/changelog/20250429060119_added_entity_AppObject.xml))
creates no `uuid_field`, `is_valid`, `creation_date` or `product_id` column, and `spring.jpa.hibernate.ddl-auto` is
`none`, so nothing adds them at startup. In the running demo that means `GET /api/app-object` and
`GET /api/app-object/all` always fail with a 500 —
`could not prepare statement [Column "AO1_0.CREATION_DATE" not found]` — filter or no filter, because materialising the
entity selects all its columns. `GET /api/app-object/count` and `GET /api/app-object/lov` do work, and so does any
filter over the ten fields that do have columns; a filter naming `uuidField`, `isValid` or `product` fails on those two
endpoints as well. `Product` and `ProductType` have no such gap — reach for them first when trying filter strings out.

Each service builds one `RsqlQueryService` in its constructor and hands it out through `getQueryService()`. From
[src/main/java/com/nomendi6/rsql/demo/service/ProductService.java](src/main/java/com/nomendi6/rsql/demo/service/ProductService.java):

```java
private RsqlQueryService<Product, ProductDTO, ProductRepository, ProductMapper> queryService;

public ProductService(ProductRepository productRepository, ProductMapper productMapper, EntityManager entityManager) {
    // ...
    this.queryService = new RsqlQueryService<>(productRepository, productMapper, entityManager, Product.class);
}

public Page<ProductDTO> findAll(String filter, Pageable pageable) {
    return getQueryService().findByFilter(filter, pageable);
}
```

`AppObjectService` and `ProductTypeService` are the same code with different type parameters.

## Endpoints that take a filter

Everything is under `/api`, in `ProductResource`, `ProductTypeResource` and `AppObjectResource`. Substitute `product`,
`product-type` or `app-object` for `<entity>`:

| Endpoint                    | Query parameters              | What it calls                                                                                    |
| --------------------------- | ----------------------------- | ------------------------------------------------------------------------------------------------ |
| `GET /api/<entity>`         | `filter`, `page`, `size`, `sort` | `findByFilter(filter, pageable)` — one page, with the usual `X-Total-Count` and `Link` headers   |
| `GET /api/<entity>/all`     | `filter`, `sort`              | `findByFilterAndSort(filter, pageable)` — the whole result set; only the sort part of the pageable is used, `page` and `size` are ignored |
| `GET /api/<entity>/lov`     | `filter`, `page`, `size`, `sort` | `getResultAsMap(filter, pageable, "id", "code", "name")` — a list of maps with those three keys |
| `GET /api/<entity>/count`   | `filter`                      | `countByFilter(filter)` — a single number                                                        |

`filter` is optional; leaving it out returns everything. Each of the three controllers runs
`URLDecoder.decode(filter, UTF_8)` on the value on top of the decoding the servlet container has already done, so the
filter is URL-decoded twice and percent escapes need one extra round of encoding. The case that bites is a timezone
offset: write `#2025-04-28T00:00:00Z#` rather than `#2025-04-28T00:00:00+01:00#`, or the `+` reaches the parser as a
space.

`/api/**` is authenticated with a stateless JWT, so a plain browser URL gets a 401. Ask for a token first — this call
only works if the application was started with the `dev` Spring profile actually active, which a plain `./mvnw` does not
do; see [Running the demo](#running-the-demo) below:

```
curl -s -X POST http://localhost:8080/api/authenticate \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin"}'
```

and send the returned `id_token` as an `Authorization: Bearer <token>` header:

```
curl -G http://localhost:8080/api/product \
  -H "Authorization: Bearer $TOKEN" \
  --data-urlencode "filter=status==#ACTIVE#;seq=gt=1000" \
  --data-urlencode "sort=name,asc" \
  --data-urlencode "size=50"
```

## Running the demo

The demo needs the library in the local repository, so build the reactor from the repository root first:

```
mvn clean install -DskipTests
```

`mvn install -pl rsql-filter -DskipTests` is enough when only the library has changed: this module resolves its parent
from `../pom.xml` on disk and needs nothing else from the reactor but the `com.nomendi6:rsql-filter` artifact.

Then start the application from this directory. `./mvnw` has `spring-boot:run` as its default goal and the `dev` *Maven*
profile is active by default, but that does not give you the `dev` *Spring* profile: this module's POM has no resource
filtering block, so the `active: '@spring.profiles.active@'` placeholder in
[src/main/resources/config/application.yml](src/main/resources/config/application.yml) is copied to `target/classes`
verbatim. A bare `./mvnw` does start, and logs `Profile(s): [@spring.profiles.active@]` to prove it — but
`application-dev.yml` is never read. The JWT secret and the `jdbc:h2:mem:rsql-filter-demo` datasource live in that file,
so the application falls back to an auto-configured embedded H2 and `POST /api/authenticate` answers
`500 expiresAt must be after issuedAt`, which puts every curl above out of reach. Name the profile yourself, and keep
`api-docs` out of it:

```
./mvnw -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--spring.profiles.group.dev=dev
```

The second argument is not decoration. `application.yml` declares a profile *group* that expands `dev` into
`dev, api-docs`, and `api-docs` brings up springdoc, which cannot start in this repository: this module's POM pins
`springdoc-openapi-starter-webmvc-api` at 2.8.6 while the `jhipster-dependencies` 8.0.0 BOM imported by the root POM
drags in `springdoc-openapi-starter-common` 2.2.0, and the two land on the classpath together. The context refresh then
dies with `Failed to instantiate [org.springdoc.webmvc.core.service.RequestService]`. Redefining the group as `dev`
alone leaves one profile active and the application comes up — `The following 1 profile is active: "dev"`.

The backend then listens on [http://localhost:8080](http://localhost:8080) against the in-memory H2 database configured
in `application-dev.yml` (`jdbc:h2:mem:rsql-filter-demo`), with the H2 console at
[http://localhost:8080/h2-console/login.jsp](http://localhost:8080/h2-console/login.jsp) — the bare `/h2-console` path
falls through to the Angular app and returns its index page. For the Angular client with hot reload, run `./npmw start`
in a second terminal and open [http://localhost:9000](http://localhost:9000); it proxies `/api` to port 8080. Sign in as
`admin`/`admin` or `user`/`user`.

There is no working OpenAPI UI in this demo. [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
is served as a static page, but the specification it fetches is published only by the `api-docs` Spring profile, which
is the profile that cannot start; with `dev` alone the request is refused before it can 404: `/v3/api-docs/**` is guarded by
`hasAuthority(ADMIN)` (SecurityConfiguration.java:84), so the browser - which fetches it without a token -
gets 401 and the page stays empty. With an admin Bearer token the same URL answers 404, because no
specification is published at all. `./mvnw -Papi-docs`
is not a way round it: naming a profile with `-P` switches off the `activeByDefault` `dev` Maven profile, which is the
only place H2 is declared at compile scope, so the context fails with `Failed to configure a DataSource: 'url' attribute
is not specified and no embedded datasource could be configured` and the run ends in BUILD FAILURE. Nor would it select
the profile if it started — the `api-docs` Maven profile only feeds `${profile.api-docs}` into the `prod` profile's
`spring.profiles.active`, and the Spring-side `api-docs` you want is already in the `dev` group anyway.

The `dev` profile also loads ten faker rows per entity from
[src/main/resources/config/liquibase/fake-data](src/main/resources/config/liquibase/fake-data). Those CSVs fill only the
scalar columns, so `tproduct` and `parent` are null on every seeded row — filters that navigate a relation parse and
execute but return nothing until you create data of your own. `AppObject.product` is the worse case described above: no
column, so no query either.

RSQL-specific tests for this application live in
[src/test/java/com/nomendi6/rsql/demo/rsql](src/test/java/com/nomendi6/rsql/demo/rsql). They are `*IT` classes bound to
failsafe, so `./mvnw verify` runs them.

## Sample filters

Every string below parses. Drop it into the `filter` parameter as shown above.

| Filter                                             | Entity  | Shows                                                    |
| -------------------------------------------------- | ------- | -------------------------------------------------------- |
| `status==#ACTIVE#`                                 | any     | enum literal, written between `#`                        |
| `code=='basket redesign'`                          | any     | string literal; `"` and `` ` `` work as delimiters too   |
| `name=*'*ee*'`                                     | any     | case-insensitive LIKE — the wildcard goes inside the quotes |
| `seq=gt=1000;status==#ACTIVE#`                     | any     | AND; `and` is accepted in place of `;`                   |
| `(status==#ACTIVE#,status==#NOT_ACTIVE#);seq=lt=1000` | any  | OR (`,`) with parentheses for grouping                   |
| `seq=bt=(100,20000)`                               | any     | BETWEEN                                                  |
| `objectType=in=(#FORM#,#REPORT#)`                  | AppObject | IN over an enum                                        |
| `validFrom=ge=#2025-04-28T00:00:00Z#`              | any     | datetime literal — a zone is required; prefer `Z` to `+01:00` over HTTP, see the decoding note above |
| `createdDate=ge=#2025-01-01#`                      | any     | plain date literal, and an inherited auditing field      |
| `uuidField==null`                                  | AppObject | IS NULL                                                |
| `isValid==true`                                    | AppObject | boolean literal                                        |
| `tproduct.name=='reel'`                            | Product | navigating a many-to-one relation                        |
| `product.status==#ACTIVE#;quantity=gt=10000`       | AppObject | a relation and a scalar in one filter                  |

Parsing is one thing, running them against the demo's own schema another. The `Product` rows execute as they stand. The
four `AppObject` rows need care, for the reason given under [What is wired to rsql-filter](#what-is-wired-to-rsql-filter):
`objectType=in=(#FORM#,#REPORT#)` runs on `/api/app-object/count` and `/api/app-object/lov` but 500s on
`/api/app-object`, and the last three — `uuidField`, `isValid` and `product` all being Java-only members — 500s on every
`/api/app-object` endpoint with `Column "AO1_0.UUID_FIELD" not found` or its equivalent. They stay in the table because
they are the shapes worth copying into your own application, not because this application can serve them.

The full operator list is in the [project README](../README.md); SELECT and HAVING have their own documents
([../SELECT.md](../SELECT.md), [../HAVING.md](../HAVING.md)) and the library API is described in [../API.md](../API.md).

---

This application was generated using JHipster 8.10.0, you can find documentation and help at [https://www.jhipster.tech/documentation-archive/v8.10.0](https://www.jhipster.tech/documentation-archive/v8.10.0).

## Project Structure

Node is required for generation and recommended for development. `package.json` is always generated for a better development experience with prettier, commit hooks, scripts and so on.

In the project root, JHipster generates configuration files for tools like git, prettier, eslint, husky, and others that are well known and you can find references in the web.

`/src/*` structure follows default Java structure.

- `.yo-rc.json` - Yeoman configuration file
  JHipster configuration is stored in this file at `generator-jhipster` key. You may find `generator-jhipster-*` for specific blueprints configuration.
- `.yo-resolve` (optional) - Yeoman conflict resolver
  Allows to use a specific action when conflicts are found skipping prompts for files that matches a pattern. Each line should match `[pattern] [action]` with pattern been a [Minimatch](https://github.com/isaacs/minimatch#minimatch) pattern and action been one of skip (default if omitted) or force. Lines starting with `#` are considered comments and are ignored.
- `.jhipster/*.json` - JHipster entity configuration files

- `npmw` - wrapper to use locally installed npm.
  JHipster installs Node and npm locally using the build tool by default. This wrapper makes sure npm is installed locally and uses it avoiding some differences different versions can cause. By using `./npmw` instead of the traditional `npm` you can configure a Node-less environment to develop or test your application.
- `/src/main/docker` - Docker configurations for the application and services that the application depends on

## Development

The build system will install automatically the recommended version of Node and npm.

We provide a wrapper to launch npm.
You will only need to run this command when dependencies change in [package.json](package.json).

```
./npmw install
```

We use npm scripts and [Angular CLI][] with [Webpack][] as our build system.

Run the following commands in two separate terminals to create a blissful development experience where your browser
auto-refreshes when files change on your hard drive.

```
./mvnw
./npmw start
```

Npm is also used to manage CSS and JavaScript dependencies used in this application. You can upgrade dependencies by
specifying a newer version in [package.json](package.json). You can also run `./npmw update` and `./npmw install` to manage dependencies.
Add the `help` flag on any command to see how you can use it. For example, `./npmw help update`.

The `./npmw run` command will list all the scripts available to run for this project.

### PWA Support

JHipster ships with PWA (Progressive Web App) support, and it's turned off by default. One of the main components of a PWA is a service worker.

The service worker is already registered in `src/main/webapp/app/app.config.ts`, but switched off. To enable it, set `enabled: true` in this line:

```typescript
importProvidersFrom(ServiceWorkerModule.register('ngsw-worker.js', { enabled: false })),
```

### Managing dependencies

For example, to add [Leaflet][] library as a runtime dependency of your application, you would run following command:

```
./npmw install --save --save-exact leaflet
```

To benefit from TypeScript type definitions from [DefinitelyTyped][] repository in development, you would run following command:

```
./npmw install --save-dev --save-exact @types/leaflet
```

Then you would import the JS and CSS files specified in library's installation instructions so that [Webpack][] knows about them:
Edit [src/main/webapp/app/app.config.ts](src/main/webapp/app/app.config.ts) file:

```
import 'leaflet/dist/leaflet.js';
```

Edit [src/main/webapp/content/scss/vendor.scss](src/main/webapp/content/scss/vendor.scss) file:

```
@import 'leaflet/dist/leaflet.css';
```

Note: There are still a few other things remaining to do for Leaflet that we won't detail here.

For further instructions on how to develop with JHipster, have a look at [Using JHipster in development][].

### Using Angular CLI

You can also use [Angular CLI][] to generate some custom client code.

For example, the following command:

```
ng generate component my-component
```

will generate few files:

```
create src/main/webapp/app/my-component/my-component.component.html
create src/main/webapp/app/my-component/my-component.component.ts
update src/main/webapp/app/app.config.ts
```

## Building for production

### Packaging as jar

To build the final jar and optimize the rsql-filter-demo application for production, run:

```
./mvnw -Pprod clean verify
```

This will concatenate and minify the client CSS and JavaScript files. It will also modify `index.html` so it references these new files.
To ensure everything worked, run:

```
java -jar target/rsql-filter-demo-0.6.21.jar
```

Name the jar explicitly: the parent POM attaches a javadoc jar to this module, so `target/` holds both
`rsql-filter-demo-0.6.21.jar` and `rsql-filter-demo-0.6.21-javadoc.jar` and `target/*.jar` expands to two arguments.

Then navigate to [http://localhost:8080](http://localhost:8080) in your browser.

Refer to [Using JHipster in production][] for more details.

### Packaging as war

To package your application as a war in order to deploy it to an application server, run:

```
./mvnw -Pprod,war clean verify
```

### JHipster Control Center

JHipster Control Center can help you manage and control your application(s). You can start a local control center server (accessible on http://localhost:7419) with:

```
docker compose -f src/main/docker/jhipster-control-center.yml up
```

## Testing

### Spring Boot tests

To launch your application's tests, run:

```
./mvnw verify
```

### Client tests

Unit tests are run by [Jest][]. They're located near components and can be run with:

```
./npmw test
```

UI end-to-end tests are powered by [Cypress][]. They're located in [src/test/javascript/cypress](src/test/javascript/cypress)
and can be run by starting Spring Boot in one terminal (`./mvnw spring-boot:run`) and running the tests (`./npmw run e2e`) in a second one.

#### Lighthouse audits

You can execute automated [Lighthouse audits](https://developers.google.com/web/tools/lighthouse/) with [cypress-audit](https://github.com/mfrachet/cypress-audit) by running `./npmw run e2e:cypress:audits`.
Note that the script as generated points at `cypress-audits.config.js` while the repository ships `cypress-audits.config.ts`, so it fails until the script or the file name is fixed.
You should only run the audits when your application is packaged with the production profile.
The lighthouse report is created in `target/cypress/lhreport.html`

### E2E Webapp Code Coverage

When using Cypress, you can generate code coverage report by running your dev server with instrumented code:

Build your Angular application with instrumented code:

    npm run webapp:instrumenter

Start your backend without compiling frontend:

    npm run backend:start

Start your Cypress end to end testing:

    npm run e2e:cypress:coverage

The coverage report is generated under `./coverage/lcov-report/`

## Others

### Code quality using Sonar

Sonar is used to analyse code quality. You can start a local Sonar server (accessible on http://localhost:9001) with:

```
docker compose -f src/main/docker/sonar.yml up -d
```

Note: we have turned off forced authentication redirect for UI in [src/main/docker/sonar.yml](src/main/docker/sonar.yml) for out of the box experience while trying out SonarQube, for real use cases turn it back on.

You can run a Sonar analysis with using the [sonar-scanner](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) or by using the maven plugin.

Then, run a Sonar analysis. The pinned server is SonarQube 25.3 (see [src/main/docker/sonar.yml](src/main/docker/sonar.yml)), which no longer supports `sonar.login`/`sonar.password`; generate a token in the Sonar UI and pass it as `sonar.token`:

```
./mvnw -Pprod clean verify sonar:sonar -Dsonar.token=<token>
```

If you need to re-run the Sonar phase, please be sure to specify at least the `initialize` phase since Sonar properties are loaded from the sonar-project.properties file.

```
./mvnw initialize sonar:sonar -Dsonar.token=<token>
```

Additionally, Instead of passing `sonar.token` as a CLI argument, it can be configured from [sonar-project.properties](sonar-project.properties) as shown below:

```
sonar.token=<token>
```

For more information, refer to the [Code quality page][].

### Docker Compose support

JHipster generates a number of Docker Compose configuration files in the [src/main/docker/](src/main/docker/) folder to launch required third party services.

For example, to start required services in Docker containers, run:

```
docker compose -f src/main/docker/services.yml up -d
```

To stop and remove the containers, run:

```
docker compose -f src/main/docker/services.yml down
```

[Spring Docker Compose Integration](https://docs.spring.io/spring-boot/reference/features/dev-services.html) is enabled by default. It's possible to disable it in application.yml:

```yaml
spring:
  ...
  docker:
    compose:
      enabled: false
```

You can also fully dockerize your application and all the services that it depends on.
To achieve this, first build a Docker image of your app by running:

```sh
npm run java:docker
```

Or build a arm64 Docker image when using an arm64 processor os like MacOS with M1 processor family running:

```sh
npm run java:docker:arm64
```

Then run:

```sh
docker compose -f src/main/docker/app.yml up -d
```

For more information refer to [Using Docker and Docker-Compose][], this page also contains information on the Docker Compose sub-generator (`jhipster docker-compose`), which is able to generate Docker configurations for one or several JHipster applications.

## Continuous Integration (optional)

To configure CI for your project, run the ci-cd sub-generator (`jhipster ci-cd`), this will let you generate configuration files for a number of Continuous Integration systems. Consult the [Setting up Continuous Integration][] page for more information.

[JHipster Homepage and latest documentation]: https://www.jhipster.tech
[JHipster 8.10.0 archive]: https://www.jhipster.tech/documentation-archive/v8.10.0
[Using JHipster in development]: https://www.jhipster.tech/documentation-archive/v8.10.0/development/
[Using Docker and Docker-Compose]: https://www.jhipster.tech/documentation-archive/v8.10.0/docker-compose
[Using JHipster in production]: https://www.jhipster.tech/documentation-archive/v8.10.0/production/
[Running tests page]: https://www.jhipster.tech/documentation-archive/v8.10.0/running-tests/
[Code quality page]: https://www.jhipster.tech/documentation-archive/v8.10.0/code-quality/
[Setting up Continuous Integration]: https://www.jhipster.tech/documentation-archive/v8.10.0/setting-up-ci/
[Node.js]: https://nodejs.org/
[NPM]: https://www.npmjs.com/
[Webpack]: https://webpack.github.io/
[BrowserSync]: https://www.browsersync.io/
[Jest]: https://jestjs.io
[Cypress]: https://www.cypress.io/
[Leaflet]: https://leafletjs.com/
[DefinitelyTyped]: https://definitelytyped.org/
[Angular CLI]: https://cli.angular.io/
