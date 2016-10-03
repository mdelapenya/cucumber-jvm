package cucumber.runtime.io;

import java.net.URL;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;

import org.junit.rules.ExternalResource;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;


/**
 * @author mdelapenya
 */
public class ModuleFrameworkTestRule extends ExternalResource {

	public Bundle getTestBundle() {
		return testBundle;
	}

	@Override
	protected void before() throws Throwable {
		felixModuleFramework = startOSGi();

		if (felixModuleFramework == null) {
			throw new RuntimeException(
				"Module framework is not ready.");
		}

		ClassLoader classLoader = getClass().getClassLoader();

		BundleContext bundleContext =
			felixModuleFramework.getBundleContext();

		URL bundleURL = classLoader.getResource(
			"cucumber/runtime/http-servlet.jar");

		testBundle = bundleContext.installBundle(bundleURL.toString());

		System.out.println(
			"Bundle " + testBundle.getLocation() + " has been installed.");
	}

	@Override
	protected void after() {
		try {
			stopOSGi(felixModuleFramework, testBundle);
		}
		catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	static class HostActivator implements BundleActivator {

		private BundleContext context = null;

		@Override
		public void start(BundleContext bc) {
			context = bc;
		}

		@Override
		public void stop(BundleContext bc) {
			context = null;
		}

		public BundleContext getContext() {
			return context;
		}

	}

	private static Felix startOSGi() throws BundleException {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
			"cucumber.runtime.tests; version=1.0.0");

		HostActivator activator = new HostActivator();

		List<Object> list = new LinkedList<Object>();

		list.add(activator);

		map.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);

		Felix f = new Felix(map);

		System.out.println("Starting OSGI...");

		f.start();

		return f;
	}

	private static void stopOSGi(Felix felixModuleFramework, Bundle... bundles)
		throws BundleException, InterruptedException {

		for (Bundle bundle : bundles) {
			bundle.uninstall();
		}

		felixModuleFramework.stop();

		felixModuleFramework.waitForStop(10);

		System.out.println("Stopping OSGI");
	}

	private Felix felixModuleFramework;
	private Bundle testBundle;

}
