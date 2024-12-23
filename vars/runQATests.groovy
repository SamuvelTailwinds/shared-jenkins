def call(Map params = [:]) {
    def seleniumHost = params.seleniumHost
    def seleniumUser = params.seleniumUser
    def seleniumPassword = params.seleniumPassword
    def gitRepoURL = params.gitRepoURL
    def gitCredentialsId = params.gitCredentialsId
    def branch      = .params.gitBranch
    def selectedFiles = params.selectedFiles ?: ''

    if (!seleniumHost || !seleniumUser || !seleniumPassword || !gitRepoURL || !gitCredentialsId) {
        error "Missing required parameters: seleniumHost, seleniumUser, seleniumPassword, gitRepoURL, gitCredentialsId"
    }

    def scripts = selectedFiles.split(',')

    catchError(buildResult: 'SUCCESS', stageResult: 'SUCCESS') {

        if (scripts.size() > 0 && scripts[0].trim()) {
            def remote = [:]
            remote.name = 'windowssh'
            remote.host = seleniumHost
            remote.user = seleniumUser
            remote.password = seleniumPassword
            remote.allowAnyHosts = true

            sshCommand remote: remote, command: """
                git -c credential.helper='!echo username=${GIT_USERNAME}; echo password=${GIT_PASSWORD}' clone --branch ${branch} ${gitRepoURL}
            """

            scripts.each { scriptName ->
                sshCommand remote: remote, command: "pytest -v --html=report.html navigator-selenium/automation-testing/tests/${scriptName.trim()}"
            }
        } else {
            echo 'No QA testing files selected, skipping QA testing stage'
        }
    }
}
