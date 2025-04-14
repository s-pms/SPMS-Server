package cn.hamm.spms.module.system.file;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>文件分类</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum FileCategory implements IDictionary {
    /**
     * <h3>临时文件</h3>
     */
    TEMP(0, "临时文件"),

    /**
     * <h3>普通文件</h3>
     */
    NORMAL(1, "普通文件"),

    /**
     * <h3>头像</h3>
     */
    AVATAR(1001, "头像"),

    /**
     * <h3>合同附件</h3>
     */
    CONTRACT_ATTACHMENT(2001, "合同附件"),
    ;

    private final int key;
    private final String label;
    private final Boolean isProtected = false;
}
