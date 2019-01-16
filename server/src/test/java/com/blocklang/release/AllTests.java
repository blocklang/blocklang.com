package com.blocklang.release;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.blocklang.release.api.InstallerApiTest;
import com.blocklang.release.service.impl.AppReleaseFileServiceImplTest;
import com.blocklang.release.service.impl.AppReleaseRelationServiceImplTest;
import com.blocklang.release.service.impl.AppReleaseServiceImplTest;
import com.blocklang.release.service.impl.AppServiceImplTest;
import com.blocklang.release.service.impl.InstallerServiceImplTest;
import com.blocklang.release.util.IdGeneratorTest;

@RunWith(Suite.class)
@SuiteClasses({
	// unit test
	IdGeneratorTest.class,
	
	// api tests
	InstallerApiTest.class,
	
	// controller tests
	
	// service tests
	AppServiceImplTest.class,
	AppReleaseServiceImplTest.class,
	AppReleaseRelationServiceImplTest.class,
	AppReleaseFileServiceImplTest.class,
	InstallerServiceImplTest.class
})
public class AllTests {

}
