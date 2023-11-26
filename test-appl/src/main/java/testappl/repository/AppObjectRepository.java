package testappl.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import testappl.domain.AppObject;

/**
 * Spring Data JPA repository for the AppObject entity.
 */
@Repository
public interface AppObjectRepository extends JpaRepository<AppObject, Long>, JpaSpecificationExecutor<AppObject> {
    default Optional<AppObject> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<AppObject> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<AppObject> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select appObject from AppObject appObject left join fetch appObject.parent left join fetch appObject.product left join fetch appObject.product2 left join fetch appObject.product3",
        countQuery = "select count(appObject) from AppObject appObject"
    )
    Page<AppObject> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select appObject from AppObject appObject left join fetch appObject.parent left join fetch appObject.product left join fetch appObject.product2 left join fetch appObject.product3"
    )
    List<AppObject> findAllWithToOneRelationships();

    @Query(
        "select appObject from AppObject appObject left join fetch appObject.parent left join fetch appObject.product left join fetch appObject.product2 left join fetch appObject.product3 where appObject.id =:id"
    )
    Optional<AppObject> findOneWithToOneRelationships(@Param("id") Long id);
}
