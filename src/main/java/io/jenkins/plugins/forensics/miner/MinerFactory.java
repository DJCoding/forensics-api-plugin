package io.jenkins.plugins.forensics.miner;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import edu.hm.hafner.util.FilteredLog;

import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;

import io.jenkins.plugins.forensics.miner.RepositoryMiner.NullMiner;
import io.jenkins.plugins.forensics.util.ScmResolver;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Jenkins extension point that allows plugins to create {@link RepositoryMiner} instances based on a supported {@link
 * SCM}.
 *
 * @author Ullrich Hafner
 */
public abstract class MinerFactory implements ExtensionPoint {
    private static final Function<Optional<RepositoryMiner>, Stream<? extends RepositoryMiner>> OPTIONAL_MAPPER
            = o -> o.map(Stream::of).orElseGet(Stream::empty);

    /**
     * Returns a repository miner for the specified {@link SCM}.
     *
     * @param scm
     *         the {@link SCM} to create the miner for
     * @param run
     *         the current build
     * @param workspace
     *         the workspace of the current build
     * @param listener
     *         a task listener
     * @param logger
     *         a logger to report error messages
     *
     * @return a repository miner instance that creates statistics for all available files in the specified {@link SCM}
     */
    public abstract Optional<RepositoryMiner> createMiner(SCM scm, Run<?, ?> run, FilePath workspace,
            TaskListener listener, FilteredLog logger);

    /**
     * Returns a miner for the repository of the specified {@link Run build}.
     *
     * @param run
     *         the current build
     * @param scmDirectories
     *         paths to search for the SCM repository
     * @param listener
     *         a task listener
     * @param logger
     *         a logger to report error messages
     *
     * @return a miner for the SCM of the specified build or a {@link NullMiner} if the SCM is not supported
     */
    public static RepositoryMiner findMiner(final Run<?, ?> run,
            final Collection<FilePath> scmDirectories, final TaskListener listener, final FilteredLog logger) {
        return scmDirectories.stream()
                .map(directory -> findMiner(run, directory, listener, logger))
                .flatMap(OPTIONAL_MAPPER)
                .findFirst()
                .orElseGet(() -> createNullMiner(logger));
    }

    private static RepositoryMiner createNullMiner(final FilteredLog logger) {
        if (findAllExtensions().isEmpty()) {
            logger.logInfo("-> No miner installed yet. You need to install the `git-forensics` plugin to enable mining of Git repositories.");
        }
        else {
            logger.logInfo("-> No suitable miner found.");
        }
        return new NullMiner();
    }

    private static Optional<RepositoryMiner> findMiner(final Run<?, ?> run, final FilePath workTree,
            final TaskListener listener, final FilteredLog logger) {
        SCM scm = new ScmResolver().getScm(run);

        return findAllExtensions().stream()
                .map(minerFactory -> minerFactory.createMiner(scm, run, workTree, listener, logger))
                .flatMap(OPTIONAL_MAPPER)
                .findFirst();
    }

    /**
     * Returns a miner for the repository of the specified {@link Run build}.
     *
     * @param scm
     *         the SCM repository
     * @param run
     *         the current build
     * @param workTree
     *         the working tree of the repository
     * @param listener
     *         a task listener
     * @param logger
     *         a logger to report error messages
     *
     * @return a miner for the SCM of the specified build or a {@link NullMiner} if the SCM is not supported
     */
    public static RepositoryMiner findMiner(final SCM scm, final Run<?, ?> run,
            final FilePath workTree, final TaskListener listener, final FilteredLog logger) {
        return findAllExtensions().stream()
                .map(minerFactory -> minerFactory.createMiner(scm, run, workTree, listener, logger))
                .flatMap(OPTIONAL_MAPPER)
                .findFirst()
                .orElse(new NullMiner());
    }

    private static List<MinerFactory> findAllExtensions() {
        return new JenkinsFacade().getExtensionsFor(MinerFactory.class);
    }
}
