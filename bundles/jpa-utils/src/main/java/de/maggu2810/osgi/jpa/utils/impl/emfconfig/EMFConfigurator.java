/*-
 * #%L
 * OSGi JPA :: Bundles :: JPA Utilities
 * %%
 * Copyright (C) 2018 maggu2810
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package de.maggu2810.osgi.jpa.utils.impl.emfconfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "de.maggu2810.osgi.jpa.utils.emfconfig")
public class EMFConfigurator {

    private static final String DS_TYPE_KEY = "emfc.ds.type";
    private static final String DS_TYPE_VAL_DATASOURCE = "data-source";
    private static final String DS_TYPE_VAL_JTA_DATASOURCE = "jta-data-source";
    private static final String DS_TYPE_VAL_NON_JTA_DATASOURCE = "non-jta-data-source";

    private static final String JAVAX_PERSISTENCE_DATASOURCE = "javax.persistence.dataSource";
    private static final String JAVAX_PERSISTENCE_JTA_DATASOURCE = "javax.persistence.jtaDataSource";
    private static final String JAVAX_PERSISTENCE_NON_JTA_DATASOURCE = "javax.persistence.nonJtaDataSource";

    // private static final String JPA_CONFIGURATION_PREFIX = "org.apache.aries.jpa.";
    // private static final String JAVAX_PERSISTENCE_JDBC_DRIVER = "javax.persistence.jdbc.driver";
    // private static final String JAVAX_PERSISTENCE_TX_TYPE = "javax.persistence.transactionType";

    private final Logger logger = LoggerFactory.getLogger(EMFConfigurator.class);

    @Reference
    @SuppressWarnings({ "initialization.fields.uninitialized" /* OSGi: mandatory reference */ })
    private EntityManagerFactoryBuilder emfb;

    @Reference
    @SuppressWarnings({ "initialization.fields.uninitialized" /* OSGi: mandatory reference */ })
    private DataSource ds;

    @SuppressWarnings({ "initialization.fields.uninitialized" /* OSGi: non null if active */ })
    private EntityManagerFactory emf;

    /*
     * See:
     * https://github.com/hibernate/hibernate-orm/blob/bd256e4/hibernate-osgi/src/main/java/org/hibernate/osgi/
     * OsgiJtaPlatform.java
     *
     * Cite:
     * The Enterprise OSGi spec requires all containers to register UserTransaction and TransactionManager OSGi
     * services.
     *
     * If we don't require that this services are present the "createEntityManagerFactory" can throw an exception that
     * the service is not available.
     */

    @Reference
    @SuppressWarnings({ "initialization.fields.uninitialized" /* OSGi: mandatory reference */ })
    private TransactionManager transactionManager;

    @Reference
    @SuppressWarnings({ "initialization.fields.uninitialized" /* OSGi: mandatory reference */ })
    private UserTransaction userTransaction;

    @Activate
    void activate(final Map<String, Object> props) {
        if (logger.isTraceEnabled()) {
            logger.trace("transaction manager: {}", transactionManager);
            logger.trace("user transaction: {}", userTransaction);
        }

        final Map<String, Object> jpaProps = new HashMap<>();
        for (final Map.Entry<String, Object> entry : props.entrySet()) {
            final String key = entry.getKey();

            if (logger.isDebugEnabled()) {
                logger.debug("{} - props: {}={}", System.identityHashCode(this), key, entry.getValue());
            }

            // "service.pid"
            // "service.factoryPid"
            // "component.name"
            // "component.id"
            // "felix.fileinstall.filename"
            if (key.startsWith("service.") || key.startsWith("component.") || key.startsWith("felix.")) {
                continue;
            }
            jpaProps.put(key, entry.getValue());
        }

        getKeysForDsTypes(props.get(DS_TYPE_KEY)).forEach((key) -> {
            jpaProps.put(key, ds);
        });

        // This line also causes the emf to be registered as a service
        emf = emfb.createEntityManagerFactory(jpaProps);
    }

    @Deactivate
    void deactivate() {
        // This unregisters the emf service
        emf.close();
    }

    private Set<String> getKeysForDsTypes(final @Nullable Object jpaTypes) {
        if (jpaTypes == null) {
            return Collections.singleton(JAVAX_PERSISTENCE_JTA_DATASOURCE);
        } else {
            final Set<String> keys = new HashSet<>();
            for (final String type : jpaTypes.toString().split(",")) {
                switch (type) {
                    case DS_TYPE_VAL_DATASOURCE:
                        keys.add(JAVAX_PERSISTENCE_DATASOURCE);
                        break;
                    case DS_TYPE_VAL_JTA_DATASOURCE:
                        keys.add(JAVAX_PERSISTENCE_JTA_DATASOURCE);
                        break;
                    case DS_TYPE_VAL_NON_JTA_DATASOURCE:
                        keys.add(JAVAX_PERSISTENCE_NON_JTA_DATASOURCE);
                        break;
                    default:
                        logger.warn("Unknown datasource type: {}", type);
                        break;
                }
            }
            return keys;
        }
    }
}
