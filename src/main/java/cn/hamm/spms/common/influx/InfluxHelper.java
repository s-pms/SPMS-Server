package cn.hamm.spms.common.influx;

import cn.hamm.spms.common.Configs;
import cn.hamm.spms.module.iot.report.ReportConstant;
import cn.hamm.spms.module.iot.report.ReportInfluxPayload;
import cn.hamm.spms.module.iot.report.ReportPayload;
import cn.hamm.spms.module.iot.report.enums.ReportDataType;
import cn.hamm.spms.module.iot.report.enums.ReportGranularity;
import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.SERVICE_ERROR;
import static cn.hamm.spms.module.iot.report.enums.ReportDataType.*;
import static cn.hamm.spms.module.system.config.ConfigService.STRING_ONE;

/**
 * <h1>设备时序数据读写</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Configuration
public class InfluxHelper {
    public static final String INFLUX_FIELD_VALUE = "value";
    private static final String INFLUX_TAG_UUID = "uuid";
    private static final String INFLUX_SQL_SPLIT = " |> ";
    private static final String INFLUX_RECORD_VALUE_KEY = "_value";

    /**
     * InfluxDB 客户端
     * <p>
     * 必须声明为 {@code volatile}：{@code save()} 由 MQTT 回调线程调用，{@code query()} 由 HTTP
     * 工作线程调用，两者会并发读写这个字段，不加 volatile 会让其他线程长期看不到新值而反复重建客户端
     * </p>
     */
    private volatile InfluxDBClient influxDbClient;

    /**
     * 转义 Flux 字符串字面量
     *
     * @param value 原始字符串
     * @return 转义后的字符串
     * @apiNote Flux 的字符串用双引号包围，内部的 {@code "} 和 {@code \} 都要转义。
     * {@code uuid} 只有 {@code @NotBlank} 无格式校验，不转义的话任意登录用户传一个带引号的 uuid
     * 就能改写 {@code filter} 条件、读到其他设备的历史数据
     */
    private static @NotNull String escapeFlux(@Nullable String value) {
        if (Objects.isNull(value)) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * 保存采集数据
     *
     * @param code  数采参数编码
     * @param uuid  设备 ID
     * @param value 上报值，只支持数字、布尔、字符串
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
            InfluxConfig influxConfig = Configs.getInfluxConfig();
            writeApi.writePoint(influxConfig.getBucket(), influxConfig.getOrg(), point);
        }
    }

    /**
     * 获取写入 API
     *
     * @return 写入 API
     */
    private @Nullable WriteApiBlocking getWriteApi() {
        try {
            initInfluxDbClient();
            influxDbClient.setLogLevel(LogLevel.NONE);
            return influxDbClient.getWriteApiBlocking();
        } catch (Exception e) {
            // initInfluxDbClient() 失败时 influxDbClient 还是 null，对它 close() 抛出的 NPE
            // 会从 catch 块逃逸，盖掉本来要表达的「写入失败」
            log.warn("获取 InfluxDB 写入 API 失败，本次采集数据将丢失: {}", e.getMessage(), e);
            closeInfluxDbClient();
        }
        return null;
    }

    /**
     * 关闭并丢弃当前客户端
     * <p>
     * 关闭动作必须判空且自行吞掉异常：调用点往往正处于异常处理路径上，
     * 关闭失败不应该再抛一次异常把原始故障盖掉
     * </p>
     */
    private void closeInfluxDbClient() {
        InfluxDBClient client = influxDbClient;
        influxDbClient = null;
        if (Objects.isNull(client)) {
            return;
        }
        try {
            client.close();
        } catch (Exception e) {
            log.debug("关闭 InfluxDB 客户端失败，已忽略", e);
        }
    }

    /**
     * 查询数量数据
     *
     * @param payload           报告负载
     * @param reportGranularity 报告颗粒度
     * @return 数据点列表
     */
    public List<ReportInfluxPayload> queryQuantity(ReportPayload payload, ReportGranularity reportGranularity) {
        return query(payload, NUMBER, reportGranularity);
    }

    /**
     * 查询开关量数据
     *
     * @param payload           报告负载
     * @param reportGranularity 报告颗粒度
     * @return 数据点列表
     */
    public List<ReportInfluxPayload> querySwitch(ReportPayload payload, ReportGranularity reportGranularity) {
        return query(payload, BOOLEAN, reportGranularity);
    }

    /**
     * 查询文本数据
     *
     * @param payload           报告负载
     * @param reportGranularity 报告颗粒度
     * @return 数据点列表
     */
    public List<ReportInfluxPayload> queryInformation(ReportPayload payload, ReportGranularity reportGranularity) {
        return query(payload, STRING, reportGranularity);
    }

    /**
     * 查询状态数据
     *
     * @param payload           报告负载
     * @param reportGranularity 报告颗粒度
     * @return 数据点列表
     */
    public List<ReportInfluxPayload> queryStatus(ReportPayload payload, ReportGranularity reportGranularity) {
        return query(payload, STATUS, reportGranularity);
    }

    /**
     * 查询报告
     *
     * @param reportPayload     报告负载
     * @param reportDataType    数据类型
     * @param reportGranularity 报告颗粒度
     * @return 数据
     */
    private @NotNull List<ReportInfluxPayload> query(ReportPayload reportPayload, ReportDataType reportDataType, ReportGranularity reportGranularity) {
        List<String> queryParams = getFluxQuery(reportPayload, reportDataType, reportGranularity);
        String flux = String.join(INFLUX_SQL_SPLIT, queryParams);
        // 改回 System.out.println 会绕过 logback-spring.xml 与 traceId 关联，线上排障定位不到
        // 具体请求，查询量大时还会刷爆 stdout
        log.info("执行 Flux 查询: {}", flux);
        try {
            initInfluxDbClient();
            influxDbClient.setLogLevel(LogLevel.BASIC);
            List<ReportInfluxPayload> result = new ArrayList<>();
            QueryApi queryApi = influxDbClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(flux);
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
                            payload.setStrValue(Objects.isNull(value) ? "" : value.toString());
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
        } catch (Exception e) {
            // 不兜底的话 InfluxDB 宕机时异常会一路冒到 DeviceService，前端只看到一个 500。
            // 这里给出明确的业务错误，并丢弃可能半初始化的客户端以便下次重建
            log.error("查询 InfluxDB 失败, code={}, uuid={}: {}", reportPayload.getCode(),
                    reportPayload.getUuid(), e.getMessage(), e);
            closeInfluxDbClient();
            SERVICE_ERROR.show("时序数据库暂时不可用，请稍后重试");
            return List.of();
        }
    }

    /**
     * 初始化 InfluxDB 客户端
     * <p>
     * {@code synchronized} + 双重检查不可去掉：采集端高频写入与前端高频查询并发时，
     * 两个线程可以同时通过 null 判断各建一个客户端（每个客户端各含一个 OkHttp 连接池），
     * 被覆盖的那个永远不会被 close，造成连接与线程泄漏
     * </p>
     */
    private void initInfluxDbClient() {
        if (Objects.nonNull(influxDbClient)) {
            return;
        }
        synchronized (this) {
            if (Objects.nonNull(influxDbClient)) {
                return;
            }
            InfluxConfig influxConfig = Configs.getInfluxConfig();
            String token = influxConfig.getToken();
            if (Objects.isNull(token) || token.isBlank()) {
                throw new IllegalStateException("未配置 app.influxdb.token，无法连接 InfluxDB");
            }
            influxDbClient = InfluxDBClientFactory.create(
                    influxConfig.getUrl(),
                    token.toCharArray(),
                    influxConfig.getOrg(),
                    influxConfig.getBucket()
            );
            log.info("InfluxDB 客户端已初始化: {}", influxConfig.getUrl());
        }
    }

    /**
     * 获取查询参数
     *
     * @param reportPayload     数采报告
     * @param reportDataType    数据类型
     * @param reportGranularity 报告颗粒度
     * @return 参数列表
     * @apiNote 拼进 Flux 的每个变量都要过 {@link #escapeFlux(String)}。{@code code} 已由
     * {@code DeviceService} 校验过是已注册参数，真正缺格式校验的注入入口是 {@code uuid}，
     * 但两个都转义，避免以后放宽 code 校验时留下缺口
     */
    private @NotNull List<String> getFluxQuery(@NotNull ReportPayload reportPayload, ReportDataType reportDataType, ReportGranularity reportGranularity) {
        List<String> queryParams = new ArrayList<>();
        InfluxConfig influxConfig = Configs.getInfluxConfig();
        queryParams.add(String.format("from(bucket:\"%s\")", influxConfig.getBucket()));
        queryParams.add(String.format("range(start: %s, stop: %s)", Integer.parseInt(String.valueOf(reportPayload.getStartTime() / 1000)), Integer.parseInt(String.valueOf(reportPayload.getEndTime() / 1000))));
        // code 已由 DeviceService.getDevicePayloadHistory 用 getByCode 校验过必须是已注册参数，
        // 但 uuid 只有 @NotBlank 无格式校验，是真正的注入入口，两个都做转义
        queryParams.add(String.format("filter(fn: (r) => r._measurement == \"%s\" and r.uuid == \"%s\")",
                escapeFlux(ReportConstant.CACHE_PREFIX + reportPayload.getCode()),
                escapeFlux(reportPayload.getUuid())));
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
