package cn.hamm.spms.module.asset.contract.document;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>合同附件数据源</h1>
 * <p>
 * 该实体此前只被 {@code ContractEntity.documentList} 的 {@code @ManyToMany} 引用，
 * 从未拥有独立的数据源。改为中间表实体后，附件本身需要能独立落库，故补充本数据源。
 * </p>
 *
 * @author Hamm.cn
 */
@Repository
public interface ContractDocumentRepository extends BaseRepository<ContractDocumentEntity> {
}
