package br.com.cinema.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cinemaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cinema API - Monólito Modular Hexagonal")
                        .description("API REST do sistema de Cinema para gestão de Catálogo de Filmes/Sessões, Reservas de Assentos e Cobranças/Pagamentos com controle de concorrência e timeout de 10 minutos.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Equipe Cinema")
                                .email("contato@cinema.com.br"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Ambiente Local de Desenvolvimento")
                ));
    }
}
