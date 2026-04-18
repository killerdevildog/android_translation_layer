package android.webkit;

public class ServiceWorkerController {
	public ServiceWorkerController() {}

	public static ServiceWorkerController getInstance() {
		return new ServiceWorkerController();
	}

	public ServiceWorkerWebSettings getServiceWorkerWebSettings() {
		return new ServiceWorkerWebSettings();
	}

	public void setServiceWorkerClient(ServiceWorkerClient client) {}
}
