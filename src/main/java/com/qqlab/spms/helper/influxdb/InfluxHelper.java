package com.qqlab.spms.helper.influxdb;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.qqlab.spms.module.iot.report.ReportInfluxPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
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

    public List<ReportInfluxPayload> query(String uuid, String code) {
        if (Objects.isNull(influxDbClient)) {
            influxDbClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
            influxDbClient.setLogLevel(LogLevel.BASIC);
        }
        List<ReportInfluxPayload> result = new ArrayList<>();
        QueryApi queryApi = influxDbClient.getQueryApi();
        List<String> queryParams = new ArrayList<>();
        queryParams.add(String.format("from(bucket:\"%s\")", bucket));
        queryParams.add("range(start: -2h)");
        queryParams.add(String.format("filter(fn: (r) => r._measurement == \"report\" and r.code == \"%s\")", code));
        queryParams.add("filter(fn: (r) => r._measurement == \"report\" and r._field == \"value\")");
        queryParams.add("aggregateWindow(every: 1m, fn: min)");
        queryParams.add("fill(usePrevious: true)");
        List<FluxTable> tables = queryApi.query(String.join("|>", queryParams));
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object value = record.getValueByKey("_value");
                if (Objects.isNull(value)) {
                    value = 0;
                }
                result.add(new ReportInfluxPayload()
                        .setValue(Double.valueOf(value.toString()))
                        .setTimestamp(Objects.requireNonNull(record.getTime()).toEpochMilli())
                );
            }
        }
        return result;
    }
}