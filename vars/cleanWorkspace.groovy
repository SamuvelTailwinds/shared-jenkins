def call(Map params = [:]) {
    def excludePatterns = params.get('excludePatterns', [])
    def workspaceDir = params.get('workspaceDir', pwd())

    echo "Cleaning up workspace: ${workspaceDir}"

    dir(workspaceDir) {
        // Find and exclude patterns
        def excludeArgs = excludePatterns.collect { "--exclude=${it}" }.join(' ')
        def cleanupCommand = "find . -mindepth 1 -maxdepth 1 ${excludeArgs} -exec rm -rf {} +"

        echo "Executing cleanup command: ${cleanupCommand}"
        sh cleanupCommand
    }

    echo "Workspace cleanup completed."
}
