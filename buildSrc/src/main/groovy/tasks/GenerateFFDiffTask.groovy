package tasks

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import utils.GitUtils
import utils.Release
import utils.ReleaseUtils

import java.nio.charset.Charset

class GenerateFFDiffTask extends DefaultTask {
    private static final String FF_DIR = "featureflags"
    private static final String FF_REMOTE_FILE = "$FF_DIR/FF.csv"
    private static final String FF_FILE = "../$FF_REMOTE_FILE"
    private static final String FF_DIFF_FILE = "../$FF_DIR/FF_diff.csv"

    @Input
    protected String username = project.property(GitUtils.GITHUB_USERNAME_KEY)

    @Input
    protected String token = project.property(GitUtils.GITHUB_TOKEN_KEY)

    @TaskAction
    void run() {
        println("Attempting to diff current and previous feature flags.")

        File releaseInfo = project.file(ReleaseUtils.CSV_FILE)
        File plist = project.file(ReleaseUtils.PLIST_FILE)
        File featureFlagFile = project.file(FF_FILE)
        Release releases = ReleaseUtils.getRelease(plist, releaseInfo)

        if (releases && releases.prevName && releases.prevVersion) {
            String branch = "${releases.prevName}/${releases.prevVersion}"

            if (!GitUtils.hasRemoteBranch(branch, username, token)) {
                println("Previous version's branch '$branch' does not exist. The diff will only contain current branch's feature flag contents.")
                writeFile(featureFlagFile)
                return
            }

            String content = GitUtils.getContents(FF_REMOTE_FILE, branch, username, token)

            if (content) {
                try {
                    CSVParser currentFeatureFlags = CSVParser.parse(featureFlagFile, Charset.forName("UTF-8"), CSVFormat.DEFAULT)
                    CSVParser previousFeatureFlags = CSVParser.parse(content, CSVFormat.DEFAULT)

                    diffFiles(currentFeatureFlags, previousFeatureFlags, releases)
                    println("Successfully diffed current and previous feature flags.")
                } catch (IOException e) {
                    throw new GradleException("There was a problem reading '$FF_FILE'. See stacktrace for details.", e)
                }
            } else writeFile(featureFlagFile)
        } else {
            println("There was no previous version. Diff will only contain current version's feature flag content.")
            writeFile(featureFlagFile)
        }
    }

    private void writeFile(File file) {
        try {
            project.file(FF_DIFF_FILE).write(file.text)
        } catch (IOException e) {
            throw new GradleException("There was an error writing to $FF_DIFF_FILE.", e)
        }
    }

    /**
     * Runs a set difference function to find the differences between the two CSV files.
     * The set differences are then written to {@code FF_DIFF_FILE} in the form of a CSV file.
     * @param current
     * @param previous
     * @param info
     */
    void diffFiles(CSVParser current, CSVParser previous, Release info) throws IOException {
        project.file(FF_DIFF_FILE).withWriter {
            HashMap<String, String> map = new HashMap<>()
            String header1 = "${info.name}/${info.version}"
            String header2 = "${info.prevName}/${info.prevVersion}"
            CSVFormat format = CSVFormat.DEFAULT.withHeader(
                    "[${header1}]_name",
                    "[${header1}]_version",
                    "[${header2}_name]",
                    "[${header2}]_version"
            )
            CSVPrinter printer = new CSVPrinter(it, format)

            current.each { map.put(it[0], it[1]) }
            previous
                    .records
                    .each {
                        if (map[it[0]] != it[1]) {
                            String currName = map.containsKey(it[0]) ? it[0].trim() : "null"
                            String currVersion = map.containsKey(it[0]) ? map[it[0]].trim() : "null"
                            String prevName = it[0].trim()
                            String prevVersion = it[1].trim()

                            printer.printRecord(currName, currVersion, prevName, prevVersion)
                        }
                    }
        }
    }
}
