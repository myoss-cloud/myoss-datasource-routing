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

package app.myoss.cloud.datasource.routing.jdbc;

import java.io.Closeable;
import java.io.PrintWriter;
import java.security.AccessControlException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.LoggerFactory;

/**
 * Abstract {@link javax.sql.DataSource} implementation that routes
 * {@link #getConnection()} calls to one of various target DataSources based on
 * a lookup key. The latter is usually (but not necessarily) determined through
 * some thread-bound transaction context.
 *
 * @author Jerry.Chen
 * @since 2019年5月7日 下午9:38:57
 * @see org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
 */
public abstract class AbstractRoutingDataSource implements DataSource, Closeable {
    /**
     * Logger available to subclasses.
     */
    protected final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Returns 0, indicating the default system timeout is to be used.
     */
    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    /**
     * Setting a login timeout is not supported.
     */
    @Override
    public void setLoginTimeout(int timeout) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout");
    }

    /**
     * LogWriter methods are not supported.
     */
    @Override
    public PrintWriter getLogWriter() {
        throw new UnsupportedOperationException("getLogWriter");
    }

    /**
     * LogWriter methods are not supported.
     */
    @Override
    public void setLogWriter(PrintWriter pw) throws SQLException {
        throw new UnsupportedOperationException("setLogWriter");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return determineTargetDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineTargetDataSource().getConnection(username, password);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return determineTargetDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return (iface.isInstance(this) || determineTargetDataSource().isWrapperFor(iface));
    }

    /**
     * number of the datasource.
     *
     * @param prefix number prefix
     * @return number of the datasource.
     */
    protected String getDataSourceNumber(String prefix) {
        try {
            // DataSource number is global to the VM to avoid overlapping DataSource numbers in classloader scoped environments
            synchronized (System.getProperties()) {
                String name = this.getClass().getName() + ".datasource_number";
                final String next = String.valueOf(Integer.getInteger(name, 0) + 1);
                System.setProperty(name, next);
                return prefix + next;
            }
        } catch (AccessControlException e) {
            logger.warn(
                    "The SecurityManager didn't allow us to read/write system properties, so just generate a random DataSource number instead",
                    e);
            return prefix + RandomStringUtils.randomAlphanumeric(4);
        }
    }

    /**
     * Retrieve the current target DataSource.
     *
     * @return determine target DataSource
     */
    protected abstract DataSource determineTargetDataSource();

    //---------------------------------------------------------------------
    // Implementation of JDBC 4.1's getParentLogger method
    //---------------------------------------------------------------------

    @Override
    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
}
