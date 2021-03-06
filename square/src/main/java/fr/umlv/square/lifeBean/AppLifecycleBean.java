package fr.umlv.square.lifeBean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import fr.umlv.square.controllers.AutoScaleEndPoint;
import fr.umlv.square.docker.DockerDeploy;
import fr.umlv.square.models.ApplicationsList;
import io.quarkus.runtime.StartupEvent;

/**
 * This class allows us to initialize the data and 
 * launch a thread at the start of Square in order to manage the auto-scale and the death of the containers 
 * @author FAU
 *
 */
@ApplicationScoped
public class AppLifecycleBean {

	private final ApplicationsList app;
	private final AutoScaleEndPoint autoScale;

	/**
	 * Constructor
	 * @param appL injected ApplicationsList
	 * @param autoScale injected AutoScaleEndPoint
	 */
	@Inject
	public AppLifecycleBean(ApplicationsList appL, AutoScaleEndPoint autoScale) {
		this.app = appL;
		this.autoScale = autoScale;

	}

	@Transactional
	void onStart(@Observes StartupEvent ev) {
		this.app.wrapperInit();
		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(30_000);
				} catch (InterruptedException e) {
					throw new AssertionError();
				}
				var list = DockerDeploy.getRunningInstancesNames();
				this.app.getList().forEach(e -> {
					if (!list.contains(e.getDockerInst()))
						this.app.deleteApp(e);
				});
				this.autoScale.tryUpdating();
			}
		}).start();
	}
}