package utils

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.gradle.api.GradleException
import xmlwise.Plist

import java.nio.charset.Charset

class ReleaseUtils {
    private static final String COLUMN_NAME_0 = "rls_name"
    private static final String COLUMN_NAME_1 = "rls_ver"

    static final String RELEASE_NAME_KEY = "SLKReleaseName"
    static final String RELEASE_VERSION_KEY = "CFBundleShortVersionString"

    static parseReleasesFile(File file) {
        trimLeadingSpaces(file)

        try {
            return Plist.load(file)
        } catch (Exception e) {
            throw new GradleException("There was an error parsing '${file.path}'.", e)
        }
    }

    /**
     * Trims any leading whitespace/newline characters in the given file.
     * NOTE: XML parsing is Java doesn't work when there are leading newline characters/spaces, so this is needed
     * in order to handle this case.
     */
    private static void trimLeadingSpaces(File file) {
        String contents = file.withReader { it.text.trim() }
        file.write(contents)
    }

    static Map getCurrRelease(File plist) {
        Map release = parseReleasesFile(plist)

        if (release[RELEASE_NAME_KEY] == null || release[RELEASE_VERSION_KEY] == null) {
            throw new Exception("There was an error: '${plist.path}' does contains invalid keys.")
        }

        release
    }

    static void updateRelease(File plist, String name, String version) {
        Map currRelease = getCurrRelease(plist)
        currRelease[RELEASE_NAME_KEY] = name
        currRelease[RELEASE_VERSION_KEY] = version

        String newPList = Plist.toXml(currRelease)

        plist.write(newPList)
    }

    /**
     * Get the previous release (name, version).
     * @param name
     * @param version
     */
    static Map getPreviousRecord(File csv, File plist) {
        CSVRecord prev = null
        Map currRelease = getCurrRelease(plist)
        String name = currRelease[RELEASE_NAME_KEY]
        String version = currRelease[RELEASE_VERSION_KEY]
        CSVParser parser = CSVParser.parse(
                csv,
                Charset.forName("UTF-8"),
                CSVFormat.DEFAULT.withHeader()
        )

        for (CSVRecord curr : parser) {
            if (prev != null && curr.get(COLUMN_NAME_0) == name && curr.get(COLUMN_NAME_1) == version) {
                return [
                        curr_name: name,
                        curr_version: version,
                        prev_name: prev.get(COLUMN_NAME_0),
                        prev_version: prev.get(COLUMN_NAME_1)
                ]
            }
            prev = curr
        }

        return null
    }

    /**
     * Get the next release (name, version) from a given (name, version).
     * @param name
     * @param version
     */
    static Map getNextRecord(File csv, File plist) {
        CSVRecord prev = null
        Map currRelease = getCurrRelease(plist)
        String name = currRelease[RELEASE_NAME_KEY]
        String version = currRelease[RELEASE_VERSION_KEY]
        CSVParser parser = CSVParser.parse(
                csv,
                Charset.forName("UTF-8"),
                CSVFormat.DEFAULT.withHeader()
        )

        for (CSVRecord next : parser) {
            if (prev != null && prev.get(COLUMN_NAME_0) == name && prev.get(COLUMN_NAME_1) == version) {
                return [
                        curr_name: name,
                        curr_version: version,
                        next_name: next.get(COLUMN_NAME_0),
                        next_version: next.get(COLUMN_NAME_1)
                ]
            }
            prev = next
        }

        return null
    }
}
