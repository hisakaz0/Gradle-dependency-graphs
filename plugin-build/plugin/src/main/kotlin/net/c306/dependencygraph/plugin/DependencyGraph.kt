package net.c306.dependencygraph.plugin

import groovy.lang.Tuple2
import org.gradle.api.Project
import java.io.File

//import org.codehaus.groovy.runtime.DefaultGroovyMethods.collectMany
//
////class GraphDetails {
////    LinkedHashSet<Project> projects
////    LinkedHashMap<Tuple2<Project, Project>, List<String>> dependencies
////    ArrayList<Project> multiplatformProjects
////    ArrayList<Project> androidProjects
////    ArrayList<Project> javaProjects
////    ArrayList<Project> rootProjects
////
////    // Used for excluding module from graph
////    public static final SystemTestName = "system-test"
////    // Used for linking module nodes to their graphs
////    public static final RepoPath = "https://github.com/oorjalabs/todotxt-for-android/blob/main"
////
////    public static final GraphFileName = "dependency-graph.md"
////}
//
///**
// * Creates mermaid graphs for all modules in the app and places each graph within the module's folder.
// * An app-wide graph is also created and added to the project's root directory.
// *
// *
// * Derived from https://github.com/JakeWharton/SdkSearch/blob/master/gradle/projectDependencyGraph.gradle
// *
// *
// * The key differences are:
// * 1. Output is in mermaidjs format to support auto display on githib
// * 2. Graphs are also generated for every module and placed in their root directory
// * 3. Module graphs also show other modules directly dependent on that module (using dashed lines)
// * 4. API dependencies are displayed with the text "API" on the connector
// * 5. Direct dependencies are connected using a bold line
// * 6. Indirect dependencies have thin lines as connectors
// * 7. Java/Kotlin modules used a hexagon for a shape, except when they are the root module in the graph
// *    * These nodes are filled with a Pink-ish colour
// * 8. Android and multiplatform modules used a rounded shape, except when they are the root module in the graph
// *    * Android nodes are filled with a Green colour
// *    * MPP nodes are filled with an Orange-ish colour
// * 9. Provided but unsupported on Github - click navigation
// *    * Module nodes are clickable, clicking through to the graph of the respective module
// */
//task projectDependencyGraph {
//    doLast {
//        // Create graph of all dependencies
//        final graph = createGraph()
//
//        // For each module, draw its sub graph of dependencies and dependents
//        graph.projects.forEach { drawDependencies(it, graph, false, rootDir) }
//
//        // Draw the full graph of all modules
//        drawDependencies(rootProject, graph, true, rootDir)
//    }
//}
//
///**
// * Create a graph of all project modules, their types, dependencies and root projects.
// * @return An object of type GraphDetails containing all details
// */
//private GraphDetails createGraph() {
//
//    def rootProjects = []
//    def queue = [rootProject]
//
//    // Traverse the list of all subfolders starting with root project and add them to
//    // rootProjects
//    while (!queue.isEmpty()) {
//        def project = queue.remove(0)
//        if (project.name != GraphDetails.SystemTestName) {
//            rootProjects.add(project)
//        }
//        queue.addAll(project.childProjects.values())
//    }
//
//    def projects = new LinkedHashSet<Project>()
//    def dependencies = new LinkedHashMap<Tuple2<Project, Project>, List<String>>()
//    ArrayList<Project> multiplatformProjects = []
//    ArrayList<Project> androidProjects = []
//    ArrayList<Project> javaProjects = []
//
//    // Again traverse the list of all subfolders starting with the current project
//    // * Add projects to project-type lists
//    // * Add project dependencies to dependency hashmap with record for api/impl
//    // * Add projects & their dependencies to projects list
//    // * Remove any dependencies from rootProjects list
//    queue = [rootProject]
//    while (!queue.isEmpty()) {
//        def project = queue.remove(0)
//        if (project.name == GraphDetails.SystemTestName) {
//            continue
//        }
//        queue.addAll(project.childProjects.values())
//
//        if (project.plugins.hasPlugin('org.jetbrains.kotlin.multiplatform')) {
//            multiplatformProjects.add(project)
//        }
//        if (project.plugins.hasPlugin('com.android.library') || project.plugins.hasPlugin('com.android.application')) {
//            androidProjects.add(project)
//        }
//        if (project.plugins.hasPlugin('java-library') || project.plugins.hasPlugin('java') || project.plugins.hasPlugin('org.jetbrains.kotlin.jvm')) {
//            javaProjects.add(project)
//        }
//
//        project.configurations.all { config ->
//            config.dependencies
//                .withType(ProjectDependency)
//                .collect { it.dependencyProject }
//                .each { dependency ->
//                    projects.add(project)
//                    projects.add(dependency)
//                    if (project.name != GraphDetails.SystemTestName && project.path != dependency.path) {
//                        rootProjects.remove(dependency)
//                    }
//
//                    def graphKey = new Tuple2<Project, Project>(project, dependency)
//                    def traits = dependencies.computeIfAbsent(graphKey) { new ArrayList<String>() }
//
//                    if (config.name.toLowerCase().endsWith('implementation')) {
//                        traits.add('impl')
//                    } else {
//                        traits.add('api')
//                    }
//                }
//        }
//    }
//
//    // Collect leaf projects which may be denoted with a different shape or rank
//    def leafProjects = []
//    projects.forEach { Project p ->
//        def allDependencies = p.configurations
//            .collectMany { Configuration config ->
//                config.dependencies.withType(ProjectDependency)
//                    .findAll {
//                        it.dependencyProject.path != p.path
//                    }
//            }
//
//        if (allDependencies.size() == 0) {
//            leafProjects.add(p)
//        } else {
//            leafProjects.remove(p)
//        }
//    }
//
//    projects = projects.sort { it.path }
//
//    return new GraphDetails(
//        projects: projects,
//        dependencies: dependencies,
//        multiplatformProjects: multiplatformProjects,
//        androidProjects: androidProjects,
//        javaProjects: javaProjects,
//        rootProjects: rootProjects
//    )
//}

