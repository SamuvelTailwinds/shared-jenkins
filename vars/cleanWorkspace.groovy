def call(Map params = [:]) {
    def excludePatterns = params.get('excludePatterns', ['reports']) // Default exclusion is 'reports'
    def disableDeferredWipeout = params.get('disableDeferredWipeout', false)

    // Convert string patterns into Pattern objects
    def patternsToKeep = excludePatterns.collect { pattern ->
        [pattern: "**/${pattern}/**", type: 'INCLUDE']
    }

    echo "Cleaning workspace while excluding: ${excludePatterns}"

    cleanWs(
        patterns: patternsToKeep,
        disableDeferredWipeout: disableDeferredWipeout
    )

    echo "Workspace cleanup completed."
}
