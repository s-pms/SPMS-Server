package cn.hamm.spms.module.system.file.enums;

import cn.hamm.airpower.web.dictionary.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>上传平台</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum FileUploadPlatform implements IDictionary {
    /**
     * 本地上传
     */
    LOCAL(0, "本地上传"),

    ALIYUN_OSS(1, "阿里云 OSS 上传");

    private final int key;
    private final String label;
}
