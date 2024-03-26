package org.drools.cloudevents;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import org.drools.model.codegen.ExecutableModelProject;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.conf.PrototypesOption;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieRuntimeBuilder;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.builder.InternalKieBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Default
@ApplicationScoped
public class ProjectRuntimeBuilder implements KieRuntimeBuilder {

    private static Logger LOG = LoggerFactory.getLogger(ProjectRuntimeBuilder.class);

    private static final String RULES_DIR = "/opt/rules";
    private static final Path RULES_DIR_PATH = Path.of(RULES_DIR);
    private static final String RULES_FILE_EXTENSION = ".drl.yaml";

    private KieBase kieBase;

    /**
     * Collect rule files under /opt/rules and build KieBase
     */
    @Startup
    void init() {
        LOG.info("ProjectRuntimeBuilder init");
        long start = System.currentTimeMillis();

        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        try (Stream<Path> stream = Files.walk(RULES_DIR_PATH)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(RULES_FILE_EXTENSION))
                    .forEach(path -> addToKieFileSystem(path, ks, kfs));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        KieModuleModel kieModuleModel = ks.newKieModuleModel();
        KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel("KBase").setDefault(true);
        kieBaseModel.setPrototypes(PrototypesOption.ALLOWED);

        InternalKieBuilder kieBuilder = (InternalKieBuilder) ks.newKieBuilder(kfs);
        kieBuilder.withKModuleModel(kieModuleModel);
        kieBuilder.buildAll(ExecutableModelProject.class);
        KieContainer kieContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
        kieBase = kieContainer.getKieBase();

        LOG.info("kieBase : " + kieBase);
        Collection<KiePackage> kiePackages = kieBase.getKiePackages();
        kiePackages.forEach(kiePackage -> {
            Collection<Rule> rules = kiePackage.getRules();
            LOG.info("rules : " + rules);
        });

        LOG.info("ProjectRuntimeBuilder init done : elapsed time = " + (System.currentTimeMillis() - start) + "ms");
    }

    private void addToKieFileSystem(Path path, KieServices ks, KieFileSystem kfs) {
        try {
            LOG.info("Adding " + RULES_DIR_PATH.relativize(path) + " to KieFileSystem");
            kfs.write("src/main/resources/" + RULES_DIR_PATH.relativize(path), Files.readString(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public KieBase getKieBase() {
        return kieBase;
    }

    @Override
    public KieBase getKieBase(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public KieSession newKieSession() {
        return kieBase.newKieSession();
    }

    @Override
    public KieSession newKieSession(String sessionName) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public KieSession newKieSession(KieSessionConfiguration conf) {
        return getKieBase().newKieSession(conf, null);
    }
}
