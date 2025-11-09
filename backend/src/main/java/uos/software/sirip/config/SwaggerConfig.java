package uos.software.sirip.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String jwtSchemeName = "JWT Token";

        return new OpenAPI()
            .info(new Info()
                .title("Sirip API")
                .description("JWT 기반 인증이 필요한 API 문서입니다.")
                .version("1.0.0"))
            // JWT 설정 추가
            .addSecurityItem(new SecurityRequirement().addList(jwtSchemeName))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes(jwtSchemeName,
                    new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
