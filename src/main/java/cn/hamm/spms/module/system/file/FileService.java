package cn.hamm.spms.module.system.file;

import cn.hamm.airpower.core.FileUtil;
import cn.hamm.airpower.core.exception.ServiceException;
import cn.hamm.airpower.file.AbstractFilePlatformFactory;
import cn.hamm.airpower.file.FileConfig;
import cn.hamm.airpower.file.FileHelper;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.system.file.enums.FileCategory;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <h1>文件</h1>
 *
 * @author Hamm.cn
 */
@Service
@Slf4j
public class FileService extends BaseService<FileEntity, FileRepository> {
    @Autowired
    private FileHelper fileHelper;

    @Autowired
    private FileConfig fileConfig;

    /**
     * 上传文件
     *
     * @param multipartFile 文件
     * @param fileCategory  文件类别
     * @return 存储的文件信息
     */
    public FileEntity upload(@NotNull MultipartFile multipartFile, @NotNull FileCategory fileCategory) {
        return upload(fileConfig.getDefaultPlatform(), multipartFile, fileCategory, null);
    }

    /**
     * 上传文件
     *
     * @param multipartFile 文件
     * @param fileCategory  文件类别
     * @param uploaderId    上传人 ID
     * @return 存储的文件信息
     * @apiNote 受保护的文件类别（如合同附件）必须传 {@code uploaderId}，
     * 否则下载时没有归属可校验，等于对所有人开放
     */
    public FileEntity upload(@NotNull MultipartFile multipartFile, @NotNull FileCategory fileCategory, Long uploaderId) {
        return upload(fileConfig.getDefaultPlatform(), multipartFile, fileCategory, uploaderId);
    }

    /**
     * 文件上传
     *
     * @param multipartFile 文件
     * @param fileCategory  文件类别
     * @return 存储的文件信息
     */
    public FileEntity upload(String platform, @NotNull MultipartFile multipartFile, @NotNull FileCategory fileCategory) {
        return upload(platform, multipartFile, fileCategory, null);
    }

    /**
     * 文件上传
     *
     * @param platform      存储平台
     * @param multipartFile 文件
     * @param fileCategory  文件类别
     * @param uploaderId    上传人 ID，用于受保护文件的归属校验
     * @return 存储的文件信息
     */
    public FileEntity upload(String platform, @NotNull MultipartFile multipartFile,
                             @NotNull FileCategory fileCategory, Long uploaderId) {
        // 获取文件的MD5
        AbstractFilePlatformFactory filePlatform = fileHelper.getPlatform(platform);
        String fileHash = filePlatform.getFileHash(multipartFile);
        FileEntity file = repository.getByCategoryAndHashMd5(fileCategory.getKey(), fileHash);
        if (Objects.nonNull(file)) {
            return file;
        }

        // 验证文件类型
        String fileName = filePlatform.getFileName(multipartFile);
        String fileExtension = FileUtil.getExtension(fileName);
        filePlatform.validateUploadFileExtension(fileExtension, fileCategory.getExtensions());
        // 扩展名只来自文件名，改个名就能绕过上面的白名单，
        // 因此还要按文件头（魔数）确认真实类型，防止把可执行内容伪装成图片
        validateRealFileType(multipartFile, fileExtension);

        // 存储的相对路径目录
        String category = fileCategory.name().toLowerCase();
        try {
            // 文件名
            String fileUrl = filePlatform.upload(multipartFile, category);

            file = new FileEntity()
                    .setExtension(fileExtension)
                    .setSize(multipartFile.getSize())
                    .setPlatform(filePlatform.getKey())
                    .setCategory(fileCategory.getKey())
                    .setName(multipartFile.getOriginalFilename())
                    .setHashMd5(fileHash)
                    .setUrl(fileUrl);
            if (Objects.nonNull(uploaderId)) {
                file.setUploader(new UserEntity().setId(uploaderId));
            }
            return addAndGet(file);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException("上传文件失败，" + e.getMessage());
        }
    }

