package cn.hamm.spms.module.asset;

import cn.hamm.spms.module.asset.contract.ContractService;
import cn.hamm.spms.module.asset.contract.document.ContractDocumentLinkService;
import cn.hamm.spms.module.asset.contract.document.ContractDocumentService;
import cn.hamm.spms.module.asset.contract.participant.ContractParticipantLinkService;
import cn.hamm.spms.module.asset.contract.participant.ParticipantService;
import cn.hamm.spms.module.asset.device.DeviceService;
import cn.hamm.spms.module.asset.material.MaterialService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>资产模块服务聚合</h1>
 *
 * @author Hamm.cn
 */
@Component
public class AssetServices {
    @Getter
    private static ContractService contractService;

    @Getter
    private static ParticipantService participantService;

    @Getter
    private static ContractParticipantLinkService contractParticipantLinkService;

    @Getter
    private static ContractDocumentService contractDocumentService;

    @Getter
    private static ContractDocumentLinkService contractDocumentLinkService;

    @Getter
    private static DeviceService deviceService;

    @Getter
    private static MaterialService materialService;

    @Autowired
    private void initService(
            ContractService contractService,
            ParticipantService participantService,
            ContractParticipantLinkService contractParticipantLinkService,
            ContractDocumentService contractDocumentService,
            ContractDocumentLinkService contractDocumentLinkService,
            DeviceService deviceService,
            MaterialService materialService
    ) {
        AssetServices.contractService = contractService;
        AssetServices.participantService = participantService;
        AssetServices.contractParticipantLinkService = contractParticipantLinkService;
        AssetServices.contractDocumentService = contractDocumentService;
        AssetServices.contractDocumentLinkService = contractDocumentLinkService;
        AssetServices.deviceService = deviceService;
        AssetServices.materialService = materialService;
    }
}
