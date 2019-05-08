package com.blocklang;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.blocklang.core.controller.DocumentControllerTest;
import com.blocklang.core.controller.LoggedUserControllerTest;
import com.blocklang.core.controller.UserValidatorTest;
import com.blocklang.core.filter.RouterFilterTest;
import com.blocklang.core.git.GitUtilsTest;
import com.blocklang.core.service.impl.GithubLoginServiceImplTest;
import com.blocklang.core.service.impl.PropertyServiceImplTest;
import com.blocklang.core.service.impl.QqLoginServiceImplTest;
import com.blocklang.core.service.impl.UserBindServiceImplTest;
import com.blocklang.core.service.impl.UserServiceImplTest;
import com.blocklang.core.util.IdGeneratorTest;
import com.blocklang.core.util.LoginTokenTest;
import com.blocklang.core.util.RangeHeaderTest;
import com.blocklang.core.util.UrlUtilTest;
import com.blocklang.develop.constant.AppTypeTest;
import com.blocklang.develop.controller.CommitControllerTest;
import com.blocklang.develop.controller.GroupControllerTest;
import com.blocklang.develop.controller.PageControllerTest;
import com.blocklang.develop.controller.ProjectControllerTest;
import com.blocklang.develop.controller.PropertyControllerTest;
import com.blocklang.develop.model.AppGlobalContextTest;
import com.blocklang.develop.model.ProjectContextTest;
import com.blocklang.develop.model.ProjectResourceTest;
import com.blocklang.develop.service.impl.ProjectAuthorizationServiceImplTest;
import com.blocklang.develop.service.impl.ProjectDeployServiceImplTest;
import com.blocklang.develop.service.impl.ProjectFileServiceImplTest;
import com.blocklang.develop.service.impl.ProjectResourceServiceImplTest;
import com.blocklang.develop.service.impl.ProjectServiceImplTest;
import com.blocklang.release.api.AppApiTest;
import com.blocklang.release.api.InstallerApiTest;
import com.blocklang.release.controller.AppControllerTest;
import com.blocklang.release.controller.ReleaseControllerTest;
import com.blocklang.release.service.impl.AppReleaseFileServiceImplTest;
import com.blocklang.release.service.impl.AppReleaseRelationServiceImplTest;
import com.blocklang.release.service.impl.AppReleaseServiceImplTest;
import com.blocklang.release.service.impl.AppServiceImplTest;
import com.blocklang.release.service.impl.BuildServiceImplTest;
import com.blocklang.release.service.impl.InstallerServiceImplTest;
import com.blocklang.release.service.impl.ProjectReleaseTaskServiceImplTest;
import com.blocklang.release.service.impl.ProjectTagServiceImplTest;
import com.blocklang.release.service.impl.WebServerServiceImplTest;
import com.blocklang.release.task.AppBuildContextTest;
import com.blocklang.release.task.GitTagTaskTest;

@RunWith(Suite.class)
@SuiteClasses({
	BlockLangApplicationTests.class,
	
	// unit test
	IdGeneratorTest.class,
	AppGlobalContextTest.class,
	ProjectContextTest.class,
	AppBuildContextTest.class,
	GitTagTaskTest.class,
	GitUtilsTest.class,
	UrlUtilTest.class,
	RangeHeaderTest.class,
	LoginTokenTest.class,
	
	UserValidatorTest.class,
	
	// api tests
	InstallerApiTest.class,
	AppApiTest.class,
	
	// controller tests
	ProjectControllerTest.class,
	ReleaseControllerTest.class,
	LoggedUserControllerTest.class,
	AppControllerTest.class,
	DocumentControllerTest.class,
	PropertyControllerTest.class,
	PageControllerTest.class,
	GroupControllerTest.class,
	CommitControllerTest.class,

	// service tests
	AppServiceImplTest.class,
	AppReleaseServiceImplTest.class,
	AppReleaseRelationServiceImplTest.class,
	AppReleaseFileServiceImplTest.class,
	InstallerServiceImplTest.class,
	WebServerServiceImplTest.class,
	ProjectServiceImplTest.class,
	ProjectResourceServiceImplTest.class,
	ProjectFileServiceImplTest.class,
	ProjectTagServiceImplTest.class,
	ProjectReleaseTaskServiceImplTest.class,
	ProjectAuthorizationServiceImplTest.class,
	PropertyServiceImplTest.class,
	GithubLoginServiceImplTest.class,
	QqLoginServiceImplTest.class,
	ProjectDeployServiceImplTest.class,
	BuildServiceImplTest.class,
	UserServiceImplTest.class,
	UserBindServiceImplTest.class,
	
	// model
	ProjectResourceTest.class,
	
	// filter
	RouterFilterTest.class,
	
	// constant
	AppTypeTest.class
	
})
public class AllTests {

}
