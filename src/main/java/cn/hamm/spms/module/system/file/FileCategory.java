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
     * 临时文件
     */
    TEMP(0, "临时文件"),

    /**
     * 普通文件
     */
    NORMAL(1, "普通文件"),

    /**
     * 头像
     */
    AVATAR(1001, "头像"),

    /**
     * 合同附件
     */
    CONTRACT_ATTACHMENT(2001, "合同附件"),
    ;

    private final int key;
    private final String label;
    private final Boolean isProtected = false;
}
