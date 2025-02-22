package cn.hamm.spms.module.system.file;

import cn.hamm.spms.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * <h1>Repository</h1>
 *
 * @author Hamm.cn
 */
@Repository
public interface FileRepository extends BaseRepository<FileEntity> {
    /**
     * <h2>根据MD5查询文件</h2>
     *
     * @param hashMd5 MD5
     * @return 文件
     */
    FileEntity getByHashMd5(String hashMd5);
}
