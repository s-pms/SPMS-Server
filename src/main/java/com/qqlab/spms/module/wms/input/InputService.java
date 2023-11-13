package com.qqlab.spms.module.wms.input;

import com.qqlab.spms.base.bill.AbstractBaseBillService;
import com.qqlab.spms.module.wms.input.detail.InputDetailEntity;
import com.qqlab.spms.module.wms.input.detail.InputDetailRepository;
import com.qqlab.spms.module.wms.input.detail.InputDetailService;
import org.springframework.stereotype.Service;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class InputService extends AbstractBaseBillService<InputEntity, InputRepository, InputDetailEntity, InputDetailService, InputDetailRepository> {
    @Override
    public InputEntity setAudited(InputEntity bill) {
        return bill.setStatus(InputStatus.PRODUCING.getValue());
    }

    @Override
    public InputEntity setAuditing(InputEntity bill) {
        return bill.setStatus(InputStatus.AUDITING.getValue());
    }

    @Override
    public boolean isAudited(InputEntity bill) {
        return bill.getStatus() != InputStatus.AUDITING.getValue();
    }

    @Override
    public boolean canReject(InputEntity bill) {
        return bill.getStatus() == InputStatus.AUDITING.getValue();
    }

    @Override
    public InputEntity setReject(InputEntity bill) {
        return bill.setStatus(InputStatus.REJECTED.getValue());
    }

    @Override
    public boolean canEdit(InputEntity bill) {
        return bill.getStatus() == InputStatus.REJECTED.getValue();
    }
}