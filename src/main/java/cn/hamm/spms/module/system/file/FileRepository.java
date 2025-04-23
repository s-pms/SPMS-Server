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
     * 根据文件类别和MD5查询文件
     *
     * @param category 文件类别
     * @param hashMd5  MD5
     * @return 文件
     */
    FileEntity getByCategoryAndHashMd5(Integer category, String hashMd5);
}
