package com.mongodb.internal.diagnostics.logging;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Loggers {

    private static final String PREFIX = "org.mongodb.driver";
    private static final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(Loggers.class);

    public static Logger getLogger(@NotNull final String suffix) {
        return new LoggerWrapper(plugin.getLogger());
    }

    private static class LoggerWrapper implements Logger {
        private final java.util.logging.Logger logger;

        public LoggerWrapper(java.util.logging.Logger logger) {
            this.logger = logger;
        }

        @Override
        public String getName() {
            return logger.getName();
        }


        @Override
        public boolean isWarnEnabled() {
            return true;
        }

        @Override
        public void warn(String msg) {
            logger.warning(msg);
        }

        @Override
        public void warn(String msg, Throwable t) {
            logger.warning(msg);
        }

        @Override
        public boolean isErrorEnabled() {
            return true;
        }

        @Override
        public void error(String msg) {
            logger.severe(msg);
        }

        @Override
        public void error(String msg, Throwable t) {
            logger.severe(msg);
        }
    }

}