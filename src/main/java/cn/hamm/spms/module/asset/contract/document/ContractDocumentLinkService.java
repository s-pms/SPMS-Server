package cn.hamm.spms.module.asset.contract.document;

import cn.hamm.spms.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import cn.hamm.spms.module.asset.contract.ContractEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
@Slf4j
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
        var documentService = cn.hamm.spms.module.asset.AssetServices.getContractDocumentService();
        // 本次要保留的关联：没有 ID 的先落库拿到 ID
        List<ContractDocumentEntity> targets = new ArrayList<>();
        for (ContractDocumentEntity document : documents) {
            if (Objects.isNull(document)) {
                continue;
            }
            if (Objects.isNull(document.getId())) {
                document = documentService.addAndGet(document);
            }
            targets.add(document);
        }
        Set<Long> targetIds = targets.stream()
                .map(ContractDocumentEntity::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<ContractDocumentLinkEntity> exists = filter(
                new ContractDocumentLinkEntity().setContract(new ContractEntity().setId(contractId)));
        ContractEntity contract = new ContractEntity().setId(contractId);
        Set<Long> kept = new LinkedHashSet<>();
        List<ContractDocumentLinkEntity> stale = new ArrayList<>();
        for (ContractDocumentLinkEntity link : exists) {
            Long documentId = Objects.isNull(link.getDocument()) ? null : link.getDocument().getId();
            if (Objects.isNull(documentId) || !targetIds.contains(documentId)) {
                stale.add(link);
            } else {
                kept.add(documentId);
            }
        }
        // 走 service.delete 保证前后置钩子被触发
        stale.forEach(entity -> delete(entity.getId()));
        for (ContractDocumentEntity document : targets) {
            if (kept.contains(document.getId())) {
                continue;
            }
            addAndGet(new ContractDocumentLinkEntity()
                    .setContract(contract)
                    .setDocument(document));
        }
        log.info("合同 {} 附件同步完成：原有 {} 个，现 {} 个", contractId, exists.size(), targetIds.size());
    }
}
