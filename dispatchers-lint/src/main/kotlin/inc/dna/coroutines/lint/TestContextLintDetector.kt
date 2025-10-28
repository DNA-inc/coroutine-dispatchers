package inc.dna.coroutines.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.TextFormat
import com.intellij.psi.PsiMethod
import java.util.EnumSet
import org.jetbrains.uast.UCallExpression

class TestContextLintDetector : Detector(), SourceCodeScanner {

  override fun getApplicableMethodNames(): List<String> = listOf("runTest", "TestScope")

  override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
    if (method.qualifiedMethodName.orEmpty().startsWith("kotlinx.coroutines.test.runTest")) {
      val contextParameterIndex = method.parameters.indexOfFirst { it.name == "context" }
      val hasContextParameter = node.getArgumentForParameter(contextParameterIndex) != null
      if (!hasContextParameter) {
        context.report(
            UseTestContextIssue,
            node,
            context.getNameLocation(node),
            UseTestContextIssue.getExplanation(TextFormat.TEXT),
            fix()
                .replace()
                .text(node.methodIdentifier?.name.orEmpty())
                .with("inc.dna.coroutines.test.runTest")
                .build(),
        )
      }
    } else if (method.qualifiedMethodName
        .orEmpty()
        .startsWith("kotlinx.coroutines.test.TestScope")) {
      context.report(
          issue = UseTestContextIssue,
          scope = node,
          location = context.getNameLocation(node),
          message =
              "Use inc.dna.coroutines.test.TestScope() to ensure test dispatcher replacement.",
          quickfixData =
              fix().replace().text("TestScope").with("inc.dna.coroutines.test.TestScope").build(),
      )
    }
  }

  companion object {
    val UseTestContextIssue =
        Issue.create(
            id = "UseTestContextIssue",
            briefDescription =
                "Use inc.dna.coroutines.test.runTest or pass a TestDispatchersContext to runTest.",
            explanation =
                "Use inc.dna.coroutines.test.runTest or pass a TestDispatchersContext to runTest to ensure that dispatchers are properly replaced.",
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.ERROR,
            implementation =
                Implementation(
                    TestContextLintDetector::class.java,
                    EnumSet.of(Scope.JAVA_FILE, Scope.TEST_SOURCES),
                ),
        )
  }
}

val PsiMethod.qualifiedMethodName
  get() = containingClass?.let { it.qualifiedName?.replace(it.name.orEmpty(), this.name) }
