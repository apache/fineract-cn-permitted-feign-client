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
package org.apache.fineract.cn.permittedfeignclient.annotation;

import static org.apache.fineract.cn.api.config.ApiConfiguration.LOGGER_NAME;

import feign.Feign;
import feign.Target;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.apache.fineract.cn.permittedfeignclient.security.ApplicationTokenedTargetInterceptor;
import org.apache.fineract.cn.permittedfeignclient.service.ApplicationAccessTokenService;
import org.apache.fineract.cn.api.util.AnnotatedErrorDecoder;
import org.apache.fineract.cn.api.util.TenantedTargetInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.feign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

/**
 * @author Myrle Krantz
 */
public class PermittedFeignClientsConfiguration extends FeignClientsConfiguration {
  private static class FeignBuilder extends Feign.Builder {
    private final ApplicationAccessTokenService applicationAccessTokenService;
    private final Logger logger;

    FeignBuilder(
            final ApplicationAccessTokenService applicationAccessTokenService,
            final Logger logger) {
      this.applicationAccessTokenService = applicationAccessTokenService;
      this.logger = logger;
    }

    public <T> T target(final Target<T> target) {
      this.errorDecoder(new AnnotatedErrorDecoder(logger, target.type()));
      this.requestInterceptor(new TenantedTargetInterceptor());
      this.requestInterceptor(new ApplicationTokenedTargetInterceptor(
              applicationAccessTokenService,
              target.type()));
      return build().newInstance(target);
    }
  }

  @Bean
  @ConditionalOnMissingBean
  public Decoder feignDecoder() {
    return new GsonDecoder();
  }

  @Bean
  @ConditionalOnMissingBean
  public Encoder feignEncoder() {
    return new GsonEncoder();
  }

  @Bean(name = LOGGER_NAME)
  @ConditionalOnMissingBean
  public Logger logger() {
    return LoggerFactory.getLogger(LOGGER_NAME);
  }

  @Bean
  @Scope("prototype")
  @ConditionalOnMissingBean
  public Feign.Builder permittedFeignBuilder(
          @SuppressWarnings("SpringJavaAutowiringInspection")
          final ApplicationAccessTokenService applicationAccessTokenService,
          @Qualifier(LOGGER_NAME) final Logger logger) {
    return new FeignBuilder(
            applicationAccessTokenService,
            logger);
  }
}