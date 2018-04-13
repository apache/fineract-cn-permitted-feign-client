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
package org.apache.fineract.cn.permittedfeignclient.service;

import org.apache.fineract.cn.permittedfeignclient.LibraryConstants;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.fineract.cn.anubis.config.TenantSignatureRepository;
import org.apache.fineract.cn.anubis.security.AmitAuthenticationException;
import org.apache.fineract.cn.anubis.token.TenantRefreshTokenSerializer;
import org.apache.fineract.cn.anubis.token.TokenSerializationResult;
import org.apache.fineract.cn.api.context.AutoGuest;
import org.apache.fineract.cn.api.util.NotFoundException;
import org.apache.fineract.cn.identity.api.v1.client.IdentityManager;
import org.apache.fineract.cn.identity.api.v1.domain.Authentication;
import org.apache.fineract.cn.lang.ApplicationName;
import org.apache.fineract.cn.lang.AutoTenantContext;
import org.apache.fineract.cn.lang.security.RsaKeyPairFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Myrle Krantz
 */
@Component
public class ApplicationAccessTokenService {
  private static final long REFRESH_TOKEN_LIFESPAN = TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES);

  private final String applicationName;
  private final TenantSignatureRepository tenantSignatureRepository;
  private final IdentityManager identityManager;
  private final TenantRefreshTokenSerializer tenantRefreshTokenSerializer;
  private final Logger logger;

  private final Map<TokenCacheKey, TokenSerializationResult> refreshTokenCache;
  private final Map<TokenCacheKey, Authentication> accessTokenCache;

  @Autowired
  public ApplicationAccessTokenService(
          final @Nonnull ApplicationName applicationName,
          final @Nonnull TenantSignatureRepository tenantSignatureRepository,
          final @Nonnull IdentityManager identityManager,
          final @Nonnull TenantRefreshTokenSerializer tenantRefreshTokenSerializer,
          @Qualifier(LibraryConstants.LOGGER_NAME) final @Nonnull Logger logger
  ) {

    this.applicationName = applicationName.toString();
    this.tenantSignatureRepository = tenantSignatureRepository;
    this.identityManager = identityManager;
    this.tenantRefreshTokenSerializer = tenantRefreshTokenSerializer;
    this.logger = logger;

    this.refreshTokenCache = ExpiringMap.builder()
            .maxSize(300)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .expiration(30, TimeUnit.SECONDS)
            .entryLoader(tokenCacheKey -> this.createRefreshToken((TokenCacheKey)tokenCacheKey))
            .build();
    this.accessTokenCache = ExpiringMap.builder()
            .maxSize(300)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .expiration(30, TimeUnit.SECONDS)
            .entryLoader(tokenCacheKey -> this.createAccessToken((TokenCacheKey)tokenCacheKey))
            .build();
  }

  @SuppressWarnings("WeakerAccess")
  public String getAccessToken(final String user, final String tenant) {
    return getAccessToken(user, tenant, null);
  }

  public String getAccessToken(final String user, final String tenant, final @Nullable String endpointSetIdentifier) {
    final TokenCacheKey tokenCacheKey = new TokenCacheKey(user, tenant, endpointSetIdentifier);
    final Authentication authentication = accessTokenCache.get(tokenCacheKey);
    return authentication.getAccessToken();
  }

  private Authentication createAccessToken(final TokenCacheKey tokenCacheKey) {
    final String refreshToken = refreshTokenCache.get(tokenCacheKey).getToken();
    try (final AutoTenantContext ignored = new AutoTenantContext(tokenCacheKey.getTenant())) {
      try (final AutoGuest ignored2 = new AutoGuest()) {
        logger.debug("Getting access token for {}", tokenCacheKey);
        return identityManager.refresh(refreshToken);
      }
      catch (final Exception e) {
        logger.error("Couldn't get access token from identity for {}.", tokenCacheKey, e);
        throw new NotFoundException("Couldn't get access token");
      }
    }
  }

  private TokenSerializationResult createRefreshToken(final TokenCacheKey tokenCacheKey) {
    try (final AutoTenantContext ignored = new AutoTenantContext(tokenCacheKey.getTenant())) {
      logger.debug("Creating refresh token for {}", tokenCacheKey);

      final Optional<RsaKeyPairFactory.KeyPairHolder> optionalSigningKeyPair
              = tenantSignatureRepository.getLatestApplicationSigningKeyPair();

      final RsaKeyPairFactory.KeyPairHolder signingKeyPair = optionalSigningKeyPair.orElseThrow(AmitAuthenticationException::missingTenant);

      final TenantRefreshTokenSerializer.Specification specification = new TenantRefreshTokenSerializer.Specification()
              .setSourceApplication(applicationName)
              .setUser(tokenCacheKey.getUser())
              .setSecondsToLive(REFRESH_TOKEN_LIFESPAN)
              .setPrivateKey(signingKeyPair.privateKey())
              .setKeyTimestamp(signingKeyPair.getTimestamp());

      tokenCacheKey.getEndpointSet().ifPresent(specification::setEndpointSet);

      return tenantRefreshTokenSerializer.build(specification);
    }
    catch (final Exception e) {
      logger.error("Couldn't create refresh token for {}.", tokenCacheKey, e);
      throw new NotFoundException("Couldn't create refresh token.");
    }
  }
}
