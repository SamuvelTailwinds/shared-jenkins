def call(Map params) {
    if (!params.containsKey('envVariable') || !params.envVariable) {
        error "'envVariable' parameter is required and should contain repository URLs separated by commas."
    }

    if (!params.containsKey('credentialsId') || !params.credentialsId) {
        error "'credentialsId' parameter is required and should specify the Jenkins credentials ID for SonarQube."
    }

    if (!params.sonarHostUrl('credentialsId') || !params.sonarHostUrl) {
        error "'sonarHostUrl' parameter is required and should specify Host Url  for SonarQube."
    }

    def repositories = params.envVariable.split(',').collect { it.trim() }

    repositories.each { repoUrl ->
        if (!repoUrl) {
            error "Repository URL cannot be empty."
        }

        def repoName = repoUrl.split('/').last().replace('.git', '')
        def sonarProjectKey = params.get('sonarProjectKeyPrefix', 'project-') + repoName

        dir("workspace/${repoName}") {
            // Assuming the code is already cloned into workspace/<repoName>
            withCredentials([usernamePassword(credentialsId: params.credentialsId, usernameVariable: 'SONAR_URL', passwordVariable: 'SONAR_AUTH_TOKEN')]) {
                sh "/downloads/sonarqube/sonar-scanner-4.8.0.2856-linux/bin/sonar-scanner \
                    -Dsonar.projectKey=${sonarProjectKey} \
                    -Dsonar.sources=. \
                    -Dsonar.host.url=${sonarHostUrl} \
                    -Dsonar.login=\${SONAR_AUTH_TOKEN}"
            }
        }
    }
}
