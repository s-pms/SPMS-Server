package com.qqlab.spms.helper.influxdb;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.qqlab.spms.module.iot.report.*;
import org.jetbrains.annotations.NotNull;
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

    public List<ReportInfluxPayload> queryQuantity(ReportPayload payload, ReportGranularity reportGranularity) {
        return query(payload, ReportDataType.QUANTITY, reportGranularity);
    }

    public List<ReportInfluxPayload> querySwitch(ReportPayload payload, ReportGranularity reportGranularity) {
        return query(payload, ReportDataType.SWITCH, reportGranularity);
    }

    public List<ReportInfluxPayload> queryInformation(ReportPayload payload, ReportGranularity reportGranularity) {
        return query(payload, ReportDataType.INFORMATION, reportGranularity);
    }

    public List<ReportInfluxPayload> queryStatus(ReportPayload payload, ReportGranularity reportGranularity) {
        return query(payload, ReportDataType.STATUS, reportGranularity);
    }

    private List<ReportInfluxPayload> query(ReportPayload reportPayload, ReportDataType reportDataType, ReportGranularity reportGranularity) {
        if (Objects.isNull(influxDbClient)) {
            influxDbClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
            influxDbClient.setLogLevel(LogLevel.BASIC);
        }
        List<ReportInfluxPayload> result = new ArrayList<>();
        QueryApi queryApi = influxDbClient.getQueryApi();
        List<String> queryParams = getFluxQuery(reportPayload, reportDataType, reportGranularity);
        List<FluxTable> tables = queryApi.query(String.join("|>", queryParams));
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object value = record.getValueByKey("_value");
                ReportInfluxPayload payload = new ReportInfluxPayload()
                        .setTimestamp(Objects.requireNonNull(record.getTime()).toEpochMilli());
                switch (reportDataType) {
                    case QUANTITY:
                        payload.setValue(Objects.isNull(value) ? 0 : Double.parseDouble(value.toString()));
                        break;
                    case INFORMATION:
                        payload.setStrValue(Objects.isNull(value) ? "" : value.toString());
                        break;
                    case SWITCH:
                        payload.setBoolValue(!Objects.isNull(value) && "1".equals(value.toString()));
                        break;
                    case STATUS:
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

    @NotNull
    private List<String> getFluxQuery(ReportPayload reportPayload, ReportDataType reportDataType, ReportGranularity reportGranularity) {
        List<String> queryParams = new ArrayList<>();
        queryParams.add(String.format("from(bucket:\"%s\")", bucket));
        queryParams.add(String.format("range(start: %s, stop: %s)", Integer.parseInt(String.valueOf(reportPayload.getStartTime() / 1000)), Integer.parseInt(String.valueOf(reportPayload.getEndTime() / 1000))));
        queryParams.add(String.format("filter(fn: (r) => r._measurement == \"%s\" and r.uuid == \"%s\")", ReportEvent.CACHE_PREFIX + reportPayload.getCode(), reportPayload.getUuid()));
        queryParams.add("filter(fn: (r) => r._field == \"value\")");
        if (Objects.requireNonNull(reportDataType) == ReportDataType.QUANTITY) {
            queryParams.add("aggregateWindow(every: " + reportGranularity.getMark() + ", fn: mean)");
            queryParams.add("fill(usePrevious: true)");
        }
        return queryParams;
    }

}