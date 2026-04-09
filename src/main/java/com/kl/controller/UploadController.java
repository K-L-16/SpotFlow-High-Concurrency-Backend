package com.kl.controller;


import com.kl.dto.Result;
import com.kl.utils.SystemConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("upload")
@Tag(name = "Upload api",description = "Local upload")
public class UploadController {

    @PostMapping("blog")
    @Operation( summary = "Upload blog image")
    public Result uploadImage(@RequestParam("file") MultipartFile image) {
        try {
            // 获取原始文件名称
            String originalFilename = image.getOriginalFilename();
            // 生成新文件名
            String fileName = createNewFileName(originalFilename);
            // 保存文件
            image.transferTo(new File(SystemConstants.IMAGE_UPLOAD_DIR, fileName));
            // 返回结果
            log.debug("文件上传成功，{}", fileName);
            return Result.ok(fileName);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @GetMapping("/blog/delete")
    @Operation( summary = "Delete blog image")
    public Result deleteBlogImg(@RequestParam("name") String filename) {
        File file = new File(SystemConstants.IMAGE_UPLOAD_DIR, filename);
        if (file.isDirectory()) {
            return Result.fail("错误的文件名称");
        }
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        return Result.ok();
    }

    private String createNewFileName(String originalFilename) {
        // 1. 获取后缀
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        }

        // 2. 生成随机文件名
        String name = UUID.randomUUID().toString();

        int hash = name.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;

        // 3. 生成目录
        String dirPath = String.format("/blogs/%d/%d", d1, d2);
        File dir = new File(SystemConstants.IMAGE_UPLOAD_DIR, dirPath);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 4. 返回完整路径
        return String.format("/blogs/%d/%d/%s.%s", d1, d2, name, suffix);
    }
}
