package cn.hamm.spms.module.system.coderule;

import cn.hamm.spms.base.BaseRepository;
import cn.hamm.spms.module.system.coderule.enums.CodeRuleField;
import org.springframework.stereotype.Repository;

/**
 * <h1>编码规则</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface CodeRuleRepository extends BaseRepository<CodeRuleEntity> {
    /**
     * 按规则字段查询编码规则
     *
     * @param ruleField {@link CodeRuleField} 的 key
     * @return 编码规则，未配置时返回 {@code null}
     */
    CodeRuleEntity getByRuleField(Integer ruleField);
}
