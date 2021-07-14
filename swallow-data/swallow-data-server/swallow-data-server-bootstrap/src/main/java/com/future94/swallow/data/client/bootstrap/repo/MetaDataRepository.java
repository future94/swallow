package com.future94.swallow.data.client.bootstrap.repo;

import com.future94.swallow.data.client.bootstrap.entity.MetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author weilai
 */
@Repository
public interface MetaDataRepository extends JpaRepository<MetaData, Integer> {

    MetaData findByAppNameAndPath(String appName, String path);
}
