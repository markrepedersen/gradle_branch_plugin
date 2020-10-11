package utils

import javax.net.ssl.HttpsURLConnection

class GitUtils {
    static final String GITHUB_USERNAME_KEY = "github_username"
    static final String GITHUB_TOKEN_KEY = "github_token"

    private static final String API_BASE_URL = "https://api.github.com"
    private static final String OWNER = "SlackRecruiting"
    private static final String REPO = "br-code-exercise-170536289"

    static void createBranch() {

    }

    /**
     * Retrieves the contents of the file for a given branch.
     * @param options
     * @throws Exception
     */
    static String getContents(String path, String branch, String username, String token) throws Exception {
        //String url = "$API_BASE_URL/repos/$OWNER/$REPO/contents/$path?ref=$branch"
        String url = "$API_BASE_URL/repos/$OWNER/$REPO/contents/$path"

        try {
            return sendRequest(
                    url,
                    "GET",
                    [
                            username: username,
                            token   : token,
                            headers : ["Accept": "application/vnd.github.v3.raw"]
                    ]
            )
        } catch (IOException e) {
            System.err.println("Feature Flag contents from previous release could not be found: ${e.toString()}")
            return null
        }
    }

    private static String sendRequest(String url, String method, Map options = [:]) {
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

        conn.inputStream.text
    }
}
