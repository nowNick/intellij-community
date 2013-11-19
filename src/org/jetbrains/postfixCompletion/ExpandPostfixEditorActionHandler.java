package org.jetbrains.postfixCompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.util.*;
import com.intellij.psi.*;
import com.intellij.psi.util.*;
import org.jetbrains.annotations.*;
import org.jetbrains.postfixCompletion.Infrastructure.*;

import java.util.*;

public final class ExpandPostfixEditorActionHandler extends EditorActionHandler {
  @NotNull private final EditorActionHandler myUnderlying;
  @NotNull private final PostfixTemplatesManager myTemplatesManager;

  public ExpandPostfixEditorActionHandler(
      @NotNull EditorActionHandler underlying, @NotNull PostfixTemplatesManager templatesManager) {
    myTemplatesManager = templatesManager;
    myUnderlying = underlying;
  }

  @Override public boolean isEnabled(Editor editor, DataContext dataContext) {


    if (findFoo(editor) != null) return true;

    return myUnderlying.isEnabled(editor, dataContext);
  }

  @Nullable
  protected LookupElement findFoo(Editor editor) {
    Project project = editor.getProject();
    if (project == null) return null;

    PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
    if (file == null) return null;

    int offset = editor.getCaretModel().getOffset();
    PsiElement elementAt = file.findElementAt(offset);
    if (elementAt == null) return null;

    if (elementAt instanceof PsiWhiteSpace && offset > 0) {
      // todo: handle other situations?
      elementAt = file.findElementAt(offset - 1);
    }

    if (elementAt == null) return null;

    PostfixExecutionContext executionContext = new PostfixExecutionContext(true, "postfix", false /* ? */);
    PostfixTemplateContext templateContext = myTemplatesManager.isAvailable(elementAt, executionContext);
    if (templateContext == null) return null;

    List<LookupElement> elements = myTemplatesManager.collectTemplates(templateContext);
    Document document = editor.getDocument();

    for (LookupElement element : elements) {

      String lookupString = element.getLookupString();
      if (offset > lookupString.length()) {
        String prefix = document.getText(new TextRange(offset - lookupString.length(), offset));
        if (prefix.equals(lookupString)) {
          return element;
        }
      }


      /*element.handleInsert(new InsertionContext(
        new OffsetMap(document),
        '\t', elements.toArray(), ,

      ));*/
    }

    return null;
  }

  @Override public void execute(Editor editor, DataContext dataContext) {
    final LookupElement lookupElement = findFoo(editor);
    if (lookupElement != null) {
      PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, editor.getProject());

      OffsetMap offsetMap = new OffsetMap(editor.getDocument());
      int offset = editor.getCaretModel().getOffset() - lookupElement.getLookupString().length();
      offsetMap.addOffset(CompletionInitializationContext.START_OFFSET, offset);

      final InsertionContext context = new InsertionContext(
        offsetMap, '\t',
        new LookupElement[]{lookupElement}, psiFile,
        editor, false);


      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override public void run() {
          lookupElement.handleInsert(context);
        }
      });

      /*public InsertionContext(
      com.intellij.codeInsight.completion.OffsetMap offsetMap,
       char completionChar, com.intellij.codeInsight.lookup.LookupElement[] elements,
        @org.jetbrains.annotations.NotNull com.intellij.psi.PsiFile file, @org.jetbrains.annotations.NotNull
       com.intellij.openapi.editor.Editor editor, boolean addCompletionChar)*/

      return;
    }

    myUnderlying.execute(editor, dataContext);
  }
}
