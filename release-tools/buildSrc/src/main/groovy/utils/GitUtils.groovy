package utils

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.GradleException

import javax.net.ssl.HttpsURLConnection

class GitUtils {
    static final String GITHUB_USERNAME_KEY = "github_username"
    static final String GITHUB_TOKEN_KEY = "github_token"
    private static final String API_BASE_URL = "https://api.github.com"
    private static final String OWNER = "SlackRecruiting"
    private static final String REPO = "br-code-exercise-170536289"

    /**
     * Creates a branch named {@param newBranch} from the revision at the head of {@param fromBranch}.
     * If {@param newBranch} has no ref or does not exist, then an exception will be thrown.
     * @param fromBranch
     * @param newBranch
     * @param username
     * @param token
     */
    static void createBranch(String fromBranch, String newBranch, String username, String token) throws Exception {
        String url = "$API_BASE_URL/repos/$OWNER/$REPO/git/refs"
        String hash = getRef(fromBranch, username, token)

        if (hash) {
            try {
                println("Attempting to create branch with name '$newBranch' from '$fromBranch'.")
                sendRequest(url, "POST", [
                        username: username,
                        token   : token,
                        headers : ["Content-Type": "application/json"],
                        body    : [ref: "refs/heads/$newBranch", sha: hash]
                ])

                println("Branch '$newBranch' was successfully created.")
            } catch (Exception e) {
                throw new GradleException("There was a problem while sending request to GitHub due to error.", e)
            }
        } else throw new GradleException("Unable to fetch a ref for this repository.")
    }

    static String getRef(String branch, String username, String token) {
        String url = "$API_BASE_URL/repos/$OWNER/$REPO/git/refs/heads/$branch"

        try {
            println("Attempting to retrieve ref from branch '$branch'.")
            HttpsURLConnection conn = sendRequest(url, "GET", [username: username, token: token])
            String json = conn.inputStream.text

            JsonSlurper slurper = new JsonSlurper()
            String hash = slurper.parseText(json)?.object?.sha

            println("Successfully retrieved ref: '$hash'.")
            hash
        } catch (Exception e) {
            throw new GradleException("There was a problem getting the ref.", e)
        }
    }

    /**
     * Checks that the remote branch actually exists.
     */
    static boolean hasRemoteBranch(String branch, String username, String token) {
        String url = "$API_BASE_URL/repos/$OWNER/$REPO/git/ref/heads/$branch"

        try {
            sendRequest(
                    url,
                    "GET",
                    [
                            username: username,
                            token   : token,
                            headers : ["Accept": "application/vnd.github.v3.raw"]
                    ]
            )

            true
        } catch (Exception e) {
            System.err.println("No remote branch found due to invalid response: $e")
            false
        }
    }

    /**
     * Retrieves the contents of the file for a given branch.
     * @param options
     * @throws Exception
     */
    static String getContents(String path, String branch, String username, String token) {
        String url = "$API_BASE_URL/repos/$OWNER/$REPO/contents/$path?ref=$branch"

        try {
            println("Attempting to retrieve file contents for '$path' from branch '$branch'.")
            HttpsURLConnection conn = sendRequest(
                    url,
                    "GET",
                    [
                            username: username,
                            token   : token,
                            headers : ["Accept": "application/vnd.github.v3.raw"]
                    ]
            )
            println("Successfully retrieved '$path' from branch '$branch'.")

            conn.inputStream.text
        } catch (IOException e) {
            System.err.println("Feature Flag contents from previous release could not be found: ${e.toString()}")
            return null
        }
    }

    /**
     * Sends a GitHub API request with the given <path, method, and additional parameters>.
     *
     * The following additional parameters are accepted:
     * - username
     * - token (username must be specified with this one)
     * - headers (a map of header (key, val) pairs)
     * - body
     *
     * @param url
     * @param method
     * @param options
     */
    private static HttpsURLConnection sendRequest(String url, String method, Map options = [:]) {
        HttpsURLConnection conn = (HttpsURLConnection) url
                .toURL()
                .openConnection()

        conn.setRequestMethod(method)
        conn.setDoOutput(true)

        if (options.username && options.token) {
            String auth = "${options.username}:${options.token}".bytes.encodeBase64()
            conn.setRequestProperty("Authorization", "Basic $auth")
        }

        if (options.headers && options.headers instanceof Map) {
            (options.headers as Map).each {
                conn.setRequestProperty(it.key as String, it.value as String)
            }
        }

        if (options.body) {
            conn.getOutputStream().with {
                byte[] input = JsonOutput.toJson(options.body).getBytes("UTF-8")
                it.write(input, 0, input.length)
            }
        }

        if (conn.responseCode < 200 || conn.responseCode > 299) {
            throw new Exception("Request returned with code ${conn.responseCode}: ${conn.responseMessage}.")
        }

        println("Request returned successfully with response code ${conn.responseCode} and message '${conn.responseMessage}'.")

        conn
    }
}
