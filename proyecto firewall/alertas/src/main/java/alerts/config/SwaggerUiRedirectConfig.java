package alerts.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SpringDoc 3 sirve la UI en {@code /swagger-ui/index.html}; muchos tutoriales usan {@code /swagger-ui.html}.
 */
@Configuration
public class SwaggerUiRedirectConfig implements WebMvcConfigurer {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addRedirectViewController("/swagger-ui.html", "/swagger-ui/index.html");
	}
}
