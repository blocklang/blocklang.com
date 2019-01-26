package com.blocklang.release.service;

import java.util.Optional;

public interface ProjectService {

	Optional<?> find(String userName, String projectName);

}
