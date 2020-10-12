package tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import utils.GitUtils
import utils.Release
import utils.ReleaseUtils

class CutBranchTask extends DefaultTask {
    private static final BASE_BRANCH = "master"
    private static final String PLIST_FILE = "../release.plist"

    @Input
    protected String username = project.property(GitUtils.GITHUB_USERNAME_KEY)

    @Input
    protected String token = project.property(GitUtils.GITHUB_TOKEN_KEY)

    @TaskAction
    void run() {
        Release currRelease = ReleaseUtils.getCurrRelease(project.file(PLIST_FILE))
        String branch = "${currRelease.name}/${currRelease.version}"

        if (!GitUtils.hasRemoteBranch(branch, username, token)) {
            GitUtils.createBranch(BASE_BRANCH, branch, username, token)
        } else throw new GradleException("The branch '$branch' already exists.")
    }
}
