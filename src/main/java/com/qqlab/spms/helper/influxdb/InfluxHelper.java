package com.qqlab.spms.helper.influxdb;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.Query;
import com.influxdb.client.domain.WritePrecision;
import com.qqlab.spms.module.iot.report.ReportPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Objects;

/**
 * @author Hamm
 */
@Configuration
public class InfluxHelper {
    /**
     * 连接地址
     */
    @Value("${spring.influxdb.url:''}")
    private String url;

    /**
     * 连接令牌
     */
    @Value("${spring.influxdb.token:''}")
    private String token;

    /**
     * 使用的组织名
     */
    @Value("${spring.influxdb.org:''}")
    private String org;

    /**
     * 使用的存储库
     */
    @Value("${spring.influxdb.bucket:''}")
    private String bucket;

    private InfluxDBClient influxDbClient;

    /**
     * 保存数据
     *
     * @param data 数据
     */
    public void save(Object data) {
        if (Objects.isNull(influxDbClient)) {
            influxDbClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
            influxDbClient.setLogLevel(LogLevel.NONE);
        }
        WriteApiBlocking writeApi = influxDbClient.getWriteApiBlocking();
        try {
            writeApi.writeMeasurement(bucket, org, WritePrecision.NS, data);
        } catch (Exception ignored) {
            influxDbClient.close();
        }
    }

    /**
     * 查询数据
     *
     * @param query 查询语句
     * @return 返回结果
     */
    public List<ReportPayload> query(String query) {
        if (Objects.isNull(influxDbClient)) {
            influxDbClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
            influxDbClient.setLogLevel(LogLevel.NONE);
        }
        QueryApi queryApi = influxDbClient.getQueryApi();
        return queryApi.query(new Query().query(query), ReportPayload.class);
    }
}