package cn.hamm.spms.common.config;

/**
 * <h1>系统常量</h1>
 *
 * @author Hamm.cn
 */
public class AppConstant {
    /**
     * 应用自定义异常代码基数
     */
    public static final int BASE_CUSTOM_ERROR = 200000;

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
