package com.blocklang.develop.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.develop.model.RepositoryCommit;

public interface RepositoryCommitDao extends JpaRepository<RepositoryCommit, Integer> {

	List<RepositoryCommit> findAllByRepositoryIdAndBranchOrderByCommitTimeDesc(Integer repositoryId, String branch);

	Optional<RepositoryCommit> findByRepositoryIdAndBranchAndCommitId(Integer repositoryId, String branch, String commitId);

}
