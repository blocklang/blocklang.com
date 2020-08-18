package com.blocklang.develop.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blocklang.core.constant.Constant;
import com.blocklang.core.controller.SpringMvcUtil;
import com.blocklang.core.exception.InvalidRequestException;
import com.blocklang.core.exception.NoAuthorizationException;
import com.blocklang.core.exception.ResourceNotFoundException;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.UserService;
import com.blocklang.develop.constant.AppType;
import com.blocklang.develop.constant.RepositoryResourceType;
import com.blocklang.develop.data.CheckPageKeyParam;
import com.blocklang.develop.data.CheckPageNameParam;
import com.blocklang.develop.data.NewPageParam;
import com.blocklang.develop.model.Repository;
import com.blocklang.develop.model.RepositoryResource;
import com.blocklang.develop.service.RepositoryResourceService;
import com.blocklang.develop.service.RepositoryService;

@RestController
public class PageController extends AbstractRepositoryController {
	private static final Logger logger = LoggerFactory.getLogger(PageController.class);
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryResourceService repositoryResourceService;
	@Autowired
	private UserService userService;
	@Autowired
	private MessageSource messageSource;

	@PostMapping("/repos/{owner}/{repoName}/pages/check-key")
	public ResponseEntity<Map<String, String>> checkKey(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("repoName") String repoName,
			@Valid @RequestBody CheckPageKeyParam param, 
			BindingResult bindingResult){
		
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canWrite(principal, repository).orElseThrow(NoAuthorizationException::new);
		
		//校验 key: 是否为空
		if(bindingResult.hasErrors()) {
			logger.error("名称不能为空");
			throw new InvalidRequestException(bindingResult);
		}
		
		String key = param.getKey().trim();
		//校验：只支持英文字母、数字、中划线(-)、下划线(_)、点(.)
		String regEx = "^[a-zA-Z0-9\\-\\w]+$";
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(key);
		if(!matcher.matches()) {
			logger.error("包含非法字符");
			bindingResult.rejectValue("key", "NotValid.pageKey");
			throw new InvalidRequestException(bindingResult);
		}
		
		Integer parentId = param.getParentId();
		repositoryResourceService.findByKey(
				repository.getId(), 
				parentId, 
				RepositoryResourceType.PAGE, 
				param.getAppType(),
				key).map(resource -> {
			logger.error("key 已被占用");
			
			if(parentId == Constant.TREE_ROOT_ID) {
				return new Object[] {"根目录", key};
			}
			
			// 这里不需要做是否存在判断，因为肯定存在。
			RepositoryResource parentResource = repositoryResourceService.findById(parentId).get();
			String parentResourceName = StringUtils.isBlank(parentResource.getName()) ? parentResource.getKey() : parentResource.getName();
			return new Object[] {parentResourceName, key};
		}).ifPresent(args -> {
			bindingResult.rejectValue("key", "Duplicated.pageKey", args, null);
			throw new InvalidRequestException(bindingResult);
		});
		
		return ResponseEntity.ok(new HashMap<String, String>());
	}
	
	@PostMapping("/repos/{owner}/{repoName}/pages/check-name")
	public ResponseEntity<Map<String, String>> checkName(
			Principal principal,
			@PathVariable("owner") String owner,
			@PathVariable("repoName") String repoName,
			@Valid @RequestBody CheckPageNameParam param, 
			BindingResult bindingResult){

		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canWrite(principal, repository).orElseThrow(NoAuthorizationException::new);
		
		// name 的值可以为空
		if(StringUtils.isNotBlank(param.getName())) {
			String name = param.getName().trim();
			
			Integer parentId = param.getParentId();
			repositoryResourceService.findByName(
					repository.getId(), 
					parentId, 
					RepositoryResourceType.PAGE, 
					param.getAppType(),
					name).map(resource -> {
				logger.error("name 已被占用");
				
				if(parentId == Constant.TREE_ROOT_ID) {
					return new Object[] {"根目录", name};
				}
				
				// 这里不需要做是否存在判断，因为肯定存在。
				RepositoryResource parentResource = repositoryResourceService.findById(parentId).get();
				String parentResourceName = StringUtils.isBlank(parentResource.getName()) ? parentResource.getKey() : parentResource.getName();
				return new Object[] {parentResourceName, name};
			}).ifPresent(args -> {
				bindingResult.rejectValue("name", "Duplicated.pageName", args, null);
				throw new InvalidRequestException(bindingResult);
			});
		}
		return ResponseEntity.ok(new HashMap<String, String>());
	}

