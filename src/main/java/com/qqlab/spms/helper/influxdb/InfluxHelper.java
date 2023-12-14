package com.qqlab.spms.helper.influxdb;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.qqlab.spms.module.iot.report.ReportDataType;
import com.qqlab.spms.module.iot.report.ReportEvent;
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
     * 使用的数据库
     */
    @Value("${spring.influxdb.bucket:''}")
    private String bucket;

    private InfluxDBClient influxDbClient;

    /**
     * 保存数据
     *
     * @param code  参数名
     * @param value 数据
     * @param uuid  设备ID
     */
    public void save(String code, double value, String uuid) {
        WriteApiBlocking writeApi = getWriteApi();
        if (Objects.nonNull(writeApi)) {
            writeApi.writePoint(bucket, org, new Point(ReportEvent.CACHE_PREFIX + code)
                    .addField("value", value)
                    .addTag("uuid", uuid)
            );
        }
    }

    /**
     * 保存数据
     *
     * @param code  参数名
     * @param value 数据
     * @param uuid  设备ID
     */
    public void save(String code, String value, String uuid) {
        WriteApiBlocking writeApi = getWriteApi();
        if (Objects.nonNull(writeApi)) {
            writeApi.writePoint(bucket, org, new Point(ReportEvent.CACHE_PREFIX + code)
                    .addField("value", value)
                    .addTag("uuid", uuid)
            );
        }
    }


    /**
     * 保存数据
     *
     * @param code  参数名
     * @param value 数据
     * @param uuid  设备ID
     */
    public void save(String code, int value, String uuid) {
        WriteApiBlocking writeApi = getWriteApi();
        if (Objects.nonNull(writeApi)) {
            writeApi.writePoint(bucket, org, new Point(ReportEvent.CACHE_PREFIX + code)
                    .addField("value", value)
                    .addTag("uuid", uuid)
            );
        }
    }

    private WriteApiBlocking getWriteApi() {
        if (Objects.isNull(influxDbClient)) {
            influxDbClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
            influxDbClient.setLogLevel(LogLevel.NONE);
        }
        WriteApiBlocking writeApi = influxDbClient.getWriteApiBlocking();
        try {
            return writeApi;
        } catch (Exception ignored) {
            influxDbClient.close();
            influxDbClient = null;
        }
        return null;
    }

    public List<ReportInfluxPayload> queryDouble(String uuid, String code) {
        return query(uuid, code, ReportDataType.DOUBLE);
    }

    public List<ReportInfluxPayload> queryBool(String uuid, String code) {
        return query(uuid, code, ReportDataType.BOOL);
    }

    public List<ReportInfluxPayload> queryString(String uuid, String code) {
        return query(uuid, code, ReportDataType.STRING);
    }

    public List<ReportInfluxPayload> queryInt(String uuid, String code) {
        return query(uuid, code, ReportDataType.INT);
    }

    private List<ReportInfluxPayload> query(String uuid, String code, ReportDataType reportDataType) {
        if (Objects.isNull(influxDbClient)) {
            influxDbClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
            influxDbClient.setLogLevel(LogLevel.BASIC);
        }
        List<ReportInfluxPayload> result = new ArrayList<>();
        QueryApi queryApi = influxDbClient.getQueryApi();
        List<String> queryParams = new ArrayList<>();
        queryParams.add(String.format("from(bucket:\"%s\")", bucket));
        queryParams.add("range(start: -24h)");
        queryParams.add(String.format("filter(fn: (r) => r._measurement == \"%s\" and r.uuid == \"%s\")", ReportEvent.CACHE_PREFIX + code, uuid));
        queryParams.add("filter(fn: (r) => r._field == \"value\")");
        switch (reportDataType) {
            case DOUBLE:
                queryParams.add("aggregateWindow(every: 1m, fn: min)");
                queryParams.add("fill(usePrevious: true)");
                break;
            default:
        }
        List<FluxTable> tables = queryApi.query(String.join("|>", queryParams));
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object value = record.getValueByKey("_value");
                ReportInfluxPayload payload = new ReportInfluxPayload()
                        .setTimestamp(Objects.requireNonNull(record.getTime()).toEpochMilli());
                switch (reportDataType) {
                    case DOUBLE:
                        payload.setValue(Objects.isNull(value) ? 0 : Double.parseDouble(value.toString()));
                        break;
                    case STRING:
                        payload.setStrValue(Objects.isNull(value) ? "" : value.toString());
                        break;
                    case BOOL:
                        payload.setBoolValue(!Objects.isNull(value) && "1".equals(value.toString()));
                        break;
                    case INT:
                        payload.setIntValue(Objects.isNull(value) ? 0 : Integer.parseInt(value.toString()));
                        break;
                    default:
                        continue;
                }
                result.add(payload);
            }
        }
        return result;
    }

}