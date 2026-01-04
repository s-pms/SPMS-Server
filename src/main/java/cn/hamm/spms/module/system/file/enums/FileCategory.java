package cn.hamm.spms.module.system.file.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import cn.hamm.spms.common.config.AppConstant;
import lombok.Getter;
import org.jetbrains.annotations.Contract;

/**
 * <h1>文件分类</h1>
 *
 * @author Hamm.cn
 */
@Getter
public enum FileCategory implements IDictionary {
    /**
     * 临时文件
     */
    TEMP(0, "临时文件", AppConstant.DEFAULT_EXTENSIONS),

    /**
     * 普通文件
     */
    NORMAL(1, "普通文件", AppConstant.DEFAULT_EXTENSIONS),

    /**
     * 头像
     */
    AVATAR(1001, "头像", new String[]{
            "jpg", "jpeg", "png"
    }),

    /**
     * 合同附件
     */
    CONTRACT_ATTACHMENT(2001, "合同附件", new String[]{
            "jpg", "jpeg", "png", "bmp", "pdf", "zip"
    }, true),
    ;

    private final int key;
    private final String label;
    private final String[] extensions;
    private final Boolean isProtected;

    @Contract(pure = true)
    FileCategory(int key, String label, String[] extensions) {
        this.key = key;
        this.label = label;
        this.extensions = extensions;
        this.isProtected = false;
    }

    @Contract(pure = true)
    FileCategory(int key, String label, String[] extensions, Boolean isProtected) {
        this.key = key;
        this.label = label;
        this.extensions = extensions;
        this.isProtected = isProtected;
    }
}
