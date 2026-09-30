package cn.hamm.spms.base.bill;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * <h1>独立事务助手</h1>
 * <p>
 * 框架的 {@code TransactionHelper} 两个重载都是默认的 {@code REQUIRED} 传播，
 * 嵌套调用会合并进外层事务，无法让「状态推进」先于「下游单据生成」提交。
 * <p>
 * 本类提供 {@link Propagation#REQUIRES_NEW}：开启一个<b>独立</b>事务并立即提交，
 * 使其中的数据库行锁在方法返回时释放。
 * <p>
 * 必须作为独立的 Spring Bean（本类的实例方法）调用，
 * {@code @Transactional} 才会经由代理生效 —— 自调用不会生效。
 *
 * @author Hamm.cn
 */
@Service
public class NewTransactionHelper {

    /**
     * 在一个独立事务中执行，立即提交
     *
     * @param supplier 待执行逻辑
     * @param <T>      返回类型
     * @return 逻辑的返回值
     */
    @Transactional(rollbackFor = Exception.class,
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.READ_COMMITTED)
    public <T> T run(@NotNull Supplier<T> supplier) {
        return supplier.get();
    }
}
