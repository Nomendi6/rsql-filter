package com.nomendi6.rsql.it.repository.hibernate66;

import com.nomendi6.rsql.it.domain.hibernate66.FilteredShortcutRoot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FilteredShortcutRootRepository extends JpaRepository<FilteredShortcutRoot, Long>, JpaSpecificationExecutor<FilteredShortcutRoot> {}
