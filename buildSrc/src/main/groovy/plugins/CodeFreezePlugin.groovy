package plugins

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import tasks.CutBranchTask
import tasks.GenerateFFDiffTask
import tasks.UpdateReleaseVersionTask

@CompileStatic
class CodeFreezePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.tasks.register("cutBranch", CutBranchTask)
        project.tasks.register("generateFFDiff", GenerateFFDiffTask)
        project.tasks.register("updateReleaseVersion", UpdateReleaseVersionTask)
    }
}
