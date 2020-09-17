package io.jenkins.plugins.forensics.miner;

import java.util.Arrays;
import java.util.Collection;

import edu.hm.hafner.util.VisibleForTesting;

import org.kohsuke.stapler.StaplerProxy;
import hudson.model.Action;
import hudson.model.Run;

import io.jenkins.plugins.util.BuildAction;

/**
 * Controls the live cycle of the forensics results in a job. This action persists the results of a build and displays a
 * summary on the build page. The actual visualization of the results is defined in the matching {@code summary.jelly}
 * file. This action also provides access to the forensics details: these are rendered using a new view instance.
 *
 * @author Ullrich Hafner
 */
public class ForensicsBuildAction extends BuildAction<RepositoryStatistics> implements StaplerProxy {
    private static final long serialVersionUID = -263122257268060032L;

    private final int numberOfFiles;
    private final int miningDurationSeconds;

    /**
     * Creates a new instance of {@link ForensicsBuildAction}.
     *
     * @param owner
     *         the associated build that created the statistics
     * @param repositoryStatistics
     *         the statistics to persist with this action
     * @param miningDurationSeconds
     *         the duration of the mining operation in [s]
     */
    public ForensicsBuildAction(final Run<?, ?> owner, final RepositoryStatistics repositoryStatistics,
            final int miningDurationSeconds) {
        this(owner, repositoryStatistics, true, miningDurationSeconds);
    }

    /**
     * Creates a new instance of {@link ForensicsBuildAction}.
     *
     * @param owner
     *         the associated build that created the statistics
     * @param repositoryStatistics
     *         the statistics to persist with this action
     * @param canSerialize
     *         determines whether the result should be persisted in the build folder
     * @param miningDurationSeconds
     *         the duration of the mining operation in [s]
     */
    @VisibleForTesting
    ForensicsBuildAction(final Run<?, ?> owner, final RepositoryStatistics repositoryStatistics,
            final boolean canSerialize, final int miningDurationSeconds) {
        super(owner, repositoryStatistics, canSerialize);

        numberOfFiles = repositoryStatistics.size();
        this.miningDurationSeconds = miningDurationSeconds;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Arrays.asList(new ForensicsJobAction(getOwner().getParent()),
                new ForensicsCodeMetricAction(getOwner().getParent()));
    }

    @Override
    protected RepositoryStatisticsXmlStream createXmlStream() {
        return new RepositoryStatisticsXmlStream();
    }

    @Override
    protected ForensicsJobAction createProjectAction() {
        return new ForensicsJobAction(getOwner().getParent());
    }

    @Override
    protected String getBuildResultBaseName() {
        return "repository-statistics.xml";
    }

    @Override
    public String getIconFileName() {
        return ForensicsJobAction.SMALL_ICON;
    }

    @Override
    public String getDisplayName() {
        return Messages.ForensicsView_Title();
    }

    /**
     * Returns the detail view for the forensics data for all Stapler requests.
     *
     * @return the detail view for the forensics data
     */
    @Override
    public Object getTarget() {
        return new ForensicsViewModel(getOwner(), getResult());
    }

    @Override
    public String getUrlName() {
        return ForensicsJobAction.FORENSICS_ID;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public int getMiningDurationSeconds() {
        return miningDurationSeconds;
    }
}
