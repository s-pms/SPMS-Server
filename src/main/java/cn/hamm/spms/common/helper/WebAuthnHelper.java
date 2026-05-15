package cn.hamm.spms.common.helper;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.data.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <h1>WebAuthn 工具类</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Component
public class WebAuthnHelper {
    @Autowired
    private RelyingParty relyingParty;

    /**
     * 生成 WebAuthn 断言选项
     *
     * @param username 用户名
     * @param userHandle 用户句柄
     * @param credentialIds 允许的凭证 ID 列表
     * @return WebAuthn 断言选项
     */
    public StartAssertionOptions generateAssertionOptions(String username, ByteArray userHandle, List<ByteArray> credentialIds) {
        StartAssertionOptions.StartAssertionOptionsBuilder builder = StartAssertionOptions.builder()
            .username(username)
            .userHandle(userHandle);

        return builder.userVerification(UserVerificationRequirement.PREFERRED)
            .build();
    }
}
