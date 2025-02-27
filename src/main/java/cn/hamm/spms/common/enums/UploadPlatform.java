package cn.hamm.spms.common.enums;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>上传平台</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum UploadPlatform implements IDictionary {
    /**
     * <h3>本地上传</h3>
     */
    LOCAL(0, "本地上传"),

    ALIYUN_OSS(1, "阿里云OSS上传");

    private final int key;
    private final String label;
}
