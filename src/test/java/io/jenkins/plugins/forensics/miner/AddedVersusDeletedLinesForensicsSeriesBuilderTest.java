package io.jenkins.plugins.forensics.miner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddedVersusDeletedLinesForensicsSeriesBuilderTest {

    private static final String ADDED = "added";
    private static final String DELETED = "deleted";

    @Test
    void shouldCreateSeriesWithMockito() {
        // Given
        AddedVersusDeletedLinesForensicsSeriesBuilder builder = new AddedVersusDeletedLinesForensicsSeriesBuilder();
        ForensicsBuildAction actionStub = mock(ForensicsBuildAction.class);
        when(actionStub.getCommitStatistics()).thenReturn(createCommitStatistics(1, 2));

        // When
        Map<String, Integer> series = builder.computeSeries(actionStub);

        // Then
        assertThat(series)
                .containsEntry(ADDED, 1)
                .containsEntry(DELETED, 2);

    }

    private CommitStatistics createCommitStatistics(final int added, final int deleted) {
        List<CommitDiffItem> commits = new ArrayList<>();
        CommitDiffItem item = new CommitDiffItem("1", "author", 1);
        item.addLines(added).deleteLines(deleted);
        commits.add(item);
        return new CommitStatistics(commits);
    }
}