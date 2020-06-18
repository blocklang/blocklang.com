package com.blocklang.marketplace.service;

import java.util.List;

import com.blocklang.marketplace.componentrepo.RefData;

public interface PersistComponentRepoService {

	/**
	 * 保存每个 tag 以及 master 分支中 blocklang.json 文件中的内容
	 * 
	 * @param refData 组件仓库里每个 tag 以及 master 分支中 blocklang.json 文件
	 */
	void save(List<RefData> refDatas);
	
}
