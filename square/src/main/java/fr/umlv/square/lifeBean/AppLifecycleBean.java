package fr.umlv.square.lifeBean;

import java.sql.DriverManager;
import java.sql.SQLException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import fr.umlv.square.models.ApplicationsList;
import io.agroal.pool.ConnectionFactory;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class AppLifecycleBean {

	private final ApplicationsList app;

	@Inject
	public AppLifecycleBean(ApplicationsList appL) {
		this.app = appL;
	}

	@Transactional
	void onStart(@Observes StartupEvent ev) {
		this.app.wrapperInit();
		/*new Thread(() -> {
			try {
				var connction = DriverManager.getConnection("jdbc:postgresql:square", "nfau_marti", "Square93");
				var rs = connction.createStatement().executeQuery("SELECT * from application");
				while (rs.next())
					System.out.println(rs.getString("DOCKER_INSTANCE"));
				connction.close();
				rs.close();
			} catch (SQLException e) {
				return;
			}
		}).start();*/
	}

}