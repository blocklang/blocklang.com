package com.blocklang.release.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blocklang.release.model.Installer;

public interface InstallerDao extends JpaRepository<Installer, Integer>{

	Optional<Installer> findByInstallerToken(String installerToken);

}
