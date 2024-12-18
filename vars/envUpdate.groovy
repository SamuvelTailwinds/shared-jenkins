def call(Map params) {
    if (!params.containsKey('commands') || !(params.commands instanceof List)) {
        error "'commands' parameter is required and should be a list of commands."
    }
    if (!params.containsKey('basePath') || !params.basePath) {
        error "'basePath' parameter is required to specify the working directory."
    }

    dir(params.basePath) {
        params.commands.each { command ->
            echo "Executing command: ${command}"
            sh command
        }
    }
}
