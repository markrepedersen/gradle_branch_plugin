package tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import utils.GitUtils

class CutBranchTask extends DefaultTask {
    @Input
    protected String username = project.property(GitUtils.GITHUB_USERNAME_KEY)

    @Input
    protected String token = project.property(GitUtils.GITHUB_TOKEN_KEY)

    @TaskAction
    void run() {
    }
}
