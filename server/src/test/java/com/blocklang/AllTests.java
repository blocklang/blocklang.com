package com.blocklang;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.blocklang.core.controller.LoggedUserControllerTest;
import com.blocklang.core.git.GitUtilsTest;
import com.blocklang.core.service.impl.GithubLoginServiceImplTest;
import com.blocklang.core.service.impl.PropertyServiceImplTest;
import com.blocklang.core.util.IdGeneratorTest;
import com.blocklang.release.api.AppApiTest;
import com.blocklang.release.api.InstallerApiTest;
import com.blocklang.release.controller.ReleaseControllerTest;
import com.blocklang.release.service.impl.AppReleaseFileServiceImplTest;
import com.blocklang.release.service.impl.AppReleaseRelationServiceImplTest;
import com.blocklang.release.service.impl.AppReleaseServiceImplTest;
import com.blocklang.release.service.impl.AppServiceImplTest;
import com.blocklang.release.service.impl.InstallerServiceImplTest;
import com.blocklang.release.service.impl.ProjectReleaseTaskServiceImplTest;
import com.blocklang.release.service.impl.ProjectTagServiceImplTest;
import com.blocklang.release.service.impl.WebServerServiceImplTest;
import com.blocklang.release.task.AppBuildContextTest;

@RunWith(Suite.class)
@SuiteClasses({
	// unit test
	IdGeneratorTest.class,
	AppBuildContextTest.class,
	GitUtilsTest.class,
	
	// api tests
	InstallerApiTest.class,
	AppApiTest.class,
	
	// controller tests
	ReleaseControllerTest.class,
	LoggedUserControllerTest.class,
	
	// service tests
	AppServiceImplTest.class,
	AppReleaseServiceImplTest.class,
	AppReleaseRelationServiceImplTest.class,
	AppReleaseFileServiceImplTest.class,
	InstallerServiceImplTest.class,
	WebServerServiceImplTest.class,
	ProjectTagServiceImplTest.class,
	ProjectReleaseTaskServiceImplTest.class,
	PropertyServiceImplTest.class,
	GithubLoginServiceImplTest.class
})
public class AllTests {

}
