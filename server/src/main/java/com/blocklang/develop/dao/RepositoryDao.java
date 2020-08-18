package com.blocklang.develop.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.Repository;

public interface RepositoryDao extends JpaRepository<Repository, Integer> {

	Optional<Repository> findByCreateUserIdAndName(Integer id, String repositoryName);

}
