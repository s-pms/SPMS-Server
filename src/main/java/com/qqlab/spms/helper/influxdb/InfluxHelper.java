package com.qqlab.spms.helper.influxdb;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * @author Hamm
 */
@Configuration
public class InfluxHelper {
    /**
     * <h2>连接地址</h2>
     */
    @Value("${spring.influxdb.url:''}")
    private String url;

    /**
     * <h2>连接令牌</h2>
     */
    @Value("${spring.influxdb.token:''}")
    private String token;

    /**
     * <h2>使用的组织名</h2>
     */
    @Value("${spring.influxdb.org:''}")
    private String org;

    /**
     * <h2>使用的存储库</h2>
     */
    @Value("${spring.influxdb.bucket:''}")
    private String bucket;

    private InfluxDBClient influxDbClient;

    /**
     * <h2>保存数据</h2>
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

//    public List<FluxTable> findAll() {
//        InfluxDBClient influxDbClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
//        influxDbClient.setLogLevel(LogLevel.BASIC);
//        String flux = String.format("from(bucket: \"%s\") |> range(start: 0)", bucket);
//        QueryApi queryApi = influxDbClient.getQueryApi();
//        return queryApi.query(flux, org);
//    }
}