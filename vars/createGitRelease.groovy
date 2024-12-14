def call(Map params = [:]) {
    def gitRepo = params.gitRepo ?: error("'gitRepo' parameter is required.")
    def releaseTag = params.releaseTag ?: error("'releaseTag' parameter is required.") // for md
    def extraTag = params.extraTag ?: error("'extraTag' parameter is required.") // for iff 
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
    def authHeader = "Authorization: token ${withCredentials([usernamePassword(credentialsId: githubTokenId, usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) { return GIT_PASSWORD }}"
    // def authHeader
    // withCredentials([usernamePassword(credentialsId: githubTokenId, usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
    //     def authString = "${GIT_USERNAME}:${GIT_PASSWORD}"
    //     authHeader = "Authorization: Basic " + authString.bytes.encodeBase64().toString()
    // }
    // echo "Auth header: ${authHeader}"

    try {
        // Package the artifact with only specific files (docker-compose and .env)
        sh """
            mkdir -p artifacts
            cp ${sourcePath}/docker-compose.yaml ${sourcePath}/.env artifacts/
            cd artifacts
            chmod 644 docker-compose.yaml .env
            # Replace multiple variables
            sed -i -e 's|IMAGE_TAG=.*|IMAGE_TAG=${releaseTag}|' \
                .env

            zip ${releaseTag}-${artifactName} docker-compose.yaml .env

            sed -i -e 's|IMAGE_TAG=.*|IMAGE_TAG=${extraTag}|' \
                .env
            zip ${extraTag}-${artifactName} docker-compose.yaml .env

        """

        def artifactPath = "artifacts/${releaseTag}-${artifactName}"
        if (!fileExists(artifactPath)) {
            error "Artifact creation failed: ${artifactPath}"
        }
        def artifactPath2 = "artifacts/${extraTag}-${artifactName}"
        if (!fileExists(artifactPath2)) {
            error "Artifact creation failed: ${artifactPath2}"
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

        // Upload the artifact for md
        sh """
            curl -X POST -H "${authHeader}" \
            -H "Content-Type: application/zip" \
            --data-binary @${artifactPath} \
            "https://uploads.github.com/repos/${gitRepo}/releases/${releaseId}/assets?name=${releaseTag}-${artifactName}"
        """
        // Upload the artifact for iff
        sh """
            curl -X POST -H "${authHeader}" \
            -H "Content-Type: application/zip" \
            --data-binary @${artifactPath2} \
            "https://uploads.github.com/repos/${gitRepo}/releases/${releaseId}/assets?name=${extraTag}-${artifactName}"
        """

        echo "Artifact ${artifactName} uploaded successfully to release ${releaseTag}."
    } catch (Exception e) {
        error "Failed to create or update the release: ${e.message}"
    }
}
