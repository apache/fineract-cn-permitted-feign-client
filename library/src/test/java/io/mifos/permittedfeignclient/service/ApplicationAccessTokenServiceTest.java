package io.mifos.permittedfeignclient.service;

import io.mifos.anubis.config.TenantSignatureRepository;
import io.mifos.anubis.token.TenantRefreshTokenSerializer;
import io.mifos.anubis.token.TokenSerializationResult;
import io.mifos.core.lang.ApplicationName;
import io.mifos.core.lang.AutoTenantContext;
import io.mifos.core.lang.security.RsaKeyPairFactory;
import io.mifos.identity.api.v1.client.IdentityManager;
import io.mifos.identity.api.v1.domain.Authentication;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author Myrle Krantz
 */
public class ApplicationAccessTokenServiceTest {
  private static final String APP_NAME = "app-v1";
  private static final String BEARER_TOKEN_MOCK = "bearer token mock";
  private static final String USER_NAME = "user";
  private static final String TENANT_NAME = "tenant";

  @Test
  public void testHappyCase() {
    final ApplicationName applicationNameMock = Mockito.mock(ApplicationName.class);
    Mockito.when(applicationNameMock.toString()).thenReturn(APP_NAME);

    final TenantSignatureRepository tenantSignatureRepositoryMock = Mockito.mock(TenantSignatureRepository.class);
    final Optional<RsaKeyPairFactory.KeyPairHolder> keyPair = Optional.of(RsaKeyPairFactory.createKeyPair());
    Mockito.when(tenantSignatureRepositoryMock.getLatestApplicationSigningKeyPair()).thenReturn(keyPair);

    final IdentityManager identityManagerMock = Mockito.mock(IdentityManager.class);
    Mockito.when(identityManagerMock.refresh(Mockito.anyString()))
            .thenReturn(new Authentication(BEARER_TOKEN_MOCK, "accesstokenexpiration", "refreshtokenexpiration", null));

    final TenantRefreshTokenSerializer tenantRefreshTokenSerializerMock = Mockito.mock(TenantRefreshTokenSerializer.class);
    Mockito.when(tenantRefreshTokenSerializerMock.build(Mockito.anyObject()))
            .thenReturn(new TokenSerializationResult(BEARER_TOKEN_MOCK, LocalDateTime.now()));

    final ApplicationAccessTokenService testSubject = new ApplicationAccessTokenService(
            applicationNameMock,
            tenantSignatureRepositoryMock,
            identityManagerMock,
            tenantRefreshTokenSerializerMock);

    try (final AutoTenantContext ignored1 = new AutoTenantContext(TENANT_NAME)) {
      final String accessToken = testSubject.getAccessToken(USER_NAME, "blah");
      Assert.assertEquals(BEARER_TOKEN_MOCK, accessToken);

      final String accessTokenAgain = testSubject.getAccessToken(USER_NAME, "blah");
      Assert.assertEquals(BEARER_TOKEN_MOCK, accessTokenAgain);
    }
  }
}