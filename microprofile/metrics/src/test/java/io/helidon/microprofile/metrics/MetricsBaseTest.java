/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.microprofile.metrics;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.inject.spi.CDI;

import io.helidon.metrics.MetricsSupport;
import io.helidon.microprofile.metrics.MetricsCdiExtension;

import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Class MetricsBaseTest.
 */
public class MetricsBaseTest {

    private static final String METRIC_NAME_TEMPLATE = "%s.%s";

    private static SeContainer cdiContainer;

    private static MetricRegistry metricRegistry;

    @BeforeAll
    public synchronized static void startCdiContainer() {
        MetricsSupport.create();        // needed by metrics CDI
        final SeContainerInitializer initializer = SeContainerInitializer.newInstance();
        assertThat(initializer, is(notNullValue()));
        cdiContainer = initializer.initialize();
    }

    @AfterAll
    public synchronized static void shutDownCdiContainer() {
        if (cdiContainer != null) {
            cdiContainer.close();
        }
    }

    static synchronized MetricRegistry getMetricRegistry() {
        if (metricRegistry == null) {
            metricRegistry = CDI.current().select(MetricRegistry.class).get();
        }
        return metricRegistry;
    }

    @SuppressWarnings("unchecked")
    static <T extends Metric> T getMetric(Object bean, String name) {
        String metricName = String.format(METRIC_NAME_TEMPLATE,
                                          MetricsCdiExtension.getRealClass(bean).getName(),        // CDI proxies
                                          name);
        return (T) getMetricRegistry().getMetrics().get(metricName);
    }

    <T> T newBean(Class<T> beanClass) {
        return CDI.current().select(beanClass).get();
    }
}
