package com.blocklang.marketplace.apirepo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.blocklang.core.git.GitBlobInfo;
import com.blocklang.core.runner.common.CliLogger;

/**
 * 对 Ref 中所有的 changelog 名称进行规则校验
 * 
 * @author Zhengwei Jin
 *
 */
public class RefChangelogNameValidator {

	private CliLogger logger;
	
	public RefChangelogNameValidator(CliLogger logger) {
		this.logger = logger;
	}
	
	/**
	 * 校验存放 changelog 文件的目录名和 changelog 文件名是否遵循以下规则：
	 * 
	 * <ul>
	 * <li>格式为 {order}__{description}，或者 {order}__， 或者 {order}
	 * <li>order 必须为 yyyyMMddHHmm 时间戳
	 * <li>同一层目录名中的 order 不能重复
	 * <li>同一层文件名中的 order 不能重复
	 * </ul>
	 * 
	 * <p>
	 * 注意，要将所有目录名和 changelog 文件名都校验一遍，而不是遇到一个错误就退出。
	 * 
	 * @param allChangelogFiles changelog 文件列表，按照目录分组
	 * @return 如果校验时出错，则返回 <code>false<code>；如果校验全部通过，则返回 <code>true</code>
	 */
	public boolean isValid(LinkedHashMap<String, List<GitBlobInfo>> allChangelogFiles) {
		ApiRepoPathReader pathReader = new ApiRepoPathReader();
		boolean allValid = true;
		
		List<ApiRepoPathInfo> dirs = new ArrayList<ApiRepoPathInfo>();
		for(Map.Entry<String, List<GitBlobInfo>> entry : allChangelogFiles.entrySet()) {
			String dirName = entry.getKey();
			List<String> errors = pathReader.validate(dirName);
			if(!errors.isEmpty()) {
				allValid = false;
				for(String error : errors) {
					logger.error("文件夹名称 {0} 不符合规范 {1}", dirName, error);
				}
			}
			dirs.add(pathReader.read(dirName));
		
			// 校验分组下的 changelog 文件名
			List<ApiRepoPathInfo> files = new ArrayList<ApiRepoPathInfo>();
			for(GitBlobInfo fileInfo : entry.getValue()) {
				String fileName = fileInfo.getName();
				List<String> fileErrors = pathReader.validate(fileName);
				if(!fileErrors.isEmpty()) {
					allValid = false;
					for(String error : fileErrors) {
						logger.error("changelog 文件名称 {0} 不符合规范 {1}", fileName, error);
					}
				}
				files.add(pathReader.read(fileName));
			}
			
			Map<String, Long> duplicatedFileNames = files
				.stream()
				.collect(Collectors.groupingBy(path -> path.getOrder(), Collectors.counting()));
			for(Map.Entry<String, Long> item : duplicatedFileNames.entrySet()) {
				if(item.getValue() > 1) {
					allValid = false;
					logger.error("{0} 被用了 {1} 次", item.getKey(), item.getValue());
				}
			}
		}
		
		Map<String, Long> duplicatedDirNames = dirs
			.stream()
			.collect(Collectors.groupingBy(path -> path.getOrder(), Collectors.counting()));
		for(Map.Entry<String, Long> item : duplicatedDirNames.entrySet()) {
			if(item.getValue() > 1) {
				allValid = false;
				logger.error("{0} 被用了 {1} 次", item.getKey(), item.getValue());
			}
		}
		
		return allValid;
	}
	
}
