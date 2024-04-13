package cn.hamm.spms.module.system.coderule;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>数据库连接信息</h1>
 *
 * @author Hamm
 */
@Repository
public interface CodeRuleRepository extends BaseRepository<CodeRuleEntity> {
    /**
     * <h2>查询指定表的编码规则实体</h2>
     *
     * @param ruleField 从枚举字典中传入
     * @return 编码规则实体
     */
    CodeRuleEntity getByRuleField(Integer ruleField);
}