/**
 * Returns a list of all modules that are direct or indirect dependencies of the provided module
 * @param currentProjectAndDependencies the module(s) whose dependencies we need
 * @param dependencies hash map of dependencies generated by [createGraph]
 * @return List of module and all its direct & indirect dependencies
 */
private fun gatherDependencies(
    currentProjectAndDependencies: ArrayList<Project>,
    dependencies: LinkedHashMap<Tuple2<Project, Project>, List<String>>,
): ArrayList<Project> {
    var addedNew = false
    dependencies
        .map { it.key }
        .forEach {
            if (currentProjectAndDependencies.contains(it.v1) && !currentProjectAndDependencies.contains(it.v2)) {
                currentProjectAndDependencies.add(it.v2)
                addedNew = true
            }
        }
    return if (addedNew) {
        gatherDependencies(
            currentProjectAndDependencies = currentProjectAndDependencies,
            dependencies = dependencies
        )
    } else {
        currentProjectAndDependencies
    }
}

/**
 * Returns a list of all modules that depend on the given module
 * @param currentProject the module whose dependencies we need
 * @param dependencies hash map of dependencies generated by [createGraph]
 * @return List of all modules that depend on the given module
 */
private fun gatherDependents(
    currentProject: Project,
    dependencies: LinkedHashMap<Tuple2<Project, Project>, List<String>>,
): List<Project> {
    return dependencies
        .filter { (key, traits) ->
            key.v2 == currentProject
        }
        .map { (key, _) -> key.v1 }
}

/**
 * Creates a graph of dependencies for the given project and writes it to a file in the project's
 * directory.
 */
