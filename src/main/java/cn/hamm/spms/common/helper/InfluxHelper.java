package cn.hamm.spms.common.helper;

import cn.hamm.spms.common.config.InfluxConfig;
import cn.hamm.spms.module.iot.report.*;
import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.config.Constant.*;
import static cn.hamm.spms.module.iot.report.ReportDataType.*;

/**
 * <h1>Influx助手类</h1>
 *
 * @author Hamm.cn
 */
@Configuration
public class InfluxHelper {
    public static final String INFLUX_FIELD_VALUE = STRING_VALUE;
    private static final String INFLUX_TAG_UUID = "uuid";
    private static final String INFLUX_SQL_SPLIT = " |> ";
    private static final String INFLUX_RECORD_VALUE_KEY = "_value";

    @Autowired
    private InfluxConfig influxConfig;

    private InfluxDBClient influxDbClient;

    /**
     * <h3>保存数据</h3>
     *
     * @param code  参数名
     * @param uuid  设备ID
     * @param value 数据
     */
    public void save(String code, String uuid, Object value) {
        WriteApiBlocking writeApi = getWriteApi();
        if (Objects.nonNull(writeApi)) {
            Point point = new Point(ReportConstant.CACHE_PREFIX + code)
                    .addTag(INFLUX_TAG_UUID, uuid);
            if (value instanceof Number numberValue) {
                point.addField(INFLUX_FIELD_VALUE, numberValue);
            } else if (value instanceof Boolean booleanValue) {
                point.addField(INFLUX_FIELD_VALUE, booleanValue);
            } else if (value instanceof String stringValue) {
                point.addField(INFLUX_FIELD_VALUE, stringValue);
            } else {
                throw new RuntimeException("不支持的数据类型");
            }
            writeApi.writePoint(influxConfig.getBucket(), influxConfig.getOrg(), point);
        }
    }

    /**
     * <h3>获取写入API</h3>
     *
     * @return 写入API
     */
    private @Nullable WriteApiBlocking getWriteApi() {
        try {
            initInfluxDbClient();
            influxDbClient.setLogLevel(LogLevel.NONE);
            return influxDbClient.getWriteApiBlocking();
        } catch (Exception ignored) {
            influxDbClient.close();
            influxDbClient = null;
        }
        return null;
    }

    /**
     * <h3>查询数量</h3>
     *
     * @param payload           报告负载
     * @param reportGranularity 报告颗粒度
     * @return 数据
     */
    public List<ReportInfluxPayload> queryQuantity(ReportPayload payload, ReportGranularity reportGranularity) {
        return query(payload, NUMBER, reportGranularity);
    }

    /**
     * <h3>查询是否开启</h3>
     *
     * @param payload           报告负载
     * @param reportGranularity 报告颗粒度
     * @return 数据
     */
    public List<ReportInfluxPayload> querySwitch(ReportPayload payload, ReportGranularity reportGranularity) {
        return query(payload, BOOLEAN, reportGranularity);
    }

    /**
     * <h3>查询报告信息</h3>
     *
     * @param payload           报告负载
     * @param reportGranularity 报告颗粒度
     * @return 数据
     */
    public List<ReportInfluxPayload> queryInformation(ReportPayload payload, ReportGranularity reportGranularity) {
        return query(payload, STRING, reportGranularity);
    }

    /**
     * <h3>查询报告状态</h3>
     *
     * @param payload           报告负载
     * @param reportGranularity 报告颗粒度
     * @return 数据
     */
    public List<ReportInfluxPayload> queryStatus(ReportPayload payload, ReportGranularity reportGranularity) {
        return query(payload, STATUS, reportGranularity);
    }

    /**
     * <h3>查询报告</h3>
     *
     * @param reportPayload     报告负载
     * @param reportDataType    数据类型
     * @param reportGranularity 报告颗粒度
     * @return 数据
     */
    private @NotNull List<ReportInfluxPayload> query(ReportPayload reportPayload, ReportDataType reportDataType, ReportGranularity reportGranularity) {
        initInfluxDbClient();
        influxDbClient.setLogLevel(LogLevel.BASIC);
        List<ReportInfluxPayload> result = new ArrayList<>();
        QueryApi queryApi = influxDbClient.getQueryApi();
        List<String> queryParams = getFluxQuery(reportPayload, reportDataType, reportGranularity);
        System.out.println(String.join(INFLUX_SQL_SPLIT, queryParams));
        List<FluxTable> tables = queryApi.query(String.join(INFLUX_SQL_SPLIT, queryParams));
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object value = record.getValueByKey(INFLUX_RECORD_VALUE_KEY);
                ReportInfluxPayload payload = new ReportInfluxPayload()
                        .setTimestamp(Objects.requireNonNull(record.getTime()).toEpochMilli());
                switch (reportDataType) {
                    case NUMBER:
                        payload.setValue(Objects.isNull(value) ? 0 : Double.parseDouble(value.toString()));
                        break;
                    case STRING:
                        payload.setStrValue(Objects.isNull(value) ? STRING_EMPTY : value.toString());
                        break;
                    case BOOLEAN:
                        payload.setBoolValue(!Objects.isNull(value) && STRING_ONE.equals(value.toString()));
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

    /**
     * <h3>初始化InfluxDB</h3>
     */
    private void initInfluxDbClient() {
        if (Objects.isNull(influxDbClient)) {
            influxDbClient = InfluxDBClientFactory.create(
                    influxConfig.getUrl(),
                    influxConfig.getToken().toCharArray(),
                    influxConfig.getOrg(),
                    influxConfig.getBucket()
            );
        }
    }

    /**
     * <h3>获取查询参数</h3>
     *
     * @param reportPayload     数采报告
     * @param reportDataType    数据类型
     * @param reportGranularity 报告颗粒度
     * @return 参数列表
     */
    private @NotNull List<String> getFluxQuery(@NotNull ReportPayload reportPayload, ReportDataType reportDataType, ReportGranularity reportGranularity) {
        List<String> queryParams = new ArrayList<>();
        queryParams.add(String.format("from(bucket:\"%s\")", influxConfig.getBucket()));
        queryParams.add(String.format("range(start: %s, stop: %s)", Integer.parseInt(String.valueOf(reportPayload.getStartTime() / 1000)), Integer.parseInt(String.valueOf(reportPayload.getEndTime() / 1000))));
        queryParams.add(String.format("filter(fn: (r) => r._measurement == \"%s\" and r.uuid == \"%s\")", ReportConstant.CACHE_PREFIX + reportPayload.getCode(), reportPayload.getUuid()));
        queryParams.add("filter(fn: (r) => r._field == \"value\")");
        if (Objects.requireNonNull(reportDataType) == NUMBER) {
            queryParams.add("aggregateWindow(every: " + reportGranularity.getMark() + ", fn: mean)");
            queryParams.add("fill(usePrevious: true)");
        } else {
            queryParams.add("limit(n: 500)");
        }
        return queryParams;
    }

}
