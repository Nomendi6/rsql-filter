package testappl.service;

import jakarta.persistence.criteria.JoinType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;
import testappl.domain.*; // for static metamodels
import testappl.domain.AppObject;
import testappl.repository.AppObjectRepository;
import testappl.service.criteria.AppObjectCriteria;
import testappl.service.dto.AppObjectDTO;
import testappl.service.mapper.AppObjectMapper;

/**
 * Service for executing complex queries for {@link AppObject} entities in the database.
 * The main input is a {@link AppObjectCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link AppObjectDTO} or a {@link Page} of {@link AppObjectDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AppObjectQueryService extends QueryService<AppObject> {

    private final Logger log = LoggerFactory.getLogger(AppObjectQueryService.class);

    private final AppObjectRepository appObjectRepository;

    private final AppObjectMapper appObjectMapper;

    public AppObjectQueryService(AppObjectRepository appObjectRepository, AppObjectMapper appObjectMapper) {
        this.appObjectRepository = appObjectRepository;
        this.appObjectMapper = appObjectMapper;
    }

    /**
     * Return a {@link List} of {@link AppObjectDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<AppObjectDTO> findByCriteria(AppObjectCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<AppObject> specification = createSpecification(criteria);
        return appObjectMapper.toDto(appObjectRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link AppObjectDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AppObjectDTO> findByCriteria(AppObjectCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<AppObject> specification = createSpecification(criteria);
        return appObjectRepository.findAll(specification, page).map(appObjectMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AppObjectCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<AppObject> specification = createSpecification(criteria);
        return appObjectRepository.count(specification);
    }

    /**
     * Function to convert {@link AppObjectCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<AppObject> createSpecification(AppObjectCriteria criteria) {
        Specification<AppObject> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), AppObject_.id));
            }
            if (criteria.getCode() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCode(), AppObject_.code));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), AppObject_.name));
            }
            if (criteria.getObjectType() != null) {
                specification = specification.and(buildSpecification(criteria.getObjectType(), AppObject_.objectType));
            }
            if (criteria.getLastChange() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLastChange(), AppObject_.lastChange));
            }
            if (criteria.getSeq() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getSeq(), AppObject_.seq));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), AppObject_.status));
            }
            if (criteria.getQuantity() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getQuantity(), AppObject_.quantity));
            }
            if (criteria.getValidFrom() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getValidFrom(), AppObject_.validFrom));
            }
            if (criteria.getValidUntil() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getValidUntil(), AppObject_.validUntil));
            }
            if (criteria.getIsValid() != null) {
                specification = specification.and(buildSpecification(criteria.getIsValid(), AppObject_.isValid));
            }
            if (criteria.getCreationDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreationDate(), AppObject_.creationDate));
            }
            if (criteria.getParentId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getParentId(), root -> root.join(AppObject_.parent, JoinType.LEFT).get(AppObject_.id))
                    );
            }
        }
        return specification;
    }
}
