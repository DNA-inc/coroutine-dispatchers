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
import org.jetbrains.uast.UCallExpression

class CoroutinesLintDetector : Detector(), SourceCodeScanner {

  override fun getApplicableMethodNames(): List<String> = listOf("runTest")

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
                .imports("org.jetbrains.kotlinx.coroutines.test.runTest")
                .all()
                .with("inc.dna.coroutines.test.runTest")
                .build(),
        )
      }
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
                Implementation(CoroutinesLintDetector::class.java, Scope.JAVA_FILE_SCOPE),
        )
  }
}

val PsiMethod.qualifiedMethodName
  get() = containingClass?.let { it.qualifiedName?.replace(it.name.orEmpty(), this.name) }
