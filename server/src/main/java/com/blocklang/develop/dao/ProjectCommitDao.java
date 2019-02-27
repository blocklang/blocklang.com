package com.blocklang.develop.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.ProjectCommit;

public interface ProjectCommitDao extends JpaRepository<ProjectCommit, Integer> {

	List<ProjectCommit> findAllByProjectIdAndBranchOrderByCommitTimeDesc(Integer projectId, String branch);

	Optional<ProjectCommit> findByProjectIdAndBranchAndCommitId(Integer projectId, String branch, String commitId);

}
