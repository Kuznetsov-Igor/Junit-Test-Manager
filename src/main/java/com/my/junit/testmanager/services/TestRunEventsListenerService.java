package com.my.junit.testmanager.services;

import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsAdapter;
import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsListener;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.my.junit.testmanager.data.ResultStatusTest;
import com.my.junit.testmanager.data.TestResultRowData;
import com.my.junit.testmanager.data.TestResultsSummary;
import com.my.junit.testmanager.utils.LoggerUtils;
import com.my.junit.testmanager.utils.MessagesBundle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Слушает события SMTRunner и передает результаты в агрегатор.
 */
@Service(Service.Level.PROJECT)
public final class TestRunEventsListenerService {

    private final TestResultsAggregatorService aggregatorService;
    private final LoggerUtils log = LoggerUtils.getLogger(TestRunEventsListenerService.class);
    private final Map<SMTestProxy.SMRootTestProxy, RunContext> runningContexts = new ConcurrentHashMap<>();

    public TestRunEventsListenerService(@NotNull Project project) {
        this.aggregatorService = project.getService(TestResultsAggregatorService.class);
        final var listener = new SMTRunnerEventsAdapter() {
            @Override
            public void onTestingStarted(@NotNull SMTestProxy.SMRootTestProxy testsRoot) {
                runningContexts.put(testsRoot, new RunContext());
                log.logInfo("SMT testing started: " + testsRoot.getName());
            }

            @Override
            public void onTestingFinished(@NotNull SMTestProxy.SMRootTestProxy testsRoot) {
                final var context = runningContexts.remove(testsRoot);
                if (context == null || context.rows.isEmpty()) {
                    log.logInfo("SMT testing finished with no collected rows.");
                    return;
                }
                final var summary = context.buildSummary();
                aggregatorService.publishResults(summary, context.rows);
                log.logInfo("SMT testing finished: " + testsRoot.getName() + ", rows=" + context.rows.size());
            }

            @Override
            public void onTestFinished(@NotNull SMTestProxy testProxy) {
                if (testProxy.isSuite() || testProxy.getParent() == null) {
                    return;
                }
                final var root = testProxy.getRoot();
                final var context = runningContexts.get(root);
                if (context == null) {
                    return;
                }
                final var info = resolveTestInfo(testProxy);
                final var status = resolveStatus(testProxy);
                final var duration = Math.max(0L, getDuration(testProxy));
                final var logOutput = buildLogOutput(testProxy);
                final var row = TestResultRowData.builder()
                        .testClassName(info.displayClassName())
                        .displayTestClassName(info.displayClassName())
                        .classQualifiedName(info.qualifiedClassName())
                        .methodName(info.displayMethodName())
                        .currentResult(localizeStatus(status))
                        .previousResult(null)
                        .durationMillis(duration)
                        .durationFormatted(formatDuration(duration))
                        .summaryRow(false)
                        .logOutput(logOutput)
                        .previousLogOutput(null)
                        .locationUrl(info.locationUrl())
                        .build();
                context.addRow(row, status);
            }
        };
        if (!registerViaManager(project, listener)) {
            project.getMessageBus().connect().subscribe(SMTRunnerEventsListener.TEST_STATUS, listener);
            log.logWarn("SMTRunnerEventsManager not available. Falling back to MessageBus subscription.");
        }
    }

    private boolean registerViaManager(@NotNull Project project, @NotNull SMTRunnerEventsListener listener) {
        try {
            final var managerClass =
                    Class.forName("com.intellij.execution.testframework.sm.SMTRunnerEventsManager");
            final var getInstance = managerClass.getMethod("getInstance", Project.class);
            final var manager = getInstance.invoke(null, project);
            if (manager == null) {
                return false;
            }
            final var addListener =
                    managerClass.getMethod("addEventsListener", SMTRunnerEventsListener.class, Object.class);
            addListener.invoke(manager, listener, project);
            return true;
        } catch (Exception ex) {
            log.logWarn("Failed to register listener via SMTRunnerEventsManager: " + ex.getMessage());
            return false;
        }
    }

    private static long getDuration(@NotNull SMTestProxy testProxy) {
        final var duration = testProxy.getDuration();
        if (duration != null) {
            return duration;
        }
        if (testProxy.getParent() != null) {
            final var parentDuration = testProxy.getParent().getDuration();
            return parentDuration != null ? parentDuration : 0L;
        }
        return 0L;
    }

    private static TestMethodInfo resolveTestInfo(@NotNull SMTestProxy testProxy) {
        final var parent = testProxy.getParent();
        var displayClass = parent == null || parent instanceof SMTestProxy.SMRootTestProxy
                ? testProxy.getName()
                : parent.getName();
        String fallbackMethodFromClass = null;

        final int slashIdx = displayClass.indexOf('/');
        if (slashIdx > 0 && slashIdx + 1 < displayClass.length()) {
            fallbackMethodFromClass = displayClass.substring(slashIdx + 1);
            displayClass = displayClass.substring(0, slashIdx);
        }

        displayClass = sanitizeClassDisplayName(displayClass);
        final var locationUrl = testProxy.getLocationUrl();
        String qualifiedClass = null;
        String methodIdentifier = null;

        if (locationUrl != null) {
            var payload = extractLocationPayload(locationUrl);
            final var hash = payload.indexOf('#');
            if (hash > 0) {
                qualifiedClass = payload.substring(0, hash);
                methodIdentifier = payload.substring(hash + 1);
            } else {
                qualifiedClass = payload;
            }
        }

        final var simpleClass = qualifiedClass != null ? simpleClassName(qualifiedClass) : displayClass;
        final var sanitizedClass = sanitizeClassDisplayName(simpleClass);
        if (methodIdentifier == null) {
            methodIdentifier = fallbackMethodFromClass != null ? fallbackMethodFromClass : testProxy.getName();
        }

        final var normalizedMethod = stripMethodDecorators(methodIdentifier);
        final var displayMethod = testProxy.getName() != null ? testProxy.getName() : normalizedMethod + "()";
        return new TestMethodInfo(sanitizedClass, qualifiedClass, displayMethod, normalizedMethod, locationUrl);
    }