	@PostMapping("/repos/{owner}/{repoName}/pages")
	public ResponseEntity<RepositoryResource> newPage(
			Principal principal, 
			@PathVariable("owner") String owner,
			@PathVariable("repoName") String repoName,
			@Valid @RequestBody NewPageParam param, 
			BindingResult bindingResult) {
		if(principal == null) {
			throw new NoAuthorizationException();
		}
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canWrite(principal, repository).orElseThrow(NoAuthorizationException::new);
		
		//校验 key: 
		boolean keyIsValid = true;
		// 一、是否为空
		if(bindingResult.hasErrors()) {
			logger.error("名称不能为空");
			keyIsValid = false;
		}
		
		String key = Objects.toString(param.getKey(), "").trim();
		if(keyIsValid) {
			//校验：只支持英文字母、数字、中划线(-)、下划线(_)、点(.)
			String regEx = "^[a-zA-Z0-9\\-\\w]+$";
			Pattern pattern = Pattern.compile(regEx);
			Matcher matcher = pattern.matcher(key);
			if(!matcher.matches()) {
				logger.error("包含非法字符");
				bindingResult.rejectValue("key", "NotValid.pageKey");
				keyIsValid = false;
			}
		}
		
		if(keyIsValid) {
			Integer parentId = param.getParentId();
			repositoryResourceService.findByKey(
					repository.getId(), 
					parentId, 
					RepositoryResourceType.PAGE, 
					param.getAppType(),
					key).map(resource -> {
				logger.error("key 已被占用");
				
				if(parentId == Constant.TREE_ROOT_ID) {
					return new Object[] {"根目录", key};
				}
				
				// 这里不需要做是否存在判断，因为肯定存在。
				return new Object[] {repositoryResourceService.findById(parentId).get().getName(), key};
			}).ifPresent(args -> {
				bindingResult.rejectValue("key", "Duplicated.pageKey", args, null);
			});
		}
		
		Integer parentId = param.getParentId();
		// 校验 name
		// name 可以为空
		if(StringUtils.isNotBlank(param.getName())) {
			String name = param.getName().trim();
			repositoryResourceService.findByName(
					repository.getId(), 
					parentId, 
					RepositoryResourceType.PAGE, 
					param.getAppType(),
					name).map(resource -> {
				logger.error("name 已被占用");
				
				if(parentId == Constant.TREE_ROOT_ID) {
					return new Object[] {"根目录", name};
				}
				
				// 这里不需要做是否存在判断，因为肯定存在。
				return new Object[] {repositoryResourceService.findById(parentId).get().getName(), name};
			}).ifPresent(args -> {
				bindingResult.rejectValue("name", "Duplicated.pageName", args, null);
			});
			
		}
		
		if(bindingResult.hasErrors()) {
			throw new InvalidRequestException(bindingResult);
		}
		
		RepositoryResource resource = new RepositoryResource();
		resource.setRepositoryId(repository.getId());
		resource.setParentId(parentId);
		resource.setAppType(param.getAppType());
		resource.setKey(key);
		resource.setName(param.getName() == null ? null : param.getName().trim());
		if(param.getDescription() != null) {
			resource.setDescription(param.getDescription().trim());
		}
		resource.setResourceType(RepositoryResourceType.PAGE);
		
		UserInfo currentUser = userService.findByLoginName(principal.getName()).orElseThrow(NoAuthorizationException::new);
		resource.setCreateUserId(currentUser.getId());
		resource.setCreateTime(LocalDateTime.now());
		
		RepositoryResource savedProjectResource = repositoryResourceService.insert(repository, resource);
		savedProjectResource.setMessageSource(messageSource);
		return new ResponseEntity<RepositoryResource>(savedProjectResource, HttpStatus.CREATED);
	}
	
	@GetMapping("/repos/{owner}/{repoName}/pages/**")
	public ResponseEntity<Map<String, Object>> getPage(Principal user,
			@PathVariable String owner,
			@PathVariable String repoName,
			HttpServletRequest req) {
		// 先校验用户对项目是否有读取权限
		Repository repository = repositoryService.find(owner, repoName).orElseThrow(ResourceNotFoundException::new);
		repositoryPermissionService.canRead(user, repository).orElseThrow(NoAuthorizationException::new);
		
		String pagePath = SpringMvcUtil.getRestUrl(req, 4);
		// 获取表示页面的 key
		String[] pathes = pagePath.split("/");
		String groupPath = StringUtils.join(pathes, "/", 0, pathes.length - 1);
		// 要校验根据 parentPath 中的所有节点都能准确匹配
		// 这个列表中不包含页面信息
		List<RepositoryResource> parentGroups = repositoryResourceService.findParentGroupsByParentPath(repository.getId(), groupPath);
		
		// 约定一个仓库的目录结构为
		// repo
		//     project1
		//         resource
		//     project2
		//         resource
		//     mini-program
		//         app
		//         pages/
		//             index
		//     BUILD.json
		//     README.md
		// 所有 resource 的 AppType 都取 project 的 AppType 的值
		String pageKey = pathes[pathes.length - 1];
		Integer parentGroupId = Constant.TREE_ROOT_ID;
		if(!parentGroups.isEmpty()) {
			parentGroupId = parentGroups.get(parentGroups.size() - 1).getId();
			
			AppType appType = parentGroups.get(0).getAppType();
			RepositoryResourceType resourceType = RepositoryResourceType.PAGE;
			RepositoryResource repositoryResource = repositoryResourceService.findByKey(
					repository.getId(), 
					parentGroupId, 
					resourceType, 
					appType, 
					pageKey)
				.orElseThrow(ResourceNotFoundException::new);
			repositoryResource.setMessageSource(messageSource); // 不加这行代码，在生成 json 时会出错
			
			parentGroups.add(repositoryResource);
			List<Map<String, String>> stripedParentGroups = RepositoryResourcePathUtil.combinePathes(parentGroups);
			
			Map<String, Object> result = new HashMap<>();
			result.put("repositoryResource", repositoryResource);
			result.put("parentGroups", stripedParentGroups);
			
			return ResponseEntity.ok(result);
		}
		
		// 获取仓库根目录下的文件，位于仓库根目录下的文件的 AppType 的值必须为 UNKNOWN
		RepositoryResource repositoryResource = repositoryResourceService.findByKey(
				repository.getId(), 
				parentGroupId, 
				RepositoryResourceType.PAGE, 
				AppType.UNKNOWN, 
				pageKey)
			.orElseThrow(ResourceNotFoundException::new);
		repositoryResource.setMessageSource(messageSource); // 不加这行代码，在生成 json 时会出错
		
		parentGroups.add(repositoryResource);
		List<Map<String, String>> stripedParentGroups = RepositoryResourcePathUtil.combinePathes(parentGroups);
		
		Map<String, Object> result = new HashMap<>();
		result.put("repositoryResource", repositoryResource);
		result.put("parentGroups", stripedParentGroups);
		
		return ResponseEntity.ok(result);
	}

}
