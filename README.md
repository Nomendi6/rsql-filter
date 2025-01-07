# **Rsql-filter**

Rsql-filter is a Java library for Spring Boot JPA applications that provides a simple and convenient way to transfer filter parameters in REST GET requests. The library makes it easy to build RESTful APIs that allow clients to filter data based on specific criteria.

## **Usage**

To use Rsql-filter in your application, you simply need to pass a **`filter`** parameter in your REST GET request. The value of the filter parameter should be a string that specifies the filtering criteria.

For example, to retrieve all records where the **`name`** field is equal to **`John`**, you can make the following request:

```http
GET /records?filter=name=='John'

```

You can also use logical operators and parentheses to build more complex filter expressions. For example, to retrieve all records where the **`name`** field is equal to **`John`** and the **`age`** field is greater than **`30`**, you can make the following request:

```http
GET /records?filter=name=='John';age=gt=30

```

The following table shows the list of supported operators:

| Operator | Description              |
|----------|--------------------------|
| ==       | Equal to                 |
| !=       | Not equal to             |
| =!       | Not equal to             |
| =*       | Like                     |
| =like=   | Like                     |
| !=*      | Not like                 |
| =!*      | Not like                 |
| =nlike=  | Not like                 |
| =gt=     | Greater than             |
| =ge=     | Greater than or equal to |
| =lt=     | Less than                |
| =le=     | Less than or equal to    |
| =in=     | In                       |
| =nin=    | Not in                   |
| =bt=     | Between                  |
| =nbt=    | Not between              |
| ==null   | Is null                  |
| !=null   | Is not null              |
| ==true   | Equal to true            |
| ==false  | Equal to false           |

Supported data types:

| Data Type      | Description                                                                 |
|----------------|-----------------------------------------------------------------------------|
| String         | Expression in quotes ("" or '' or ``), for example `name=='Ana'`            |
| Integer        | Integer number, for example `id==2345`                                      |
| Decimal number | Decimal number, for example `amount=gt=10.23`                               |
| Enum           | Enum name, for example `status==#ACTIVE#`                                   |
| Date           | Date in ISO format, for example `date=ge=#2019-01-01#`                      |
| Datetime       | Datetime in ISO format, for example `date=ge=#2019-01-01T00:00:00#`         |
| Boolean        | Boolean value, for example `active==true` or `active==false`                |
| UUID           | UUID value, for example `uuidField=='f47ac10b-58cc-4372-a567-0e02b2c3d479'` |



### Example REST controller

```java
@RestController
@RequestMapping("/api")
public class ProductTypeResource {

    private final Logger log = LoggerFactory.getLogger(ProductTypeResource.class);
    private static final String ENTITY_NAME = "productType";

    private final ProductTypeService productTypeService;

    private final ProductTypeRepository productTypeRepository;

    public ProductTypeResource(ProductTypeService productTypeService, ProductTypeRepository productTypeRepository) {
        this.productTypeService = productTypeService;
        this.productTypeRepository = productTypeRepository;
    }
    
    @GetMapping("/product-types")
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public ResponseEntity<List<ProductTypeDTO>> getAllProductTypes(
            @Parameter(
                    name = "filter"
            ) @RequestParam(value = "filter", required = false) String filter,
            Pageable pageable
    ) throws UnsupportedEncodingException {
        if (filter != null) {
            filter = decode(filter, StandardCharsets.UTF_8);
        }

        log.debug("REST request to get ProductTypes by filter: {}", filter);
        Page<ProductTypeDTO> page = productTypeService.getQueryService().findByFilter(filter, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
```

### Example service
```java
@Service
@Transactional
public class ProductTypeService {
    private RsqlQueryService<ProductType, ProductTypeDTO, ProductTypeRepository, ProductTypeMapper> queryService;

    /**
     * Return a rsqlQueryService used for executing queries with rsql filters.
     *
     * @return RsqlQueryService
     */
    public RsqlQueryService<ProductType, ProductTypeDTO, ProductTypeRepository, ProductTypeMapper> getQueryService() {
        if (this.queryService == null) {
            this.queryService = new RsqlQueryService<>(productTypeRepository, productTypeMapper, entityManager, ProductType.class);
        }
        return this.queryService;
    }
    
}
```

### Example repository
```java
@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long>, JpaSpecificationExecutor<ProductType> {
}
```

Complete example application can be found [here](./test-appl).

## Spring Boot integration

This library is designed to work with Spring Boot JPA applications. It provides a convenient way to transfer filter parameters in REST GET requests. The library makes it easy to build RESTful APIs that allow clients to filter data based on specific criteria.

This specific version is working with Spring Boot 3 (3.3.5) and Hibernate version 6 (6.5.3.Final).   

## **References**

This work is based on the following projects:
- RSQL / FIQL parser by Jakub Jirutka: [https://github.com/jirutka/rsql-parser](https://github.com/jirutka/rsql-parser)
- Perplexhub/rsql-jpa-specification by Perplexhub: [https://github.com/perplexhub/rsql-jpa-specification](https://github.com/perplexhub/rsql-jpa-specification)

## **Conclusion**

Rsql-filter is a simple and convenient library for Spring Boot JPA applications that makes it easy to transfer filter parameters in REST GET requests. Whether you are building a new RESTful API or adding filtering to an existing one, Rsql-filter is an excellent choice for your filtering needs.