private fun drawDependencies(
    currentProject: Project,
    graphDetails: GraphDetails,
    isRootGraph: Boolean,
    rootDir: File,
) {
    val projects: LinkedHashSet<Project> = graphDetails.projects
    val dependencies: LinkedHashMap<Tuple2<Project, Project>, List<String>> =
        graphDetails.dependencies
    val multiplatformProjects: ArrayList<Project> = graphDetails.multiplatformProjects
    val androidProjects: ArrayList<Project> = graphDetails.androidProjects
    val javaProjects: ArrayList<Project> = graphDetails.javaProjects
    val rootProjects: ArrayList<Project> = graphDetails.rootProjects

    val currentProjectDependencies = gatherDependencies([currentProject], dependencies)
    val dependents = gatherDependents(currentProject, dependencies)

    var fileText = """
    ```mermaid
    %%{ init: { 'theme': 'base' } }%%
    graph LR;\n
    %% Styling for module nodes by type
    classDef rootNode stroke-width:4px;
    classDef mppNode fill:#ffd2b3;
    classDef andNode fill:#baffc9;
    classDef javaNode fill:#ffb3ba;

    %% Modules
    """.trimIndent()
    // This ensures the graph is wrapped in a box with a background, so it's consistently visible
    // when rendered in dark mode.
    fileText += """
    subgraph
      direction LR

    """.trimIndent()

    val normalNodeStart = "(["
    val normalNodeEnd = "])"
    val rootNodeStart = "["
    val rootNodeEnd = "]"
    val javaNodeStart = "{{"
    val javaNodeEnd = "}}"

    var clickText = ""

    for (project in projects) {
        if (!isRootGraph && !(currentProjectDependencies.contains(project) || dependents.contains(
                project
            ))
        ) {
            continue
        }
        val isRoot = if (isRootGraph) {
            rootProjects.contains(project) || project == currentProject
        } else {
            project == currentProject
        }

        var nodeStart = if (isRoot) {
            rootNodeStart
        } else {
            normalNodeStart
        }
        var nodeEnd = if (isRoot) {
            rootNodeEnd
        } else {
            normalNodeEnd
        }

        val nodeClass = if (multiplatformProjects.contains(project)) {
            ":::mppNode"
        } else if (androidProjects.contains(project)) {
            ":::andNode"
        } else if (javaProjects.contains(project)) {
            if (!isRoot) {
                nodeStart = javaNodeStart
                nodeEnd = javaNodeEnd
            }
            ":::javaNode"
        } else {
            ""
        }

        fileText += "  ${project.path}${nodeStart}${project.path}${nodeEnd}${nodeClass};\n"

//        val relativePath = rootDir.relativePath(project.projectDir)
        val relativePath = project.projectDir.relativeTo(rootDir)
        clickText += "click ${project.path} ${GraphDetails.RepoPath}/${relativePath}\n"
    }

    fileText += """
    end

    %% Dependencies
    """.trimIndent()

    dependencies
        .filter { (key, traits) ->
            val origin = key.v1
            val target = key.v2
            (isRootGraph || currentProjectDependencies.contains(origin)) && origin.path != target.path
        }
        .forEach { (key, traits) ->
            val isApi = !traits.isEmpty() && traits[0] == "api"
            val isDirectDependency = key.v1 == currentProject

            val arrow = when {
                isApi && isDirectDependency -> "==API===>"
                isApi -> "--API--->"
                isDirectDependency -> "===>"
                else -> "--->"
            }
            fileText += "${key.v1.path}${arrow}${key.v2.path}\n"
        }

    fileText += "\n"

    fileText += "%% Dependents\n"
    dependencies
        .filter { (key, traits) ->
            val origin = key.v1
            val target = key.v2
            dependents.contains(origin) && target == currentProject && origin.path != target.path
        }
        .forEach { (key, traits) ->
            // bold dashed arrows aren't supported
            val isApi = traits.isNotEmpty() && traits[0] == "api"
            val arrow = if (isApi) {
                "-.API.->"
            } else {
                "-.->"
            }
            fileText += "${key.v1.path}${arrow}${key.v2.path}\n"
        }


    fileText += """

    %% Click interactions
    $clickText
    ```
    """.trimIndent()

    val graphFile = File(currentProject.projectDir, GraphDetails.GraphFileName)
    graphFile.parentFile.mkdirs()
    graphFile.delete()
    graphFile.writeText(fileText)

    println("Project module dependency graph created at ${graphFile.absolutePath}")
}
