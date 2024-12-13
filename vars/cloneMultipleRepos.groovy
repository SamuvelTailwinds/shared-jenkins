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

        // Extract repository name to create a unique targetDir
        String repoName = repoUrl.split('/').last().replace('.git', '')
        String targetDir = params.get('targetDir', repoName)

        println "Checking out ${repoName} to directory: ${targetDir}"

        checkout([
            $class: 'GitSCM',
            branches: [[name: "*/${branch}" ]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [
                [$class: 'CloneOption', depth: depth, noTags: false, shallow: depth > 0],
                // [$class: 'RelativeTargetDirectory', relativeTargetDir: targetDir]
            ],
            submoduleCfg: [],
            userRemoteConfigs: [[
                url: repoUrl,
                credentialsId: credentialsId
            ]]
        ])
    }
}
