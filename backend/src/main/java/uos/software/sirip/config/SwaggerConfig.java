package uos.software.sirip.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.parameters.Parameter;
import uos.software.sirip.config.security.CurrentUser;

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



    @Bean
    public OperationCustomizer hideCurrentUserParameter() {
        return (operation, handlerMethod) -> {

            // Swagger 문서 모델 Parameter
            List<io.swagger.v3.oas.models.parameters.Parameter> swaggerParams = operation.getParameters();
            if (swaggerParams == null) return operation;

            // Java Reflection Parameter
            java.lang.reflect.Parameter[] methodParams = handlerMethod.getMethod().getParameters();

            // @CurrentUser 붙은 파라미터명 찾기
            Set<String> hiddenParamNames = Arrays.stream(methodParams)
                    .filter(p -> p.isAnnotationPresent(CurrentUser.class))
                    .map(java.lang.reflect.Parameter::getName)
                    .collect(Collectors.toSet());

            // Swagger 문서 모델에서 해당 파라미터 제거
            operation.setParameters(
                    swaggerParams.stream()
                            .filter(p -> !hiddenParamNames.contains(p.getName()))
                            .collect(Collectors.toList())
            );

            return operation;
        };
    }
}
