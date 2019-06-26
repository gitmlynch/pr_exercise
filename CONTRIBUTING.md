
#### Table of Contents

* [Submitting Pull Requests](#submitting-pull-requests)
    * [Responding to Comments](#responding-to-comments)
    * [Quote Reply to Comments](#quote-reply-to-comments)
    * [Resolving Conversations](#resolving-conversations)
    * [Target Branch](#target-branch)
* [Code Formatting](#code-formatting)
    * [General](#general)
        * [Names](#names)
        * [Whitespace](#whitespace)
        * [Typing](#typing)
        * [Return Statements](#return-statements)
    * [Private Functions](#private-functions)
* [Writing Tests](#writing-tests)
    * [Test Files And Classes](#test-files-and-classes)
    * [Test Data](#test-data)
    * [Tests Methods](#test-methods)
        * [Mocking](#mocking)
            * [Mocking Functions](#mocking-functions)
            * [Mocking Exceptions](#mocking-exceptions)
        * [Assertions](#assertions)
            * [Interactions](#interactions)
            * [Value Based Assertions](#value-based-assertions)
        * [Data Driven Tests](#data-driven-tests)

## Submitting Pull Requests

Anyone with GitHub access may submit a pull request. 

Please adhere to the following guidelines when submitting Pull Requests (PRs) to the repository.

* Smaller changesets are preferred over larger changesets.
    * Generally, the larger the changeset, the (exponentially) longer the turnaround time.
* Always include a detailed description of the changeset with the PR.
* Always keep your branches up to date with the target mainline (`master` or `release/N`).
* Include test changes with changes to main source code (do not file them as separate PRs)

### Responding to Comments

#### Quote Reply to Comments

In GitHub, when a comment is made in the Conversation of a PR without being associated with a specific line of code, there is no "Reply" field for responding directly to that comment. In such cases, the "Quote Reply" feature should be used to associate responses with the relevant comment. That feature can be accessed by clicking on the ellipsis in the upper right, of the comment in question, to reveal a menu of available actions.

### Resolving Conversations

GitHub has the capability of folding conversations when they have been resolved by clicking the `Resolve Conversation` button.

It is the responsibility of the person who initiates a conversation on a PR to resolve said conversation when they feel it has been resolved to their satisfaction. Conversely, to be courteous to the original commenter, no one but the original commenter should resolve a conversation without the original commenter's consent.

### Target Branch

Pull requests may target any branch. However, the code owners will only commit to looking at pull requests which target `master` or `release/*`.


## [Library Structure](https://jenkins.io/doc/book/pipeline/shared-libraries/#directory-structure)
* All files must generate step behavior:  https://jenkins.io/doc/book/pipeline/shared-libraries/#defining-custom-steps
    * As such any new file that adds functionality to the library must be placed in the vars folder
    * Custom steps must follow requirements described in the [Steps](#steps) section of this document.
* All steps must have corresponding test step files in test/groovy/tests.
    * Test files must be named according to the following pattern. (Note use of TitleCase rather than camelCase):
          `Test[StepName].groovy`

## Code Formatting

### General

* Do not use semicolons at the end of statements. In groovy newlines are sufficient.
* Do not use parenthesis in a method call where the method being called is a custom [step](#steps).

#### Names

* Use camel case for variable and function names: https://en.wikipedia.org/wiki/Camel_case
* Name "private" functions within a step beginning with underscore, e.g. `_myPrivateFunction`
* Name functions and variables descriptively. Favor clarity over terseness, e.g.

    **Good**
    ```groovy
    def _getCustomPipelineDefinitionFilePath() { 

        // code here
    }
    ```

    **Bad**
    ```groovy
    def _getCustPipeLDefPth() {

        // code here
    }
    ```

#### Whitespace

* Use spaces for indenting code.
    * :confetti_ball:**\*\*Not tabs. Never tabs!\*\***:confetti_ball:
    * Each indent must be 4 spaces.
* 1 space before `{` anywhere it is used.
* 1 space before `(` and after `)` in control, and control-like structures.
    * From a coding style point of view, we are treating traditional groovy control structures and pipeline control structures in the same fashion, e.g. `if`, `for`, `timeout`

    **Good**
    ```groovy
    if (x > 1) {

        timeout (time: 5, unit: 'MINUTES') {

            // do stuff
        }
    }
    ```

    **Bad**
    ```groovy
    if(x > 1){

        timeout(time: 5, unit: 'MINUTES'){

            // do stuff
        }
    }
    ```

* 1 space separating binary/ternary operators and operands, e.g. `x = y`, `a + b`, `c || d`
* 1 space following the comma separating elements in a list, e.g. `def shortList = ['item1', 'item2', 'item3']`
* 1 space following a colon (`:`) in a Map, e.g `def lilMap = [key1: 'val1', key2: 'val2']`
* 0 spaces between enclosing brackets and items in a list or Map:

    **Good**
    ```groovy
    def listFull  = ['a', 'b', 'c']
    def listEmpty = []

    def mapFull  = [a: 1, b: 2, c: 3]
    def mapEmpty = [:]
    ```

    **Bad**
    ```groovy
    def listFull  = [ 'a', 'b', 'c' ]
    def listEmpty = [ ]

    def mapFull  = [ a: 1, b: 2, c: 3 ]
    def mapEmpty = [ : ]
    ```

* Separate lines of code with empty lines:

    **Good**
    ```groovy
    sh 'docker pull'

    sh 'docker build -t foo .'
    ```

    **Bad**
    ```groovy
    sh 'docker pull'
    sh 'docker build -t foo .'
    ```

  This rule has the following exceptions:

    * Assignments:

        ```groovy
        def place   = getPlace()
        def time    = getTime()
        def persona = getPersona()

        def details = [
            'PLACE': place,
            'TIME': time,
            'PERSONA': persona
        ]
        ```

    * Method calls which span multiple lines:

        ```groovy
        def value = callSomeMethod([
            paramName0: paramValue0,
            paramName1: paramValue1
        ])
        ```

    * Comments:

        ```groovy
        // I would like to take this opportunity to thank you for reading through the 
        // Contributing documentation.
        thanks()

        weReallyAppreciateIt()
        ```

    * Literals:

        ```groovy
        def haiku = '''\
        it was built
        to streamline development
        how are we doing?
        '''
        ```

* Align assignment operators when grouping statements, e.g.

    **Good**
    ```groovy
    def var       = 'some value'
    def biggerVar = 'some other value'
    def superVar  = "concatenate ${var1} and ${var2}"
    ```

    **Bad**
    ```groovy
    def var = 'some value'
    def biggerVar = 'some other value'
    def superVar = "concatenate ${var1} and ${var2}"
    ```

* Write complex objects in a multi-line form.

    * Do not group assignment of complex objects with other assignments.

    **Good**
    ```groovy
    def var               = 'some value'
    def simpleSmallMapVar = [key1: 'val1', key2: 'val2']

    def complexMapVar = [
        key1: [
            subKey1: 'subVal1'
            subKey2: 'subVal2'
        ],
        key2: 'val2'
    ]
    ```

    **Bad**
    ```groovy
    def var           = 'some value'
    def simpleMapVar  = [key1: 'val1', key2: 'val2']
    def complexMapVar = [key1: [subKey1: 'subVal1', subKey2: 'subVal2'], key2: 'val2']
    ```

* Do not precede `}` with empty line if no code immediately follows, e.g.

    **Good**
    ```groovy
    def _runTheThing() {

        if (somethingExists) {

            try {

                _runThing()

            } catch {

                _handleError()
            }
        }
    }
    ```

    **Bad**
    ```groovy
    def _runTheThing() {

        if (somethingExists) {

            try {

                _runThing()

            } catch {

                _handleError()

            }

        }

    }
    ```

#### Typing

* Strong typing is discouraged unless absolutely needed. All variables should be declared using `def`.

    **Good**
    ```groovy
    def defTypeVariable = ''
    ```

    **Bad**
    ```groovy
    String stringTypeVariable = ''
    ```

#### Return Statements
* Generally, return statements should be simple and short.
  * Return statements may include simple computation, e.g. `return "${param1}/${param2}"`
  * Return statements may include complex closures.
* In all but exceptional circumstances, there should be only 1 return statement in a function.

### Private Functions
* Unlike Java, Groovy has no concept of private functions or methods.
* By convention we define "private" functions within steps by prepending the function name with an underscore, e.g. `def _mySortOfPrivateFunction() { }` and **only** call them from within the step they are defined in.
* Although "private" functions _can_ be called from outside of their step, they **must not** be.
    * Any need to use a function in multiple steps means that it should be made into a new step itself.


## Writing Tests

* Every step must have tests for every function.
* We use the Spock test framework: http://spockframework.org/spock/docs/1.2/index.html
* We do not do "strict mocking": http://spockframework.org/spock/docs/1.2/interaction_based_testing.html#_default_behavior_of_mock_objects
* For the most part, test code style should adhere to all of the same rules as the step code.

### Test Files And Classes

* Test Class names must correspond to their file name and adhere to the following pattern: 'Test[StepName]'
    * Note the use of TitleCase instead of camelCase for test class names.
* Test Classes must:
    * Import and extend `PipelineStepSpecification`
    * Import the script class under test (the class under test will not be in a package)
    * Include a `setup()` method which calls `createAndAssignStepInstance`, passing in the `Class` of the script class under test
* Repeated literals _may_ be declared as `final static` (or `static final`) members of the test class or base class. 

    ```groovy
    import PipelineStepSpecification

    // script class under test
    import getTurtle

    class TestGetTurtle extends PipelineStepSpecification {

        def setup() {

            // this will create an instance of the `getTurtle` class and assign it to `this.script`
            createAndAssignStepInstance(getTurtle.class)
        }

        // test methods go here
        // see specific guidance for writing tests in the Tests section of this document
    }
    ```

### Test Data

* Test variables for which the value and type are irrelevant (e.g. variables used only in interactions and/or return statements) should be assigned using `generateUniqueValue()`.

    ```groovy
    // method under test
    def _getStuff(thing) { // thing will be passed around but its value will not be dereferenced

        def stuff = _getStuffFromThing(thing) // stuff will be returned but its value will not be dereferenced

        return stuff
    }

    // test method
    def 'getStuff gets stuff from the given thing'() {

        given:
        def thing = generateUniqueValue()
        def stuff = generateUniqueValue()

        def mockGetStuffFromThing = create1ArgMock('_getStuffFromThing')

        when:
        def actual = script._getStuff(thing)

        then:
        1 * mockGetStuffFromThing(thing) >> stuff

        and:
        actual == stuff
    }
    ```

* Test variables for which the value is irrelevant, but the type matters, should be assigned using type specific `generate...` functions, e.g. a var that will be interpolated should be assigned using `generateString()`.

    ```groovy
    // method under test
    def getBaseImageNameNode() {

        return "${getDockerInfrastructureRepositoryPrefix()}/docker-base-node:latest-0"
    }

    // test method
    def 'getBaseImageNameNode script returns node base image'() {

        given:
        def prefix = generateString()

        def mockGetDockerInfrastructureRepositoryPrefix = createGetDockerInfrastructureRepositoryPrefixMock()

        when:
        def actual = script.call()

        then:
        1 * mockGetDockerInfrastructureRepositoryPrefix() >> prefix

        and:
        actual == "${prefix}/docker-base-node:latest-0"
    }
    ```

### Test Methods

* In general, test methods should adhere to the following pattern:

    ```groovy
    def 'detailed spec description sentence'() {

        given:
        // define exception FIRST if throwing one in an interaction
        // def exception = generateException()

        // define local test vars here, e.g. def expectedResult = 'some value'
        def applicationDefinition = generateApplicationDefinition()
         
        def something = generateUniqueValue()

        // create mock functions needed here
        // see guidance in the Mocking sections of this document for creating mocks

        when:
        def actual = script._privateFunction(applicationDefinition, something)

        then:
        // add interaction-based assertions here
        // see guidance in the Assertions section of this document for assertion patterns

        // use the following pattern for asserting on return values
        and:
        actual == something

        // use the following pattern for asserting on exception cases
        and:
        Exception actual = thrown()
        actual == exception
    }
    ```

#### Mocking

Mocking is fundamental to the [Spock testing framework](http://spockframework.org/spock/docs/1.2/index.html). In Spock terminology, most of the time, our System Under Specification (SUS) is a function within a step. As we take an interaction-oriented approach to groovy code testing, most steps and functions that are called by a SUS should be mocked. We have several patterns for doing this in a consistent fashion.

##### Mocking Functions

* To mock a function within the step under test, do the following:

    ```groovy
    // function to be mocked
    def _privateFunction(arg1, arg2) { } // function of two arguments
    
    // 'given' section of test
    def mockPrivateFunction = create2ArgMock('_privateFunction') // create2ArgMock creates a mock which takes 2 arguments
    ```

    * For a function with X arguments, use the `createXArgMock()` creator function.
    * If the same mock is needed by multiple test cases, then add a creator function to the top of the test class, e.g.

        ```groovy
        class TestThing extends PipelineStepSpecification {

            def setup() {
            
                createAndAssignStepInstance(thing.class)
            }

            def createPrivateFunctionMock() {

                return create2ArgMock('_privateFunction')
            }

            def 'some test'() {

                given:
                ...
                def mockPrivateFunction = createPrivateFunctionMock()
                ...
            }

            def 'some other test'() {

                given:
                ...
                def mockPrivateFunction = createPrivateFunctionMock()
                ...
            }
        ```


##### Mocking Exceptions

* When mocking an exception it should adhere to the following pattern:

    * Note the absence of `->` in the `{ throw exception }` closure - an edge case when writing a closure.

    ```groovy
    def '_assertThingExists calls error and bubbles up exception when _doesThingExist returns false'() {

        given:
        def exception = generateException()
        
        def mockDoesThingExist = create0ArgMock('_doesThingExist')
        def mockError          = createErrorMock()

        when:
        script._assertThingExists()

        then:
        1 * mockDoesThingExist() >> false

        then:
        1 * mockError('Expected thing to exist, but it doesn't!') >> { throw exception }

        and: // convention is to use `and` rather than `then` for exception cases
        Exception actual = thrown()
        actual == exception
    }
    ```

    ```groovy
    def '_doThings bubbles up exception when _doFirstThing errors'() {

        given:
        def exception = generateException()

        def mockDoFirstThing = create0ArgMock('_doFirstThing')

        when:
        script._doThings()

        then:
        1 * mockDoFirstThing() >> { throw exception }

        and: // convention is to use `and` rather than `then` for exception cases
        Exception actual = thrown()
        actual == exception
    }
    ```

#### Assertions

##### Interactions

* Most of our assertions are interaction-oriented and conform to the following pattern:

    ```groovy
    n * mockSomething([optionalArgsList]) >> optionalResultOfMock
    ```

    ... e.g.

    ```groovy
    1 * mockSomeFunction(paramA, paramB) >> valueUsedInSubsequentAssertion
    ```

##### Value Based Assertions
* In some cases, it makes sense to assert on the result of the call to the SUS. In that case, the last assertion should test that 2 values (or objects) bare the correct equivalence relationship.
* If the "expected" value is used more than once in the test spec, it should be assigned to a variable. Otherwise, a literal value should be asserted.

    ```groovy
    given:
    def expected = generateUniqueValue()

    def mockSomeStep = createSomeStepMock()

    when:
    def actual = script._somePrivateFunction()

    then:
    1 * mockSomeStep() >> expected

    and: // convention is to use `and` rather than `then` for return values
    actual == expected
    ```

#### Data Driven Tests
* Data driven tests involve the use of a `where:` clause in the specification: http://spockframework.org/spock/docs/1.2/data_driven_testing.html
* Fundamentally they allow a specification to be written once, but used to execute multiple similar tests. 
* The `where:` clause contains "data tables" or "data pipes", and comes after all `then:` clauses.
    * The column names of a data table are equivalent to the var names defined in data pipes, enabling their use in other parts of the specification.
        * Data pipes are suitable for small data sets or data sets in which computation needs to the take place.
        * Data tables are preferred for larger, more complex data sets.

    **Good**
    ```groovy
    where:
    isMaster | isPr  | isMasterOrPr
    false    | false | false
    true     | false | true
    false    | true  | true
    true     | true  | true
    ```

    **Good**
    ```groovy
    hookFileExists << [true, false]
    interactions = hookFileExists ? 1    : 0
    result       = hookFileExists ? hook : null
    ```

    **Bad**
    ```groovy
    isMaster     = [false, true, false, true]
    isPr         = [false, false, true, true]
    isMasterOrPr = [false, true, true, true]
    ```

* Where appropriate, 1 or more of the data table/pipe variable names should be used in the spec name along with the `@Unroll` decorator to distinguish each iteration of the test spec.
    * When `@Unroll` is used in a Test file, it must be imported at the top of the file, e.g.

        ```groovy
        import spock.lang.Unroll

        import PipelineStepSpecification

        class TestGetTurtle extends PipelineStepSpecification {

            @Unroll
            def 'spec under test verifies that its turtles #position'() {

                when:
                script.call(position)

                then:
                // assertions here

                where:
                position << ['first', 'last']
            }
        }
        ```
