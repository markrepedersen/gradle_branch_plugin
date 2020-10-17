package plugins

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import tasks.CutBranchTask
import tasks.GenerateFFDiffTask
import tasks.UpdateReleaseVersionTask

@CompileStatic
class CodeFreezePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        Task cutBranchTask = project.tasks.create("cutBranch", CutBranchTask)
        Task generateFFDiffTask = project.tasks.create("generateFFDiff", GenerateFFDiffTask)
        Task updateReleaseVersionTask = project.tasks.create("updateReleaseVersion", UpdateReleaseVersionTask)
        Task codeFreezeTask = project.tasks.create("codeFreeze")

        codeFreezeTask.finalizedBy(cutBranchTask)
        codeFreezeTask.finalizedBy(generateFFDiffTask)
        codeFreezeTask.finalizedBy(updateReleaseVersionTask)

        updateReleaseVersionTask.mustRunAfter(cutBranchTask, generateFFDiffTask)
        generateFFDiffTask.mustRunAfter(cutBranchTask)
    }
}
