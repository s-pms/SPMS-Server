package cn.hamm.spms.common.third.aliyun.oss;

import cn.hamm.spms.common.third.aliyun.AliyunConfig;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.OSSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * <h1>阿里云OSS工具类</h1>
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
     * 从阿里云OSS获取文件流
     *
     * @param path 路径
     * @return 文件流
     */
    public InputStream download(String path) {
        OSS ossClient = getClient();
        try {
            OSSObject ossObject = ossClient.getObject(aliyunOssConfig.getBucketName(), path);
            return ossObject.getObjectContent();
        } catch (Exception ignored) {
        }
        if (ossClient != null) {
            ossClient.shutdown();
        }
        return null;
    }

    /**
     * 上传到阿里云OSS
     *
     * @param fileName 保存对象名称
     * @param bytes    文件
     */
    public void upload(String fileName, byte[] bytes) {
        OSS ossClient = getClient();
        ossClient.putObject(aliyunOssConfig.getBucketName(), fileName, new ByteArrayInputStream(bytes));
        ossClient.shutdown();
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    private OSS getClient() {
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
        return new OSSClientBuilder().build(aliyunOssConfig.getEndpoint(), credentialsProvider);
    }
}
