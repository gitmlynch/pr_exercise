def call(Map args) {

def branchn = env.BRANCH_NAME

    truFals ==~ /release\/\d+/

    return truFals
}
