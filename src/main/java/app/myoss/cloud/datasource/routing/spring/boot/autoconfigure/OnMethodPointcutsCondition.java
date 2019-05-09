/*
 * Copyright 2018-2019 https://github.com/myoss
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
 *
 */

package app.myoss.cloud.datasource.routing.spring.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.CollectionUtils;

import app.myoss.cloud.datasource.routing.config.DataSourceProperty;
import app.myoss.cloud.datasource.routing.constants.DataSourceRoutingConstants;

/**
 * {@link Conditional} that checks if the specified properties
 * 'myoss-cloud.datasource-routing.databases.methodPointcuts' have a value
 *
 * @author Jerry.Chen
 * @since 2019年5月8日 下午11:54:41
 */
public class OnMethodPointcutsCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder messageBuilder = ConditionMessage.forCondition(Conditional.class,
                "(" + DataSourceRoutingConstants.CONFIG_PREFIX + ".databases.methodPointcuts)");
        DataSourceRoutingProperties dataSourceRoutingProperties = new DataSourceRoutingProperties();
        Binder binder = Binder.get(context.getEnvironment());
        binder.bind(DataSourceRoutingConstants.CONFIG_PREFIX, Bindable.ofInstance(dataSourceRoutingProperties));
        if (!CollectionUtils.isEmpty(dataSourceRoutingProperties.getDatabases())) {
            for (DataSourceProperty database : dataSourceRoutingProperties.getDatabases()) {
                if (!CollectionUtils.isEmpty(database.getMethodPointcuts())) {
                    return new ConditionOutcome(true, messageBuilder.resultedIn(true));
                }
            }
        }
        return ConditionOutcome.noMatch(messageBuilder.because("no configuration."));
    }
}
