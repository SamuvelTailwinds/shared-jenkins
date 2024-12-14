def call(Map params = [:]) {
    def gitRepo = params.gitRepo ?: error("'gitRepo' parameter is required.")
    def releaseTag = params.releaseTag ?: error("'releaseTag' parameter is required.")
    def artifactName = params.artifactName ?: error("'artifactName' parameter is required.")
    def sourcePath = params.sourcePath ?: error("'sourcePath' parameter is required.")
    def githubTokenId = params.githubTokenId ?: error("'githubTokenId' parameter is required.")
    
    // Optional parameters
    def releaseName = params.get('releaseName', releaseTag)
    def releaseDescription = params.get('releaseDescription', "Release ${releaseTag}")

    // Validate the source path
    if (!fileExists(sourcePath)) {
        error "Source path not found: ${sourcePath}"
    }

    // GitHub API URLs
    def apiBaseUrl = "https://api.github.com/repos/${gitRepo}"
    def authHeader = "Authorization: token ${withCredentials([string(credentialsId: githubTokenId, variable: 'GITHUB_TOKEN')]) { return GITHUB_TOKEN }}"

    try {
        // Package the artifact
        sh """
            mkdir -p artifacts
            zip -r artifacts/${artifactName} ${sourcePath}
        """
        def artifactPath = "artifacts/${artifactName}"
        if (!fileExists(artifactPath)) {
            error "Artifact creation failed: ${artifactPath}"
        }

        // Check if the release exists
        def releaseInfo = sh(
            script: """
                curl -s -H "${authHeader}" \
                "${apiBaseUrl}/releases/tags/${releaseTag}"
            """,
            returnStdout: true
        ).trim()

        def releaseId
        if (releaseInfo && releaseInfo.contains('id')) {
            echo "Release ${releaseTag} exists. Updating it."
            releaseId = readJSON(text: releaseInfo).id
        } else {
            echo "Creating a new release ${releaseTag}."
            // Create the release
            def createReleaseResponse = sh(
                script: """
                    curl -s -H "${authHeader}" \
                    -d '{
                        "tag_name": "${releaseTag}",
                        "name": "${releaseName}",
                        "body": "${releaseDescription}",
                        "draft": false,
                        "prerelease": false
                    }' \
                    "${apiBaseUrl}/releases"
                """,
                returnStdout: true
            ).trim()

            releaseId = readJSON(text: createReleaseResponse).id
        }

        // Upload the artifact
        sh """
            curl -X POST -H "${authHeader}" \
            -H "Content-Type: application/zip" \
            --data-binary @${artifactPath} \
            "${apiBaseUrl}/releases/${releaseId}/assets?name=${artifactName}"
        """

        echo "Artifact ${artifactName} uploaded successfully to release ${releaseTag}."
    } catch (Exception e) {
        error "Failed to create or update the release: ${e.message}"
    }
}
