def call(Map params = [:]) {
    def patterns = params.get('excludePatterns', [])
    def disableDeferredWipeout = params.get('disableDeferredWipeout', false)
    def patternsToKeep = patterns.collect { "**/${it}/**" }

    echo "Cleaning workspace with exclusions: ${patterns}"

    cleanWs(
        patterns: patternsToKeep,
        disableDeferredWipeout: disableDeferredWipeout
    )

    echo "Workspace cleanup completed."
}
