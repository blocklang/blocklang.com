package com.blocklang.release;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.blocklang.release.api.InstallerApiTest;
import com.blocklang.release.service.impl.AppReleaseServiceImplTest;
import com.blocklang.release.service.impl.AppServiceImplTest;

@RunWith(Suite.class)
@SuiteClasses({
	// api tests
	InstallerApiTest.class,
	// controller tests
	
	// service tests
	AppServiceImplTest.class,
	AppReleaseServiceImplTest.class
})
public class AllTests {

}
