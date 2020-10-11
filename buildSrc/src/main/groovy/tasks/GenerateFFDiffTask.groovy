package tasks

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import utils.GitUtils
import utils.ReleaseUtils

import java.nio.charset.Charset

class GenerateFFDiffTask extends DefaultTask {
    private static final String PLIST_FILE = "release.plist"
    private static final String CSV_FILE = "releng/release_info.csv"
    private static final String FF_FILE = "featureflags/FF.csv"
    private static final String FF_DIFF_FILE = "featureflags/FF_diff.csv"

    @Input
    protected String username = project.property(GitUtils.GITHUB_USERNAME_KEY)

    @Input
    protected String token = project.property(GitUtils.GITHUB_TOKEN_KEY)

    @TaskAction
    void run() {
        File releaseInfo = project.file(CSV_FILE)
        File plist = project.file(PLIST_FILE)
        File featureFlagFile = project.file(FF_FILE)
        Map releases = ReleaseUtils.getPreviousRecord(releaseInfo, plist)
        String branch = "${releases.prev_name}/${releases.prev_version}"

        if (!GitUtils.hasRemoteBranch(branch, username, token)) {
            throw new GradleException("Branch '$branch' does not exist.")
        }

        String content = GitUtils.getContents(FF_FILE, branch, username, token)

        if (content) {
            try {
                CSVParser currentFeatureFlags = CSVParser.parse(featureFlagFile, Charset.forName("UTF-8"), CSVFormat.DEFAULT)
                CSVParser previousFeatureFlags = CSVParser.parse(content, CSVFormat.DEFAULT)

                diffFiles(currentFeatureFlags, previousFeatureFlags, releases)
            } catch (IOException e) {
                throw new GradleException("There was a problem reading '$FF_FILE'. See stacktrace for details.", e)
            }
        } else {
            try {
                project.file(FF_DIFF_FILE).write(featureFlagFile.text)
            } catch (IOException e) {
                throw new GradleException("There was an error writing to $FF_DIFF_FILE.", e)
            }
        }
    }

    /**
     * Runs a set difference function to find the differences between the two CSV files.
     * The set differences are then written to {@code FF_DIFF_FILE} in the form of a CSV file.
     * @param current
     * @param previous
     * @param info
     */
    void diffFiles(CSVParser current, CSVParser previous, Map info) throws IOException {
        project.file(FF_DIFF_FILE).withWriter {
            HashMap<String, String> map = new HashMap<>()
            String header1 = "${info.curr_name}/${info.curr_version}"
            String header2 = "${info.prev_name}/${info.prev_version}"
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
