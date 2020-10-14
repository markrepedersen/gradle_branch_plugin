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

    @Input
    protected String username = project.property(GitUtils.GITHUB_USERNAME_KEY)

    @Input
    protected String token = project.property(GitUtils.GITHUB_TOKEN_KEY)

    @TaskAction
    void run() {
        Release releases = ReleaseUtils.getRelease(project.file(ReleaseUtils.PLIST_FILE), project.file(ReleaseUtils.CSV_FILE))

        if (releases && releases.name && releases.version) {
            String branch = "${releases.name}/${releases.version}"

            if (!GitUtils.hasRemoteBranch(branch, username, token)) {
                GitUtils.createBranch(BASE_BRANCH, branch, username, token)
            } else throw new GradleException("The branch '$branch' already exists.")
        } else throw new GradleException("There is no current <name/version> for cutting the branch.")
    }
}
