package com.blocklang;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.blocklang.git.GitUtilsTest;
import com.blocklang.release.api.AppApiTest;
import com.blocklang.release.api.InstallerApiTest;
import com.blocklang.release.controller.ReleaseControllerTest;
import com.blocklang.release.service.impl.AppReleaseFileServiceImplTest;
import com.blocklang.release.service.impl.AppReleaseRelationServiceImplTest;
import com.blocklang.release.service.impl.AppReleaseServiceImplTest;
import com.blocklang.release.service.impl.AppServiceImplTest;
import com.blocklang.release.service.impl.InstallerServiceImplTest;
import com.blocklang.release.service.impl.WebServerServiceImplTest;
import com.blocklang.release.task.AppBuildContextTest;
import com.blocklang.util.IdGeneratorTest;

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
	
	// service tests
	AppServiceImplTest.class,
	AppReleaseServiceImplTest.class,
	AppReleaseRelationServiceImplTest.class,
	AppReleaseFileServiceImplTest.class,
	InstallerServiceImplTest.class,
	WebServerServiceImplTest.class
})
public class AllTests {

}