    /**
     * 扩展名与真实类型（魔数）的对应关系
     * <p>
     * 只收录 {@link cn.hamm.spms.common.AppConstant#DEFAULT_EXTENSIONS}
     * 与各类目白名单里确实用到的类型。压缩包类（zip/rar/7z/tar/gz）一律映射为
     * {@code application/octet-stream} 或 {@code application/zip}，因为它们本身
     * 就是各种格式的容器，不做进一步区分。
     * </p>
     */
    private static final Map<String, Set<String>> EXTENSION_MIME = Map.ofEntries(
            Map.entry("jpg", Set.of("image/jpeg")),
            Map.entry("jpeg", Set.of("image/jpeg")),
            Map.entry("png", Set.of("image/png")),
            Map.entry("gif", Set.of("image/gif")),
            Map.entry("bmp", Set.of("image/bmp", "image/x-ms-bmp")),
            Map.entry("pdf", Set.of("application/pdf")),
            Map.entry("mp4", Set.of("video/mp4", "application/mp4")),
            Map.entry("mp3", Set.of("audio/mpeg", "audio/mp3")),
            Map.entry("wav", Set.of("audio/x-wav", "audio/wav")),
            Map.entry("wma", Set.of("audio/x-ms-wma")),
            Map.entry("zip", Set.of("application/zip", "application/x-zip-compressed", "application/octet-stream")),
            Map.entry("rar", Set.of("application/x-rar-compressed", "application/octet-stream")),
            Map.entry("7z", Set.of("application/x-7z-compressed", "application/octet-stream")),
            Map.entry("tar", Set.of("application/x-tar", "application/octet-stream")),
            Map.entry("gz", Set.of("application/gzip", "application/x-gzip", "application/octet-stream")),
            Map.entry("doc", Set.of("application/msword")),
            Map.entry("docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/zip", "application/octet-stream")),
            Map.entry("xls", Set.of("application/vnd.ms-excel")),
            Map.entry("xlsx", Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/zip", "application/octet-stream"))
    );

    /**
     * 校验文件的真实类型
     * <p>
     * 修复前只按文件名取扩展名做白名单校验，把 {@code evil.exe} 改名成
     * {@code evil.jpg} 就能通过。头像、合同附件这类会被浏览器直接渲染或下载的
     * 文件是伪造类型后的主要落点，因此按文件头再确认一次。
     * <p>
     * 未登记的类型（如 {@code markdown}、{@code xlsx} 在部分 JDK 下识别不出）
     * 一律放行，避免误伤正常业务；已登记的类型必须匹配，否则拒绝。
     * </p>
     *
     * @param multipartFile 上传的文件
     * @param extension     文件名中的扩展名
     */
    private void validateRealFileType(@NotNull MultipartFile multipartFile, @NotNull String extension) {
        Set<String> expected = EXTENSION_MIME.get(extension.toLowerCase());
        if (Objects.isNull(expected)) {
            // 该扩展名没有可依赖的魔数特征，跳过校验
            return;
        }
        String detected;
        try (InputStream inputStream = multipartFile.getInputStream()) {
            detected = URLConnection.guessContentTypeFromStream(inputStream);
        } catch (IOException e) {
            log.warn("读取上传文件失败，无法校验真实类型: {}", e.getMessage());
            return;
        }
        if (Objects.isNull(detected)) {
            // JDK 认不出（常见于 zip 容器类），只要不是明显的可执行/脚本类型就放行
            if (isDangerousContent(multipartFile)) {
                throw new ServiceException("文件内容与扩展名不符，上传被拒绝");
            }
            return;
        }
        if (!expected.contains(detected)) {
            log.warn("文件真实类型与扩展名不符, 扩展名={}, 真实类型={}", extension, detected);
            throw new ServiceException("文件内容与扩展名不符，上传被拒绝");
        }
    }

    /**
     * 粗略判断文件头是否为可执行内容或脚本
     *
     * @param multipartFile 上传的文件
     * @return 是则返回 true
     */
    private boolean isDangerousContent(@NotNull MultipartFile multipartFile) {
        byte[] head = new byte[1024];
        int read;
        try (InputStream inputStream = multipartFile.getInputStream()) {
            read = inputStream.read(head);
        } catch (IOException e) {
            return false;
        }
        if (read <= 0) {
            return false;
        }
        String content = new String(head, 0, read, StandardCharsets.ISO_8859_1);
        String lower = content.toLowerCase();
        // ELF 可执行文件、PE (MZ) 可执行文件、shebang 脚本、PHP 标签
        return lower.startsWith("<?php")
                || lower.contains("<script")
                || lower.startsWith("#!/bin/sh")
                || lower.startsWith("#!/bin/bash")
                || lower.startsWith("#!/usr/bin/env")
                || (read > 1 && (head[0] & 0xFF) == 0x7F && head[1] == 'E' && head[2] == 'L' && head[3] == 'F')
                || (read > 1 && head[0] == 'M' && head[1] == 'Z');
    }
}
