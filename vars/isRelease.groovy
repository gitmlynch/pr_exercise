def call(Map args) {

    def isRelease = isReleaseBranch()

    if (env.BRANCH_NAME == 'master') {

        def isMaster = true

    } else {

        def isMaster = false
    }

    if (isRelease == true) {

        if (isMaster == true) {

            return true

        } else {

            return false
        }

    } else {

        return false
    }

}
