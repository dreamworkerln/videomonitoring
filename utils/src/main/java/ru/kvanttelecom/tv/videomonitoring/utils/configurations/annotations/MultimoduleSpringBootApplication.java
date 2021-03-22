package ru.kvanttelecom.tv.videomonitoring.utils.configurations.annotations;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

@SpringBootApplication(scanBasePackages = "ru.kvanttelecom.tv.videomonitoring")
//@EnableJpaRepositories(basePackages = "ru.kvanttelecom.tv.videomonitoring",
//                       repositoryBaseClass = RepositoryWithEntityManager.class,
//                       repositoryFactoryBeanClass = EntityGraphJpaRepositoryFactoryBean.class)
//@EntityScan(basePackages = {"ru.kvanttelecom.tv.videomonitoring"})
//@EnableGlobalMethodSecurity(
//    prePostEnabled = true,
//    securedEnabled = true,
//    jsr250Enabled = true)
public @interface MultimoduleSpringBootApplication {
}
