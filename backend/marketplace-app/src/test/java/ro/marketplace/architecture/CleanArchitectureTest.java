package ro.marketplace.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "ro.marketplace",
        importOptions = {ImportOption.DoNotIncludeTests.class}
)
public class CleanArchitectureTest {

    @ArchTest
    static final ArchRule domain_classes_do_not_depend_on_spring_or_jpa =
            noClasses()
                    .that().resideInAPackage("ro.marketplace.domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "jakarta.transaction..",
                            "org.hibernate..",
                            "ro.marketplace.application..",
                            "ro.marketplace.infrastructure..",
                            "ro.marketplace.app.."
                    )
                    .because("Domain classes must not depend on Spring, JPA, or outer layers");

    @ArchTest
    static final ArchRule application_classes_do_not_depend_on_spring_jpa_or_infrastructure =
            noClasses()
                    .that().resideInAPackage("ro.marketplace.application..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "jakarta.transaction..",
                            "org.hibernate..",
                            "ro.marketplace.infrastructure..",
                            "ro.marketplace.app.."
                    )
                    .because("Application classes must not depend on Spring, JPA, infrastructure, or app layers");

    @ArchTest
    static final ArchRule api_classes_do_not_import_from_infrastructure =
            noClasses()
                    .that().resideInAPackage("ro.marketplace.app..")
                    .should().dependOnClassesThat().resideInAPackage("ro.marketplace.infrastructure..")
                    .because("API (app) classes must not import from infrastructure");

    @ArchTest
    static final ArchRule infrastructure_does_not_depend_on_app =
            noClasses()
                    .that().resideInAPackage("ro.marketplace.infrastructure..")
                    .should().dependOnClassesThat().resideInAPackage("ro.marketplace.app..")
                    .because("Infrastructure must not depend on the API layer");

    @ArchTest
    static final ArchRule controllers_must_not_depend_on_repositories =
            noClasses()
                    .that().resideInAPackage("ro.marketplace.app.controller..")
                    .should().dependOnClassesThat().haveNameMatching(".*Repository")
                    .because("Controllers must go through application services, not repositories");

    @ArchTest
    static final ArchRule port_interfaces_are_in_domain_port_package =
            classes()
                    .that().haveNameMatching(".*Repository")
                    .and().areInterfaces()
                    .and().resideInAPackage("ro.marketplace.domain..")
                    .should().resideInAPackage("ro.marketplace.domain.port..")
                    .because("Repository port interfaces must reside in domain.port package");

    @ArchTest
    static final ArchRule domain_avoids_concurrency_primitives =
            noClasses()
                    .that().resideInAPackage("ro.marketplace.domain..")
                    .should().dependOnClassesThat().resideInAPackage("java.util.concurrent..")
                    .because("Domain must stay free of concurrency primitives; transactions belong to UnitOfWork");

    @ArchTest
    static final ArchRule port_interfaces_must_be_interfaces =
            classes()
                    .that().resideInAPackage("ro.marketplace.domain.port..")
                    .should().beInterfaces()
                    .because("All ports in domain.port must be interfaces");
}
