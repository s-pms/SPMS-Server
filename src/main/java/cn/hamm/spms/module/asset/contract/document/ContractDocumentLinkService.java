package cn.hamm.spms.module.asset.contract.document;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.asset.contract.ContractEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <h1>合同附件关联服务</h1>
 * <p>
 * 承载 {@code contract <-> document} 的多对多关系，取代 {@code @ManyToMany}。
 * </p>
 *
 * @author Hamm.cn
 */
@Service
public class ContractDocumentLinkService extends BaseService<ContractDocumentLinkEntity, ContractDocumentLinkRepository> {

    /**
     * 查询某个合同的附件
     *
     * @param contractId 合同 ID
     * @return 附件集合
     */
    public @NotNull Set<ContractDocumentEntity> getDocuments(long contractId) {
        List<ContractDocumentLinkEntity> links = filter(
                new ContractDocumentLinkEntity().setContract(new ContractEntity().setId(contractId)));
        return links.stream()
                .map(ContractDocumentLinkEntity::getDocument)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 把合同的附件同步到中间表（全量覆盖）
     *
     * @param contractId 合同 ID
     * @param documents  前端提交的附件
     */
    public void syncByContractId(long contractId, @NotNull Collection<ContractDocumentEntity> documents) {
        // 先清掉旧关联。
        // 必须用 deleteAll + flush，不能写成 forEach(this::delete)：
        // CurdService.delete(long) 走的是 TransactionHelper.run(Function)，
        // 而该重载没有 @Transactional（只有 run(Supplier) 有），
        // 循环里下一条的 get(id) 查询会触发 auto-flush 把上一条刷进库，
        // **最后一条的删除标记永远丢失**。
        List<ContractDocumentLinkEntity> exists = filter(
                new ContractDocumentLinkEntity().setContract(new ContractEntity().setId(contractId)));
        if (!exists.isEmpty()) {
            repository.deleteAll(exists);
            repository.flush();
        }

        if (documents.isEmpty()) {
            return;
        }
        var documentService = cn.hamm.spms.module.asset.AssetServices.getContractDocumentService();
        ContractEntity contract = new ContractEntity().setId(contractId);
        for (ContractDocumentEntity document : documents) {
            if (Objects.isNull(document)) {
                continue;
            }
            if (Objects.isNull(document.getId())) {
                document = documentService.addAndGet(document);
            }
            addAndGet(new ContractDocumentLinkEntity()
                    .setContract(contract)
                    .setDocument(document));
        }
    }
}
