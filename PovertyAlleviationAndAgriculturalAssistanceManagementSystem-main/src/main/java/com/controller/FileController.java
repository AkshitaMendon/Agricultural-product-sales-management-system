// Refactored FileController.java
package com.controller;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.entity.ConfigEntity;
import com.entity.EIException;
import com.service.ConfigService;
import com.utils.R;

@RestController
@RequestMapping("file")
@SuppressWarnings({"unchecked", "rawtypes"})
public class FileController {
	@Autowired
	private ConfigService configService;

	@RequestMapping("/upload")
	public R upload(@RequestParam("file") MultipartFile file, String type) throws Exception {
		validateFile(file);
		String fileName = generateFileName(file);
		File uploadDirectory = getUploadDirectory();
		saveFile(file, uploadDirectory, fileName);

		if (StringUtils.isNotBlank(type) && type.equals("1")) {
			updateFaceFileConfig(fileName);
		}

		return R.ok().put("file", fileName);
	}

	private void validateFile(MultipartFile file) {
		if (file.isEmpty()) {
			throw new EIException("Uploaded file cannot be empty");
		}
	}

	private String generateFileName(MultipartFile file) {
		String fileExt = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
		return new Date().getTime() + "." + fileExt;
	}

	private File getUploadDirectory() throws IOException {
		File path = new File(ResourceUtils.getURL("classpath:static").getPath());
		if (!path.exists()) {
			path = new File("");
		}
		File upload = new File(path.getAbsolutePath(), "/src/main/resources/static/upload/");
		if (!upload.exists()) {
			upload.mkdirs();
		}
		return upload;
	}

	private void saveFile(MultipartFile file, File uploadDirectory, String fileName) throws IOException {
		File dest = new File(uploadDirectory.getAbsolutePath() + "/" + fileName);
		file.transferTo(dest);
	}

	private void updateFaceFileConfig(String fileName) {
		ConfigEntity configEntity = configService.selectOne(new EntityWrapper<ConfigEntity>().eq("name", "faceFile"));
		if (configEntity == null) {
			configEntity = new ConfigEntity();
			configEntity.setName("faceFile");
		}
		configEntity.setValue(fileName);
		configService.insertOrUpdate(configEntity);
	}
}
