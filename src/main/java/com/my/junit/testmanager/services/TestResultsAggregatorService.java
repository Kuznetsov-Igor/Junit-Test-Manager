package com.my.junit.testmanager.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.my.junit.testmanager.data.TestResultRowData;
import com.my.junit.testmanager.data.TestResultsSummary;
import com.my.junit.testmanager.toolwindow.TestResultsToolWindowManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.my.junit.testmanager.utils.MessagesBundle.message;

/**
 * Сервис агрегирует текущие и предыдущие результаты запусков тестов.
 */
@Service(Service.Level.PROJECT)
public final class TestResultsAggregatorService {

    private final TestResultsToolWindowManager toolWindowManager;
    private final Map<String, TestResultRowData> lastResults = new HashMap<>();

    public TestResultsAggregatorService(@NotNull Project project) {
        this.toolWindowManager = project.getService(TestResultsToolWindowManager.class);
    }

    public void publishResults(
            @NotNull TestResultsSummary summary,
            @NotNull List<TestResultRowData> currentRows
    ) {
        final var withHistory = currentRows.stream()
                .map(row -> {
                    final var previous = lastResults.get(rowKey(row));
                    final var rowWithPrev = previous == null
                            ? row
                            : row.withPreviousResult(previous.getCurrentResult(), previous.getLogOutput());
                    lastResults.put(rowKey(row), row);
                    return rowWithPrev;
                })
                .collect(Collectors.toList());

        final var displayRows = formatForDisplay(withHistory);
        if (toolWindowManager != null) {
            toolWindowManager.showResults(summary, displayRows);
        }
    }

    private List<TestResultRowData> formatForDisplay(@NotNull List<TestResultRowData> rows) {
        final var grouped = new LinkedHashMap<String, List<TestResultRowData>>();
        rows.forEach(row -> grouped.computeIfAbsent(row.getTestClassName(), key -> new ArrayList<>()).add(row));

        final var formatted = new ArrayList<TestResultRowData>();
        grouped.forEach((className, classRows) -> {
            var isFirstRow = true;
            long totalDuration = 0L;
            final var displayName = classRows.isEmpty()
                    ? className
                    : classRows.get(0).getDisplayTestClassName();

            for (var row : classRows) {
                totalDuration += row.getDurationMillis();
                formatted.add(row.toBuilder()
                        // Keep the real test class name for navigation/search,
                        // but hide duplicates in the UI via displayTestClassName.
                        .testClassName(row.getTestClassName())
                        .displayTestClassName(isFirstRow ? displayName : "")
                        .durationFormatted(formatDuration(row.getDurationMillis()))
                        .build());
                isFirstRow = false;
            }
            final var summaryRow = TestResultRowData.builder()
                    .testClassName(displayName)
                    .displayTestClassName(displayName)
                    .classQualifiedName(classRows.isEmpty() ? null : classRows.get(0).getClassQualifiedName())
                    .methodName(message("toolwindow.results.row.total"))
                    .currentResult("")
                    .previousResult("")
                    .durationMillis(totalDuration)
                    .durationFormatted(formatDuration(totalDuration))
                    .summaryRow(true)
                    .logOutput("")
                    .previousLogOutput("")
                    .locationUrl(null)
                    .build();
            formatted.add(summaryRow);
        });
        return formatted;
    }

    private String formatDuration(long millis) {
        return message("toolwindow.results.duration.ms", millis);
    }

    private String rowKey(TestResultRowData row) {
        final var classKey = row.getClassQualifiedName() != null && !row.getClassQualifiedName().isBlank()
                ? row.getClassQualifiedName()
                : row.getTestClassName();
        return classKey + "#" + row.getNormalizedMethodName();
    }
}

