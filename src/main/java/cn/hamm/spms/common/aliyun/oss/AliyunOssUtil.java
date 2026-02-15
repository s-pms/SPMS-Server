package cn.hamm.spms.common.aliyun.oss;

import cn.hamm.airpower.core.DateTimeUtil;
import cn.hamm.spms.common.aliyun.AliyunConfig;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.OSSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static cn.hamm.airpower.exception.Errors.PARAM_INVALID;

/**
 * <h1>阿里云 OSS 工具类</h1>
 *
 * @author zhoul
 */
@Component
public class AliyunOssUtil {
    @Autowired
    private AliyunConfig aliyunConfig;

    @Autowired
    private AliyunOssConfig aliyunOssConfig;

    /**
     * 从阿里云 OSS 获取文件流
     *
     * @param path 路径
     * @return 文件流
     */
    public InputStream download(String path) {
        OSS ossClient = getClient();
        PARAM_INVALID.whenEmpty(aliyunOssConfig.getBucketName(), "请配置阿里云的 BucketName");
        OSSObject ossObject = ossClient.getObject(aliyunOssConfig.getBucketName(), path);
        ossClient.shutdown();
        return ossObject.getObjectContent();
    }

    /**
     * 获取文件 URL
     *
     * @param path 文件路径
     * @return 文件 URL
     */
    public String getUrl(String path) {
        OSS ossClient = getClient();
        String url = ossClient.generatePresignedUrl(getBucketName(), path, DateTimeUtil.addDays(7)).toString();
        ossClient.shutdown();
        return url;
    }

    /**
     * 获取 BucketName
     *
     * @return BucketName
     */
    private String getBucketName() {
        String bucketName = aliyunOssConfig.getBucketName();
        PARAM_INVALID.whenEmpty(bucketName, "请配置阿里云的 BucketName");
        return bucketName;
    }

    /**
     * 上传到阿里云 OSS
     *
     * @param fileName 保存对象名称
     * @param bytes    文件
     */
    public void upload(String fileName, byte[] bytes) {
        OSS ossClient = getClient();
        PARAM_INVALID.whenEmpty(aliyunOssConfig.getBucketName(), "请配置阿里云的 BucketName");
        ossClient.putObject(aliyunOssConfig.getBucketName(), fileName, new ByteArrayInputStream(bytes));
        ossClient.shutdown();
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    private OSS getClient() {
        PARAM_INVALID.whenEmpty(aliyunConfig.getAccessKeyId(), "请配置阿里云的 AccessKeyId");
        PARAM_INVALID.whenEmpty(aliyunConfig.getAccessKeySecret(), "请配置阿里云的 AccessKeySecret");
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
        PARAM_INVALID.whenEmpty(aliyunOssConfig.getEndpoint(), "请配置阿里云的 Endpoint");
        return new OSSClientBuilder().build(aliyunOssConfig.getEndpoint(), credentialsProvider);
    }
}
