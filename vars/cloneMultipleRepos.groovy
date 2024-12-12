def call(Map params) {
    if (!params.containsKey('repositories') || !(params.repositories instanceof List)) {
        error "'repositories' parameter is required and should be a list of maps."
    }

    params.repositories.each { repoConfig ->
        if (!repoConfig.containsKey('repoUrl')) {
            error "Each repository configuration must have a 'repoUrl'."
        }

        String repoUrl = repoConfig.repoUrl
        String branch = repoConfig.get('branch', 'main')
        String credentialsId = repoConfig.get('credentialsId', '')
        String targetDir = repoConfig.get('targetDir', '')
        int depth = repoConfig.get('depth', 1)

        stage("Clone: ${repoUrl}") {
            checkout([
                $class: 'GitSCM',
                branches: [[name: "*/${branch}" ]],
                doGenerateSubmoduleConfigurations: false,
                extensions: [
                    [$class: 'CloneOption', depth: depth, noTags: false, shallow: depth > 0],
                    [$class: 'RelativeTargetDirectory', relativeTargetDir: targetDir]
                ],
                submoduleCfg: [],
                userRemoteConfigs: [[
                    url: repoUrl,
                    credentialsId: credentialsId
                ]]
            ])
        }
    }
}
