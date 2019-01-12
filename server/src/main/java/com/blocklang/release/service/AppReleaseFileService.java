package com.blocklang.release.service;

import java.util.Optional;

import com.blocklang.release.model.AppReleaseFile;

/**
 * APP 发行版文件逻辑服务接口
 * 
 * @author Zhengwei Jin
 *
 */
public interface AppReleaseFileService {

	Optional<AppReleaseFile> find(int appReleaseId, String targetOs, String arch);

}
