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
package org.apache.fineract.cn.permittedfeignclient.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.fineract.cn.permittedfeignclient.annotation.EndpointSet;
import org.apache.fineract.cn.permittedfeignclient.service.ApplicationAccessTokenService;
import javax.annotation.Nonnull;
import org.apache.fineract.cn.api.util.ApiConstants;
import org.apache.fineract.cn.api.util.UserContextHolder;
import org.apache.fineract.cn.lang.TenantContextHolder;
import org.springframework.util.Assert;

/**
 * @author Myrle Krantz
 */
public class ApplicationTokenedTargetInterceptor implements RequestInterceptor {
  private final ApplicationAccessTokenService applicationAccessTokenService;
  private final String endpointSetIdentifier;

  public <T> ApplicationTokenedTargetInterceptor(
          final @Nonnull ApplicationAccessTokenService applicationAccessTokenService,
          final @Nonnull Class<T> type) {
    Assert.notNull(applicationAccessTokenService);
    Assert.notNull(type);

    this.applicationAccessTokenService = applicationAccessTokenService;
    final EndpointSet endpointSet = type.getAnnotation(EndpointSet.class);
    Assert.notNull(endpointSet, "Permitted feign clients require an endpoint set identifier provided via @EndpointSet.");
    this.endpointSetIdentifier = endpointSet.identifier();
  }

  @Override
  public void apply(final RequestTemplate template) {
    UserContextHolder.getUserContext().ifPresent(userContext -> {
      final String accessToken = applicationAccessTokenService.getAccessToken(userContext.getUser(),
              TenantContextHolder.checkedGetIdentifier(), endpointSetIdentifier);

      template.header(ApiConstants.USER_HEADER, userContext.getUser());
      template.header(ApiConstants.AUTHORIZATION_HEADER, accessToken);
    });
  }
}