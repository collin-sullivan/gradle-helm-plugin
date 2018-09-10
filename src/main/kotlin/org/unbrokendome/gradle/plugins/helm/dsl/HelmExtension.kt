package org.unbrokendome.gradle.plugins.helm.dsl

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.process.ExecResult
import org.unbrokendome.gradle.plugins.helm.command.GlobalHelmOptions
import org.unbrokendome.gradle.plugins.helm.command.HelmExecProvider
import org.unbrokendome.gradle.plugins.helm.command.HelmExecProviderSupport
import org.unbrokendome.gradle.plugins.helm.command.HelmExecSpec
import org.unbrokendome.gradle.plugins.helm.util.booleanProviderFromProjectProperty
import org.unbrokendome.gradle.plugins.helm.util.dirProviderFromProjectProperty
import org.unbrokendome.gradle.plugins.helm.util.fileProviderFromProjectProperty
import org.unbrokendome.gradle.plugins.helm.util.intProviderFromProjectProperty
import org.unbrokendome.gradle.plugins.helm.util.listProperty
import org.unbrokendome.gradle.plugins.helm.util.orElse
import org.unbrokendome.gradle.plugins.helm.util.property
import org.unbrokendome.gradle.plugins.helm.util.providerFromProjectProperty
import javax.inject.Inject


/**
 * The main Helm DSL extension, accessible using the `helm { ... }` block in build scripts.
 */
interface HelmExtension : HelmExecProvider, GlobalHelmOptions {

    override val executable: Property<String>

    override val debug: Property<Boolean>

    override val home: DirectoryProperty

    /**
     * Address of Tiller, in the format `host:port`.
     *
     * If this property is set, its value will be used to set the `HELM_HOST` environment variable for each
     * Helm invocation.
     */
    val host: Property<String>

    /**
     * Name of the kubeconfig context to use.
     *
     * Corresponds to the `--kube-context` command line option in the Helm CLI.
     */
    val kubeContext: Property<String>

    /**
     * Path to the Kubernetes configuration file.
     *
     * If this property is set, its value will be used to set the `KUBECONFIG` environment variable for each
     * Helm invocation.
     */
    val kubeConfig: RegularFileProperty

    /**
     * Time in seconds to wait for any individual Kubernetes operation (like Jobs for hooks). Default is 300.
     *
     * Corresponds to the `--timeout` command line option in the Helm CLI.
     */
    val timeoutSeconds: Provider<Int>

    /**
     * Base output directory for Helm charts.
     *
     * Defaults to `"${project.buildDir}/helm/charts"`.
     */
    val outputDir: DirectoryProperty
}


private open class DefaultHelmExtension
@Inject constructor(project: Project)
    : HelmExtension {

    @Suppress("LeakingThis")
    private val execProviderSupport = HelmExecProviderSupport(project, this)


    override val executable =
            project.objects.property(
                    project.providerFromProjectProperty("helm.executable", evaluateGString = true)
                            .orElse("helm"))


    override val debug: Property<Boolean> =
            project.objects.property(
                    project.booleanProviderFromProjectProperty("helm.debug"))


    override val home: DirectoryProperty =
            project.layout.directoryProperty(
                    project.dirProviderFromProjectProperty("helm.home", evaluateGString = true))


    override val host: Property<String> =
            project.objects.property(
                    project.providerFromProjectProperty("helm.host"))


    override val kubeContext: Property<String> =
            project.objects.property(
                    project.providerFromProjectProperty("helm.kubeContext"))


    override val kubeConfig: RegularFileProperty =
            project.layout.fileProperty(
                    project.fileProviderFromProjectProperty("helm.kubeConfig", evaluateGString = true))


    override val timeoutSeconds: Property<Int> =
            project.objects.property(
                    project.intProviderFromProjectProperty("helm.timeoutSeconds"))


    override val extraArgs: ListProperty<String> =
            project.objects.listProperty()


    override val outputDir: DirectoryProperty =
            project.layout.directoryProperty(
                    project.dirProviderFromProjectProperty("helm.outputDir", evaluateGString = true)
                            .orElse(project.layout.buildDirectory.dir("helm/charts")))


    override fun execHelm(command: String, subcommand: String?, action: Action<HelmExecSpec>): ExecResult =
            execProviderSupport.execHelm(command, subcommand, action)
}


/**
 * Creates a new [HelmExtension] object using the given project's [ObjectFactory].
 *
 * @param project the Gradle [Project]
 * @return the created [HelmExtension] object
 */
fun createHelmExtension(project: Project): HelmExtension =
        project.objects.newInstance(DefaultHelmExtension::class.java, project)
