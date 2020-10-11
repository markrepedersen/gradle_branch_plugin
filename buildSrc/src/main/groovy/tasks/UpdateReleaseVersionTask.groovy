package tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import utils.ReleaseUtils
import xmlwise.XmlParseException

class UpdateReleaseVersionTask extends DefaultTask {
    private static final String PLIST_FILE = "release.plist"
    private static final String CSV_FILE = "releng/release_info.csv"

    /**
     * Reads the current release (name, version) and replaces it with the next one.
     *
     * An exception will be thrown if any parsing problems occur.
     * The task output will show the details.
     *
     * NOTE: This will read the entirety of each of the files' contents into memory. If they are large, then
     * this may become a problem. However, since both are simply configuration files, then this is unlikely.
     */
    @TaskAction
    void run() {
        try {
            File pListFile = project.file(PLIST_FILE)
            Map nextRelease = ReleaseUtils.getNextRecord(project.file(CSV_FILE), pListFile)

            if (nextRelease == null) {
                throw new Exception("There is no new version available.")
            }

            ReleaseUtils.updateRelease(pListFile, nextRelease.next_name as String, nextRelease.next_version as String)
        } catch (Exception e) {
            if (e instanceof IOException || e instanceof XmlParseException) {
                throw new GradleException("There was an error parsing one of '$PLIST_FILE' or '$CSV_FILE'. Please verify.", e)
            } else {
                throw e
            }
        }
    }
}
