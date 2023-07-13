package com.microservices.demo.elastic.query.service.business;

import com.microservices.demo.elastic.query.service.dataaccess.entity.UserPermissions;

import java.util.List;
import java.util.Optional;

public interface QueryUserService {

    Optional<List<UserPermissions>> findAllPermissionsByUsername(String username);
}
