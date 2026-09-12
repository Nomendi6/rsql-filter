package rsql;

/**
 * How {@link RsqlQueryService} fetches one page of a JPQL query.
 *
 * <p>The choice only matters for the paged JPQL methods - {@code findByFilter(String, Pageable)},
 * {@code findEntitiesByFilter(String, Pageable)} and {@code getJpqlQueryResultAsPage} - and only when the
 * service runs in JPQL mode. The Specification path and the unpaged methods are unaffected.</p>
 */
public enum PagingStrategy {

    /**
     * One statement: the full select, with the filter, the sort and the offset/limit all applied to it.
     *
     * <p>The database therefore has to join and sort the <em>whole</em> filtered result before it can skip to
     * the requested page. For a wide select over many joins that cost is paid on every page, and it grows
     * with the page number. This is the behaviour of every version before 0.6.23, and the default.</p>
     */
    SINGLE_QUERY,

    /**
     * Two statements: first the identifiers of the requested page, then the full select for those
     * identifiers only.
     *
     * <p>The first statement selects nothing but the identifier and the sort columns, so the database sorts
     * narrow rows and joins only what the filter and the sort need. The second runs the caller's own select
     * with {@code where <alias>.<id> in (...)}, without sort or limit, and the rows are put back in the order
     * the first statement returned. The cost of a page stops depending on how wide the select is or how deep
     * the page is.</p>
     *
     * <p>Where it cannot be applied it quietly falls back to {@link #SINGLE_QUERY}: an unpaged request, an
     * entity with a composite identifier, or a select whose {@code from} clause cannot be found.</p>
     */
    IDS_THEN_HYDRATE
}
