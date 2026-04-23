package cn.hamm.spms.module.factory;

import cn.hamm.spms.module.factory.storage.StorageService;
import cn.hamm.spms.module.factory.structure.StructureService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>服务整合助手类</h1>
 *
 * @author Hamm.cn
 */
@Component
public class FactoryServices {

    @Getter
    private static StorageService storageService;

    @Getter
    private static StructureService structureService;

    @Autowired
    private void initService(
            StorageService storageService,
            StructureService structureService
    ) {
        FactoryServices.storageService = storageService;
        FactoryServices.structureService = structureService;
    }
}
