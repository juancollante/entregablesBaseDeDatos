package com.logistica.logistica_notificaciones;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.logistica.logistica_notificaciones.adapter.out.persistence.repository.ConsumoEventoJpaRepository;
import com.logistica.logistica_notificaciones.adapter.out.persistence.repository.NotificacionJpaRepository;
import com.logistica.logistica_notificaciones.adapter.out.persistence.repository.PlantillaNotificacionJpaRepository;

@SpringBootTest(properties = {
	"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class LogisticaNotificacionesApplicationTests {

	@MockBean
	private ConsumoEventoJpaRepository consumoEventoJpaRepository;

	@MockBean
	private NotificacionJpaRepository notificacionJpaRepository;

	@MockBean
	private PlantillaNotificacionJpaRepository plantillaNotificacionJpaRepository;

	@Test
	void contextLoads() {
	}

}
