package inc.dna.coroutines.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import kotlinx.coroutines.Dispatchers
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.tryResolve
import org.jetbrains.uast.tryResolveNamed

class DispatcherProviderUsageDetector : Detector(), SourceCodeScanner {

  override fun getApplicableUastTypes() = listOf(UQualifiedReferenceExpression::class.java)

  override fun createUastHandler(context: JavaContext): UElementHandler = MyHandler(context)

  class MyHandler(val context: JavaContext) : UElementHandler() {

    override fun visitQualifiedReferenceExpression(node: UQualifiedReferenceExpression) {
      val receiver = node.receiver.tryResolve() as? PsiClass
      if (receiver?.qualifiedName == Dispatchers::class.java.canonicalName) {
        val method = node.selector.tryResolve() as? PsiMethod
        val returnType = (method?.returnType as? PsiClassType)?.resolve()
        if (returnType != null &&
            context.evaluator.implementsInterface(
                returnType, "kotlinx.coroutines.CoroutineDispatcher")) {
          var issueNode = node
          val parentExpression = node.uastParent as? UQualifiedReferenceExpression
          val isMainImmediate =
              method.name == "getMain" &&
                  parentExpression?.selector?.tryResolveNamed()?.name == "getImmediate"
          if (isMainImmediate) issueNode = parentExpression
          val alternative =
              when (method.name) {
                "getIO" -> "io"
                "getDefault" -> "default"
                "getMain" -> if (isMainImmediate) "mainImmediate" else "main"
                else -> null
              }
          val isSuspending =
              (node.getParentOfType<UMethod>(true)?.sourcePsi as? KtNamedFunction)?.hasModifier(
                  KtTokens.SUSPEND_KEYWORD) == true
          context.report(
              issue = UseDispatcherProviderIssue,
              scope = node,
              location = context.getLocation(issueNode),
              message =
                  "Dispatchers should not be used directly. Use the DispatcherProvider instead.",
              quickfixData =
                  LintFix.create()
                      .name("Use currentDispatchers()")
                      .replace()
                      .text(issueNode.asSourceString())
                      .with("currentDispatchers().${alternative}")
                      .imports("inc.dna.coroutines.currentDispatchers")
                      .build()
                      .takeIf { isSuspending },
          )
        }
      }
    }
  }

  companion object {
    val UseDispatcherProviderIssue =
        Issue.create(
            id = "UseDispatcherProvider",
            briefDescription =
                "Using kotlinx.coroutines.Dispatchers directly is not recommended because it can lead to slow and/or flaky tests.",
            explanation =
                "Use inc.dna.coroutines.test.runTest or pass a TestDispatchersContext to runTest to ensure that dispatchers are properly replaced.",
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.ERROR,
            implementation =
                Implementation(
                    DispatcherProviderUsageDetector::class.java,
                    Scope.Companion.JAVA_FILE_SCOPE,
                ),
        )
  }
}
