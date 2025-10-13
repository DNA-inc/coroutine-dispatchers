package inc.dna.coroutines.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.Issue

class CoroutinesIssueRegistry : IssueRegistry() {
  override val vendor = Vendor(vendorName = "DNA Inc.")
  override val issues: List<Issue> = listOf(CoroutinesLintDetector.UseTestContextIssue)
}
