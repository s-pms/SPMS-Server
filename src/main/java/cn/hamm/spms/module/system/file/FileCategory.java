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
     * <h3>普通文件</h3>
     */
    NORMAL(1, "普通文件"),

    /**
     * <h3>临时文件</h3>
     */
    TEMP(2, "临时文件"),

    /**
     * <h3>图片</h3>
     */
    IMAGE(3, "图片"),

    /**
     * <h3>视频</h3>
     */
    VIDEO(4, "视频"),

    /**
     * <h3>音频</h3>
     */
    AUDIO(5, "音频"),

    /**
     * <h3>头像</h3>
     */
    AVATAR(6, "头像"),
    ;

    private final int key;
    private final String label;
    private final Boolean isProtected = false;
}
