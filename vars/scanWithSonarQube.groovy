// def call(Map params) {
//     if (!params.containsKey('envVariable') || !params.envVariable) {
//         error "'envVariable' parameter is required and should contain repository URLs separated by commas."
//     }

//     if (!params.containsKey('sonarServer') || !params.sonarServer) {
//         error "'sonarServer' parameter is required and should specify the SonarQube server name configured in Jenkins."
//     }

//     def repositories = params.envVariable.split(',').collect { it.trim() }

//     stage("SonarQube Analysis") {
//         repositories.each { repoUrl ->
//             if (!repoUrl) {
//                 error "Repository URL cannot be empty."
//             }

//             def repoName = repoUrl.split('/').last().replace('.git', '')
//             def sonarProjectKey = params.get('sonarProjectKeyPrefix', 'project-') + repoName

//             dir("workspace/${repoName}") {
//                 // Assuming the code is already cloned into workspace/<repoName>
//                 withSonarQubeEnv(params.sonarServer) {
//                     sh "sonar-scanner \
//                         -Dsonar.projectKey=${sonarProjectKey} \
//                         -Dsonar.sources=. \
//                         -Dsonar.host.url=\${SONAR_HOST_URL} \
//                         -Dsonar.login=\${SONAR_AUTH_TOKEN}"
//                 }
//             }
//         }
//     }
// }


def call(Map params) {
    if (!params.containsKey('envVariable') || !params.envVariable) {
        error "'envVariable' parameter is required and should contain repository URLs separated by commas."
    }

    if (!params.containsKey('credentialsId') || !params.credentialsId) {
        error "'credentialsId' parameter is required and should specify the Jenkins credentials ID for SonarQube."
    }

    def repositories = params.envVariable.split(',').collect { it.trim() }

    // stage("SonarQube Analysis") {
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
                        -Dsonar.host.url=\${SONAR_URL} \
                        -Dsonar.login=\${SONAR_AUTH_TOKEN}"
                // }
            }
        }
    }
}
