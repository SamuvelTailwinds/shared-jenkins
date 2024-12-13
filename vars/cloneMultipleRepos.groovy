def call(Map params) {
    if (!params.containsKey('envVariable') || !params.envVariable) {
        error "'envVariable' parameter is required and should contain repository URLs separated by commas."
    }

    def repositories = params.envVariable.split(',').collect { it.trim() }

    repositories.each { repoUrl ->
        if (!repoUrl) {
            error "Repository URL cannot be empty."
        }

        String branch = params.get('branch', 'main')
        String credentialsId = params.get('credentialsId', '')
        int depth = params.get('depth', 1)

        // Extract repository name from the URL
        String repoName = repoUrl.split('/').last().replace('.git', '')

        println "Checking out ${repoName} into a folder named ${repoName}"

        checkout([
            $class: 'GitSCM',
            branches: [[name: "*/${branch}" ]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [
                [$class: 'CloneOption', depth: depth, noTags: false, shallow: depth > 0],
                [$class: 'RelativeTargetDirectory', relativeTargetDir: repoName]
            ],
            submoduleCfg: [],
            userRemoteConfigs: [[
                url: repoUrl,
                credentialsId: credentialsId
            ]]
        ])
    }
}
