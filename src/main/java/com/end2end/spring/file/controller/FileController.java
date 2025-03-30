package com.end2end.spring.file.controller;

import com.end2end.spring.file.dto.FileDTO;
import com.end2end.spring.util.FileUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/file")
@Controller
public class FileController {
    @ResponseBody
    @RequestMapping("/upload/image/temp")
    public String uploadTemp(MultipartFile file) {
        // TODO: summernote의 임시 이미지 파일 업로드
        return FileUtil.uploadTempImage(file);
    }

    @RequestMapping("/download")
    public void download() {
        // TODO: 다운로드
    }

    @RequestMapping("/upload/test")
    public String test(MultipartFile[] files) {
        List<FileDTO> dto = FileUtil.upload("image/test", files);

        for (FileDTO fileDTO : dto) {
            System.out.println(fileDTO.getSystemFileName());
        }

        return "redirect:/";
    }

    @RequestMapping("/delete/test")
    public String delete(String path) {
        FileUtil.removeFile(path);

        return "redirect:/";
    }
}
