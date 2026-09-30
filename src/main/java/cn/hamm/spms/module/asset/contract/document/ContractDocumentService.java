package cn.hamm.spms.module.asset.contract.document;

import cn.hamm.spms.base.BaseService;
import org.springframework.stereotype.Service;

/**
 * <h1>合同附件服务</h1>
 * <p>
 * 该实体此前只作为 {@code @ManyToMany} 的目标端存在，没有独立服务。
 * 改为中间表实体后，附件需要能被单独落库，故补充本服务。
 * </p>
 *
 * @author Hamm.cn
 */
@Service
public class ContractDocumentService extends BaseService<ContractDocumentEntity, ContractDocumentRepository> {
}
