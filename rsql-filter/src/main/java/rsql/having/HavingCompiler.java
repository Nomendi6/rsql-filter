package rsql.having;

import jakarta.persistence.criteria.Predicate;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;
import rsql.where.RsqlContext;
import rsql.exceptions.SyntaxErrorException;

import java.util.List;

/**
 * Compiler that orchestrates the process of compiling a HAVING filter string into a JPA Criteria Predicate.
 * This class coordinates the parsing and visitor pattern to transform RSQL HAVING syntax into executable predicates.
 */
public class HavingCompiler {

    private final HavingTreeParser treeParser = new HavingTreeParser();

    /**
     * Compiles a HAVING filter string into a JPA Criteria Predicate.
     *
     * @param havingFilter The HAVING filter string (e.g., "totalPrice=gt=1000;productCount=ge=3")
     * @param havingContext HAVING context with alias-to-expression mappings from SELECT
     * @param rsqlContext RSQL context with EntityManager
     * @param groupByFields List of GROUP BY fields for validation (fields used without aggregate functions in HAVING)
     * @param <ENTITY> The entity type
     * @return Predicate for query.having(), or null if havingFilter is null/empty
     * @throws SyntaxErrorException if parsing fails or HAVING syntax is invalid
     * @throws IllegalArgumentException if HAVING references fields not in GROUP BY or SELECT aliases
     */
    public <ENTITY> Predicate compile(
        String havingFilter,
        HavingContext<ENTITY> havingContext,
        RsqlContext<ENTITY> rsqlContext,
        List<String> groupByFields
    ) {
        if (havingFilter == null || havingFilter.trim().isEmpty()) {
            return null;
        }

        try {
            // 1. Parse string into ParseTree
            CharStream inputStream = CharStreams.fromString(havingFilter);
            ParseTree tree = treeParser.parseStream(inputStream);

            // 2. Create visitor
            HavingSpecificationVisitor<ENTITY> visitor = new HavingSpecificationVisitor<>();
            visitor.setContext(havingContext, rsqlContext, groupByFields);

            // 3. Visit tree and get Predicate
            Predicate predicate = (Predicate) visitor.visit(tree);

            if (predicate == null) {
                throw new SyntaxErrorException("Error parsing HAVING clause: " + havingFilter);
            }

            return predicate;

        } catch (SyntaxErrorException e) {
            // Re-throw syntax errors as-is
            throw e;
        } catch (IllegalArgumentException e) {
            // Re-throw validation errors as-is
            throw e;
        } catch (Exception e) {
            // Wrap unexpected errors
            throw new SyntaxErrorException(
                "Unexpected error while compiling HAVING clause '" + havingFilter + "': " + e.getMessage()
            );
        }
    }
}
