package br.com.cinema;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "br.com.cinema", importOptions = ImportOption.DoNotIncludeTests.class)
@DisplayName("Fiscalização Automatizada de Arquitetura (Spring Modulith + ArchUnit)")
class ArchitectureGuardTest {

    // 1. Fiscal Macro: Monólito Modular (Spring Modulith)
    @Test
    @DisplayName("Deve validar fronteiras entre módulos do Monólito Modular")
    void deveValidarFronteirasEntreModulos() {
        ApplicationModules.of(CinemaApplication.class).verify();
    }

    // 2. Fiscal Micro: Pureza do Domínio (Zero Spring / Zero JPA / Zero Infra)
    @ArchTest
    static final ArchRule dominioDeveSerPuro = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "..application..",
                    "..infrastructure..",
                    "org.springframework..",
                    "jakarta.persistence..",
                    "org.hibernate.."
            )
            .allowEmptyShould(true)
            .because("A camada domain deve conter apenas Java puro, sem acoplamento com frameworks ou banco de dados.");

    // 3. Fiscal Micro: Aplicação não conhece Infraestrutura
    @ArchTest
    static final ArchRule aplicacaoNaoDeveDependerDeInfraestrutura = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .allowEmptyShould(true)
            .because("A camada application deve depender apenas de domain, shared e de suas próprias ports.");

    // 4. Fiscal Micro: Controllers só podem chamar Ports In
    @ArchTest
    static final ArchRule controllersNaoDevemAcessarServicesOuRepositoriosDiretamente = noClasses()
            .that().resideInAPackage("..infrastructure.adapter.in.web..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..application.service..", "..infrastructure.adapter.out.persistence..")
            .allowEmptyShould(true)
            .because("Controllers REST devem invocar exclusivamente interfaces em application.port.in.");
}
