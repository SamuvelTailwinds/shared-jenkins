def call(Map params) {
    if (!params.containsKey('envVariable') || !params.envVariable) {
        error "'envVariable' parameter is required and should contain repository URLs separated by commas."
    }

    def repositories = params.envVariable.split(',').collect { it.trim() }

    repositories.each { repoUrl ->
        if (!repoUrl) {
            error "Repository URL cannot be empty."
        }

        // Extract repository name from the URL
        String repoName = repoUrl.split('/').last().replace('.git', '')

        String branch = params.get('branch', 'main')
        String credentialsId = params.get('credentialsId', '')
        int depth = params.get('depth', 1)

        println "Checking out ${repoName} into folder named '${repoName}' on branch '${branch}'"

        try {
            // Clean workspace to avoid conflicts
            // deleteDir()

            // Perform the checkout
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
        } catch (Exception e) {
            println "Failed to checkout ${repoName}: ${e.message}"
        }
    }
}
