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

    static Map parseReleasesFile(File file) {
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

    static Release getCurrRelease(File plist) {
        Map release = parseReleasesFile(plist)

        if (release[RELEASE_NAME_KEY] == null || release[RELEASE_VERSION_KEY] == null) {
            throw new Exception("There was an error: '${plist.path}' does contains invalid keys.")
        }

        new Release(
                name: release[RELEASE_NAME_KEY],
                version: release[RELEASE_VERSION_KEY],
        )
    }

    static void updateRelease(File plist, String name, String version) {
        Map release = parseReleasesFile(plist)
        String oldName = release[RELEASE_NAME_KEY]
        String oldVersion = release[RELEASE_VERSION_KEY]

        println("Attempting to update release <name/version> from <$oldName/$oldVersion> to <$name/$version>")

        release[RELEASE_NAME_KEY] = name
        release[RELEASE_VERSION_KEY] = version

        String newPList = Plist.toXml(release)

        plist.write(newPList)
        println("Successfully updated release <name/version> from <$oldName/$oldVersion> to <$name/$version>")
    }

    /**
     * Get the previous release (name, version).
     * @param name
     * @param version
     */
    static Release getPreviousRecord(File csv, File plist) {
        CSVRecord prev = null
        Release currRelease = getCurrRelease(plist)
        CSVParser parser = CSVParser.parse(
                csv,
                Charset.forName("UTF-8"),
                CSVFormat.DEFAULT.withHeader()
        )

        for (CSVRecord curr : parser) {
            if (prev != null && curr.get(COLUMN_NAME_0) == currRelease.name && curr.get(COLUMN_NAME_1) == currRelease.version) {
                return new Release(
                        name: currRelease.name,
                        version: currRelease.version,
                        prevName: prev.get(COLUMN_NAME_0),
                        prevVersion: prev.get(COLUMN_NAME_1)
                )
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
    static Release getNextRecord(File csv, File plist) {
        CSVRecord prev = null
        Release currRelease = getCurrRelease(plist)
        CSVParser parser = CSVParser.parse(
                csv,
                Charset.forName("UTF-8"),
                CSVFormat.DEFAULT.withHeader()
        )

        for (CSVRecord next : parser) {
            if (prev != null && prev.get(COLUMN_NAME_0) == currRelease.name && prev.get(COLUMN_NAME_1) == currRelease.version) {
                return new Release(
                        name: currRelease.name,
                        version: currRelease.version,
                        nextName: next.get(COLUMN_NAME_0),
                        nextVersion: next.get(COLUMN_NAME_1)
                )
            }
            prev = next
        }

        return null
    }
}