    private static String extractLocationPayload(@NotNull String locationUrl) {
        final var schemeIdx = locationUrl.indexOf("://");
        if (schemeIdx >= 0 && schemeIdx + 3 < locationUrl.length()) {
            return locationUrl.substring(schemeIdx + 3);
        }
        return locationUrl;
    }

    private static String stripMethodDecorators(String name) {
        if (name == null) {
            return "";
        }
        var normalized = name;
        final var braceIdx = normalized.indexOf('(');
        if (braceIdx >= 0) {
            normalized = normalized.substring(0, braceIdx);
        }
        final var bracketIdx = normalized.indexOf('[');
        if (bracketIdx >= 0) {
            normalized = normalized.substring(0, bracketIdx);
        }
        return normalized.trim();
    }

    private static String sanitizeClassDisplayName(String name) {
        if (name == null) {
            return "";
        }
        var sanitized = name.trim();
        final int slashIdx = sanitized.indexOf('/');
        if (slashIdx >= 0) {
            sanitized = sanitized.substring(0, slashIdx);
        }
        return sanitized;
    }

    private static String simpleClassName(@NotNull String qualifiedName) {
        final var idx = qualifiedName.lastIndexOf('.');
        if (idx >= 0 && idx + 1 < qualifiedName.length()) {
            return qualifiedName.substring(idx + 1);
        }
        return qualifiedName;
    }

    private static ResultStatusTest resolveStatus(@NotNull SMTestProxy proxy) {
        if (proxy.isIgnored()) {
            return ResultStatusTest.IGNORED;
        }
        if (proxy.isDefect() || proxy.isInterrupted()) {
            return ResultStatusTest.FAILED;
        }
        if (proxy.isPassed()) {
            return ResultStatusTest.PASSED;
        }
        return ResultStatusTest.WARNING;
    }

    private static String localizeStatus(ResultStatusTest status) {
        return switch (status) {
            case PASSED -> message("toolwindow.results.status.success");
            case WARNING -> message("toolwindow.results.status.warning");
            case FAILED -> message("toolwindow.results.status.failed");
            case IGNORED -> message("toolwindow.results.status.ignored");
        };
    }

    private static String formatDuration(long duration) {
        return message("toolwindow.results.duration.ms", duration);
    }

    private static String buildLogOutput(@NotNull SMTestProxy proxy) {
        final var builder = new StringBuilder();
        appendSection(builder, "MESSAGE", proxy.getErrorMessage());
        appendSection(builder, "STACKTRACE", proxy.getStacktrace());
        final var text = builder.toString().trim();
        return text.isEmpty()
                ? message("toolwindow.results.log.empty")
                : text;
    }

    private static void appendSection(StringBuilder builder, String title, String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append("\n\n");
        }
        builder.append("[").append(title).append("]\n");
        builder.append(content.trim());
    }

    private static final class RunContext {
        private final List<TestResultRowData> rows = new ArrayList<>();
        private final EnumMap<ResultStatusTest, Integer> counters = new EnumMap<>(ResultStatusTest.class);
        private long totalDuration;

        private RunContext() {
            for (var status : ResultStatusTest.values()) {
                counters.put(status, 0);
            }
        }

        private void addRow(TestResultRowData row, ResultStatusTest status) {
            rows.add(row);
            totalDuration += row.getDurationMillis();
            counters.computeIfPresent(status, (key, value) -> value + 1);
        }

        private TestResultsSummary buildSummary() {
            final var passed = counters.getOrDefault(ResultStatusTest.PASSED, 0);
            final var warnings =
                    counters.getOrDefault(ResultStatusTest.WARNING, 0)
                            + counters.getOrDefault(ResultStatusTest.IGNORED, 0);
            final var failed = counters.getOrDefault(ResultStatusTest.FAILED, 0);
            return TestResultsSummary.builder()
                    .totalDuration(formatDuration(totalDuration))
                    .passedCount(passed)
                    .warningCount(warnings)
                    .failedCount(failed)
                    .build();
        }
    }

    /**
     * Информация о тестовом методе.
     *
     * @param displayClassName     Отображаемое название класса
     * @param qualifiedClassName   Название класса с пакетом
     * @param displayMethodName    Отображаемое название метода
     * @param normalizedMethodName Нормализованное название метода
     * @param locationUrl          URL расположения метода
     */
    private record TestMethodInfo(
            String displayClassName,
            String qualifiedClassName,
            String displayMethodName,
            String normalizedMethodName,
            String locationUrl
    ) {
    }
}

