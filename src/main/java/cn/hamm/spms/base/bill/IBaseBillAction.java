package cn.hamm.spms.base.bill;

/**
 * <h1>单据校验分组</h1>
 *
 * @author Hamm.cn
 */
public interface IBaseBillAction {
    /**
     * 驳回单据时生效的校验分组，此时「驳回原因」必填
     */
    interface WhenReject {
    }
}
