package com.qqlab.spms.module.wms.move;

import com.qqlab.spms.base.bill.AbstractBaseBillService;
import com.qqlab.spms.module.wms.move.detail.MoveDetailEntity;
import com.qqlab.spms.module.wms.move.detail.MoveDetailRepository;
import com.qqlab.spms.module.wms.move.detail.MoveDetailService;
import org.springframework.stereotype.Service;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class MoveService extends AbstractBaseBillService<MoveEntity, MoveRepository, MoveDetailEntity, MoveDetailService, MoveDetailRepository> {
    @Override
    public MoveEntity setAudited(MoveEntity bill) {
        return bill.setStatus(MoveStatus.MOVING.getValue());
    }

    @Override
    public MoveEntity setAuditing(MoveEntity bill) {
        return bill.setStatus(MoveStatus.AUDITING.getValue());
    }

    @Override
    public boolean isAudited(MoveEntity bill) {
        return bill.getStatus() != MoveStatus.AUDITING.getValue();
    }

    @Override
    public boolean canReject(MoveEntity bill) {
        return bill.getStatus() == MoveStatus.AUDITING.getValue();
    }

    @Override
    public MoveEntity setReject(MoveEntity bill) {
        return bill.setStatus(MoveStatus.REJECTED.getValue());
    }

    @Override
    public boolean canEdit(MoveEntity bill) {
        return bill.getStatus() == MoveStatus.REJECTED.getValue();
    }
}