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
package org.apache.fineract.cn.permittedfeignclient.config;

import feign.Client;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.apache.fineract.cn.permittedfeignclient.LibraryConstants;
import javax.annotation.Nonnull;
import org.apache.fineract.cn.anubis.config.EnableAnubis;
import org.apache.fineract.cn.api.util.AnnotatedErrorDecoder;
import org.apache.fineract.cn.api.util.EmptyBodyInterceptor;
import org.apache.fineract.cn.api.util.TenantedTargetInterceptor;
import org.apache.fineract.cn.api.util.TokenedTargetInterceptor;
import org.apache.fineract.cn.identity.api.v1.client.IdentityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.netflix.feign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Myrle Krantz
 */
@EnableAnubis
@Configuration
public class PermittedFeignClientConfiguration {
  @Bean(name = LibraryConstants.LOGGER_NAME)
  public Logger logger() {
    return LoggerFactory.getLogger(LibraryConstants.LOGGER_NAME);
  }

  @Bean
  public IdentityManager identityManager(
          @SuppressWarnings("SpringJavaAutowiringInspection") final @Nonnull Client feignClient,
          final @Qualifier(LibraryConstants.LOGGER_NAME) @Nonnull Logger logger) {
    return Feign.builder()
            .contract(new SpringMvcContract())
            .client(feignClient) //Integrates to ribbon.
            .errorDecoder(new AnnotatedErrorDecoder(logger, IdentityManager.class))
            .requestInterceptor(new TenantedTargetInterceptor())
            .requestInterceptor(new TokenedTargetInterceptor())
            .requestInterceptor(new EmptyBodyInterceptor())
            .decoder(new GsonDecoder())
            .encoder(new GsonEncoder())
            .target(IdentityManager.class, "http://identity-v1/identity/v1");
  }
}
