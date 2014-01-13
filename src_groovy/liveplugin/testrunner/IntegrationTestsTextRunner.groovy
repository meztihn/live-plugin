package liveplugin.testrunner
import com.intellij.openapi.project.Project
import liveplugin.PluginUtil
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import static liveplugin.testrunner.IntegrationTestsRunner.runTestsInClass

@SuppressWarnings(["GroovyUnusedDeclaration"])
class IntegrationTestsTextRunner {
	static void runIntegrationTests(List<Class> testClasses, @NotNull Project project, @Nullable String pluginPath = null) {
		def context = [project: project, pluginPath: pluginPath]
		def now = System.currentTimeMillis()

		def textReport = new TextTestReport()
		textReport.startedAllTests(now)
		testClasses.collect{ runTestsInClass(it, context, textReport, now) }
		textReport.finishedAllTests(now)

		PluginUtil.showInConsole(textReport.result, "Integration tests", project)
	}

	private static class TextTestReport implements TestReport {
		String result = ""

		@Override def startedAllTests(long now) {}
		@Override def finishedAllTests(long now) {}
		@Override def running(String className, String methodName, long now) {}
		@Override def passed(String methodName, long now) {
			result += "\"${methodName}\" - OK\n"
		}
		@Override def failed(String methodName, String error, long now) {
			result += methodName + " - FAILED\n" + error + "\n"
		}
		@Override def error(String methodName, String error, long now) {
			result += methodName + " - ERROR\n" + error + "\n"
		}
		@Override def ignored(String methodName) {
			result += "\"${methodName}\" - IGNORED\n"
		}
		@Override def finishedClass(String className, long now) {}
	}
}
