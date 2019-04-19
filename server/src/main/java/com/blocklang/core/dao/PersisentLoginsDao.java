package com.blocklang.core.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.core.model.PersisentLogins;

public interface PersisentLoginsDao extends JpaRepository<PersisentLogins, Integer>{

	Optional<PersisentLogins> findByToken(String loginToken);

	Optional<PersisentLogins> findByLoginName(String loginName);

}
