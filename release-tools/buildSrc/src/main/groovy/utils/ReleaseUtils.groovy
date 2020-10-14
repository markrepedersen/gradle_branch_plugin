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

    static final String PLIST_FILE = "../release.plist"
    static final String CSV_FILE = "../releng/release_info.csv"
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
     * Gets the (current, previous, and next) release name and versions.
     * @param plist
     * @param csv
     */
    static Release getRelease(File plist, File csv) {
        Release res = new Release()
        Map release = parseReleasesFile(plist)

        res.name = release[RELEASE_NAME_KEY]
        res.version = release[RELEASE_VERSION_KEY]

        if (res.name == null || res.version == null) {
            throw new Exception("There was an error: '${plist.path}' does contains invalid keys.")
        }

        CSVRecord prev = null

        for (CSVRecord curr : parseCsv(csv)) {
            if (prev != null && curr.get(COLUMN_NAME_0) == res.name && curr.get(COLUMN_NAME_1) == res.version) {
                res.prevName = prev.get(COLUMN_NAME_0)
                res.prevVersion = prev.get(COLUMN_NAME_1)
            }
            if (prev != null && prev.get(COLUMN_NAME_0) == res.name && prev.get(COLUMN_NAME_1) == res.version) {
                res.nextName = curr.get(COLUMN_NAME_0)
                res.nextVersion = curr.get(COLUMN_NAME_1)
            }
            prev = curr
        }

        res
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
     * Trims any leading whitespace/newline characters in the given file.
     * NOTE: XML parsing is Java doesn't work when there are leading newline characters/spaces, so this is needed
     * in order to handle this case.
     */
    private static void trimLeadingSpaces(File file) {
        String contents = file.withReader { it.text.trim() }
        file.write(contents)
    }

    private static CSVParser parseCsv(File csv) {
        CSVParser.parse(
                csv,
                Charset.forName("UTF-8"),
                CSVFormat.DEFAULT.withHeader()
        )
    }
}
