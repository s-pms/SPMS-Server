package cn.hamm.spms.module.iot.report;

/**
 * <h1>数据采集报告动作</h1>
 *
 * @author Hamm.cn
 * @apiNote 供校验分组用的标记接口，无方法定义
 */
public interface IReportPayloadAction {
    /**
     * 获取设备某个参数的历史
     */
    interface WhenGetDevicePayloadHistory {
    }
}
