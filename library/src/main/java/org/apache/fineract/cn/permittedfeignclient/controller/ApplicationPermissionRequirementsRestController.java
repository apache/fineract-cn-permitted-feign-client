/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.permittedfeignclient.controller;

import org.apache.finearct.cn.permittedfeignclient.api.v1.domain.ApplicationPermission;
import org.apache.fineract.cn.permittedfeignclient.service.ApplicationPermissionRequirementsService;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.cn.anubis.annotation.AcceptedTokenType;
import org.apache.fineract.cn.anubis.annotation.Permittable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Myrle Krantz
 */
@RestController
@RequestMapping("/requiredpermissions")
public class ApplicationPermissionRequirementsRestController {

  private final ApplicationPermissionRequirementsService service;

  @Autowired
  public ApplicationPermissionRequirementsRestController(final ApplicationPermissionRequirementsService service) {
    this.service = service;
  }

  @Permittable(AcceptedTokenType.GUEST)
  @RequestMapping(
      method = RequestMethod.GET,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<List<ApplicationPermission>> getRequiredPermissions() {
    final List<ApplicationPermission> requiredPermissions = service.getRequiredPermissions();

    return ResponseEntity.ok(new ArrayList<>(requiredPermissions));
  }
}
