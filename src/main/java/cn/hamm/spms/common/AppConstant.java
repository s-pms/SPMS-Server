package cn.hamm.spms.common;

/**
 * <h1>系统常量</h1>
 *
 * @author Hamm.cn
 */
public class AppConstant {
    /**
     * 应用自定义异常码基数，{@code CustomError} 的枚举序号会自动加上它
     */
    public static final int BASE_CUSTOM_ERROR = 200000;

    /**
     * 超级管理员用户 ID，命中即跳过全部权限校验
     */
    public static final long ROOT_USER_ID = 1L;

    /**
     * 默认允许上传的文件后缀
     */
    public static final String[] DEFAULT_EXTENSIONS = new String[]{
            "jpg", "jpeg", "png", "gif", "bmp",
            "mp4",
            "mp3", "wav", "wma",
            "zip", "rar", "7z", "tar", "gz",
            "pdf", "doc", "docx", "xls", "xlsx",
            "markdown"
    };
}
