package cn.hamm.spms.common.config;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * <h1>WebAuthn 配置类</h1>
 *
 * @author Hamm.cn
 */
@Configuration
public class WebAuthnConfig {
    @Autowired
    private AppConfig appConfig;

    /**
     * 创建 WebAuthn RelyingParty Bean
     *
     * @return RelyingParty
     */
    @Bean
    public RelyingParty relyingParty() {
        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
            .id("spms.hamm.cn") // 依赖方 ID，通常是域名
            .name(appConfig.getProjectName()) // 项目名称
            .build();

        return RelyingParty.builder()
            .identity(rpIdentity)
            .credentialRepository(new InMemoryCredentialRepository())
            .allowUntrustedAttestation(false)
            .validateSignatureCounter(true)
            .build();
    }

    /**
     * 内存中存储凭证信息的仓库（实际项目中应该使用数据库存储）
     */
    private static class InMemoryCredentialRepository implements com.yubico.webauthn.CredentialRepository {
        @Override
        public java.util.Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
            // 这里应该从数据库中获取用户的凭证 ID 信息
            return Collections.emptySet();
        }

        @Override
        public java.util.Optional<ByteArray> getUserHandleForUsername(String username) {
            // 这里应该从数据库中获取用户的 userHandle
            return java.util.Optional.of(new ByteArray("testuserhandle".getBytes()));
        }

        @Override
        public java.util.Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
            // 这里应该从数据库中获取用户信息
            return java.util.Optional.of("testuser");
        }

        @Override
        public java.util.Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
            // 这里应该从数据库中查询凭证信息
            return java.util.Optional.empty();
        }

        @Override
        public java.util.Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
            // 这里应该从数据库中查询所有与该凭证 ID 相关的记录
            return Collections.emptySet();
        }
    }
}
