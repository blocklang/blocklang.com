package com.blocklang.develop.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.Project;

public interface ProjectDao extends JpaRepository<Project, Integer> {

	Optional<Project> findByCreateUserIdAndName(Integer id, String projectName);

}
