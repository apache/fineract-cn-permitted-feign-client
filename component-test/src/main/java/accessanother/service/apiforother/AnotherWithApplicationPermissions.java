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
package accessanother.service.apiforother;

import org.apache.fineract.cn.permittedfeignclient.annotation.EndpointSet;
import org.apache.fineract.cn.permittedfeignclient.annotation.PermittedFeignClientsConfiguration;
import org.apache.fineract.cn.anubis.annotation.Permittable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Myrle Krantz
 */
@EndpointSet(identifier = AnotherWithApplicationPermissions.ENDPOINT_SET_IDENTIFIER)
@FeignClient(name="another-v1", path="/another/v1", configuration = PermittedFeignClientsConfiguration.class)
public interface AnotherWithApplicationPermissions {
  String ENDPOINT_SET_IDENTIFIER = "x";
  String ANOTHER_FOO_PERMITTABLE_GROUP = "group_for_another";

  @RequestMapping(value = "/foo", method = RequestMethod.POST,
          consumes = {MediaType.APPLICATION_JSON_VALUE},
          produces = {MediaType.ALL_VALUE})
  @Permittable(groupId = ANOTHER_FOO_PERMITTABLE_GROUP)
  void createFoo();

  @RequestMapping(value = "/foo", method = RequestMethod.GET,
          consumes = {MediaType.APPLICATION_JSON_VALUE},
          produces = {MediaType.ALL_VALUE})
  @Permittable(groupId = ANOTHER_FOO_PERMITTABLE_GROUP)
  boolean getFoo();

  //TODO: also test multiple permittables.
  //TODO: also think about upgradeability when permission needs change.
}
